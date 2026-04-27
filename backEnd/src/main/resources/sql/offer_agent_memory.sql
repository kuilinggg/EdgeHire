CREATE TABLE IF NOT EXISTS offer_agent_memory (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  user_id INT NOT NULL COMMENT '用户ID',
  target_position VARCHAR(120) DEFAULT NULL COMMENT '长期识别的目标岗位',
  profile_summary TEXT COMMENT '用户长期画像摘要',
  skill_tags TEXT COMMENT '已识别技能标签，JSON数组',
  gap_tags TEXT COMMENT '待补能力标签，JSON数组',
  preference_tags TEXT COMMENT '求职偏好标签，JSON数组',
  suggestion_summary TEXT COMMENT '最近一次建议摘要',
  evidence_summary TEXT COMMENT '画像更新依据摘要',
  source VARCHAR(40) DEFAULT NULL COMMENT '最近更新来源：chat/workflow',
  last_interaction_at DATETIME DEFAULT NULL COMMENT '最近交互时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_offer_agent_memory_user_id (user_id),
  INDEX idx_offer_agent_memory_target_position (target_position),
  INDEX idx_offer_agent_memory_updated_at (updated_at),
  CONSTRAINT fk_offer_agent_memory_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OfferAgent长期用户画像表';
