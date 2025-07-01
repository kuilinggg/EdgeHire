package com.se.EdgeHire.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name="t_user")
@Data
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private int role;

    // 这是关键：添加与 Info 的一对一反向关联
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // 'mappedBy = "user"' 告诉JPA，这个关系的配置（如@JoinColumn）由Info实体中的'user'属性负责
    // 'cascade = CascadeType.ALL' 表示对User的操作（如保存、删除）会级联到Info
    @JsonManagedReference
    private Info info;
}