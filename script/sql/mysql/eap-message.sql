-- Message module tables (MySQL)
CREATE TABLE IF NOT EXISTS msg_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NULL,
  scene_code VARCHAR(64) NOT NULL,
  user_id BIGINT NULL,
  user_type INT NULL,
  email VARCHAR(255) NULL,
  mobile VARCHAR(64) NULL,
  wechat_open_id VARCHAR(128) NULL,
  variables_json JSON NULL,
  override_channels_json JSON NULL,
  priority INT NOT NULL DEFAULT 0,
  schedule_at DATETIME NULL,
  dedup_key VARCHAR(128) NULL,
  state INT NOT NULL DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_scene_code(scene_code),
  INDEX idx_user(user_id, user_type),
  UNIQUE KEY uk_dedup_key(dedup_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS msg_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  message_id BIGINT NOT NULL,
  channel VARCHAR(32) NOT NULL,
  status INT NOT NULL DEFAULT 0,
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_at DATETIME NULL,
  max_attempts INT NULL,
  backoff_strategy VARCHAR(32) NULL,
  backoff_base_seconds INT NULL,
  priority INT NOT NULL DEFAULT 0,
  schedule_at DATETIME NULL,
  error_code VARCHAR(64) NULL,
  error_msg VARCHAR(500) NULL,
  provider_request_id VARCHAR(128) NULL,
  provider_serial_no VARCHAR(128) NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_message(message_id),
  INDEX idx_ready(status, next_retry_at, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
