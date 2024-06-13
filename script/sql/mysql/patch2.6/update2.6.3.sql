
-- 新表 子系统 system_subsystem

-- 菜单增加所属子系统
ALTER TABLE `system_menu`
    ADD COLUMN `system_id` bigint NOT NULL default 0 COMMENT '所属子系统' AFTER `alias`;

-- 部门增加类型
ALTER TABLE `system_dept`
    ADD COLUMN `category` varchar(50) NULL COMMENT '机构分类' AFTER `parent_id`;
