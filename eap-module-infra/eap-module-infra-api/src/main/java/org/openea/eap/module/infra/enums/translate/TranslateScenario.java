package org.openea.eap.module.infra.enums.translate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 翻译场景枚举
 * 定义不同的翻译使用场景，每种场景有不同的翻译要求和限制
 *
 * @author EAP
 */
@Getter
@AllArgsConstructor
public enum TranslateScenario {

    /**
     * 模块名称 - 系统模块的名称
     */
    MODULE_NAME("module_name", "模块名称", 20, true, "系统模块的名称，需要简洁明了"),

    /**
     * 菜单项 - 导航菜单中的选项
     */
    MENU_ITEM("menu_item", "菜单项", 15, true, "导航菜单中的选项，需要简短易懂"),

    /**
     * 按钮文本 - 操作按钮上的文字
     */
    BUTTON_TEXT("button_text", "按钮文本", 10, true, "操作按钮上的文字，需要动作明确"),

    /**
     * 表单标签 - 表单字段的标签
     */
    FORM_LABEL("form_label", "表单标签", 25, true, "表单字段的标签，需要描述准确"),

    /**
     * 表格标题 - 数据表格的列标题
     */
    TABLE_HEADER("table_header", "表格标题", 20, true, "数据表格的列标题，需要简洁明确"),

    /**
     * 错误信息 - 系统错误提示信息
     */
    ERROR_MESSAGE("error_message", "错误信息", 100, false, "系统错误提示信息，需要准确描述问题"),

    /**
     * 成功信息 - 操作成功的提示信息
     */
    SUCCESS_MESSAGE("success_message", "成功信息", 50, false, "操作成功的提示信息，需要明确反馈结果"),

    /**
     * 占位符文本 - 输入框的占位符提示
     */
    PLACEHOLDER_TEXT("placeholder_text", "占位符文本", 30, false, "输入框的占位符提示，需要引导用户输入"),

    /**
     * 提示文本 - 帮助和说明文本
     */
    HINT_TEXT("hint_text", "提示文本", 80, false, "帮助和说明文本，需要清晰易懂"),

    /**
     * 验证信息 - 表单验证的错误信息
     */
    VALIDATION_MESSAGE("validation_message", "验证信息", 60, false, "表单验证的错误信息，需要指导用户修正"),

    /**
     * 页面标题 - 页面的主标题
     */
    PAGE_TITLE("page_title", "页面标题", 30, true, "页面的主标题，需要概括页面内容"),

    /**
     * 描述文本 - 详细的描述信息
     */
    DESCRIPTION_TEXT("description_text", "描述文本", 200, false, "详细的描述信息，可以较长但需要准确"),

    /**
     * 状态文本 - 表示状态的文本
     */
    STATUS_TEXT("status_text", "状态文本", 15, true, "表示状态的文本，需要简洁明确"),

    /**
     * 通用文本 - 其他未分类的文本
     */
    GENERAL_TEXT("general_text", "通用文本", 100, false, "其他未分类的文本，根据具体情况处理");

    /**
     * 场景代码
     */
    private final String code;

    /**
     * 场景名称
     */
    private final String name;

    /**
     * 建议最大长度
     */
    private final Integer maxLength;

    /**
     * 是否需要保持简洁
     */
    private final Boolean keepConcise;

    /**
     * 场景描述
     */
    private final String description;

    /**
     * 根据代码获取场景
     *
     * @param code 场景代码
     * @return 翻译场景
     */
    public static TranslateScenario getByCode(String code) {
        for (TranslateScenario scenario : values()) {
            if (scenario.getCode().equals(code)) {
                return scenario;
            }
        }
        return GENERAL_TEXT; // 默认返回通用文本场景
    }

    /**
     * 根据旧的type参数推断场景
     *
     * @param type 旧的类型参数
     * @return 翻译场景
     */
    public static TranslateScenario inferFromLegacyType(String type) {
        if (type == null) {
            return GENERAL_TEXT;
        }
        
        switch (type.toLowerCase()) {
            case "menu":
                return MENU_ITEM;
            case "button":
                return BUTTON_TEXT;
            case "label":
                return FORM_LABEL;
            case "title":
                return PAGE_TITLE;
            case "error":
                return ERROR_MESSAGE;
            case "success":
                return SUCCESS_MESSAGE;
            case "placeholder":
                return PLACEHOLDER_TEXT;
            case "hint":
                return HINT_TEXT;
            case "validation":
                return VALIDATION_MESSAGE;
            case "status":
                return STATUS_TEXT;
            case "description":
                return DESCRIPTION_TEXT;
            default:
                return GENERAL_TEXT;
        }
    }

    /**
     * 检查文本长度是否超出建议范围
     *
     * @param text 待检查的文本
     * @return 是否超出建议长度
     */
    public boolean isTextTooLong(String text) {
        return text != null && text.length() > this.maxLength;
    }

    /**
     * 获取场景的翻译配置
     *
     * @return 翻译配置描述
     */
    public String getTranslateConfig() {
        StringBuilder config = new StringBuilder();
        config.append("场景: ").append(this.name);
        config.append(", 最大长度: ").append(this.maxLength);
        config.append(", 保持简洁: ").append(this.keepConcise ? "是" : "否");
        config.append(", 描述: ").append(this.description);
        return config.toString();
    }
}