package org.openea.eap.module.system.api.translation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 翻译服务状态
 *
 * @author eap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationServiceStatus {

    /**
     * 服务是否可用
     */
    private Boolean available;

    /**
     * 支持的语言列表
     */
    private List<String> supportedLanguages;

    /**
     * 可用的翻译提供商
     */
    private List<ProviderStatus> providers;

    /**
     * 缓存状态
     */
    private CacheStatus cacheStatus;

    /**
     * 服务统计信息
     */
    private ServiceStatistics statistics;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderStatus {
        /**
         * 提供商名称
         */
        private String name;

        /**
         * 是否可用
         */
        private Boolean available;

        /**
         * 支持的模型
         */
        private List<String> supportedModels;

        /**
         * 当前使用的模型
         */
        private String currentModel;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 限流状态
         */
        private RateLimitStatus rateLimitStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitStatus {
        /**
         * 每分钟限制数
         */
        private Integer limitPerMinute;

        /**
         * 当前已使用数
         */
        private Integer currentUsage;

        /**
         * 剩余可用数
         */
        private Integer remaining;

        /**
         * 重置时间
         */
        private LocalDateTime resetTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheStatus {
        /**
         * 缓存是否启用
         */
        private Boolean enabled;

        /**
         * 缓存命中率
         */
        private Double hitRate;

        /**
         * 缓存总数
         */
        private Long totalCached;

        /**
         * 今日命中数
         */
        private Long todayHits;

        /**
         * 今日未命中数
         */
        private Long todayMisses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStatistics {
        /**
         * 今日翻译总数
         */
        private Long todayTranslations;

        /**
         * 今日成功数
         */
        private Long todaySuccessful;

        /**
         * 今日失败数
         */
        private Long todayFailed;

        /**
         * 平均响应时间（毫秒）
         */
        private Double averageResponseTime;

        /**
         * 平均质量评分
         */
        private Double averageQuality;

        /**
         * 各场景翻译统计
         */
        private Map<String, Long> sceneStatistics;
    }
}