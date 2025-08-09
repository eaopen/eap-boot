package org.openea.eap.module.infra.api.translate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 翻译元数据
 * 包含翻译过程中的统计信息和元数据
 *
 * @author EAP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslateMetadata {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 翻译开始时间
     */
    private LocalDateTime startTime;

    /**
     * 翻译结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总耗时（毫秒）
     */
    private Long totalDuration;

    /**
     * 源文本字符数
     */
    private Integer sourceCharCount;

    /**
     * 翻译的目标语言数量
     */
    private Integer targetLangCount;

    /**
     * 使用的翻译引擎列表
     */
    private java.util.List<String> enginesUsed;

    /**
     * 缓存命中次数
     */
    @Builder.Default
    private Integer cacheHits = 0;

    /**
     * 缓存未命中次数
     */
    @Builder.Default
    private Integer cacheMisses = 0;

    /**
     * 重试次数
     */
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 回退引擎使用次数
     */
    @Builder.Default
    private Integer fallbackCount = 0;

    /**
     * API调用次数
     */
    @Builder.Default
    private Integer apiCallCount = 0;

    /**
     * 翻译成本（如果适用）
     */
    private Double cost;

    /**
     * 成本单位
     */
    private String costUnit;

    /**
     * 翻译质量评估
     */
    private String qualityAssessment;

    /**
     * 检测到的源语言
     */
    private String detectedSourceLang;

    /**
     * 语言检测置信度
     */
    private Double langDetectionConfidence;

    /**
     * 翻译模式
     */
    private String translationMode;

    /**
     * 翻译场景
     */
    private String translationScenario;

    /**
     * 是否使用了术语库
     */
    @Builder.Default
    private Boolean terminologyUsed = false;

    /**
     * 术语匹配数量
     */
    @Builder.Default
    private Integer terminologyMatches = 0;

    /**
     * 格式保留状态
     */
    @Builder.Default
    private Boolean formatPreserved = false;

    /**
     * 警告信息列表
     */
    private java.util.List<String> warnings;

    /**
     * 扩展元数据
     */
    private Map<String, Object> extensions;

    /**
     * 创建基础元数据
     *
     * @param requestId 请求ID
     * @return 元数据对象
     */
    public static TranslateMetadata create(String requestId) {
        return TranslateMetadata.builder()
                .requestId(requestId)
                .startTime(LocalDateTime.now())
                .build();
    }

    /**
     * 标记翻译完成
     */
    public void markCompleted() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.totalDuration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 增加缓存命中次数
     */
    public void incrementCacheHits() {
        this.cacheHits = (this.cacheHits != null ? this.cacheHits : 0) + 1;
    }

    /**
     * 增加缓存未命中次数
     */
    public void incrementCacheMisses() {
        this.cacheMisses = (this.cacheMisses != null ? this.cacheMisses : 0) + 1;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
    }

    /**
     * 增加回退次数
     */
    public void incrementFallbackCount() {
        this.fallbackCount = (this.fallbackCount != null ? this.fallbackCount : 0) + 1;
    }

    /**
     * 增加API调用次数
     */
    public void incrementApiCallCount() {
        this.apiCallCount = (this.apiCallCount != null ? this.apiCallCount : 0) + 1;
    }

    /**
     * 添加使用的引擎
     *
     * @param engine 引擎名称
     */
    public void addEngineUsed(String engine) {
        if (this.enginesUsed == null) {
            this.enginesUsed = new java.util.ArrayList<>();
        }
        if (!this.enginesUsed.contains(engine)) {
            this.enginesUsed.add(engine);
        }
    }

    /**
     * 添加警告信息
     *
     * @param warning 警告信息
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new java.util.ArrayList<>();
        }
        this.warnings.add(warning);
    }

    /**
     * 添加扩展元数据
     *
     * @param key 键
     * @param value 值
     */
    public void addExtension(String key, Object value) {
        if (this.extensions == null) {
            this.extensions = new java.util.HashMap<>();
        }
        this.extensions.put(key, value);
    }

    /**
     * 获取扩展元数据
     *
     * @param key 键
     * @return 值
     */
    public Object getExtension(String key) {
        return this.extensions != null ? this.extensions.get(key) : null;
    }

    /**
     * 计算缓存命中率
     *
     * @return 缓存命中率（0.0-1.0）
     */
    public double getCacheHitRate() {
        int totalCacheAttempts = (this.cacheHits != null ? this.cacheHits : 0) + 
                                (this.cacheMisses != null ? this.cacheMisses : 0);
        if (totalCacheAttempts == 0) {
            return 0.0;
        }
        return (double) (this.cacheHits != null ? this.cacheHits : 0) / totalCacheAttempts;
    }

    /**
     * 检查是否有警告
     *
     * @return 是否有警告
     */
    public boolean hasWarnings() {
        return this.warnings != null && !this.warnings.isEmpty();
    }

    /**
     * 获取警告数量
     *
     * @return 警告数量
     */
    public int getWarningCount() {
        return this.warnings != null ? this.warnings.size() : 0;
    }

    /**
     * 检查是否使用了回退策略
     *
     * @return 是否使用了回退策略
     */
    public boolean hasFallback() {
        return this.fallbackCount != null && this.fallbackCount > 0;
    }

    /**
     * 检查是否有重试
     *
     * @return 是否有重试
     */
    public boolean hasRetries() {
        return this.retryCount != null && this.retryCount > 0;
    }
}