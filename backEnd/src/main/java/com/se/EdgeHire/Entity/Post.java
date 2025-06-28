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

}
