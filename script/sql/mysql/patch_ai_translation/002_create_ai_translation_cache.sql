-- ========================================
-- AI翻译缓存表创建脚本
-- 版本: 1.0.0
-- 日期: 2025-02-08
-- 描述: 创建AI翻译缓存表，提高翻译性能
-- ========================================

-- 创建AI翻译缓存表
CREATE TABLE `ai_translation_cache` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `content_hash` varchar(64) NOT NULL COMMENT '内容哈希值(MD5)',
    `source_text` text NOT NULL COMMENT '源文本',
    `source_lang` varchar(10) NOT NULL COMMENT '源语言',
    `target_lang` varchar(10) NOT NULL COMMENT '目标语言',
    `translated_text` text NOT NULL COMMENT '翻译结果',
    `ai_provider` varchar(30) NOT NULL COMMENT 'AI提供商',
    `ai_model` varchar(50) NOT NULL COMMENT 'AI模型',
    `translation_quality` tinyint DEFAULT 5 COMMENT '翻译质量评分：1-10分',
    `confidence` decimal(3,2) DEFAULT NULL COMMENT '翻译置信度：0.00-1.00',
    `context_type` varchar(50) DEFAULT NULL COMMENT '上下文类型：system_message, ecommerce, auction等',
    `hit_count` int DEFAULT 1 COMMENT '缓存命中次数',
    `last_hit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后命中时间',
    `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
    `creator` varchar(64) DEFAULT 'system' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT 'system' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_content_hash_lang` (`content_hash`, `source_lang`, `target_lang`, `context_type`),
    KEY `idx_source_target_lang` (`source_lang`, `target_lang`),
    KEY `idx_ai_provider_model` (`ai_provider`, `ai_model`),
    KEY `idx_expire_time` (`expire_time`),
    KEY `idx_last_hit_time` (`last_hit_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI翻译缓存表';

