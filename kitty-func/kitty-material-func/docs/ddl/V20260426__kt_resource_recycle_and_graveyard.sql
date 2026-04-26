-- 资源逻辑删（回收站）与坟场归档表

ALTER TABLE `kt_resource`
    ADD COLUMN `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0 正常 1 已入回收站' AFTER `fingerprint`,
    ADD KEY `idx_resource_recycle` (`deleted`, `catalog_id`);

CREATE TABLE IF NOT EXISTS `kt_resource_graveyard`
(
    `id`            varchar(64)  NOT NULL COMMENT '坟场主键',
    `archive_type`  varchar(64)  NOT NULL COMMENT '归档类型，如 kt_resource',
    `original_id`   varchar(64)  NOT NULL COMMENT '原主表主键',
    `json`          json         NOT NULL COMMENT 'EAV/快照 JSON',
    `create_time`   datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator`       varchar(64)           DEFAULT NULL COMMENT '创建者',
    PRIMARY KEY (`id`),
    KEY `idx_graveyard_original` (`original_id`),
    KEY `idx_graveyard_type` (`archive_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='资源彻底删除归档（坟场）';
