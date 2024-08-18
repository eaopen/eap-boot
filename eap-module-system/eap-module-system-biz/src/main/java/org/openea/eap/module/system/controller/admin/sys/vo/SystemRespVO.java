package org.openea.eap.module.system.controller.admin.sys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 系统应用 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SystemRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("ID")
    private Long id;

    @Schema(description = "系统编号")
    @ExcelProperty("系统编号")
    private String code;

    @Schema(description = "系统名称", example = "王五")
    @ExcelProperty("系统名称")
    private String fullName;

    @Schema(description = "系统图标")
    @ExcelProperty("系统图标")
    private String icon;

    @Schema(description = "是否主系统")
    @ExcelProperty("是否主系统")
    private Integer isMain;

    @Schema(description = "扩展属性")
    @ExcelProperty("扩展属性")
    private String propertyJson;

    @Schema(description = "描述或说明", example = "随便")
    @ExcelProperty("描述或说明")
    private String description;

    @Schema(description = "导航图标")
    @ExcelProperty("导航图标")
    private String navigationIcon;

    @Schema(description = "logo图标")
    @ExcelProperty("logo图标")
    private String workLogoIcon;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("显示顺序")
    private Integer sort;

    @Schema(description = "系统状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("系统状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}