-- 创建缓存统计表
CREATE TABLE `ai_translation_cache_stats` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `stat_date` date NOT NULL COMMENT '统计日期',
    `ai_provider` varchar(30) NOT NULL COMMENT 'AI提供商',
    `ai_model` varchar(50) DEFAULT NULL COMMENT 'AI模型',
    `source_lang` varchar(10) NOT NULL COMMENT '源语言',
    `target_lang` varchar(10) NOT NULL COMMENT '目标语言',
    `total_requests` int DEFAULT 0 COMMENT '总请求数',
    `cache_hits` int DEFAULT 0 COMMENT '缓存命中数',
    `cache_misses` int DEFAULT 0 COMMENT '缓存未命中数',
    `hit_rate` decimal(5,2) DEFAULT 0.00 COMMENT '命中率百分比',
    `avg_quality` decimal(3,1) DEFAULT 0.0 COMMENT '平均质量分数',
    `total_cost` decimal(10,4) DEFAULT 0.0000 COMMENT '总成本（如果有）',
    `creator` varchar(64) DEFAULT 'system' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT 'system' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stat_provider_lang_date` (`stat_date`, `ai_provider`, `ai_model`, `source_lang`, `target_lang`),
    KEY `idx_stat_date` (`stat_date`),
    KEY `idx_ai_provider` (`ai_provider`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI翻译缓存统计表';

-- 插入一些示例缓存数据
INSERT INTO `ai_translation_cache` (
    `content_hash`, `source_text`, `source_lang`, `target_lang`, 
    `translated_text`, `ai_provider`, `ai_model`, `translation_quality`, 
    `confidence`, `context_type`, `hit_count`, `expire_time`, `tenant_id`
) VALUES
(MD5('你好'), '你好', 'zh-CN', 'en-US', 'Hello', 'openai', 'gpt-3.5-turbo', 9, 0.95, 'system_message', 1, DATE_ADD(NOW(), INTERVAL 24 HOUR), 0),
(MD5('竞拍成功'), '竞拍成功', 'zh-CN', 'en-US', 'Bid successful', 'openai', 'gpt-3.5-turbo', 8, 0.88, 'auction', 3, DATE_ADD(NOW(), INTERVAL 24 HOUR), 0),
(MD5('商品缺货'), '商品缺货', 'zh-CN', 'en-US', 'Product out of stock', 'openai', 'gpt-3.5-turbo', 8, 0.90, 'ecommerce', 2, DATE_ADD(NOW(), INTERVAL 24 HOUR), 0),
(MD5('拍卖已结束'), '拍卖已结束', 'zh-CN', 'ja-JP', 'オークションは終了しました', 'openai', 'gpt-4', 9, 0.92, 'auction', 1, DATE_ADD(NOW(), INTERVAL 24 HOUR), 0),
(MD5('Hello'), 'Hello', 'en-US', 'zh-CN', '你好', 'mock', 'mock-v1', 7, 0.75, 'system_message', 5, DATE_ADD(NOW(), INTERVAL 24 HOUR), 0);

-- 插入示例统计数据
INSERT INTO `ai_translation_cache_stats` (
    `stat_date`, `ai_provider`, `ai_model`, `source_lang`, `target_lang`,
    `total_requests`, `cache_hits`, `cache_misses`, `hit_rate`, `avg_quality`, `tenant_id`
) VALUES
(CURDATE(), 'openai', 'gpt-3.5-turbo', 'zh-CN', 'en-US', 100, 75, 25, 75.00, 8.5, 0),
(CURDATE(), 'openai', 'gpt-4', 'zh-CN', 'ja-JP', 50, 30, 20, 60.00, 9.0, 0),
(CURDATE(), 'mock', 'mock-v1', 'en-US', 'zh-CN', 200, 180, 20, 90.00, 7.0, 0),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'openai', 'gpt-3.5-turbo', 'zh-CN', 'en-US', 80, 50, 30, 62.50, 8.2, 0);

-- 创建视图方便查询缓存统计
CREATE OR REPLACE VIEW `v_ai_cache_summary` AS
SELECT 
    `ai_provider`,
    `ai_model`,
    CONCAT(`source_lang`, '->', `target_lang`) as `language_pair`,
    COUNT(*) as `total_cached`,
    SUM(`hit_count`) as `total_hits`,
    AVG(`translation_quality`) as `avg_quality`,
    AVG(`confidence`) as `avg_confidence`,
    MIN(`create_time`) as `first_cached`,
    MAX(`last_hit_time`) as `last_used`
FROM `ai_translation_cache` 
WHERE `deleted` = 0 AND (`expire_time` IS NULL OR `expire_time` > NOW())
GROUP BY `ai_provider`, `ai_model`, `source_lang`, `target_lang`
ORDER BY `total_hits` DESC;

-- 创建清理过期缓存的存储过程
DELIMITER $$
CREATE PROCEDURE `CleanExpiredTranslationCache`()
BEGIN
    DECLARE affected_rows INT DEFAULT 0;
    
    -- 删除过期的缓存记录
    UPDATE `ai_translation_cache` 
    SET `deleted` = 1, `updater` = 'system_cleanup', `update_time` = NOW()
    WHERE `deleted` = 0 
      AND `expire_time` IS NOT NULL 
      AND `expire_time` < NOW();
    
    SET affected_rows = ROW_COUNT();
    
    -- 记录清理日志
    INSERT INTO `ai_translation_cache` (
        `content_hash`, `source_text`, `source_lang`, `target_lang`, 
        `translated_text`, `ai_provider`, `ai_model`, `context_type`, `creator`
    ) VALUES (
        MD5(CONCAT('cleanup_', NOW())), 
        CONCAT('清理过期缓存记录: ', affected_rows, ' 条'), 
        'system', 'system', 
        CONCAT('Cleaned expired cache records: ', affected_rows, ' items'),
        'system', 'cleanup', 'system_log', 'system_cleanup'
    );
    
    SELECT CONCAT('清理了 ', affected_rows, ' 条过期缓存记录') as result;
END$$
DELIMITER ;

-- 创建更新缓存命中统计的存储过程
DELIMITER $$
CREATE PROCEDURE `UpdateCacheHitStats`(
    IN p_provider VARCHAR(30),
    IN p_model VARCHAR(50),
    IN p_source_lang VARCHAR(10),
    IN p_target_lang VARCHAR(10),
    IN p_is_hit BOOLEAN
)
BEGIN
    INSERT INTO `ai_translation_cache_stats` (
        `stat_date`, `ai_provider`, `ai_model`, `source_lang`, `target_lang`,
        `total_requests`, `cache_hits`, `cache_misses`, `hit_rate`
    ) VALUES (
        CURDATE(), p_provider, p_model, p_source_lang, p_target_lang,
        1, IF(p_is_hit, 1, 0), IF(p_is_hit, 0, 1), IF(p_is_hit, 100.00, 0.00)
    ) ON DUPLICATE KEY UPDATE
        `total_requests` = `total_requests` + 1,
        `cache_hits` = `cache_hits` + IF(p_is_hit, 1, 0),
        `cache_misses` = `cache_misses` + IF(p_is_hit, 0, 1),
        `hit_rate` = ROUND((`cache_hits` + IF(p_is_hit, 1, 0)) * 100.0 / (`total_requests` + 1), 2),
        `update_time` = NOW();
END$$
DELIMITER ;

-- 添加表注释
ALTER TABLE `ai_translation_cache` COMMENT = 'AI翻译缓存表 - 基于内容哈希的缓存策略';
ALTER TABLE `ai_translation_cache_stats` COMMENT = 'AI翻译缓存统计表 - 用于性能监控和分析';

-- 记录迁移日志
INSERT INTO `sys_i18n_data` (`module`, `alias`, `name`, `json`, `remark`, `translation_source`, `creator`, `tenant_id`) VALUES
('system', 'migration.ai_cache.v1', '缓存表创建记录', '{"zh-CN": "AI翻译缓存表创建完成", "en-US": "AI translation cache tables created"}', '数据库迁移记录 - AI翻译缓存功能 v1.0.0', 'MANUAL', 'system', 0);