package org.openea.eap.module.system.controller.admin.language.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 翻译创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class I18nJsonDataCreateReqVO extends I18nJsonDataBaseVO {

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}
