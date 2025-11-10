package com.se.EdgeHire.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Post 数据传输对象（DTO）
 * 用于缓存和前端数据传输，避免直接使用 JPA 实体
 * 
 * 设计原则：
 * 1. 保持与前端期望的数据结构一致（嵌套对象）
 * 2. 只包含前端真正需要展示的字段
 * 3. 不包含任何 JPA 注解，是纯粹的 POJO
 * 4. 内部类都是静态类，可以独立序列化
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    // Post 基本信息
    private Integer id;
    private Integer userId;
    private Integer seekerInfoId;
    private Integer resumeId;
    
    // 嵌套对象（与前端期望的结构一致）
    private UserDTO user;
    private SeekerInfoDTO seekerInfo;
    private ResumeDTO resume;
    
    /**
     * User 数据传输对象（包含 Info 信息）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Integer id;
        private String username;
        private InfoDTO info;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class InfoDTO {
            private Integer id;
            private String realname;
            private String avatar;
            private Integer age;
            private Integer gender;
            private String phone;
            private String email;
        }
    }
    
    /**
     * SeekerInfo 数据传输对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeekerInfoDTO {
        private Integer id;
        private Integer userId;
        private Integer education;      // 1小学2初中3高中4大专5本科6硕士7博士
        private String school;
        private String favor;           // 理想岗位，JSON 字符串
        private Integer membership;     // 0-普通会员 1-高级会员
    }
    
    /**
     * Resume 数据传输对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeDTO {
        private Integer id;
        private Integer userId;
        private String content;
        private String avatar;
        private LocalDateTime createTime;
    }
}
