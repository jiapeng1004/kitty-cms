-- 迁移：为已有 transcode_task 表添加 task_type，strategy_id 改为可空
-- 仅对已有数据库执行，新安装的 schema.sql 已包含这些列
-- MySQL 执行前请确认表已存在且无 task_type 列
ALTER TABLE transcode_task ADD COLUMN task_type VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED_TRANSCODE';
ALTER TABLE transcode_task MODIFY strategy_id VARCHAR(64) NULL;
ALTER TABLE transcode_task ADD INDEX idx_transcode_task_task_type (task_type);
ALTER TABLE transcode_task ADD INDEX idx_transcode_task_completed_at (completed_at);
