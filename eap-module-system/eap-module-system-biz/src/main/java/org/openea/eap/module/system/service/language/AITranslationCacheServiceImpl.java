package org.openea.eap.module.system.service.language;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.i18n.ai.AITranslationResult;
import org.openea.eap.module.system.dal.dataobject.language.AITranslationCacheDO;
import org.openea.eap.module.system.dal.dataobject.language.AITranslationCacheStatsDO;
import org.openea.eap.module.system.dal.mysql.language.AITranslationCacheMapper;
import org.openea.eap.module.system.dal.mysql.language.AITranslationCacheStatsMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AI翻译缓存服务实现
 *
 * @author eap
 */
@Service
@Slf4j
public class AITranslationCacheServiceImpl implements AITranslationCacheService {

    @Resource
    private AITranslationCacheMapper cacheMapper;

    @Resource
    private AITranslationCacheStatsMapper statsMapper;

    @Override
    public AITranslationResult getCachedTranslation(String sourceText, String sourceLang, String targetLang, String contextType) {
        if (StrUtil.isEmpty(sourceText)) {
            return null;
        }

        String contentHash = generateContentHash(sourceText);
        AITranslationCacheDO cached = cacheMapper.selectByContentHashAndLang(contentHash, sourceLang, targetLang, contextType);
        
        if (cached != null) {
            // 更新命中信息
            cacheMapper.updateHitInfo(cached.getId());
            
            // 转换为AITranslationResult
            AITranslationResult result = AITranslationResult.builder()
                    .translatedText(cached.getTranslatedText())
                    .model(cached.getAiModel())
                    .provider(cached.getAiProvider())
                    .success(true)
                    .qualityScore(cached.getTranslationQuality())
                    .confidence(cached.getConfidence() != null ? cached.getConfidence().doubleValue() : null)
                    .metadata("cached_result")
                    .build();
            
            log.debug("缓存命中: {} -> {} ({}->{})", sourceText, cached.getTranslatedText(), sourceLang, targetLang);
            
            // 更新统计
            updateCacheStats(cached.getAiProvider(), cached.getAiModel(), sourceLang, targetLang, true);
            
            return result;
        }
        
        log.debug("缓存未命中: {} ({}->{})", sourceText, sourceLang, targetLang);
        return null;
    }

    @Override
    public void cacheTranslation(String sourceText, String sourceLang, String targetLang, 
                                String contextType, AITranslationResult result, Integer expireHours) {
        if (StrUtil.isEmpty(sourceText) || result == null || !result.getSuccess()) {
            return;
        }

        String contentHash = generateContentHash(sourceText);
        
        // 检查是否已存在
        AITranslationCacheDO existing = cacheMapper.selectByContentHashAndLang(contentHash, sourceLang, targetLang, contextType);
        if (existing != null) {
            log.debug("缓存已存在，跳过: {} ({}->{})", sourceText, sourceLang, targetLang);
            return;
        }

        LocalDateTime expireTime = null;
        if (expireHours != null && expireHours > 0) {
            expireTime = LocalDateTime.now().plusHours(expireHours);
        }

        AITranslationCacheDO cacheRecord = AITranslationCacheDO.builder()
                .contentHash(contentHash)
                .sourceText(sourceText)
                .sourceLang(sourceLang)
                .targetLang(targetLang)
                .translatedText(result.getTranslatedText())
                .aiProvider(result.getProvider())
                .aiModel(result.getModel())
                .translationQuality(result.getQualityScore())
                .confidence(result.getConfidence() != null ? 
                    BigDecimal.valueOf(result.getConfidence()).setScale(2, RoundingMode.HALF_UP) : null)
                .contextType(contextType)
                .hitCount(1)
                .lastHitTime(LocalDateTime.now())
                .expireTime(expireTime)
                .build();

        cacheMapper.insert(cacheRecord);
        
        log.debug("缓存翻译结果: {} -> {} ({}->{})", sourceText, result.getTranslatedText(), sourceLang, targetLang);
    }

