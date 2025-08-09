package org.openea.eap.module.system.dal.mysql.language;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.openea.eap.module.system.dal.dataobject.language.AITranslationCacheStatsDO;

import java.time.LocalDate;
import java.util.List;

/**
 * AI翻译缓存统计 Mapper
 *
 * @author eap
 */
@Mapper
public interface AITranslationCacheStatsMapper extends BaseMapperX<AITranslationCacheStatsDO> {

    /**
     * 根据日期和提供商查询统计
     */
    default AITranslationCacheStatsDO selectByDateAndProvider(LocalDate statDate, String aiProvider, 
                                                              String aiModel, String sourceLang, String targetLang) {
        LambdaQueryWrapperX<AITranslationCacheStatsDO> wrapper = new LambdaQueryWrapperX<AITranslationCacheStatsDO>()
                .eq(AITranslationCacheStatsDO::getStatDate, statDate)
                .eq(AITranslationCacheStatsDO::getAiProvider, aiProvider)
                .eq(AITranslationCacheStatsDO::getSourceLang, sourceLang)
                .eq(AITranslationCacheStatsDO::getTargetLang, targetLang);
        
        if (aiModel != null && !aiModel.isEmpty()) {
            wrapper.eq(AITranslationCacheStatsDO::getAiModel, aiModel);
        }
        
        return selectOne(wrapper);
    }

    /**
     * 查询指定日期范围的统计
     */
    @Select("SELECT * FROM ai_translation_cache_stats " +
            "WHERE stat_date BETWEEN #{startDate} AND #{endDate} " +
            "AND (ai_provider = #{aiProvider} OR #{aiProvider} IS NULL) " +
            "AND deleted = 0 " +
            "ORDER BY stat_date DESC")
    List<AITranslationCacheStatsDO> selectByDateRange(@Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate, 
                                                      @Param("aiProvider") String aiProvider);

    /**
     * 查询最近N天的统计汇总
     */
    default List<AITranslationCacheStatsDO> selectRecentStats(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        return selectList(new LambdaQueryWrapperX<AITranslationCacheStatsDO>()
                .between(AITranslationCacheStatsDO::getStatDate, startDate, endDate)
                .orderByDesc(AITranslationCacheStatsDO::getStatDate)
                .orderByDesc(AITranslationCacheStatsDO::getHitRate));
    }

    /**
     * 统计总体缓存性能
     */
    @Select("SELECT ai_provider, ai_model, source_lang, target_lang, " +
            "SUM(total_requests) as total_requests, " +
            "SUM(cache_hits) as cache_hits, " +
            "SUM(cache_misses) as cache_misses, " +
            "ROUND(SUM(cache_hits) * 100.0 / SUM(total_requests), 2) as hit_rate, " +
            "AVG(avg_quality) as avg_quality " +
            "FROM ai_translation_cache_stats " +
            "WHERE (ai_provider = #{aiProvider} OR #{aiProvider} IS NULL) " +
            "AND deleted = 0 " +
            "GROUP BY ai_provider, ai_model, source_lang, target_lang " +
            "ORDER BY hit_rate DESC")
    List<AITranslationCacheStatsDO> selectOverallStats(@Param("aiProvider") String aiProvider);
}