package org.openea.eap.module.system.controller.admin.language.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 翻译 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class I18nJsonDataBaseVO {

    @Schema(description = "模块，可选")
    private String module;

    @Schema(description = "key/别名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "key/别名不能为空")
    private String alias;

    @Schema(description = "名称", example = "张三")
    private String name;

    @Schema(description = "多语言设置json")
    private String json;

    @Schema(description = "翻译来源", example = "MANUAL")
    private String translationSource;

    @Schema(description = "AI模型", example = "gpt-3.5-turbo")
    private String aiModel;

    @Schema(description = "翻译质量评分", example = "8")
    private Integer translationQuality;

    @Schema(description = "AI提供商", example = "openai")
    private String aiProvider;

    @Schema(description = "翻译置信度", example = "0.85")
    private Double translationConfidence;

}