    @Override
    public void batchCacheTranslations(Map<String, AITranslationResult> translations, 
                                      String sourceLang, String targetLang, String contextType, Integer expireHours) {
        if (translations == null || translations.isEmpty()) {
            return;
        }

        LocalDateTime expireTime = null;
        if (expireHours != null && expireHours > 0) {
            expireTime = LocalDateTime.now().plusHours(expireHours);
        }

        List<AITranslationCacheDO> cacheList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, AITranslationResult> entry : translations.entrySet()) {
            String sourceText = entry.getKey();
            AITranslationResult result = entry.getValue();
            
            if (StrUtil.isEmpty(sourceText) || result == null || !result.getSuccess()) {
                continue;
            }

            String contentHash = generateContentHash(sourceText);
            
            // 检查是否已存在
            if (cacheMapper.selectByContentHashAndLang(contentHash, sourceLang, targetLang, contextType) != null) {
                continue;
            }

            AITranslationCacheDO cacheRecord = AITranslationCacheDO.builder()
                    .contentHash(contentHash)
                    .sourceText(sourceText)
                    .sourceLang(sourceLang)
                    .targetLang(targetLang)
                    .translatedText(result.getTranslatedText())
                    .aiProvider(result.getProvider())
                    .aiModel(result.getModel())
                    .translationQuality(result.getQualityScore())
                    .confidence(result.getConfidence() != null ? 
                        BigDecimal.valueOf(result.getConfidence()).setScale(2, RoundingMode.HALF_UP) : null)
                    .contextType(contextType)
                    .hitCount(1)
                    .lastHitTime(now)
                    .expireTime(expireTime)
                    .build();

            cacheList.add(cacheRecord);
        }

