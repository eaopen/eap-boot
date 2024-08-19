package org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.util.*;

@Schema(description = "管理后台 - 文件模板配置新增/修改 Request VO")
@Data
public class FileTemplateConfigSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11362")
    private Long id;

    @Schema(description = "模板Key")
    private String tempKey;

    @Schema(description = "模板名称", example = "王五")
    private String tempName;

    @Schema(description = "模板类型", example = "1")
    private Integer tempType;

    @Schema(description = "描述", example = "你猜")
    private String description;

    @Schema(description = "关联文件", requiredMode = Schema.RequiredMode.REQUIRED, example = "24839")
    @NotNull(message = "关联文件不能为空")
    private Long fileId;

    @Schema(description = "系统服务接口")
    private String docService;

    @Schema(description = "配置Json")
    private String configJson;

}