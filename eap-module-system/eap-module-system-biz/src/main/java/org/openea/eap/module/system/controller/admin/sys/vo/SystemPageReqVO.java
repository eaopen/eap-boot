package org.openea.eap.module.system.controller.admin.sys.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import org.openea.eap.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static org.openea.eap.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 系统应用分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SystemPageReqVO extends PageParam {

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

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "系统状态", example = "2")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}