        if (!cacheList.isEmpty()) {
            cacheMapper.insertBatch(cacheList);
            log.debug("批量缓存翻译结果: {} 条记录 ({}->{})", cacheList.size(), sourceLang, targetLang);
        }
    }

    @Override
    public boolean isCached(String sourceText, String sourceLang, String targetLang, String contextType) {
        if (StrUtil.isEmpty(sourceText)) {
            return false;
        }

        String contentHash = generateContentHash(sourceText);
        AITranslationCacheDO cached = cacheMapper.selectByContentHashAndLang(contentHash, sourceLang, targetLang, contextType);
        return cached != null;
    }

    @Override
    public int cleanExpiredCache() {
        int cleanedCount = cacheMapper.cleanExpiredCache();
        log.info("清理过期缓存: {} 条记录", cleanedCount);
        return cleanedCount;
    }

    @Override
    public int cleanLowQualityCache(Integer qualityThreshold) {
        if (qualityThreshold == null || qualityThreshold <= 0) {
            qualityThreshold = 3; // 默认清理质量分数低于3的缓存
        }

        List<AITranslationCacheDO> lowQualityCache = cacheMapper.selectLowQualityCache(qualityThreshold);
        int cleanedCount = 0;
        
        for (AITranslationCacheDO cache : lowQualityCache) {
            cacheMapper.deleteById(cache.getId());
            cleanedCount++;
        }
        
        log.info("清理低质量缓存: {} 条记录 (质量阈值: {})", cleanedCount, qualityThreshold);
        return cleanedCount;
    }

    @Override
    public Map<String, Object> getCacheStats(String aiProvider, Integer days) {
        if (days == null || days <= 0) {
            days = 7; // 默认查询最近7天
        }

        List<AITranslationCacheStatsDO> stats = statsMapper.selectRecentStats(days);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRequests", stats.stream().mapToInt(s -> s.getTotalRequests() != null ? s.getTotalRequests() : 0).sum());
        result.put("totalHits", stats.stream().mapToInt(s -> s.getCacheHits() != null ? s.getCacheHits() : 0).sum());
        result.put("totalMisses", stats.stream().mapToInt(s -> s.getCacheMisses() != null ? s.getCacheMisses() : 0).sum());
        
        int totalRequests = (Integer) result.get("totalRequests");
        int totalHits = (Integer) result.get("totalHits");
        
        double hitRate = totalRequests > 0 ? (double) totalHits / totalRequests * 100 : 0.0;
        result.put("hitRate", BigDecimal.valueOf(hitRate).setScale(2, RoundingMode.HALF_UP));
        
        result.put("avgQuality", stats.stream()
                .filter(s -> s.getAvgQuality() != null)
                .mapToDouble(s -> s.getAvgQuality().doubleValue())
                .average()
                .orElse(0.0));
        
        result.put("dailyStats", stats);
        result.put("days", days);
        
        return result;
    }

    @Override
    public void updateCacheStats(String aiProvider, String aiModel, String sourceLang, String targetLang, boolean isHit) {
        LocalDate today = LocalDate.now();
        
        AITranslationCacheStatsDO existing = statsMapper.selectByDateAndProvider(today, aiProvider, aiModel, sourceLang, targetLang);
        
        if (existing != null) {
            // 更新现有统计
            existing.setTotalRequests(existing.getTotalRequests() + 1);
            if (isHit) {
                existing.setCacheHits(existing.getCacheHits() + 1);
            } else {
                existing.setCacheMisses(existing.getCacheMisses() + 1);
            }
            
            // 重新计算命中率
            double hitRate = (double) existing.getCacheHits() / existing.getTotalRequests() * 100;
            existing.setHitRate(BigDecimal.valueOf(hitRate).setScale(2, RoundingMode.HALF_UP));
            
            statsMapper.updateById(existing);
        } else {
            // 创建新的统计记录
            AITranslationCacheStatsDO newStats = AITranslationCacheStatsDO.builder()
                    .statDate(today)
                    .aiProvider(aiProvider)
                    .aiModel(aiModel)
                    .sourceLang(sourceLang)
                    .targetLang(targetLang)
                    .totalRequests(1)
                    .cacheHits(isHit ? 1 : 0)
                    .cacheMisses(isHit ? 0 : 1)
                    .hitRate(BigDecimal.valueOf(isHit ? 100.0 : 0.0))
                    .avgQuality(BigDecimal.valueOf(5.0)) // 默认质量分数
                    .totalCost(BigDecimal.ZERO)
                    .build();
            
            statsMapper.insert(newStats);
        }
    }

    @Override
    public void warmupCache(List<String> commonTexts, String sourceLang, String targetLang, String contextType) {
        // 预热缓存的实现需要结合AI翻译服务
        // 这里只是接口定义，具体实现需要在集成AI翻译服务时完成
        log.info("预热缓存: {} 条常用文本 ({}->{})", commonTexts.size(), sourceLang, targetLang);
    }

    @Override
    public Double getCacheHitRate(String aiProvider, String sourceLang, String targetLang, Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        List<AITranslationCacheStatsDO> stats = statsMapper.selectByDateRange(startDate, endDate, aiProvider);
        
        int totalRequests = stats.stream().mapToInt(s -> s.getTotalRequests() != null ? s.getTotalRequests() : 0).sum();
        int totalHits = stats.stream().mapToInt(s -> s.getCacheHits() != null ? s.getCacheHits() : 0).sum();
        
        return totalRequests > 0 ? (double) totalHits / totalRequests * 100 : 0.0;
    }

    @Override
    public List<Map<String, Object>> exportCacheData(String aiProvider, String sourceLang, String targetLang) {
        List<AITranslationCacheDO> cacheList = cacheMapper.selectByProvider(aiProvider, null);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (AITranslationCacheDO cache : cacheList) {
            if ((StrUtil.isEmpty(sourceLang) || sourceLang.equals(cache.getSourceLang())) &&
                (StrUtil.isEmpty(targetLang) || targetLang.equals(cache.getTargetLang()))) {
                
                Map<String, Object> item = new HashMap<>();
                item.put("sourceText", cache.getSourceText());
                item.put("sourceLang", cache.getSourceLang());
                item.put("targetLang", cache.getTargetLang());
                item.put("translatedText", cache.getTranslatedText());
                item.put("aiProvider", cache.getAiProvider());
                item.put("aiModel", cache.getAiModel());
                item.put("translationQuality", cache.getTranslationQuality());
                item.put("confidence", cache.getConfidence());
                item.put("contextType", cache.getContextType());
                item.put("hitCount", cache.getHitCount());
                item.put("createTime", cache.getCreateTime());
                
                result.add(item);
            }
        }
        
        return result;
    }

    @Override
    public int importCacheData(List<Map<String, Object>> cacheData) {
        if (cacheData == null || cacheData.isEmpty()) {
            return 0;
        }

        int importedCount = 0;
        LocalDateTime now = LocalDateTime.now();
        
        for (Map<String, Object> data : cacheData) {
            try {
                String sourceText = (String) data.get("sourceText");
                String sourceLang = (String) data.get("sourceLang");
                String targetLang = (String) data.get("targetLang");
                String translatedText = (String) data.get("translatedText");
                
                if (StrUtil.isEmpty(sourceText) || StrUtil.isEmpty(translatedText)) {
                    continue;
                }

                String contentHash = generateContentHash(sourceText);
                String contextType = (String) data.get("contextType");
                
                // 检查是否已存在
                if (cacheMapper.selectByContentHashAndLang(contentHash, sourceLang, targetLang, contextType) != null) {
                    continue;
                }

                AITranslationCacheDO cacheRecord = AITranslationCacheDO.builder()
                        .contentHash(contentHash)
                        .sourceText(sourceText)
                        .sourceLang(sourceLang)
                        .targetLang(targetLang)
                        .translatedText(translatedText)
                        .aiProvider((String) data.get("aiProvider"))
                        .aiModel((String) data.get("aiModel"))
                        .translationQuality((Integer) data.get("translationQuality"))
                        .confidence(data.get("confidence") != null ? 
                            new BigDecimal(data.get("confidence").toString()) : null)
                        .contextType(contextType)
                        .hitCount(1)
                        .lastHitTime(now)
                        .build();

                cacheMapper.insert(cacheRecord);
                importedCount++;
                
            } catch (Exception e) {
                log.warn("导入缓存数据失败: {}", data, e);
            }
        }
        
        log.info("导入缓存数据完成: {} 条记录", importedCount);
        return importedCount;
    }

    /**
     * 生成内容哈希值
     */
    private String generateContentHash(String content) {
        return DigestUtil.md5Hex(content);
    }
}