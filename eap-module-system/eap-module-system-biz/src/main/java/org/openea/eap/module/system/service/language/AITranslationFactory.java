package org.openea.eap.module.system.service.language;

import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.i18n.ai.AITranslationProvider;
import org.openea.eap.framework.i18n.ai.AITranslationResult;
import org.openea.eap.framework.i18n.ai.AITranslationConfig;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI翻译服务工厂
 * 支持动态切换AI引擎和负载均衡
 *
 * @author eap
 */
@Service
@Slf4j
public class AITranslationFactory {

    /**
     * 所有注册的翻译提供商
     */
    private final Map<String, AITranslationProvider> providers = new ConcurrentHashMap<>();

    /**
     * 提供商优先级排序
     */
    private final List<String> providerPriority = new ArrayList<>();

    /**
     * 回退策略配置
     */
    private final Map<String, List<String>> fallbackChain = new ConcurrentHashMap<>();

    /**
     * 缓存服务
     */
    @Resource
    @Lazy
    private AITranslationCacheService cacheService;

    /**
     * 注册翻译提供商
     * @param provider 翻译提供商
     */
    public void registerProvider(AITranslationProvider provider) {
        String providerName = provider.getProviderName();
        providers.put(providerName, provider);
        
        // 根据配置的优先级排序
        AITranslationConfig config = provider.getConfig();
        if (config != null && config.getPriority() != null) {
            insertByPriority(providerName, config.getPriority());
        } else {
            providerPriority.add(providerName);
        }
        
        log.info("注册AI翻译提供商: {}", providerName);
    }

    /**
     * 获取最佳翻译提供商
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @return 翻译提供商
     */
    public AITranslationProvider getBestProvider(String fromLang, String toLang) {
        // 按优先级查找可用的提供商
        for (String providerName : providerPriority) {
            AITranslationProvider provider = providers.get(providerName);
            if (provider != null && provider.isAvailable() && 
                provider.supportsLanguagePair(fromLang, toLang)) {
                return provider;
            }
        }
        
        log.warn("未找到支持语言对 {}->{} 的可用翻译提供商", fromLang, toLang);
        return null;
    }

    /**
     * 获取指定的翻译提供商
     * @param providerName 提供商名称
     * @return 翻译提供商
     */
    public AITranslationProvider getProvider(String providerName) {
        return providers.get(providerName);
    }

    /**
     * 获取所有可用的翻译提供商
     * @return 可用的翻译提供商列表
     */
    public List<AITranslationProvider> getAvailableProviders() {
        return providers.values().stream()
                .filter(AITranslationProvider::isAvailable)
                .toList();
    }

    /**
     * 带回退机制的翻译（支持缓存）
     * @param text 待翻译文本
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @param context 翻译上下文
     * @return 翻译结果
     */
    public AITranslationResult translateWithFallback(String text, String fromLang, String toLang, String context) {
        // 首先检查缓存
        if (cacheService != null) {
            AITranslationResult cachedResult = cacheService.getCachedTranslation(text, fromLang, toLang, context);
            if (cachedResult != null) {
                log.debug("使用缓存翻译结果: {}", text);
                return cachedResult;
            }
        }

        // 获取回退链
        List<String> fallbackProviders = getFallbackChain(fromLang, toLang);
        
        AITranslationResult lastResult = null;
        for (String providerName : fallbackProviders) {
            AITranslationProvider provider = providers.get(providerName);
            if (provider == null || !provider.isAvailable()) {
                continue;
            }
            
            try {
                AITranslationResult result = provider.translateText(text, fromLang, toLang, context);
                if (result != null && result.getSuccess()) {
                    // 检查翻译质量
                    if (isQualityAcceptable(result, provider.getConfig())) {
                        log.debug("使用提供商 {} 成功翻译", providerName);
                        
                        // 缓存翻译结果
                        if (cacheService != null) {
                            cacheService.cacheTranslation(text, fromLang, toLang, context, result, 24); // 缓存24小时
                        }
                        
                        return result;
                    } else {
                        log.warn("提供商 {} 翻译质量不达标，质量分数: {}", providerName, result.getQualityScore());
                        lastResult = result; // 保存作为最后的备选
                    }
                } else {
                    log.warn("提供商 {} 翻译失败: {}", providerName, result != null ? result.getErrorMessage() : "未知错误");
                }
            } catch (Exception e) {
                log.error("提供商 {} 翻译异常", providerName, e);
            }
        }
        
        // 如果所有提供商都失败，返回最后一个结果或失败结果
        if (lastResult != null) {
            log.warn("所有提供商翻译质量不达标，返回最后一个结果");
            
            // 即使质量不达标，也缓存结果（设置较短的过期时间）
            if (cacheService != null) {
                cacheService.cacheTranslation(text, fromLang, toLang, context, lastResult, 1); // 缓存1小时
            }
            
            return lastResult;
        }
        
        return AITranslationResult.failure("所有翻译提供商都不可用", "factory");
    }

