package com.se.EdgeHire.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.se.EdgeHire.Entity.Message;
import com.se.EdgeHire.Entity.SocketSession;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.MessageRepository;
import com.se.EdgeHire.Repository.UserRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/webSocket")
@Slf4j
@Component
public class WebSocketHandler {
    private static final ConcurrentHashMap<Integer, SocketSession> SESSION_MAP = new ConcurrentHashMap<>();
    private static UserRepository userRepository;
    private static MessageRepository messageRepository;
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setInstance(UserRepository userRepository, MessageRepository messageRepository) {
        WebSocketHandler.userRepository = userRepository;
        WebSocketHandler.messageRepository = messageRepository;
    }

    public WebSocketHandler() {
        // 注册JavaTimeModule以支持LocalDateTime序列化和反序列化
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 设置自定义格式
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @OnOpen
    public void OnOpen(Session session) {
        // 处理连接建立事件
        Integer userId = getUserIdBySession(session);
        if(userId == null) {
            log.warn("User ID not found in session: {}", session.getId());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User ID not found"));
            } catch (Exception e) {
                log.error("Error closing session: {}", e.getMessage());
            }
            return;
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()) {
            log.warn("User not found for session: {}", session.getId());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User not found"));
            } catch (Exception e) {
                log.error("Error closing session: {}", e.getMessage());
            }
            return;
        }

        if(Objects.nonNull(SESSION_MAP.get(userId)) && SESSION_MAP.get(userId).getSession().isOpen()) {
            // 如果map中有该用户的session信息，就不再重复加入连接
            return;
        }

        SocketSession socketSession = new SocketSession().setSession(session, userId);
        SESSION_MAP.put(userId, socketSession);

        log.info("WebSocket connection opened, userId: {}", userId);
    }

    @OnClose
    public void OnClose(Session session) {
        Integer userId = getUserIdBySession(session);
        if(userId == null) {
            log.warn("User ID not found in session on close: {}", session.getId());
            return;
        }

        if(SESSION_MAP.containsKey(userId)) {
            SESSION_MAP.remove(userId);
            log.info("WebSocket connection closed, userId: {}", userId);
        } else {
            log.warn("WebSocket session not found for userId: {}", userId);
        }
    }

    @OnError
    public void OnError(Throwable throwable) {
        log.error("WebSocket error: {}", throwable.getMessage());
    }

    @OnMessage
    public void OnMessage(String message, Session session) {
        // 解析前端发送的JSON消息
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            int from = jsonNode.get("from").asInt();
            int to = jsonNode.get("to").asInt();
            String content = jsonNode.get("content").asText();
            int type = jsonNode.get("type").asInt();
            log.info("收到消息: from={}, to={}, content={} , type={}", from, to, content, type);

            if(type == 0) {
                Message msg = new Message();
                msg.setSenderId(from);
                msg.setReceiverId(to);
                msg.setContent(content);
                msg.setTime(LocalDateTime.now());
                msg.setIsRead(0);
                messageRepository.save(msg);

                sendOneMessage(msg);
            }

        } catch (Exception e) {
            log.error("消息解析失败: {}", e.getMessage());
        }
    }

    private void sendOneMessage(Message message) {
        if(SESSION_MAP.containsKey(message.getReceiverId())) {
            // 如果接收方在线，发送消息
            SocketSession receiverSession = SESSION_MAP.get(message.getReceiverId());
            try {
                synchronized (WebSocketHandler.class) {
                    // 确保线程安全地发送消息
                    if (receiverSession.getSession().isOpen()) {
                        receiverSession.getSession().getBasicRemote().sendText(objectMapper.writeValueAsString(message));
                        log.info("发送消息给用户: {}, 内容: {}", message.getReceiverId(), message.getContent());
                    }
                }
            } catch (Exception e) {
                log.error("发送消息失败: {}", e.getMessage());
            }
        } else {
            log.warn("用户 {} 不在线，无法发送消息", message.getReceiverId());
        }
    }

    private Integer getUserIdBySession(Session session) {
        // 从session中获取用户ID
        String uri = session.getRequestURI().getQuery();
        if(uri == null || uri.isEmpty()) {
            return null;
        }

        String[] segments = uri.split("=");

        return Integer.parseInt(segments[segments.length - 1]);
    }
}
