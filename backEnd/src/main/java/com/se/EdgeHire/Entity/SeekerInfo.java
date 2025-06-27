package com.se.EdgeHire.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "t_seeker_info")
@Data
public class SeekerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "education")
    private Integer education; // 1小学2初中3高中4大专5本科6硕士7博士

    @Column(name = "school", length = 255)
    private String school;

    @Column(name = "favor", length = 255)
    private String favor; // 理想岗位

    @Column(name = "membership", nullable = false)
    private Integer membership = 0; // 0-普通会员 1-高级会员
}
