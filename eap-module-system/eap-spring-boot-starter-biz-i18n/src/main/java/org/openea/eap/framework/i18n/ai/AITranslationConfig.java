package org.openea.eap.framework.i18n.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI翻译服务配置
 *
 * @author eap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AITranslationConfig {

    /**
     * 提供商名称
     */
    private String providerName;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API端点URL
     */
    private String apiEndpoint;

    /**
     * 默认模型
     */
    private String defaultModel;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;

    /**
     * 每分钟最大请求数
     */
    private Integer rateLimit;

    /**
     * 支持的语言列表
     */
    private String[] supportedLanguages;

    /**
     * 额外配置参数
     */
    private Map<String, Object> extraParams;

    /**
     * 质量阈值（低于此分数的翻译将被标记为低质量）
     */
    private Integer qualityThreshold;
}