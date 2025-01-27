package org.openea.eap.module.system.controller.admin.dict.vo.type;

import org.openea.eap.framework.excel.core.annotations.DictFormat;
import org.openea.eap.framework.excel.core.convert.DictConvert;
import org.openea.eap.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 字典类型信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DictTypeRespVO {

    @Schema(description = "字典类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("字典主键")
    private Long id;

    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "性别")
    @ExcelProperty("字典名称")
    private String name;

    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "sys_common_sex")
    @ExcelProperty("字典类型")
    private String type;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "data/json/sql/api")
    @ExcelProperty("数据类型")
    private String dataType;

    @Schema(description = "json数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String dataJson;

    @Schema(description = "查询sql", requiredMode = Schema.RequiredMode.REQUIRED, example = "select value, label from table")
    private String dataSql;

    @Schema(description = "数据源", requiredMode = Schema.RequiredMode.REQUIRED, example = "dataSourceName")
    private String dataDs;

    @Schema(description = "状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "快乐的备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "时间戳格式")
    private LocalDateTime createTime;

}
