package com.se.EdgeHire.Controller;

import com.se.EdgeHire.WebSocket.WebSocketHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {

    @GetMapping("/api/onlineCount")
    public int getOnlineUserCount() {
        return WebSocketHandler.getOnlineUserCount();
    }
}