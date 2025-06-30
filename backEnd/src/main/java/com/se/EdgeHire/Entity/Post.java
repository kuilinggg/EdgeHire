package com.se.EdgeHire.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "t_post")
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "seeker_info_id", nullable = false)
    private Integer seekerInfoId;

    @Column(name = "resume_id", nullable = false)
    private Integer resumeId;

    // 关联关系映射
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_info_id", insertable = false, updatable = false)
    private SeekerInfo seekerInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", insertable = false, updatable = false)
    private Resume resume;
}
