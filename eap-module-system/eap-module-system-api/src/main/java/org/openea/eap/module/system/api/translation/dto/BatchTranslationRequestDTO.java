package org.openea.eap.module.system.api.translation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * 批量翻译请求DTO
 *
 * @author eap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTranslationRequestDTO {

    /**
     * 待翻译文本映射
     * key: 文本标识符
     * value: 待翻译文本
     */
    @NotEmpty(message = "翻译文本不能为空")
    @Size(max = 100, message = "批量翻译最多支持100条文本")
    private Map<String, String> texts;

    /**
     * 源语言代码
     */
    @NotBlank(message = "源语言不能为空")
    private String fromLang;

    /**
     * 目标语言代码
     */
    @NotBlank(message = "目标语言不能为空")
    private String toLang;

    /**
     * 翻译场景
     */
    private String scene;

    /**
     * 上下文信息
     */
    private String context;

    /**
     * 最大长度限制
     */
    private Integer maxLength;

    /**
     * 是否启用缓存
     */
    @Builder.Default
    private Boolean enableCache = true;

    /**
     * 翻译质量要求
     */
    @Builder.Default
    private Integer qualityLevel = 5;

    /**
     * 是否并行处理
     */
    @Builder.Default
    private Boolean parallel = true;

    /**
     * 业务标识
     */
    private String businessId;
}