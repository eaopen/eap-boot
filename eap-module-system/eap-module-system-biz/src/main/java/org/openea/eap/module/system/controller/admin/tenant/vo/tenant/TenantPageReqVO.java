package org.openea.eap.module.system.controller.admin.tenant.vo.tenant;

import org.openea.eap.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static org.openea.eap.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 租户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantPageReqVO extends PageParam {

    @Schema(description = "租户名", example = "eap")
    private String name;

    @Schema(description = "联系人", example = "eap")
    private String contactName;

    @Schema(description = "联系手机", example = "13900001234")
    private String contactMobile;

    @Schema(description = "绑定域名", example = "eap")
    private String website;

    @Schema(description = "租户状态（0正常 1停用）", example = "1")
    private Integer status;

    @Schema(description = "交付部门编号，同时筛选子部门", example = "1024")
    private Long deptId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
