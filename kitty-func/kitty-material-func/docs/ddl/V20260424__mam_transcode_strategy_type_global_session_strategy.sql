-- MAM 转码策略：按资源类型 + 同类至多一条「全局默认」；分片上传可显式指定策略 ID
ALTER TABLE `kt_material_transcode_strategy`
    ADD COLUMN `resource_type` int      DEFAULT NULL COMMENT '与 kt_resource.type 一致；空=兼容旧数据/不限定' AFTER `enabled`,
    ADD COLUMN `is_global_default` tinyint NOT NULL DEFAULT 0 COMMENT '1=该 resource_type 的全局默认策略' AFTER `resource_type`;

-- 分片上传会话显式转码策略（与解析链「显式优先」一致）
ALTER TABLE `kt_chunk_upload_session`
    ADD COLUMN `transcode_strategy_id` varchar(64) DEFAULT NULL COMMENT '显式转码策略 kt_material_transcode_strategy.id' AFTER `precatalog_json`;

CREATE INDEX `idx_mts_type_global` ON `kt_material_transcode_strategy` (`resource_type`, `is_global_default`);
CREATE INDEX `idx_chunk_session_transcode` ON `kt_chunk_upload_session` (`transcode_strategy_id`);
