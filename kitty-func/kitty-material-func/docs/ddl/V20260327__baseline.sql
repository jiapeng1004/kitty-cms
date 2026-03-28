-- material 核心表结构草案（阶段1）

CREATE TABLE IF NOT EXISTS `kt_catalog` (
  `id` varchar(64) NOT NULL COMMENT '栏目ID',
  `parent_id` varchar(64) NOT NULL DEFAULT '0' COMMENT '父栏目ID, 无父级固定0',
  `name` varchar(128) NOT NULL COMMENT '栏目名称',
  `scope_type` varchar(16) NOT NULL DEFAULT 'public' COMMENT 'public/private',
  `tree_code` varchar(100) NULL DEFAULT NULL COMMENT '栏目树编码',
  `owner_user_id` varchar(64) DEFAULT NULL COMMENT '私有栏目所属用户',
  `sort_num` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_catalog_parent` (`parent_id`),
  UNIQUE KEY `uk_tree_code_parent` (`tree_code`),
  UNIQUE KEY `uk_pid_name` (`parent_id`,`name`),
  KEY `idx_catalog_scope_owner` (`scope_type`,`owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='栏目表';

CREATE TABLE IF NOT EXISTS `kt_resource` (
  `id` varchar(64) NOT NULL COMMENT '资源ID',
  `title` varchar(255) NOT NULL COMMENT '资源标题',
  `catalog_id` varchar(64) NOT NULL COMMENT '栏目ID',
  `catalog_tree_code` varchar(512) DEFAULT NULL COMMENT '栏目树编码',
  `type` int NOT NULL COMMENT '1视频2音频3图片4文本5Office6其他7文件夹',
  `parent_id` varchar(64) NOT NULL DEFAULT '0' COMMENT '父资源ID, 无父级固定0',
  `fingerprint` varchar(128) DEFAULT NULL COMMENT '文件指纹',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_resource_catalog` (`catalog_id`),
  KEY `idx_resource_parent` (`parent_id`),
  KEY `idx_resource_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

CREATE TABLE IF NOT EXISTS `kt_file_storage` (
  `id` varchar(64) NOT NULL COMMENT '存储记录ID',
  `storage_type` varchar(32) NOT NULL COMMENT 's3/disk',
  `storage_code` varchar(64) NOT NULL COMMENT '存储实例编码',
  `bucket` varchar(128) DEFAULT NULL COMMENT '桶/容器',
  `internal_endpoint` varchar(255) DEFAULT NULL COMMENT '内网地址',
  `external_endpoint` varchar(255) DEFAULT NULL COMMENT '外网地址',
  `access_key` varchar(255) DEFAULT NULL COMMENT '访问key',
  `secret_key` varchar(255) DEFAULT NULL COMMENT '访问secret',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_storage_type_code` (`storage_type`,`storage_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储记录表';

CREATE TABLE IF NOT EXISTS `kt_resource_task` (
  `id` varchar(64) NOT NULL COMMENT '任务记录ID',
  `resource_id` varchar(64) NOT NULL COMMENT '资源ID',
  `resource_title` varchar(255) NOT NULL COMMENT '资源标题',
  `task_type` varchar(32) NOT NULL COMMENT 'transcode/ai_audit/ai_tag',
  `third_task_id` varchar(128) DEFAULT NULL COMMENT '三方任务ID',
  `progress` int NOT NULL DEFAULT 0 COMMENT '任务进度0-100',
  `status` varchar(32) NOT NULL COMMENT 'pending/running/success/failed',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_task_resource` (`resource_id`),
  KEY `idx_task_type_status` (`task_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源任务表';

CREATE TABLE IF NOT EXISTS `kt_catalog_permission` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `role_id` varchar(64) NOT NULL COMMENT '角色ID或public',
  `catalog_id` varchar(64) NOT NULL COMMENT '栏目ID',
  `permission_code` varchar(128) NOT NULL COMMENT '权限编码',
  `allow_flag` tinyint NOT NULL COMMENT '1=允许,0=强制禁用',
  `editable` tinyint NOT NULL DEFAULT 1 COMMENT '是否允许修改',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_catalog_permission` (`role_id`,`catalog_id`,`permission_code`),
  KEY `idx_catalog_permission_catalog` (`catalog_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='栏目权限表';

CREATE TABLE IF NOT EXISTS `kt_metadata_template` (
  `id` varchar(64) NOT NULL COMMENT '模板ID',
  `name` varchar(128) NOT NULL COMMENT '模板名称',
  `catalog_id` varchar(64) DEFAULT NULL COMMENT '绑定栏目ID',
  `resource_type` int DEFAULT NULL COMMENT '绑定资源类型',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_template_catalog_type` (`catalog_id`,`resource_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编目模板表';

CREATE TABLE IF NOT EXISTS `kt_metadata_field` (
  `id` varchar(64) NOT NULL COMMENT '字段ID',
  `field_code` varchar(128) NOT NULL COMMENT '字段编码',
  `field_name` varchar(128) NOT NULL COMMENT '字段名',
  `input_type` varchar(32) NOT NULL COMMENT '输入类型',
  `required` tinyint NOT NULL DEFAULT 0 COMMENT '是否必填',
  `options_json` text COMMENT '可选值范围',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metadata_field_code` (`field_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编目字段表';

CREATE TABLE IF NOT EXISTS `kt_metadata_template_field_bind` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `template_id` varchar(64) NOT NULL COMMENT '模板ID',
  `field_id` varchar(64) NOT NULL COMMENT '字段ID',
  `sort_num` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_field` (`template_id`,`field_id`),
  KEY `idx_template_sort` (`template_id`,`sort_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板字段绑定表';

CREATE TABLE IF NOT EXISTS `kt_metadata_instance` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `resource_id` varchar(64) NOT NULL COMMENT '资源ID',
  `template_id` varchar(64) NOT NULL COMMENT '模板ID',
  `field_id` varchar(64) NOT NULL COMMENT '字段ID',
  `field_value` text COMMENT '字段值',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `last_version` tinyint NOT NULL DEFAULT 1 COMMENT '是否最新',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_metadata_resource` (`resource_id`),
  KEY `idx_metadata_last` (`resource_id`,`last_version`),
  KEY `idx_metadata_version` (`resource_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编目实例表';

CREATE TABLE IF NOT EXISTS `kt_review_task` (
  `id` varchar(64) NOT NULL COMMENT '审核任务ID',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型',
  `biz_id` varchar(64) NOT NULL COMMENT '业务ID',
  `submit_user_id` varchar(64) NOT NULL COMMENT '提交人',
  `review_user_id` varchar(64) DEFAULT NULL COMMENT '审核人',
  `status` varchar(32) NOT NULL COMMENT 'pending/approved/rejected',
  `review_comment` varchar(512) DEFAULT NULL COMMENT '审核意见',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_review_biz` (`biz_type`,`biz_id`),
  KEY `idx_review_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核任务表';

CREATE TABLE IF NOT EXISTS `kt_internal_message` (
  `id` varchar(64) NOT NULL COMMENT '站内信ID',
  `sender_user_id` varchar(64) NOT NULL COMMENT '发送者',
  `receiver_user_id` varchar(64) NOT NULL COMMENT '接收者',
  `content` text NOT NULL COMMENT '消息内容',
  `message_status` varchar(32) NOT NULL DEFAULT 'unread' COMMENT 'unread/read',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_message_receiver_status` (`receiver_user_id`,`message_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信表';

-- 资源物理文件记录：与 kt_file_storage、kt_resource 关联

CREATE TABLE IF NOT EXISTS `kt_meta_file` (
  `id` varchar(64) NOT NULL COMMENT '文件记录ID',
  `resource_id` varchar(64) NOT NULL COMMENT '资源ID',
  `name` varchar(512) NOT NULL COMMENT '展示文件名',
  `size` bigint DEFAULT NULL COMMENT '文件大小(字节)，可与资源指纹侧一致',
  `storage_id` varchar(64) NOT NULL COMMENT '存储记录ID（kt_file_storage.id）',
  `object_key` varchar(1024) NOT NULL COMMENT '对象键（规范化后）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meta_file_resource` (`resource_id`),
  KEY `idx_meta_file_storage` (`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源物理文件记录';

-- 资源向量：多来源并存（与 KtResourceEmbedding 对齐）

CREATE TABLE IF NOT EXISTS `kt_resource_embedding` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `resource_id` varchar(64) NOT NULL COMMENT '资源ID',
  `source_type` varchar(128) NOT NULL COMMENT '来源标识，如 none、openai:text-embedding-3-small',
  `vector_json` text NOT NULL COMMENT '向量 JSON（float 数组）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_embedding_resource_source` (`resource_id`,`source_type`),
  KEY `idx_resource_embedding_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源向量表（多来源）';

-- 8.x 转码策略（素材侧影子配置）与栏目绑定；kt_resource_task 扩展字段支撑重试

CREATE TABLE IF NOT EXISTS `kt_material_transcode_strategy` (
  `id` varchar(64) NOT NULL COMMENT '策略ID',
  `name` varchar(128) NOT NULL COMMENT '名称',
  `platform_code` varchar(32) NOT NULL COMMENT '如 kitty_transcoder_grpc',
  `external_strategy_id` varchar(64) NOT NULL COMMENT '转码服务侧策略ID',
  `params_json` text COMMENT '扩展参数JSON',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_mts_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材转码策略';

CREATE TABLE IF NOT EXISTS `kt_catalog_transcode_strategy_bind` (
  `id` varchar(64) NOT NULL,
  `catalog_id` varchar(64) NOT NULL COMMENT '栏目ID',
  `strategy_id` varchar(64) NOT NULL COMMENT 'kt_material_transcode_strategy.id',
  `resource_type` int DEFAULT NULL COMMENT '空=匹配任意资源类型',
  `sort_num` int NOT NULL DEFAULT 0 COMMENT '越小越优先',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_ctsb_catalog` (`catalog_id`,`sort_num`),
  KEY `idx_ctsb_strategy` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='栏目转码策略绑定';

ALTER TABLE `kt_resource_task`
  ADD COLUMN `input_type` varchar(16) DEFAULT NULL COMMENT 'DISK/HTTP' AFTER `status`,
  ADD COLUMN `input_path` varchar(1024) DEFAULT NULL COMMENT '入参路径或URL' AFTER `input_type`,
  ADD COLUMN `material_strategy_id` varchar(64) DEFAULT NULL COMMENT '素材侧策略ID' AFTER `input_path`;

-- 分片上传会话与已登记分片（5.5 会话模型；实际字节流与合并见后续任务）

CREATE TABLE IF NOT EXISTS `kt_chunk_upload_session` (
  `id` varchar(64) NOT NULL COMMENT '会话ID',
  `resource_id` varchar(64) NOT NULL COMMENT '资源ID',
  `storage_id` varchar(64) NOT NULL COMMENT 'kt_file_storage.id',
  `object_key` varchar(1024) NOT NULL COMMENT '对象键（规范化后）',
  `total_size` bigint NOT NULL COMMENT '对象总大小（字节）',
  `chunk_size` bigint NOT NULL COMMENT '单分片期望大小（末片可更短）',
  `chunk_count` int NOT NULL COMMENT '分片数量（total_size=0 时为0）',
  `status` varchar(32) NOT NULL COMMENT 'uploading/completed/cancelled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  KEY `idx_chunk_session_resource` (`resource_id`),
  KEY `idx_chunk_session_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传会话';

CREATE TABLE IF NOT EXISTS `kt_chunk_upload_part` (
  `id` varchar(64) NOT NULL COMMENT '分片记录ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `chunk_index` int NOT NULL COMMENT '分片序号，从0开始',
  `byte_size` bigint NOT NULL COMMENT '本分片字节数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chunk_part_session_index` (`session_id`,`chunk_index`),
  KEY `idx_chunk_part_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传已登记分片';

-- MAM：分片上传「建档」与会话关联（栏目/父级/类型/预编目快照）

ALTER TABLE `kt_chunk_upload_session`
  ADD COLUMN `catalog_id` varchar(64) DEFAULT NULL COMMENT '栏目ID（与 kt_resource 冗余快照）' AFTER `resource_id`,
  ADD COLUMN `parent_resource_id` varchar(64) DEFAULT NULL COMMENT '父资源ID（文件夹）' AFTER `catalog_id`,
  ADD COLUMN `title` varchar(255) DEFAULT NULL COMMENT '资源标题快照' AFTER `parent_resource_id`,
  ADD COLUMN `resource_type` int DEFAULT NULL COMMENT '资源类型 ResourceTypeEnum' AFTER `title`,
  ADD COLUMN `precatalog_json` mediumtext DEFAULT NULL COMMENT '可选预编目 JSON：templateId + fieldValues' AFTER `resource_type`;

