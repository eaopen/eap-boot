package org.openea.eap.module.system.controller.admin.language;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.i18n.ai.AITranslationResult;
import org.openea.eap.module.system.service.language.AITranslationCacheService;
import org.openea.eap.module.system.service.language.AITranslationFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.openea.eap.framework.common.pojo.CommonResult.success;

/**
 * AI翻译测试控制器
 * 用于测试和验证AI翻译功能
 *
 * @author eap
 */
@Tag(name = "管理后台 - AI翻译测试")
@RestController
@RequestMapping("/system/ai-translation/test")
@Slf4j
public class AITranslationTestController {

    @Resource
    private AITranslationFactory aiTranslationFactory;

    @Resource
    private AITranslationCacheService cacheService;

    @GetMapping("/translate")
    @Operation(summary = "测试翻译")
    public CommonResult<AITranslationResult> testTranslate(
            @Parameter(description = "待翻译文本") @RequestParam String text,
            @Parameter(description = "源语言") @RequestParam(defaultValue = "zh-CN") String fromLang,
            @Parameter(description = "目标语言") @RequestParam(defaultValue = "en-US") String toLang,
            @Parameter(description = "上下文") @RequestParam(required = false) String context) {
        
        try {
            AITranslationResult result = aiTranslationFactory.translateWithFallback(text, fromLang, toLang, context);
            return success(result);
        } catch (Exception e) {
            log.error("翻译测试失败", e);
            return success(AITranslationResult.failure(e.getMessage(), "test"));
        }
    }

    @PostMapping("/batch-translate")
    @Operation(summary = "批量翻译测试")
    public CommonResult<Map<String, AITranslationResult>> testBatchTranslate(
            @Parameter(description = "待翻译文本映射") @RequestBody Map<String, String> texts,
            @Parameter(description = "源语言") @RequestParam(defaultValue = "zh-CN") String fromLang,
            @Parameter(description = "目标语言") @RequestParam(defaultValue = "en-US") String toLang,
            @Parameter(description = "上下文") @RequestParam(required = false) String context) {
        
        try {
            Map<String, AITranslationResult> results = aiTranslationFactory.batchTranslateWithLoadBalance(
                texts, fromLang, toLang, context);
            return success(results);
        } catch (Exception e) {
            log.error("批量翻译测试失败", e);
            return success(Map.of("error", AITranslationResult.failure(e.getMessage(), "test")));
        }
    }

    @GetMapping("/providers/status")
    @Operation(summary = "获取翻译提供商状态")
    public CommonResult<Map<String, Object>> getProvidersStatus() {
        try {
            Map<String, Object> status = aiTranslationFactory.getProvidersStatus();
            return success(status);
        } catch (Exception e) {
            log.error("获取提供商状态失败", e);
            return success(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计")
    public CommonResult<Map<String, Object>> getCacheStats(
            @Parameter(description = "AI提供商") @RequestParam(required = false) String aiProvider,
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        try {
            Map<String, Object> stats = cacheService.getCacheStats(aiProvider, days);
            return success(stats);
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return success(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cache/clean")
    @Operation(summary = "清理缓存")
    public CommonResult<Map<String, Object>> cleanCache(
            @Parameter(description = "清理类型") @RequestParam(defaultValue = "expired") String type,
            @Parameter(description = "质量阈值") @RequestParam(required = false) Integer qualityThreshold) {
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            switch (type) {
                case "expired":
                    int expiredCount = cacheService.cleanExpiredCache();
                    result.put("cleanedExpired", expiredCount);
                    break;
                case "low-quality":
                    int lowQualityCount = cacheService.cleanLowQualityCache(qualityThreshold);
                    result.put("cleanedLowQuality", lowQualityCount);
                    break;
                case "all":
                    int expiredCount2 = cacheService.cleanExpiredCache();
                    int lowQualityCount2 = cacheService.cleanLowQualityCache(qualityThreshold);
                    result.put("cleanedExpired", expiredCount2);
                    result.put("cleanedLowQuality", lowQualityCount2);
                    break;
                default:
                    result.put("error", "不支持的清理类型: " + type);
            }
            
            return success(result);
        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return success(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cache/hit-rate")
    @Operation(summary = "获取缓存命中率")
    public CommonResult<Map<String, Object>> getCacheHitRate(
            @Parameter(description = "AI提供商") @RequestParam(required = false) String aiProvider,
            @Parameter(description = "源语言") @RequestParam(required = false) String sourceLang,
            @Parameter(description = "目标语言") @RequestParam(required = false) String targetLang,
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        try {
            Double hitRate = cacheService.getCacheHitRate(aiProvider, sourceLang, targetLang, days);
            Map<String, Object> result = new HashMap<>();
            result.put("hitRate", hitRate);
            result.put("aiProvider", aiProvider);
            result.put("sourceLang", sourceLang);
            result.put("targetLang", targetLang);
            result.put("days", days);
            
            return success(result);
        } catch (Exception e) {
            log.error("获取缓存命中率失败", e);
            return success(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/demo")
    @Operation(summary = "演示翻译功能")
    public CommonResult<Map<String, Object>> demo() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. 单个翻译测试
            AITranslationResult singleResult = aiTranslationFactory.translateWithFallback(
                "你好，世界！", "zh-CN", "en-US", "greeting");
            result.put("singleTranslation", singleResult);
            
            // 2. 批量翻译测试
            Map<String, String> batchTexts = new HashMap<>();
            batchTexts.put("greeting", "你好");
            batchTexts.put("goodbye", "再见");
            batchTexts.put("thank_you", "谢谢");
            batchTexts.put("auction_success", "竞拍成功");
            batchTexts.put("product_sold", "商品已售出");
            
            Map<String, AITranslationResult> batchResults = aiTranslationFactory.batchTranslateWithLoadBalance(
                batchTexts, "zh-CN", "en-US", "demo");
            result.put("batchTranslation", batchResults);
            
            // 3. 缓存统计
            Map<String, Object> cacheStats = cacheService.getCacheStats(null, 1);
            result.put("cacheStats", cacheStats);
            
            // 4. 提供商状态
            Map<String, Object> providersStatus = aiTranslationFactory.getProvidersStatus();
            result.put("providersStatus", providersStatus);
            
            return success(result);
        } catch (Exception e) {
            log.error("演示功能失败", e);
            return success(Map.of("error", e.getMessage()));
        }
    }
}