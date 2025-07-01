package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.UserRepository;
import com.se.EdgeHire.WebSocket.WebSocketHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    UserRepository userRepository;

    @PostMapping("/sysMsg/{id}/{content}")
    public ResponseEntity<?> sysMsg(@PathVariable Integer id, @PathVariable String content) {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        WebSocketHandler webSocketHandler = WebSocketHandler.getInstance();
        webSocketHandler.sendMessageToUser(0, user.get().getId(), content);
        return ResponseEntity.ok().build();
    }
}
