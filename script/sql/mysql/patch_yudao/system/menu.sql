

-- 菜单表维护

-- 菜单增加别名/key
ALTER TABLE `system_menu`
    ADD COLUMN `alias` varchar(50) NULL COMMENT '别名/key' AFTER `id`;

-- 菜单增加所属子系统
ALTER TABLE `system_menu`
    ADD COLUMN `system_id` bigint NOT NULL default 0 COMMENT '所属子系统' AFTER `alias`;
