package org.openea.eap.module.system.enums.language;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 翻译来源枚举
 *
 * @author eap
 */
@Getter
@AllArgsConstructor
public enum TranslationSourceEnum {

    /**
     * 人工翻译
     */
    MANUAL("MANUAL", "人工翻译"),

    /**
     * AI自动翻译
     */
    AI_AUTO("AI_AUTO", "AI自动翻译"),

    /**
     * LLM自动翻译
     */
    LLM_AUTO("LLM_AUTO", "LLM自动翻译"),

    /**
     * 系统默认
     */
    SYSTEM_DEFAULT("SYSTEM_DEFAULT", "系统默认");

    /**
     * 代码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     */
    public static TranslationSourceEnum fromCode(String code) {
        for (TranslationSourceEnum source : values()) {
            if (source.getCode().equals(code)) {
                return source;
            }
        }
        return MANUAL; // 默认返回人工翻译
    }

    /**
     * 是否为AI翻译
     */
    public boolean isAITranslation() {
        return this == AI_AUTO || this == LLM_AUTO;
    }
}