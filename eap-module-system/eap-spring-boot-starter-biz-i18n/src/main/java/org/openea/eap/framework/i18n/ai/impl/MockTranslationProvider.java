package org.openea.eap.framework.i18n.ai.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.i18n.ai.AITranslationConfig;
import org.openea.eap.framework.i18n.ai.AITranslationResult;
import org.openea.eap.framework.i18n.ai.AbstractAITranslationProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟翻译提供商
 * 用于测试和开发环境
 *
 * @author eap
 */
@Slf4j
public class MockTranslationProvider extends AbstractAITranslationProvider {

    private static final String[] SUPPORTED_MODELS = {"mock-v1", "mock-v2"};
    
    /**
     * 预定义的翻译映射
     */
    private static final Map<String, Map<String, String>> MOCK_TRANSLATIONS = new HashMap<>();
    
    static {
        // 中文到英文
        Map<String, String> zhToEn = new HashMap<>();
        zhToEn.put("你好", "Hello");
        zhToEn.put("世界", "World");
        zhToEn.put("拍卖", "Auction");
        zhToEn.put("商品", "Product");
        zhToEn.put("价格", "Price");
        zhToEn.put("竞拍", "Bid");
        zhToEn.put("成功", "Success");
        zhToEn.put("失败", "Failed");
        zhToEn.put("用户", "User");
        zhToEn.put("系统", "System");
        MOCK_TRANSLATIONS.put("zh_CN-en_US", zhToEn);
        
        // 英文到中文
        Map<String, String> enToZh = new HashMap<>();
        enToZh.put("Hello", "你好");
        enToZh.put("World", "世界");
        enToZh.put("Auction", "拍卖");
        enToZh.put("Product", "商品");
        enToZh.put("Price", "价格");
        enToZh.put("Bid", "竞拍");
        enToZh.put("Success", "成功");
        enToZh.put("Failed", "失败");
        enToZh.put("User", "用户");
        enToZh.put("System", "系统");
        MOCK_TRANSLATIONS.put("en_US-zh_CN", enToZh);
        
        // 中文到日文
        Map<String, String> zhToJa = new HashMap<>();
        zhToJa.put("你好", "こんにちは");
        zhToJa.put("世界", "世界");
        zhToJa.put("拍卖", "オークション");
        zhToJa.put("商品", "商品");
        zhToJa.put("价格", "価格");
        zhToJa.put("竞拍", "入札");
        zhToJa.put("成功", "成功");
        zhToJa.put("失败", "失敗");
        zhToJa.put("用户", "ユーザー");
        zhToJa.put("系统", "システム");
        MOCK_TRANSLATIONS.put("zh_CN-ja_JP", zhToJa);
    }

    public MockTranslationProvider(AITranslationConfig config) {
        super(config);
    }

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public String[] getSupportedModels() {
        return SUPPORTED_MODELS;
    }

    @Override
    public AITranslationResult translateText(String text, String fromLang, String toLang, String context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 验证参数
            validateTranslationParams(text, fromLang, toLang);
            
            // 记录请求
            recordTranslationRequest();
            
            // 模拟网络延迟
            Thread.sleep(100 + (long)(Math.random() * 200));
            
            // 查找预定义翻译
            String languagePair = fromLang + "-" + toLang;
            Map<String, String> translations = MOCK_TRANSLATIONS.get(languagePair);
            
            String translatedText;
            if (translations != null && translations.containsKey(text)) {
                translatedText = translations.get(text);
            } else {
                // 生成模拟翻译
                translatedText = generateMockTranslation(text, fromLang, toLang);
            }
            
            // 创建结果
            AITranslationResult result = createResult(translatedText, getModelName(), startTime);
            result.setConfidence(0.75); // 模拟翻译置信度
            result.setMetadata("mock_translation_v1");
            
            log.debug("模拟翻译: {} -> {} ({}->{})", text, translatedText, fromLang, toLang);
            
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return createFailureResult("翻译被中断", startTime);
        } catch (Exception e) {
            log.error("模拟翻译失败", e);
            return createFailureResult(e.getMessage(), startTime);
        }
    }

    @Override
    protected boolean checkServiceHealth() {
        // 模拟服务总是可用
        return true;
    }

    @Override
    public int evaluateTranslationQuality(String originalText, String translatedText, String fromLang, String toLang) {
        // 模拟质量评估
        if (StrUtil.isEmpty(translatedText)) {
            return 1;
        }
        
        // 如果是预定义翻译，质量较高
        String languagePair = fromLang + "-" + toLang;
        Map<String, String> translations = MOCK_TRANSLATIONS.get(languagePair);
        if (translations != null && translatedText.equals(translations.get(originalText))) {
            return 8;
        }
        
        // 生成的模拟翻译质量中等
        return 6;
    }

    /**
     * 生成模拟翻译
     */
    private String generateMockTranslation(String text, String fromLang, String toLang) {
        // 简单的模拟翻译逻辑
        String prefix = getMockPrefix(toLang);
        return prefix + "[" + text + "]";
    }

    /**
     * 获取模拟翻译前缀
     */
    private String getMockPrefix(String toLang) {
        return switch (toLang) {
            case "en_US", "en-US" -> "EN:";
            case "zh_CN", "zh-CN" -> "中:";
            case "ja_JP", "ja-JP" -> "日:";
            case "ko_KR", "ko-KR" -> "한:";
            case "fr_FR", "fr-FR" -> "FR:";
            case "de_DE", "de-DE" -> "DE:";
            default -> "MOCK:";
        };
    }

    /**
     * 获取使用的模型名称
     */
    private String getModelName() {
        return config.getDefaultModel() != null ? config.getDefaultModel() : "mock-v1";
    }

    /**
     * 添加自定义翻译映射
     */
    public static void addMockTranslation(String fromLang, String toLang, String original, String translation) {
        String languagePair = fromLang + "-" + toLang;
        MOCK_TRANSLATIONS.computeIfAbsent(languagePair, k -> new HashMap<>()).put(original, translation);
    }

    /**
     * 获取所有模拟翻译映射
     */
    public static Map<String, Map<String, String>> getAllMockTranslations() {
        return new HashMap<>(MOCK_TRANSLATIONS);
    }
}