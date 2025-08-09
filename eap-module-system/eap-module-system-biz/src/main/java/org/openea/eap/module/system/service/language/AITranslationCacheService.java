package org.openea.eap.module.system.service.language;

import org.openea.eap.framework.i18n.ai.AITranslationResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI翻译缓存服务接口
 *
 * @author eap
 */
public interface AITranslationCacheService {

    /**
     * 获取缓存的翻译结果
     * @param sourceText 源文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param contextType 上下文类型
     * @return 缓存的翻译结果，如果没有缓存则返回null
     */
    AITranslationResult getCachedTranslation(String sourceText, String sourceLang, String targetLang, String contextType);

    /**
     * 缓存翻译结果
     * @param sourceText 源文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param contextType 上下文类型
     * @param result 翻译结果
     * @param expireHours 过期小时数，null表示不过期
     */
    void cacheTranslation(String sourceText, String sourceLang, String targetLang, 
                         String contextType, AITranslationResult result, Integer expireHours);

    /**
     * 批量缓存翻译结果
     * @param translations 翻译结果映射
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param contextType 上下文类型
     * @param expireHours 过期小时数
     */
    void batchCacheTranslations(Map<String, AITranslationResult> translations, 
                               String sourceLang, String targetLang, String contextType, Integer expireHours);

    /**
     * 检查缓存是否存在
     * @param sourceText 源文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param contextType 上下文类型
     * @return 是否存在缓存
     */
    boolean isCached(String sourceText, String sourceLang, String targetLang, String contextType);

    /**
     * 清理过期缓存
     * @return 清理的记录数
     */
    int cleanExpiredCache();

    /**
     * 清理低质量缓存
     * @param qualityThreshold 质量阈值
     * @return 清理的记录数
     */
    int cleanLowQualityCache(Integer qualityThreshold);

    /**
     * 获取缓存统计信息
     * @param aiProvider AI提供商
     * @param days 统计天数
     * @return 缓存统计信息
     */
    Map<String, Object> getCacheStats(String aiProvider, Integer days);

    /**
     * 更新缓存统计
     * @param aiProvider AI提供商
     * @param aiModel AI模型
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param isHit 是否命中
     */
    void updateCacheStats(String aiProvider, String aiModel, String sourceLang, String targetLang, boolean isHit);

    /**
     * 预热缓存
     * @param commonTexts 常用文本列表
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param contextType 上下文类型
     */
    void warmupCache(List<String> commonTexts, String sourceLang, String targetLang, String contextType);

    /**
     * 获取缓存命中率
     * @param aiProvider AI提供商
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param days 统计天数
     * @return 命中率百分比
     */
    Double getCacheHitRate(String aiProvider, String sourceLang, String targetLang, Integer days);

    /**
     * 导出缓存数据
     * @param aiProvider AI提供商
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @return 缓存数据列表
     */
    List<Map<String, Object>> exportCacheData(String aiProvider, String sourceLang, String targetLang);

    /**
     * 导入缓存数据
     * @param cacheData 缓存数据列表
     * @return 导入的记录数
     */
    int importCacheData(List<Map<String, Object>> cacheData);
}