ALTER TABLE `system_menu`
    ADD COLUMN `alias` varchar(50) NULL COMMENT '别名/key' AFTER `id`;
