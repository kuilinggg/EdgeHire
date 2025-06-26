package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.MessageRepository;
import com.se.EdgeHire.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@AllArgsConstructor
@Slf4j
public class MessageController {
    MessageRepository messageRepository;

    UserRepository userRepository;

    /**
     * 获取聊天用户列表
     * @param id 用户id
     * @return 聊天用户列表
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getChatUsers(@PathVariable Integer id) {
        List<Integer> userIds = messageRepository.findAllChatUsers(id);
        List<User> chatUsers = new ArrayList<>();

        for(var i : userIds) {
            Optional<User> temp = userRepository.findById(i);
            if(temp.isPresent()) {
                temp.get().setPassword(null);
                chatUsers.add(temp.get());
            }
        }

        chatUsers.add(new User() {{
            setId(10);
            setUsername("牛爷爷");
            setRole(1);
        }});

        log.info("用户: {} 请求获取聊天用户列表", id);

        return ResponseEntity.ok(chatUsers);
    }
}
