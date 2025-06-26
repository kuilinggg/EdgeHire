package com.se.EdgeHire.Entity;


import jakarta.persistence.*;

@Entity
@Table(name = "t_info")
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

}