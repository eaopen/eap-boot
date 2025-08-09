package org.openea.eap.module.system.controller.admin.language.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.openea.eap.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static org.openea.eap.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 翻译分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class I18nJsonDataPageReqVO extends PageParam {

    @Schema(description = "模块，可选")
    private String module;

    @Schema(description = "key/别名")
    private String alias;

    @Schema(description = "名称", example = "张三")
    private String name;

    @Schema(description = "多语言设置json")
    private String json;

    @Schema(description = "翻译来源", example = "AI_AUTO")
    private String translationSource;

    @Schema(description = "AI提供商", example = "openai")
    private String aiProvider;

    @Schema(description = "AI模型", example = "gpt-3.5-turbo")
    private String aiModel;

    @Schema(description = "最小质量分数", example = "5")
    private Integer minQuality;

    @Schema(description = "最大质量分数", example = "10")
    private Integer maxQuality;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "最后AI更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lastAiUpdateTime;

}
