

ALTER TABLE `system_oauth2_refresh_token`
    ADD COLUMN `user_key` varchar(50) NULL COMMENT 'account/username' AFTER `user_id`;

ALTER TABLE `system_oauth2_access_token`
    ADD COLUMN `user_key` varchar(50) NULL COMMENT 'account/username' AFTER `user_id`;
