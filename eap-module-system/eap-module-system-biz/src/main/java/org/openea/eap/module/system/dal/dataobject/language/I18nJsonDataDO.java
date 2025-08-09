package org.openea.eap.module.system.dal.dataobject.language;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

import java.time.LocalDateTime;

/**
 * 翻译 DO
 *
 * @author eap
 */
@TableName("sys_i18n_data")
@KeySequence("sys_i18n_data_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class I18nJsonDataDO extends BaseDO {

    /**
     * PK
     */
    @TableId
    private Long id;
    /**
     * 模块，可选
     */
    private String module;
    /**
     * key/别名
     */
    private String alias;
    /**
     * 名称
     */
    private String name;
    /**
     * 多语言设置json
     */
    @JsonFormat
    private String json;
    /**
     * 备注
     */
    private String remark;

    /**
     * 翻译来源：MANUAL-人工翻译, AI_AUTO-AI自动翻译, LLM_AUTO-LLM自动翻译
     */
    private String translationSource;

    /**
     * 使用的AI模型：gpt-4, gpt-3.5-turbo, claude-3等
     */
    private String aiModel;

    /**
     * 翻译质量评分：1-10分，5为默认分数
     */
    private Integer translationQuality;

    /**
     * AI提供商：openai, baidu, tencent, mock等
     */
    private String aiProvider;

    /**
     * 翻译置信度：0.00-1.00
     */
    private Double translationConfidence;

    /**
     * 最后AI翻译更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAiUpdate;

}
