-- ========================================
-- AI翻译功能扩展 - 扩展sys_i18n_data表结构
-- 版本: 1.0.0
-- 日期: 2025-02-08
-- 描述: 为sys_i18n_data表添加AI翻译相关字段
-- ========================================

-- 检查表是否存在，如果不存在则创建基础表
CREATE TABLE IF NOT EXISTS `sys_i18n_data` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `module` varchar(100) DEFAULT NULL COMMENT '模块，可选',
    `alias` varchar(200) DEFAULT NULL COMMENT 'key/别名',
    `name` varchar(200) NOT NULL COMMENT '名称',
    `json` text COMMENT '多语言设置json',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_module_alias` (`module`, `alias`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='国际化翻译数据表';

-- 添加AI翻译相关字段
-- 1. 添加translation_source字段标记翻译来源
ALTER TABLE `sys_i18n_data` 
ADD COLUMN `translation_source` varchar(20) DEFAULT 'MANUAL' COMMENT '翻译来源：MANUAL-人工翻译, AI_AUTO-AI自动翻译, LLM_AUTO-LLM自动翻译' AFTER `remark`;

-- 2. 添加ai_model字段记录使用的AI模型
ALTER TABLE `sys_i18n_data` 
ADD COLUMN `ai_model` varchar(50) DEFAULT NULL COMMENT '使用的AI模型：gpt-4, gpt-3.5-turbo, claude-3等' AFTER `translation_source`;

-- 3. 添加translation_quality字段评估翻译质量
ALTER TABLE `sys_i18n_data` 
ADD COLUMN `translation_quality` tinyint DEFAULT 5 COMMENT '翻译质量评分：1-10分，5为默认分数' AFTER `ai_model`;

-- 4. 添加ai_provider字段记录AI提供商
ALTER TABLE `sys_i18n_data` 
ADD COLUMN `ai_provider` varchar(30) DEFAULT NULL COMMENT 'AI提供商：openai, baidu, tencent, mock等' AFTER `translation_quality`;

-- 5. 添加translation_confidence字段记录翻译置信度
ALTER TABLE `sys_i18n_data` 
ADD COLUMN `translation_confidence` decimal(3,2) DEFAULT NULL COMMENT '翻译置信度：0.00-1.00' AFTER `ai_provider`;

-- 6. 添加last_ai_update字段记录最后AI更新时间
ALTER TABLE `sys_i18n_data` 
ADD COLUMN `last_ai_update` datetime DEFAULT NULL COMMENT '最后AI翻译更新时间' AFTER `translation_confidence`;

-- 创建索引优化查询性能
CREATE INDEX `idx_translation_source` ON `sys_i18n_data` (`translation_source`);
CREATE INDEX `idx_ai_provider_model` ON `sys_i18n_data` (`ai_provider`, `ai_model`);
CREATE INDEX `idx_translation_quality` ON `sys_i18n_data` (`translation_quality`);
CREATE INDEX `idx_last_ai_update` ON `sys_i18n_data` (`last_ai_update`);

-- 更新现有数据的默认值
UPDATE `sys_i18n_data` 
SET `translation_source` = 'MANUAL', 
    `translation_quality` = 5 
WHERE `translation_source` IS NULL;

-- 插入一些示例数据用于测试
INSERT INTO `sys_i18n_data` (`module`, `alias`, `name`, `json`, `remark`, `translation_source`, `ai_model`, `translation_quality`, `ai_provider`, `creator`, `tenant_id`) VALUES
('mall_common', 'auction.bid.success', '竞拍成功提示', '{"zh-CN": "竞拍成功", "en-US": "Bid successful", "ja-JP": "入札成功"}', 'AI自动翻译示例', 'AI_AUTO', 'gpt-3.5-turbo', 8, 'openai', '1', 0),
('mall_common', 'auction.bid.failed', '竞拍失败提示', '{"zh-CN": "竞拍失败", "en-US": "Bid failed", "ja-JP": "入札失敗"}', 'AI自动翻译示例', 'AI_AUTO', 'gpt-3.5-turbo', 8, 'openai', '1', 0),
('mall_common', 'product.out.of.stock', '商品缺货提示', '{"zh-CN": "商品缺货", "en-US": "Product out of stock", "ja-JP": "商品在庫切れ"}', 'AI自动翻译示例', 'AI_AUTO', 'gpt-3.5-turbo', 7, 'openai', '1', 0),
('mall_auction', 'auction.session.not.started', '拍卖未开始', '{"zh-CN": "拍卖尚未开始", "en-US": "Auction has not started yet", "ja-JP": "オークションはまだ開始されていません"}', 'AI自动翻译示例', 'AI_AUTO', 'gpt-4', 9, 'openai', '1', 0),
('mall_auction', 'auction.session.ended', '拍卖已结束', '{"zh-CN": "拍卖已结束", "en-US": "Auction has ended", "ja-JP": "オークションは終了しました"}', 'AI自动翻译示例', 'AI_AUTO', 'gpt-4', 9, 'openai', '1', 0);

-- 创建视图方便查询AI翻译统计信息
CREATE OR REPLACE VIEW `v_ai_translation_stats` AS
SELECT 
    `ai_provider`,
    `ai_model`,
    `translation_source`,
    COUNT(*) as `total_count`,
    AVG(`translation_quality`) as `avg_quality`,
    MIN(`translation_quality`) as `min_quality`,
    MAX(`translation_quality`) as `max_quality`,
    COUNT(CASE WHEN `translation_quality` >= 8 THEN 1 END) as `high_quality_count`,
    COUNT(CASE WHEN `translation_quality` < 5 THEN 1 END) as `low_quality_count`
FROM `sys_i18n_data` 
WHERE `deleted` = 0 AND `translation_source` IN ('AI_AUTO', 'LLM_AUTO')
GROUP BY `ai_provider`, `ai_model`, `translation_source`
ORDER BY `avg_quality` DESC;

-- 添加注释说明
ALTER TABLE `sys_i18n_data` COMMENT = '国际化翻译数据表 - 支持AI自动翻译功能';

-- 记录迁移日志
INSERT INTO `sys_i18n_data` (`module`, `alias`, `name`, `json`, `remark`, `translation_source`, `creator`, `tenant_id`) VALUES
('system', 'migration.ai_translation.v1', '数据库迁移记录', '{"zh-CN": "AI翻译功能数据库迁移完成", "en-US": "AI translation database migration completed"}', '数据库迁移记录 - AI翻译功能扩展 v1.0.0', 'MANUAL', 'system', 0);