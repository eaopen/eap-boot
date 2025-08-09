package org.openea.eap.module.system.api.translation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 翻译响应DTO
 *
 * @author eap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponseDTO {

    /**
     * 翻译是否成功
     */
    private Boolean success;

    /**
     * 原文
     */
    private String originalText;

    /**
     * 译文
     */
    private String translatedText;

    /**
     * 源语言
     */
    private String fromLang;

    /**
     * 目标语言
     */
    private String toLang;

    /**
     * 翻译质量评分 (1-10)
     */
    private Integer qualityScore;

    /**
     * 翻译置信度 (0.0-1.0)
     */
    private Double confidence;

    /**
     * 使用的AI提供商
     */
    private String provider;

    /**
     * 使用的AI模型
     */
    private String model;

    /**
     * 是否来自缓存
     */
    private Boolean fromCache;

    /**
     * 翻译耗时（毫秒）
     */
    private Long duration;

    /**
     * 翻译场景
     */
    private String scene;

    /**
     * 错误信息（翻译失败时）
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 翻译建议（如长度超限、质量问题等）
     */
    private List<String> suggestions;

    /**
     * 替代翻译选项
     */
    private List<String> alternatives;

    /**
     * 翻译时间
     */
    private LocalDateTime translateTime;

    /**
     * 业务标识
     */
    private String businessId;

    /**
     * 创建成功的翻译响应
     */
    public static TranslationResponseDTO success(String originalText, String translatedText, 
                                               String fromLang, String toLang) {
        return TranslationResponseDTO.builder()
                .success(true)
                .originalText(originalText)
                .translatedText(translatedText)
                .fromLang(fromLang)
                .toLang(toLang)
                .translateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败的翻译响应
     */
    public static TranslationResponseDTO failure(String originalText, String errorMessage, String errorCode) {
        return TranslationResponseDTO.builder()
                .success(false)
                .originalText(originalText)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .translateTime(LocalDateTime.now())
                .build();
    }
}