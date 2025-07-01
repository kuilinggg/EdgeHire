package com.se.EdgeHire.Entity;


import jakarta.persistence.*;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "t_info")
@Data
public class Info {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    private String realname;
    private String avatar;
    private Integer age;
    private Integer gender;

    @Column(nullable = false)
    private Integer status;

    private String phone;
    private String email;

    // 这是关键：定义与 User 的一对一关系
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false) // 指定本表（t_info）中用于关联的外键列
    @JsonBackReference
    private User user;
}