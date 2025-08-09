package org.openea.eap.module.system.dal.dataobject.language;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AI翻译缓存统计 DO
 *
 * @author eap
 */
@TableName("ai_translation_cache_stats")
@KeySequence("ai_translation_cache_stats_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AITranslationCacheStatsDO extends BaseDO {

    /**
     * PK
     */
    @TableId
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * AI提供商
     */
    private String aiProvider;

    /**
     * AI模型
     */
    private String aiModel;

    /**
     * 源语言
     */
    private String sourceLang;

    /**
     * 目标语言
     */
    private String targetLang;

    /**
     * 总请求数
     */
    private Integer totalRequests;

    /**
     * 缓存命中数
     */
    private Integer cacheHits;

    /**
     * 缓存未命中数
     */
    private Integer cacheMisses;

    /**
     * 命中率百分比
     */
    private BigDecimal hitRate;

    /**
     * 平均质量分数
     */
    private BigDecimal avgQuality;

    /**
     * 总成本（如果有）
     */
    private BigDecimal totalCost;
}