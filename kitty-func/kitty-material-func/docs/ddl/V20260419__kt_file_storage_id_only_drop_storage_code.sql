-- 存储配置：主键 id 即为对外 storageId，删除独立 storage_code（与 kt_meta_file / kt_chunk_upload_session 对齐）

-- 1. 子表引用从旧 UUID 迁到原 storage_code（此后与 kt_file_storage.id 一致）
UPDATE `kt_meta_file` mf
    INNER JOIN `kt_file_storage` fs ON mf.`storage_id` = fs.`id`
SET mf.`storage_id` = fs.`storage_code`
WHERE fs.`storage_code` IS NOT NULL
  AND fs.`storage_code` <> '';

UPDATE `kt_chunk_upload_session` cu
    INNER JOIN `kt_file_storage` fs ON cu.`storage_id` = fs.`id`
SET cu.`storage_id` = fs.`storage_code`
WHERE fs.`storage_code` IS NOT NULL
  AND fs.`storage_code` <> '';

-- 2. 主表主键改为业务编码（原 storage_code）
UPDATE `kt_file_storage` SET `id` = `storage_code` WHERE `storage_code` IS NOT NULL AND `storage_code` <> '';

-- 3. 删除冗余列与旧索引
ALTER TABLE `kt_file_storage`
    DROP COLUMN `storage_code`,
    DROP INDEX `idx_storage_type_code`;

ALTER TABLE `kt_file_storage`
    ADD KEY `idx_storage_type` (`storage_type`);
