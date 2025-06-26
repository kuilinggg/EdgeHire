package com.se.EdgeHire.Entity;

import lombok.Data;
import jakarta.websocket.Session;

@Data
public class SocketSession {

    private Session session;

    private int id;

    private int role;

    private String username;

    /**
     * 真名
     */
    private String name;

    /**
     * 所属公司，求职者无此属性
     */
    private String company;

    /**
     * 头像路径
     */
    private String avatar;
}
