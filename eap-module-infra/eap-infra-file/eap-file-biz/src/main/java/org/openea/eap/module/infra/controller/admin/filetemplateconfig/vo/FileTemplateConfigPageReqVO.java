package org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import org.openea.eap.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static org.openea.eap.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 文件模板配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileTemplateConfigPageReqVO extends PageParam {

    @Schema(description = "模板Key")
    private String tempKey;

    @Schema(description = "模板名称", example = "王五")
    private String tempName;

}