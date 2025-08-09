package org.openea.eap.module.infra.api.translate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openea.eap.module.infra.enums.translate.TranslateConstants;

/**
 * 翻译选项配置
 * 用于配置翻译请求的各种选项，如引擎选择、质量要求、缓存策略等
 *
 * @author EAP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslateOptions {

    /**
     * 指定翻译引擎类型
     * 如果为null，则使用默认引擎选择策略
     */
    private String engineType;

    /**
     * 翻译质量等级
     * 可选值：FAST, STANDARD, HIGH
     */
    @Builder.Default
    private String quality = "STANDARD";

    /**
     * 翻译模式
     * 可选值：DIRECT, CONTEXT_AWARE, BATCH
     */
    @Builder.Default
    private String mode = "CONTEXT_AWARE";

    /**
     * 是否启用缓存
     * 默认启用缓存以提高性能
     */
    @Builder.Default
    private Boolean enableCache = true;

    /**
     * 缓存过期时间（秒）
     * 默认使用系统配置的缓存时间
     */
    private Integer cacheExpireSeconds;

    /**
     * 请求超时时间（毫秒）
     * 默认使用系统配置的超时时间
     */
    private Integer timeoutMs;

    /**
     * 最大重试次数
     * 当翻译失败时的重试次数
     */
    @Builder.Default
    private Integer maxRetries = 2;

    /**
     * 是否启用回退策略
     * 当首选引擎失败时，是否尝试其他引擎
     */
    @Builder.Default
    private Boolean enableFallback = true;

    /**
     * 是否保留原文格式
     * 对于包含特殊格式的文本，是否尝试保留格式
     */
    @Builder.Default
    private Boolean preserveFormat = false;

    /**
     * 是否启用术语一致性检查
     * 确保专业术语翻译的一致性
     */
    @Builder.Default
    private Boolean enableTerminologyCheck = false;

    /**
     * 自定义翻译提示
     * 为AI翻译引擎提供额外的上下文提示
     */
    private String customPrompt;

    /**
     * 创建默认翻译选项
     *
     * @return 默认配置的翻译选项
     */
    public static TranslateOptions defaultOptions() {
        return TranslateOptions.builder().build();
    }

    /**
     * 创建快速翻译选项
     * 优先速度，降低质量要求
     *
     * @return 快速翻译选项
     */
    public static TranslateOptions fastOptions() {
        return TranslateOptions.builder()
                .quality("FAST")
                .mode("DIRECT")
                .maxRetries(1)
                .enableFallback(false)
                .build();
    }

    /**
     * 创建高质量翻译选项
     * 优先质量，可能较慢
     *
     * @return 高质量翻译选项
     */
    public static TranslateOptions highQualityOptions() {
        return TranslateOptions.builder()
                .quality("HIGH")
                .mode("CONTEXT_AWARE")
                .maxRetries(3)
                .enableTerminologyCheck(true)
                .preserveFormat(true)
                .build();
    }

    /**
     * 创建批量翻译选项
     * 适用于大量文本的批量翻译
     *
     * @return 批量翻译选项
     */
    public static TranslateOptions batchOptions() {
        return TranslateOptions.builder()
                .mode("BATCH")
                .enableCache(true)
                .maxRetries(1)
                .build();
    }

    /**
     * 验证选项配置的有效性
     *
     * @throws IllegalArgumentException 当配置无效时抛出异常
     */
    public void validate() {
        if (quality != null && !isValidQuality(quality)) {
            throw new IllegalArgumentException("无效的翻译质量等级: " + quality);
        }
        
        if (mode != null && !isValidMode(mode)) {
            throw new IllegalArgumentException("无效的翻译模式: " + mode);
        }
        
        if (maxRetries != null && maxRetries < 0) {
            throw new IllegalArgumentException("重试次数不能为负数: " + maxRetries);
        }
        
        if (timeoutMs != null && timeoutMs <= 0) {
            throw new IllegalArgumentException("超时时间必须大于0: " + timeoutMs);
        }
        
        if (cacheExpireSeconds != null && cacheExpireSeconds <= 0) {
            throw new IllegalArgumentException("缓存过期时间必须大于0: " + cacheExpireSeconds);
        }
    }

    /**
     * 检查质量等级是否有效
     *
     * @param quality 质量等级
     * @return 是否有效
     */
    private boolean isValidQuality(String quality) {
        return "FAST".equals(quality) ||
               "STANDARD".equals(quality) ||
               "HIGH".equals(quality);
    }

    /**
     * 检查翻译模式是否有效
     *
     * @param mode 翻译模式
     * @return 是否有效
     */
    private boolean isValidMode(String mode) {
        return "DIRECT".equals(mode) ||
               "CONTEXT_AWARE".equals(mode) ||
               "BATCH".equals(mode);
    }

    /**
     * 获取有效的超时时间
     *
     * @return 超时时间，如果未设置则返回默认值
     */
    public int getEffectiveTimeoutMs() {
        return timeoutMs != null ? timeoutMs : 30000; // 30秒默认超时
    }

    /**
     * 获取有效的缓存过期时间
     *
     * @return 缓存过期时间，如果未设置则返回默认值
     */
    public int getEffectiveCacheExpireSeconds() {
        return cacheExpireSeconds != null ? cacheExpireSeconds : 3600; // 1小时默认缓存
    }

    /**
     * 是否启用缓存（考虑null值）
     *
     * @return 是否启用缓存
     */
    public boolean isCacheEnabled() {
        return enableCache != null ? enableCache : true;
    }

    /**
     * 是否启用回退策略（考虑null值）
     *
     * @return 是否启用回退策略
     */
    public boolean isFallbackEnabled() {
        return enableFallback != null ? enableFallback : true;
    }

    /**
     * 复制选项并修改引擎类型
     *
     * @param newEngineType 新的引擎类型
     * @return 新的选项对象
     */
    public TranslateOptions withEngineType(String newEngineType) {
        return TranslateOptions.builder()
                .engineType(newEngineType)
                .quality(this.quality)
                .mode(this.mode)
                .enableCache(this.enableCache)
                .cacheExpireSeconds(this.cacheExpireSeconds)
                .timeoutMs(this.timeoutMs)
                .maxRetries(this.maxRetries)
                .enableFallback(this.enableFallback)
                .preserveFormat(this.preserveFormat)
                .enableTerminologyCheck(this.enableTerminologyCheck)
                .customPrompt(this.customPrompt)
                .build();
    }

    /**
     * 复制选项并修改质量等级
     *
     * @param newQuality 新的质量等级
     * @return 新的选项对象
     */
    public TranslateOptions withQuality(String newQuality) {
        return TranslateOptions.builder()
                .engineType(this.engineType)
                .quality(newQuality)
                .mode(this.mode)
                .enableCache(this.enableCache)
                .cacheExpireSeconds(this.cacheExpireSeconds)
                .timeoutMs(this.timeoutMs)
                .maxRetries(this.maxRetries)
                .enableFallback(this.enableFallback)
                .preserveFormat(this.preserveFormat)
                .enableTerminologyCheck(this.enableTerminologyCheck)
                .customPrompt(this.customPrompt)
                .build();
    }
}