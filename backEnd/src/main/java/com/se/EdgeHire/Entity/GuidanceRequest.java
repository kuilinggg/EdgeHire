package com.se.EdgeHire.Entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 求职指导申请实体类
 */
@Entity
@Table(name = "guidance_requests")
@Data
public class GuidanceRequest implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 申请用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 分配的HR用户ID
     */
    @Column(name = "hr_user_id")
    private Long hrUserId;

    /**
     * 目标职位
     */
    @Column(name = "target_position", nullable = false, length = 100)
    private String targetPosition;

    /**
     * 指导类型
     */
    @Column(name = "guidance_type", nullable = false, length = 50)
    private String guidanceType;

    /**
     * 联系电话
     */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * 邮箱地址
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * 详细需求描述
     */
    @Column(name = "detailed_description", nullable = false, columnDefinition = "TEXT")
    private String detailedDescription;

    /**
     * 工作经验
     */
    @Column(name = "experience", nullable = false, length = 50)
    private String experience;

    /**
     * 教育背景
     */
    @Column(name = "education", nullable = false, length = 50)
    private String education;

    /**
     * 核心技能（JSON格式）
     */
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skillsJson;

    /**
     * 申请状态
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "pending";

    /**
     * 申请时间
     */
    @Column(name = "request_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    /**
     * HR分配时间
     */
    @Column(name = "assigned_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedTime;

    /**
     * 完成时间
     */
    @Column(name = "completed_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    /**
     * HR指导反馈
     */
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    /**
     * 用户评分
     */
    @Column(name = "rating")
    private Integer rating;

    /**
     * 用户评价
     */
    @Column(name = "user_comment", columnDefinition = "TEXT")
    private String userComment;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 软删除时间
     */
    @Column(name = "deleted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

    // 非数据库字段，用于返回给前端
    @Transient
    private List<String> skills;

    @Transient
    private String userName;

    @Transient
    private String userAvatar;

    @Transient
    private String hrName;

    @Transient
    private String hrAvatar;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.requestTime == null) {
            this.requestTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 状态枚举
     */
    public enum Status {
        PENDING("pending", "等待分配"),
        PROCESSING("processing", "指导中"),
        COMPLETED("completed", "已完成"),
        CANCELLED("cancelled", "已取消");

        private final String code;
        private final String desc;

        Status(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 指导类型枚举
     */
    public enum GuidanceType {
        RESUME_OPTIMIZATION("简历优化"),
        INTERVIEW_SKILLS("面试技巧"),
        CAREER_PLANNING("职业规划"),
        CAREER_TRANSITION("转行指导"),
        SALARY_NEGOTIATION("薪资谈判"),
        INDUSTRY_ANALYSIS("行业分析");

        private final String desc;

        GuidanceType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}