package org.openea.eap.framework.i18n.ai;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI翻译提供商抽象基类
 * 提供通用功能实现
 *
 * @author eap
 */
@Slf4j
public abstract class AbstractAITranslationProvider implements AITranslationProvider {

    /**
     * 配置信息
     */
    protected AITranslationConfig config;

    /**
     * 请求计数器（用于限流）
     */
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    /**
     * 上次重置时间
     */
    private volatile long lastResetTime = System.currentTimeMillis();

    public AbstractAITranslationProvider(AITranslationConfig config) {
        this.config = config;
    }

    @Override
    public AITranslationConfig getConfig() {
        return config;
    }

    @Override
    public boolean supportsLanguagePair(String fromLang, String toLang) {
        if (config.getSupportedLanguages() == null) {
            return true; // 如果没有配置限制，认为支持所有语言
        }
        
        boolean supportsFrom = false;
        boolean supportsTo = false;
        
        for (String lang : config.getSupportedLanguages()) {
            if (lang.equals(fromLang)) {
                supportsFrom = true;
            }
            if (lang.equals(toLang)) {
                supportsTo = true;
            }
        }
        
        return supportsFrom && supportsTo;
    }

    @Override
    public Map<String, AITranslationResult> batchTranslate(Map<String, String> texts, String fromLang, String toLang, String context) {
        Map<String, AITranslationResult> results = new HashMap<>();
        
        // 默认实现：逐个翻译
        for (Map.Entry<String, String> entry : texts.entrySet()) {
            try {
                AITranslationResult result = translateText(entry.getValue(), fromLang, toLang, context);
                results.put(entry.getKey(), result);
            } catch (Exception e) {
                log.error("批量翻译失败，key: {}, text: {}", entry.getKey(), entry.getValue(), e);
                results.put(entry.getKey(), AITranslationResult.failure(e.getMessage(), getProviderName()));
            }
        }
        
        return results;
    }

    @Override
    public AITranslationResult translateProfessional(String text, String fromLang, String toLang, String domain, String context) {
        // 默认实现：在上下文中添加领域信息
        String enhancedContext = buildProfessionalContext(domain, context);
        return translateText(text, fromLang, toLang, enhancedContext);
    }

    @Override
    public int evaluateTranslationQuality(String originalText, String translatedText, String fromLang, String toLang) {
        // 默认质量评估实现
        if (StrUtil.isEmpty(translatedText)) {
            return 1;
        }
        
        if (originalText.equals(translatedText)) {
            return 3; // 没有翻译
        }
        
        // 简单的质量评估逻辑
        int score = 5; // 基础分数
        
        // 长度合理性检查
        double lengthRatio = (double) translatedText.length() / originalText.length();
        if (lengthRatio < 0.3 || lengthRatio > 3.0) {
            score -= 2; // 长度差异过大
        }
        
        // 检查是否包含明显的错误标记
        if (translatedText.contains("ERROR") || translatedText.contains("FAILED")) {
            score = 1;
        }
        
        return Math.max(1, Math.min(10, score));
    }

    @Override
    public boolean isAvailable() {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return false;
        }
        
        // 检查限流
        if (isRateLimited()) {
            return false;
        }
        
        // 子类可以重写此方法添加更多检查
        return checkServiceHealth();
    }

    /**
     * 检查服务健康状态
     * 子类可以重写此方法
     */
    protected boolean checkServiceHealth() {
        return true;
    }

    /**
     * 构建专业翻译上下文
     */
    protected String buildProfessionalContext(String domain, String originalContext) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // 添加领域信息
        switch (domain) {
            case "ecommerce":
                contextBuilder.append("这是电商平台的内容翻译，请使用电商专业术语。");
                break;
            case "auction":
                contextBuilder.append("这是拍卖平台的内容翻译，请使用拍卖行业专业术语，保持严谨性。");
                break;
            case "legal":
                contextBuilder.append("这是法律相关内容翻译，请使用准确的法律术语。");
                break;
            case "technical":
                contextBuilder.append("这是技术文档翻译，请保持技术术语的准确性。");
                break;
            default:
                contextBuilder.append("请进行专业翻译。");
        }
        
        if (StrUtil.isNotEmpty(originalContext)) {
            contextBuilder.append(" ").append(originalContext);
        }
        
        return contextBuilder.toString();
    }

    /**
     * 检查是否被限流
     */
    protected boolean isRateLimited() {
        if (config.getRateLimit() == null || config.getRateLimit() <= 0) {
            return false; // 没有限流配置
        }
        
        long currentTime = System.currentTimeMillis();
        String timeWindow = String.valueOf(currentTime / 60000); // 按分钟分组
        
        // 重置计数器（每分钟）
        if (currentTime - lastResetTime > 60000) {
            requestCounts.clear();
            lastResetTime = currentTime;
        }
        
        int currentCount = requestCounts.getOrDefault(timeWindow, 0);
        if (currentCount >= config.getRateLimit()) {
            log.warn("翻译提供商 {} 达到限流阈值: {}/min", getProviderName(), config.getRateLimit());
            return true;
        }
        
        // 增加计数
        requestCounts.put(timeWindow, currentCount + 1);
        return false;
    }

    /**
     * 记录翻译请求
     */
    protected void recordTranslationRequest() {
        // 子类可以重写此方法记录更详细的统计信息
        log.debug("翻译请求记录: 提供商={}", getProviderName());
    }

    /**
     * 验证翻译参数
     */
    protected void validateTranslationParams(String text, String fromLang, String toLang) {
        if (StrUtil.isEmpty(text)) {
            throw new IllegalArgumentException("翻译文本不能为空");
        }
        
        if (StrUtil.isEmpty(fromLang) || StrUtil.isEmpty(toLang)) {
            throw new IllegalArgumentException("源语言和目标语言不能为空");
        }
        
        if (fromLang.equals(toLang)) {
            throw new IllegalArgumentException("源语言和目标语言不能相同");
        }
        
        if (!supportsLanguagePair(fromLang, toLang)) {
            throw new IllegalArgumentException(
                String.format("不支持的语言对: %s -> %s", fromLang, toLang));
        }
    }

    /**
     * 创建翻译结果
     */
    protected AITranslationResult createResult(String translatedText, String model, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        int qualityScore = evaluateTranslationQuality("", translatedText, "", "");
        
        return AITranslationResult.builder()
                .translatedText(translatedText)
                .model(model)
                .provider(getProviderName())
                .success(true)
                .duration(duration)
                .qualityScore(qualityScore)
                .build();
    }

    /**
     * 创建失败结果
     */
    protected AITranslationResult createFailureResult(String errorMessage, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        
        return AITranslationResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .provider(getProviderName())
                .duration(duration)
                .build();
    }
}