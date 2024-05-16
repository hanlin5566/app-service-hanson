DROP TABLE IF EXISTS `tmp_user_info`;
CREATE TABLE `tmp_user_info` (
      `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
      `user_code` varchar(20) NOT NULL DEFAULT '' COMMENT '用户编号',
      `name` varchar(20) NOT NULL COMMENT '用户姓名',
      `gender` tinyint(4) NOT NULL COMMENT '性别 1:男 2:女',
      `mobile` varchar(64) NOT NULL COMMENT '手机号',
      `mobile_md5` varchar(32) NOT NULL COMMENT '手机号md5',
      `deleted` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否删除 0-删除 1-正常',
      `create_user` bigint(20)  COMMENT '创建人user_id',
      `update_user` bigint(20)  COMMENT '更新人user_id',
      `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (`id`),
      INDEX `idx_user_code` (user_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户示例表';

CREATE TABLE `sys_file` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `original_file_name` varchar(255) NOT NULL COMMENT '原始文件名',
    `file_name` varchar(255) NOT NULL COMMENT '文件名',
    `path` varchar(255) NOT NULL COMMENT '路径',
    `url` varchar(255) DEFAULT NULL COMMENT 'URL',
    `size` bigint(20) unsigned DEFAULT NULL COMMENT '文件大小',
    `comment` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='云文件表';