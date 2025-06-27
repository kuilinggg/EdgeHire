package com.se.EdgeHire.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_resume_comment")
@Data
public class ResumeComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "resume_id", nullable = false)
    private Integer resumeId;

    @Column(name = "hr_id", nullable = false)
    private Integer hrId; // HR用户ID

    @Column(name = "comment", columnDefinition = "TEXT", nullable = false)
    private String comment; // 修改建议内容

    @Column(name = "score")
    private Integer score; // 评分 1-10

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1-有效 0-已删除
}
