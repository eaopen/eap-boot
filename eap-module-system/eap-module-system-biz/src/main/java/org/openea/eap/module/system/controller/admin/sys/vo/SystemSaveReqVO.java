package org.openea.eap.module.system.controller.admin.sys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 系统应用新增/修改 Request VO")
@Data
public class SystemSaveReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long id;

    @Schema(description = "系统编号")
    private String code;

    @Schema(description = "系统名称", example = "王五")
    private String fullName;

    @Schema(description = "系统图标")
    private String icon;

    @Schema(description = "是否主系统")
    private Integer isMain;

    @Schema(description = "扩展属性")
    private String propertyJson;

    @Schema(description = "描述或说明", example = "随便")
    private String description;

    @Schema(description = "导航图标")
    private String navigationIcon;

    @Schema(description = "logo图标")
    private String workLogoIcon;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "系统状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "系统状态不能为空")
    private Integer status;

}