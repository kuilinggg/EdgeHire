package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.Message;
import com.se.EdgeHire.Repository.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class MessageService {
    private MessageRepository messageRepository;

    public Map<Integer, List<Message>> getConversationByUserId(int userId) {
        List<Message> messages = messageRepository.findMessagesByUserId(userId);

        return messages.stream()
                .collect(Collectors.groupingBy(message -> {
                    // 确定对话伙伴ID
                    if (message.getSenderId() == userId) {
                        return message.getReceiverId();
                    } else {
                        return message.getSenderId();
                    }
                }));
    }

    public Map<Integer, Integer> getUnreadCountByUserId(int userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndIsRead(userId, 0);

        return unreadMessages.stream()
                .collect(Collectors.groupingBy(Message::getSenderId, Collectors.summingInt(m -> 1)));
    }

    public void markConversationAsRead(int user1, int user2) {
        messageRepository.markConversationAsRead(user1, user2);
    }
}
