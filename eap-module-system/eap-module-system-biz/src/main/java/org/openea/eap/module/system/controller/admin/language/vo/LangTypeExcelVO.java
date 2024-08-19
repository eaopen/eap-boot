package org.openea.eap.module.system.controller.admin.language.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语言 Excel VO
 *
 * @author eap
 */
@Data
public class LangTypeExcelVO {

    @ExcelProperty("PK")
    private Long id;

    @ExcelProperty("key/别名")
    private String alias;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
