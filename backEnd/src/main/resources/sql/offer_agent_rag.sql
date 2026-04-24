CREATE TABLE IF NOT EXISTS offer_agent_knowledge_doc (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  category VARCHAR(100) DEFAULT NULL,
  tags VARCHAR(500) DEFAULT NULL,
  target_position VARCHAR(200) DEFAULT NULL,
  source VARCHAR(200) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_category (category),
  INDEX idx_target_position (target_position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OfferAgent RAG knowledge document';

CREATE TABLE IF NOT EXISTS offer_agent_knowledge_chunk (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_id BIGINT NOT NULL,
  chunk_index INT NOT NULL,
  content TEXT NOT NULL,
  keywords VARCHAR(500) DEFAULT NULL,
  summary VARCHAR(500) DEFAULT NULL,
  embedding_json MEDIUMTEXT DEFAULT NULL COMMENT 'Optional serialized embedding for external vector stores',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_doc_id (doc_id),
  FULLTEXT INDEX ft_content (content, keywords, summary),
  CONSTRAINT fk_offer_agent_chunk_doc FOREIGN KEY (doc_id) REFERENCES offer_agent_knowledge_doc(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OfferAgent RAG knowledge chunk';

CREATE TABLE IF NOT EXISTS offer_agent_retrieval_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  query_text TEXT NOT NULL,
  matched_chunk_ids VARCHAR(500) DEFAULT NULL,
  top_score INT DEFAULT 0,
  retrieval_mode VARCHAR(50) DEFAULT 'hybrid',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OfferAgent RAG retrieval log';

CREATE TABLE IF NOT EXISTS offer_agent_tool_call_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  conversation_id VARCHAR(100) DEFAULT NULL,
  message TEXT,
  tool_name VARCHAR(100) NOT NULL,
  input_json TEXT,
  output_json MEDIUMTEXT,
  success TINYINT(1) NOT NULL DEFAULT 1,
  error_message TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_tool_name (tool_name),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OfferAgent deterministic tool call log';
