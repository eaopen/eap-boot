package org.openea.eap.module.system.api.translation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 翻译场景枚举
 *
 * @author eap
 */
@Getter
@AllArgsConstructor
public enum TranslationSceneEnum {

    /**
     * 菜单翻译
     * 特点：简洁、一致、易理解
     */
    MENU("menu", "菜单翻译", 20, "简洁明了的菜单项翻译"),

    /**
     * UI词条翻译
     * 特点：长度限制、界面友好
     */
    UI("ui", "UI词条翻译", 30, "界面元素的简短翻译"),

    /**
     * 按钮翻译
     * 特点：动作导向、简洁有力
     */
    BUTTON("button", "按钮翻译", 15, "操作按钮的动作性翻译"),

    /**
     * 表单标签翻译
     * 特点：准确描述、专业术语
     */
    FORM_LABEL("form_label", "表单标签翻译", 25, "表单字段标签的准确翻译"),

    /**
     * 提示信息翻译
     * 特点：友好提示、易理解
     */
    MESSAGE("message", "提示信息翻译", 100, "用户提示和消息的翻译"),

    /**
     * 错误信息翻译
     * 特点：准确描述问题、提供解决方向
     */
    ERROR("error", "错误信息翻译", 150, "错误提示信息的翻译"),

    /**
     * 内容翻译
     * 特点：保持原意、流畅自然
     */
    CONTENT("content", "内容翻译", 1000, "长文本内容的翻译"),

    /**
     * 标题翻译
     * 特点：吸引眼球、概括性强
     */
    TITLE("title", "标题翻译", 50, "页面和模块标题的翻译"),

    /**
     * 描述翻译
     * 特点：详细说明、准确描述
     */
    DESCRIPTION("description", "描述翻译", 200, "功能和内容描述的翻译"),

    /**
     * 拍卖专业术语
     * 特点：专业准确、行业标准
     */
    AUCTION("auction", "拍卖术语翻译", 50, "拍卖行业专业术语的翻译");

    /**
     * 场景代码
     */
    private final String code;

    /**
     * 场景名称
     */
    private final String name;

    /**
     * 推荐最大长度
     */
    private final Integer maxLength;

    /**
     * 场景描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     */
    public static TranslationSceneEnum fromCode(String code) {
        for (TranslationSceneEnum scene : values()) {
            if (scene.getCode().equals(code)) {
                return scene;
            }
        }
        return CONTENT; // 默认返回内容翻译
    }

    /**
     * 是否需要长度限制
     */
    public boolean needLengthLimit() {
        return this == MENU || this == UI || this == BUTTON || this == FORM_LABEL;
    }

    /**
     * 是否是专业术语翻译
     */
    public boolean isProfessional() {
        return this == AUCTION || this == FORM_LABEL;
    }
}