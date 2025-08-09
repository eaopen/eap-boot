package org.openea.eap.module.system.api.translation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 翻译请求DTO
 *
 * @author eap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequestDTO {

    /**
     * 待翻译文本
     */
    @NotBlank(message = "待翻译文本不能为空")
    @Size(max = 5000, message = "翻译文本长度不能超过5000字符")
    private String text;

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
     * menu: 菜单翻译
     * ui: UI词条翻译
     * content: 内容翻译
     * message: 消息翻译
     */
    private String scene;

    /**
     * 上下文信息
     */
    private String context;

    /**
     * 最大长度限制（主要用于UI翻译）
     */
    private Integer maxLength;

    /**
     * 是否启用缓存
     */
    @Builder.Default
    private Boolean enableCache = true;

    /**
     * 翻译质量要求
     * 1-10，数字越高质量要求越高
     */
    @Builder.Default
    private Integer qualityLevel = 5;

    /**
     * 是否需要人工审核
     */
    @Builder.Default
    private Boolean needReview = false;

    /**
     * 业务标识（用于统计和追踪）
     */
    private String businessId;
}