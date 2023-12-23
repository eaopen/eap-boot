package org.openea.eap.module.system.controller.admin.language.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 翻译更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class I18nJsonDataUpdateReqVO extends I18nJsonDataBaseVO {

    @Schema(description = "PK", requiredMode = Schema.RequiredMode.REQUIRED, example = "19948")
    @NotNull(message = "PK不能为空")
    private Long id;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}
