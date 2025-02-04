
-- 数据字段表结构维护

-- ALTER TABLE `system_dict_type`
--     ADD COLUMN `parent_id` bigint NULL COMMENT 'parent' AFTER `id`;

ALTER TABLE `system_dict_type`
    ADD COLUMN `is_tree` tinyint NULL DEFAULT 0 COMMENT '树形' AFTER `status`,
ADD COLUMN `parent_id` bigint NULL DEFAULT 0 COMMENT '上级' AFTER `is_tree`,
ADD COLUMN `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'data' COMMENT '数据类型(data/json/sql)' AFTER `parent_id`,
ADD COLUMN `data_json` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'JSON数据' AFTER `data_type`,
ADD COLUMN `data_sql` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '查询SQL' AFTER `data_json`,
ADD COLUMN `data_ds` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SQL数据源' AFTER `data_sql`;

ALTER TABLE `system_dict_data`
    ADD COLUMN `parent_id` bigint NULL COMMENT 'parent' AFTER `id`;


