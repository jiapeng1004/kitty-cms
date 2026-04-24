-- 资源下载分级与预览：封面/雪碧图/转码产物等（destination_type 统一标识，SOURCE 不落库、由元数据表表达）
CREATE TABLE IF NOT EXISTS `kt_resource_derivative`
(
    `id`                 varchar(64)  NOT NULL,
    `resource_id`        varchar(64)  NOT NULL,
    `destination_type`  varchar(64)  NOT NULL COMMENT '如 SOURCE(不落库) / COVER / SPRITE / 720P 等',
    `storage_id`         varchar(64)           DEFAULT NULL,
    `object_key`         varchar(1024)         DEFAULT NULL,
    `external_url`       varchar(2048)         DEFAULT NULL COMMENT '可直接访问的绝对 URL（如转码机 Magic 产出）',
    `file_size`          bigint                DEFAULT NULL,
    `meta_json`          text                  DEFAULT NULL COMMENT '拓展：编码、码率、时长等 JSON',
    `create_time`        datetime     DEFAULT CURRENT_TIMESTAMP,
    `creator`            varchar(64)  DEFAULT NULL,
    `update_time`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updater`            varchar(64)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_res_dest` (`resource_id`, `destination_type`),
    KEY `idx_rd_resource` (`resource_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '资源衍生文件（多码率/封面/雪碧等）';
