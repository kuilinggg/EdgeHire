package com.se.EdgeHire.WebSocket;

import com.se.EdgeHire.Entity.SocketSession;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.UserRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/webSocket")
@Slf4j
@Component
public class WebSocketHandler {
    private static final ConcurrentHashMap<Integer, SocketSession> SESSION_MAP = new ConcurrentHashMap<>();
    private static UserRepository userRepository;

    @Autowired
    public void setInstance(UserRepository userRepository) {
        WebSocketHandler.userRepository = userRepository;
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

        //TODO: 这里需要添加逻辑来获取用户信息，比如真名之类的

        log.info("WebSocket connection opened, userId: {}", userId);
    }

    @OnClose
    public void OnClose(Session session) {
        //TODO: 处理连接关闭事件
    }

    @OnError
    public void OnError(Throwable throwable) {
        log.error("WebSocket error: {}", throwable.getMessage());
    }

    @OnMessage
    public void OnMessage(String message, Session session) {
        // 处理接收到的消息
        // 可以在这里添加逻辑来处理消息，例如广播、存储等
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
