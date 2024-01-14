package org.openea.eap.extj.base.model.billrule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class BillRuleCrForm {
    @NotBlank(message = "必填")
    @Schema(description = "业务名称")
    private String fullName;
    @NotBlank(message = "必填")
    @Schema(description = "流水位数")
    private String enCode;
    @NotBlank(message = "必填")
    @Schema(description = "流水前缀")
    private String prefix;
    @NotBlank(message = "必填")
    @Schema(description = "流水日期格式")
    private String dateFormat;
    @NotNull(message = "必填")
    @Schema(description = "流水位数")
    private Integer digit;
    @NotBlank(message = "必填")
    @Schema(description = "流水起始")
    private String startNumber;
    @Schema(description = "流水范例")
    private String example;
    @Schema(description = "状态(0-禁用，1-启用)")
    private Integer enabledMark;
    @Schema(description = "流水说明")
    private String description;
    private long sortCode;
    private String category;
}
