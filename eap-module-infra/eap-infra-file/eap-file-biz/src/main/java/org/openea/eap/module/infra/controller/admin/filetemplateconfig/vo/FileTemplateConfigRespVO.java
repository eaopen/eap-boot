package org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文件模板配置 Response VO")
@Data
public class FileTemplateConfigRespVO {

    @Schema(description = "模板Key")
    private String tempKey;

    @Schema(description = "模板名称", example = "王五")
    private String tempName;

    @Schema(description = "模板类型", example = "1")
    private Integer tempType;

    @Schema(description = "描述", example = "你猜")
    private String description;

    @Schema(description = "关联文件", requiredMode = Schema.RequiredMode.REQUIRED, example = "24839")
    private Long fileId;

    @Schema(description = "系统服务接口")
    private String docService;

    @Schema(description = "配置Json")
    private String configJson;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}