-- EdgeHire HR模块数据库初始化脚本

-- 创建HR信息表
CREATE TABLE IF NOT EXISTS t_hr_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    company VARCHAR(255),
    position VARCHAR(255),
    experience VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_id (user_id)
);

-- 创建求职者信息表
CREATE TABLE IF NOT EXISTS t_seeker_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    education INT COMMENT '1小学2初中3高中4大专5本科6硕士7博士',
    school VARCHAR(255),
    favor VARCHAR(255) COMMENT '理想岗位',
    membership INT NOT NULL DEFAULT 0 COMMENT '0-普通会员 1-高级会员',
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_id (user_id)
);

-- 创建简历评论表
CREATE TABLE IF NOT EXISTS t_resume_comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    resume_id INT NOT NULL,
    hr_id INT NOT NULL,
    comment TEXT NOT NULL,
    score INT COMMENT '评分 1-10',
    create_time DATETIME NOT NULL,
    status INT NOT NULL DEFAULT 1 COMMENT '1-有效 0-已删除',
    FOREIGN KEY (resume_id) REFERENCES t_resume(id) ON DELETE CASCADE,
    FOREIGN KEY (hr_id) REFERENCES t_user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_resume_hr (resume_id, hr_id, status)
);

-- 插入示例HR数据（如果不存在）
INSERT IGNORE INTO t_user (username, password, role) VALUES 
('hr_wang', '$2a$10$example_hash_password', 2),
('hr_li', '$2a$10$example_hash_password', 2);

-- 插入示例求职者数据（如果不存在）
INSERT IGNORE INTO t_user (username, password, role) VALUES 
('seeker_zhang', '$2a$10$example_hash_password', 1),
('seeker_liu', '$2a$10$example_hash_password', 1);

-- 为示例用户创建基础信息
INSERT IGNORE INTO t_info (user_id, realname, status, gender) VALUES 
((SELECT id FROM t_user WHERE username = 'hr_wang'), '王HR', 1, 1),
((SELECT id FROM t_user WHERE username = 'hr_li'), '李HR', 1, 2),
((SELECT id FROM t_user WHERE username = 'seeker_zhang'), '张求职者', 1, 1),
((SELECT id FROM t_user WHERE username = 'seeker_liu'), '刘求职者', 1, 2);

-- 为HR用户创建HR信息
INSERT IGNORE INTO t_hr_info (user_id, company, position, experience) VALUES 
((SELECT id FROM t_user WHERE username = 'hr_wang'), '阿里巴巴', 'Java开发工程师', '5年招聘经验'),
((SELECT id FROM t_user WHERE username = 'hr_li'), '腾讯', '前端开发工程师', '3年招聘经验');

-- 为求职者用户创建求职者信息
INSERT IGNORE INTO t_seeker_info (user_id, education, school, favor, membership) VALUES 
((SELECT id FROM t_user WHERE username = 'seeker_zhang'), 5, '清华大学', 'Java后端开发', 0),
((SELECT id FROM t_user WHERE username = 'seeker_liu'), 6, '北京大学', '前端开发', 1);

-- 创建索引以提高查询性能
CREATE INDEX idx_hr_info_user_id ON t_hr_info(user_id);
CREATE INDEX idx_seeker_info_user_id ON t_seeker_info(user_id);
CREATE INDEX idx_seeker_info_education ON t_seeker_info(education);
CREATE INDEX idx_seeker_info_favor ON t_seeker_info(favor);
CREATE INDEX idx_resume_comment_resume_id ON t_resume_comment(resume_id);
CREATE INDEX idx_resume_comment_hr_id ON t_resume_comment(hr_id);
CREATE INDEX idx_resume_comment_create_time ON t_resume_comment(create_time);
