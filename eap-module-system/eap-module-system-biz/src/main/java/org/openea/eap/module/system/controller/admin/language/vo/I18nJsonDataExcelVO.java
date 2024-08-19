package org.openea.eap.module.system.controller.admin.language.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 翻译 Excel VO
 *
 * @author eap
 */
@Data
public class I18nJsonDataExcelVO {

    @ExcelProperty("PK")
    private Long id;

    @ExcelProperty("模块，可选")
    private String module;

    @ExcelProperty("key/别名")
    private String alias;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("多语言设置json")
    private String json;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
