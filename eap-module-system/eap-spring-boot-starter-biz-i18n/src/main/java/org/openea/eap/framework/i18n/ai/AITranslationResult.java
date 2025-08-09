package org.openea.eap.framework.i18n.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI翻译结果
 *
 * @author eap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AITranslationResult {

    /**
     * 翻译后的文本
     */
    private String translatedText;

    /**
     * 翻译质量评分 (1-10)
     */
    private Integer qualityScore;

    /**
     * 使用的AI模型
     */
    private String model;

    /**
     * 提供商名称
     */
    private String provider;

    /**
     * 翻译是否成功
     */
    private Boolean success;

    /**
     * 错误信息（如果翻译失败）
     */
    private String errorMessage;

    /**
     * 翻译耗时（毫秒）
     */
    private Long duration;

    /**
     * 置信度 (0.0-1.0)
     */
    private Double confidence;

    /**
     * 额外的元数据
     */
    private String metadata;

    /**
     * 创建成功的翻译结果
     */
    public static AITranslationResult success(String translatedText, String model, String provider) {
        return AITranslationResult.builder()
                .translatedText(translatedText)
                .model(model)
                .provider(provider)
                .success(true)
                .build();
    }

    /**
     * 创建失败的翻译结果
     */
    public static AITranslationResult failure(String errorMessage, String provider) {
        return AITranslationResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .provider(provider)
                .build();
    }
}