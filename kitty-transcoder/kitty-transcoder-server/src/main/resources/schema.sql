-- 转码任务表
CREATE TABLE IF NOT EXISTS transcode_task (
    id VARCHAR(64) PRIMARY KEY,
    input_type VARCHAR(16) NOT NULL,
    input_path VARCHAR(1024) NOT NULL,
    strategy_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    progress INT NOT NULL DEFAULT 0,
    progress_detail TEXT,
    output_path VARCHAR(1024),
    output_http_url VARCHAR(1024),
    error_message TEXT,
    watermark_url VARCHAR(1024),
    watermark_position VARCHAR(64),
    priority INT NOT NULL DEFAULT 5,
    retry_count INT NOT NULL DEFAULT 0,
    notification_config TEXT,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by_ak VARCHAR(64),
    INDEX idx_transcode_task_strategy_id (strategy_id),
    INDEX idx_transcode_task_status (status),
    INDEX idx_transcode_task_created_at (created_at)
);

-- 策略步骤表（策略=主键 id，root_id 仅用于多步骤分组，根步骤 root_id=id）
CREATE TABLE IF NOT EXISTS transcode_strategy_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    root_id BIGINT NOT NULL,
    strategy_name VARCHAR(200),
    work_dir VARCHAR(1024),
    step_id INT NOT NULL,
    depends VARCHAR(256),
    type VARCHAR(64) NOT NULL,
    ti_anchor VARCHAR(64),
    param TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_strategy_step UNIQUE (root_id, step_id),
    INDEX idx_strategy_step_root_id (root_id)
);

-- AK/SK 表
CREATE TABLE IF NOT EXISTS transcode_access_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    access_key_id VARCHAR(64) NOT NULL UNIQUE,
    secret_key VARCHAR(128) NOT NULL,
    name VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    description VARCHAR(500)
);
