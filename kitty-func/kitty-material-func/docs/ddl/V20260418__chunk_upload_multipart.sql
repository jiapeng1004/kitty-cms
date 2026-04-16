-- 对象存储分片上传：S3 Multipart uploadId；每片登记 ETag（完成 CompleteMultipartUpload 所需）

ALTER TABLE `kt_chunk_upload_session`
    ADD COLUMN `multipart_upload_id` varchar(256) DEFAULT NULL COMMENT '对象存储 multipart upload id（仅 s3 引擎且 chunk_count>0）' AFTER `object_key`;

ALTER TABLE `kt_chunk_upload_part`
    ADD COLUMN `part_etag` varchar(128) DEFAULT NULL COMMENT '对象存储分片 ETag（UploadPart 返回；磁盘引擎为空）' AFTER `byte_size`;
