package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.ChatUser;
import com.se.EdgeHire.Entity.HrInfo;
import com.se.EdgeHire.Entity.Message;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.HrInfoRepository;
import com.se.EdgeHire.Repository.MessageRepository;
import com.se.EdgeHire.Repository.UserRepository;
import com.se.EdgeHire.Service.MessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@AllArgsConstructor
@Slf4j
public class MessageController {
    MessageRepository messageRepository;

    UserRepository userRepository;

    MessageService messageService;

    HrInfoRepository hrInfoRepository;

    /**
     * 获取聊天用户列表
     * @param id 用户id
     * @return 聊天用户列表
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getChatUsers(@PathVariable Integer id) {
        List<Integer> userIds = messageRepository.findAllChatUsers(id);
        Map<Integer, Message> latestMessagesMap = messageService.getLatestMessagesMap(id);
        List<ChatUser> chatUsers = new ArrayList<>();

        Map<Integer, Integer> unreadCountMap = messageService.getUnreadCountByUserId(id);

        for(var i : userIds) {
            Optional<User> temp = userRepository.findById(i);
            if(temp.isPresent()) {
                ChatUser chatUser = new ChatUser();
                if(temp.get().getRole() == 2) {
                    Optional<HrInfo> hrInfo = hrInfoRepository.findByUserId(i);
                    hrInfo.ifPresent(info -> chatUser.setCompany(info.getCompany()));
                }
                chatUser.setChatUser(temp.get());
                int unreadCount = unreadCountMap.getOrDefault(i, 0);
                chatUser.setUnReadCount(unreadCount);
                chatUser.setLatestMessage(latestMessagesMap.get(i).getContent());
                chatUser.setLatestMessageTime(latestMessagesMap.get(i).getTime());
                chatUsers.add(chatUser);
            }
        }
        chatUsers.sort(Comparator
                .comparing(ChatUser::getLatestMessageTime
                        , Comparator.nullsLast(Comparator.reverseOrder())));

        ChatUser systemUser = new ChatUser();
        systemUser.setUsername("系统消息");
        systemUser.setId(0);
        if(latestMessagesMap.get(0) != null) {
            systemUser.setLatestMessage(latestMessagesMap.get(0).getContent());
            systemUser.setLatestMessageTime(latestMessagesMap.get(0).getTime());
        }

        systemUser.setUnReadCount(unreadCountMap.getOrDefault(0, 0));
        chatUsers.addFirst(systemUser);

        log.info("用户: {} 请求获取聊天用户列表", id);

        return ResponseEntity.ok(chatUsers);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<?> getMessages(@PathVariable Integer id) {
        Map<Integer, List<Message>> messages = messageService.getConversationByUserId(id);
        log.info("用户: {} 请求获取聊天记录", id);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages/read/{user1}/{user2}")
    public ResponseEntity<?> markConversationAsRead(
            @PathVariable Integer user1,
            @PathVariable Integer user2) {
        messageService.markConversationAsRead(user1, user2);
        log.info("用户: {} 和 用户: {} 的对话已标记为已读", user1, user2);
        return ResponseEntity.ok("Conversation marked as read");
    }

    @GetMapping("/newChatUser/{id}")
    public ResponseEntity<?> getNewChatUser(@PathVariable Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        Map<Integer, Integer> unreadCountMap = messageService.getUnreadCountByUserId(id);

        if (userOpt.isEmpty()) {
            log.warn("User with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        ChatUser chatUser = new ChatUser();
        chatUser.setChatUser(user);
        int unReadCount = unreadCountMap.getOrDefault(id, 0);
        chatUser.setUnReadCount(unReadCount);

        log.info("用户请求获取新聊天用户: {} 的信息", id);
        return ResponseEntity.ok(chatUser);
    }
}
