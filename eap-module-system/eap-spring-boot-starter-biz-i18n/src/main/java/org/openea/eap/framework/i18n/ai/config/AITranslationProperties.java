package org.openea.eap.framework.i18n.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * AI翻译配置属性
 *
 * @author eap
 */
@Data
@ConfigurationProperties(prefix = "eap.i18n.ai")
public class AITranslationProperties {

    /**
     * 是否启用AI翻译
     */
    private Boolean enabled = true;

    /**
     * 默认提供商
     */
    private String defaultProvider = "mock";

    /**
     * 翻译提供商配置
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();

    /**
     * 回退策略配置
     */
    private Map<String, String[]> fallbackChains = new HashMap<>();

    /**
     * 全局配置
     */
    private GlobalConfig global = new GlobalConfig();

    @Data
    public static class ProviderConfig {
        /**
         * 是否启用
         */
        private Boolean enabled = true;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API端点
         */
        private String apiEndpoint;

        /**
         * 默认模型
         */
        private String defaultModel;

        /**
         * 请求超时时间（毫秒）
         */
        private Integer timeout = 30000;

        /**
         * 最大重试次数
         */
        private Integer maxRetries = 3;

        /**
         * 优先级
         */
        private Integer priority = 10;

        /**
         * 每分钟最大请求数
         */
        private Integer rateLimit = 60;

        /**
         * 支持的语言列表
         */
        private String[] supportedLanguages;

        /**
         * 质量阈值
         */
        private Integer qualityThreshold = 5;

        /**
         * 额外参数
         */
        private Map<String, Object> extraParams = new HashMap<>();
    }

    @Data
    public static class GlobalConfig {
        /**
         * 默认质量阈值
         */
        private Integer defaultQualityThreshold = 5;

        /**
         * 是否启用翻译缓存
         */
        private Boolean enableCache = true;

        /**
         * 缓存过期时间（小时）
         */
        private Integer cacheExpireHours = 24;

        /**
         * 是否启用翻译质量评估
         */
        private Boolean enableQualityEvaluation = true;

        /**
         * 批量翻译最大数量
         */
        private Integer maxBatchSize = 100;
    }
}