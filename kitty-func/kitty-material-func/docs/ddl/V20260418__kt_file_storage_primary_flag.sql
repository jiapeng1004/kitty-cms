-- 主存储：分片上传未传 storageId 时使用；全局至多一条 primary_flag=1
ALTER TABLE `kt_file_storage`
    ADD COLUMN `primary_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否主存储（全局唯一）' AFTER `secret_key`;
