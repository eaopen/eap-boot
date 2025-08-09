package org.openea.eap.module.system.dal.dataobject.language;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI翻译缓存 DO
 *
 * @author eap
 */
@TableName("ai_translation_cache")
@KeySequence("ai_translation_cache_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AITranslationCacheDO extends BaseDO {

    /**
     * PK
     */
    @TableId
    private Long id;

    /**
     * 内容哈希值(MD5)
     */
    private String contentHash;

    /**
     * 源文本
     */
    private String sourceText;

    /**
     * 源语言
     */
    private String sourceLang;

    /**
     * 目标语言
     */
    private String targetLang;

    /**
     * 翻译结果
     */
    private String translatedText;

    /**
     * AI提供商
     */
    private String aiProvider;

    /**
     * AI模型
     */
    private String aiModel;

    /**
     * 翻译质量评分：1-10分
     */
    private Integer translationQuality;

    /**
     * 翻译置信度：0.00-1.00
     */
    private BigDecimal confidence;

    /**
     * 上下文类型：system_message, ecommerce, auction等
     */
    private String contextType;

    /**
     * 缓存命中次数
     */
    private Integer hitCount;

    /**
     * 最后命中时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHitTime;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}