    /**
     * 批量翻译（支持负载均衡和缓存）
     * @param texts 待翻译文本映射
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @param context 翻译上下文
     * @return 翻译结果映射
     */
    public Map<String, AITranslationResult> batchTranslateWithLoadBalance(
            Map<String, String> texts, String fromLang, String toLang, String context) {
        
        Map<String, AITranslationResult> results = new ConcurrentHashMap<>();
        Map<String, String> textsToTranslate = new HashMap<>();
        
        // 首先检查缓存
        if (cacheService != null) {
            for (Map.Entry<String, String> entry : texts.entrySet()) {
                AITranslationResult cachedResult = cacheService.getCachedTranslation(
                    entry.getValue(), fromLang, toLang, context);
                if (cachedResult != null) {
                    results.put(entry.getKey(), cachedResult);
                } else {
                    textsToTranslate.put(entry.getKey(), entry.getValue());
                }
            }
            log.debug("批量翻译缓存命中: {}/{}", results.size(), texts.size());
        } else {
            textsToTranslate.putAll(texts);
        }
        
        if (textsToTranslate.isEmpty()) {
            return results; // 全部命中缓存
        }
        
        List<AITranslationProvider> availableProviders = getAvailableProviders().stream()
                .filter(p -> p.supportsLanguagePair(fromLang, toLang))
                .toList();
        
        if (availableProviders.isEmpty()) {
            // 所有文本都标记为失败
            textsToTranslate.keySet().forEach(key -> 
                results.put(key, AITranslationResult.failure("无可用翻译提供商", "factory")));
            return results;
        }
        
        // 简单的轮询负载均衡
        int providerIndex = 0;
        Map<String, AITranslationResult> newTranslations = new HashMap<>();
        
        for (Map.Entry<String, String> entry : textsToTranslate.entrySet()) {
            AITranslationProvider provider = availableProviders.get(providerIndex % availableProviders.size());
            
            try {
                AITranslationResult result = provider.translateText(entry.getValue(), fromLang, toLang, context);
                results.put(entry.getKey(), result);
                
                if (result != null && result.getSuccess()) {
                    newTranslations.put(entry.getValue(), result);
                }
            } catch (Exception e) {
                log.error("批量翻译失败，提供商: {}, 文本: {}", provider.getProviderName(), entry.getKey(), e);
                results.put(entry.getKey(), AITranslationResult.failure(e.getMessage(), provider.getProviderName()));
            }
            
            providerIndex++;
        }
        
        // 批量缓存新的翻译结果
        if (cacheService != null && !newTranslations.isEmpty()) {
            cacheService.batchCacheTranslations(newTranslations, fromLang, toLang, context, 24);
        }
        
        return results;
    }

    /**
     * 设置回退链
     * @param languagePair 语言对，格式: "zh_CN-en_US"
     * @param fallbackProviders 回退提供商列表
     */
    public void setFallbackChain(String languagePair, List<String> fallbackProviders) {
        fallbackChain.put(languagePair, new ArrayList<>(fallbackProviders));
    }

    /**
     * 获取回退链
     */
    private List<String> getFallbackChain(String fromLang, String toLang) {
        String languagePair = fromLang + "-" + toLang;
        List<String> chain = fallbackChain.get(languagePair);
        
        if (chain == null || chain.isEmpty()) {
            // 使用默认优先级顺序
            return new ArrayList<>(providerPriority);
        }
        
        return chain;
    }

    /**
     * 按优先级插入提供商
     */
    private void insertByPriority(String providerName, int priority) {
        // 移除已存在的
        providerPriority.remove(providerName);
        
        // 按优先级插入
        int insertIndex = 0;
        for (int i = 0; i < providerPriority.size(); i++) {
            String existingProvider = providerPriority.get(i);
            AITranslationProvider existing = providers.get(existingProvider);
            if (existing != null && existing.getConfig() != null && 
                existing.getConfig().getPriority() != null &&
                existing.getConfig().getPriority() > priority) {
                insertIndex = i;
                break;
            }
            insertIndex = i + 1;
        }
        
        providerPriority.add(insertIndex, providerName);
    }

    /**
     * 检查翻译质量是否可接受
     */
    private boolean isQualityAcceptable(AITranslationResult result, AITranslationConfig config) {
        if (result.getQualityScore() == null) {
            return true; // 如果没有质量分数，认为可接受
        }
        
        int threshold = config != null && config.getQualityThreshold() != null ? 
                config.getQualityThreshold() : 5; // 默认阈值为5
        
        return result.getQualityScore() >= threshold;
    }

    /**
     * 获取所有提供商的状态信息
     */
    public Map<String, Object> getProvidersStatus() {
        Map<String, Object> status = new HashMap<>();
        
        providers.forEach((name, provider) -> {
            Map<String, Object> providerStatus = new HashMap<>();
            providerStatus.put("available", provider.isAvailable());
            providerStatus.put("supportedModels", provider.getSupportedModels());
            providerStatus.put("config", provider.getConfig());
            status.put(name, providerStatus);
        });
        
        status.put("priority", providerPriority);
        status.put("fallbackChains", fallbackChain);
        
        return status;
    }
}