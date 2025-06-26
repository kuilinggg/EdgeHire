package com.se.EdgeHire.WebSocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@ServerEndpoint("/webSocket")
@Slf4j
@Component
public class WebSocketHandler {

    @OnOpen
    public void OnOpen(Session session) {
        // 处理连接建立事件
        log.info("WebSocket connection opened: {}", session.getId());
    }

    @OnClose
    public void OnClose(Session session) {
        // 处理连接关闭事件
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
}
