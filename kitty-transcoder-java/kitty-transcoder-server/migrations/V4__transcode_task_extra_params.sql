ALTER TABLE `transcode_task`
    ADD COLUMN `extra_params_json` text NULL COMMENT 'MAM/上游透传 JSON' AFTER `notification_config`;
