package org.openea.eap.module.system.dal.mysql.language;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.openea.eap.module.system.dal.dataobject.language.AITranslationCacheDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI翻译缓存 Mapper
 *
 * @author eap
 */
@Mapper
public interface AITranslationCacheMapper extends BaseMapperX<AITranslationCacheDO> {

    /**
     * 根据内容哈希和语言对查询缓存
     */
    @Select("SELECT * FROM ai_translation_cache " +
            "WHERE content_hash = #{contentHash} " +
            "AND source_lang = #{sourceLang} " +
            "AND target_lang = #{targetLang} " +
            "AND (context_type = #{contextType} OR (#{contextType} IS NULL AND context_type IS NULL)) " +
            "AND (expire_time IS NULL OR expire_time > NOW()) " +
            "AND deleted = 0 " +
            "ORDER BY last_hit_time DESC " +
            "LIMIT 1")
    AITranslationCacheDO selectByContentHashAndLang(@Param("contentHash") String contentHash, 
                                                    @Param("sourceLang") String sourceLang, 
                                                    @Param("targetLang") String targetLang, 
                                                    @Param("contextType") String contextType);

    /**
     * 更新缓存命中信息
     */
    @Update("UPDATE ai_translation_cache SET hit_count = hit_count + 1, last_hit_time = NOW(), update_time = NOW() WHERE id = #{id} AND deleted = 0")
    void updateHitInfo(@Param("id") Long id);

    /**
     * 查询过期的缓存记录
     */
    default List<AITranslationCacheDO> selectExpiredCache() {
        return selectList(new LambdaQueryWrapperX<AITranslationCacheDO>()
                .isNotNull(AITranslationCacheDO::getExpireTime)
                .lt(AITranslationCacheDO::getExpireTime, LocalDateTime.now())
                .orderByAsc(AITranslationCacheDO::getExpireTime));
    }

    /**
     * 根据AI提供商查询缓存
     */
    @Select("SELECT * FROM ai_translation_cache " +
            "WHERE ai_provider = #{aiProvider} " +
            "AND (ai_model = #{aiModel} OR #{aiModel} IS NULL) " +
            "AND (expire_time IS NULL OR expire_time > NOW()) " +
            "AND deleted = 0 " +
            "ORDER BY last_hit_time DESC")
    List<AITranslationCacheDO> selectByProvider(@Param("aiProvider") String aiProvider, 
                                               @Param("aiModel") String aiModel);

    /**
     * 统计缓存命中率
     */
    @Select("SELECT COUNT(*) FROM ai_translation_cache " +
            "WHERE hit_count > 1 " +
            "AND (ai_provider = #{aiProvider} OR #{aiProvider} IS NULL) " +
            "AND (source_lang = #{sourceLang} OR #{sourceLang} IS NULL) " +
            "AND (target_lang = #{targetLang} OR #{targetLang} IS NULL) " +
            "AND (create_time BETWEEN #{startTime} AND #{endTime} OR #{startTime} IS NULL OR #{endTime} IS NULL) " +
            "AND deleted = 0")
    Long countCacheHits(@Param("aiProvider") String aiProvider, 
                       @Param("sourceLang") String sourceLang, 
                       @Param("targetLang") String targetLang, 
                       @Param("startTime") LocalDateTime startTime, 
                       @Param("endTime") LocalDateTime endTime);

    /**
     * 清理过期缓存
     */
    @Update("UPDATE ai_translation_cache SET deleted = 1, updater = 'system_cleanup', update_time = NOW() WHERE expire_time IS NOT NULL AND expire_time < NOW() AND deleted = 0")
    int cleanExpiredCache();

    /**
     * 根据质量分数查询低质量缓存
     */
    @Select("SELECT * FROM ai_translation_cache " +
            "WHERE translation_quality < #{qualityThreshold} " +
            "AND (expire_time IS NULL OR expire_time > NOW()) " +
            "AND deleted = 0 " +
            "ORDER BY translation_quality ASC")
    List<AITranslationCacheDO> selectLowQualityCache(@Param("qualityThreshold") Integer qualityThreshold);

    /**
     * 批量插入缓存记录
     */
    default void insertBatch(List<AITranslationCacheDO> cacheList) {
        if (CollectionUtil.isNotEmpty(cacheList)) {
            cacheList.forEach(this::insert);
        }
    }
}