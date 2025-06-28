package com.se.EdgeHire.Entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ChatUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;

    private String username;

    /**
     * 所属公司，求职者无此属性
     */
    private String company;

    /**
     * 头像路径
     */
    private String avatar;

    private int unReadCount;

    private String latestMessage;

    private LocalDateTime latestMessageTime;

    public void setChatUser(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.unReadCount = 0; // 默认未读消息数为0
    }
}
