-- 步骤输出 JSON，用于单步骤重试
ALTER TABLE transcode_task ADD COLUMN step_outputs TEXT NULL COMMENT '各步骤输出路径 JSON，如 {"1":"path/1080p.mp4","2":"path/480p.mp4"}';
