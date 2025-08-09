package org.openea.eap.framework.i18n.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openea.eap.framework.i18n.ai.impl.MockTranslationProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mock翻译提供商测试
 *
 * @author eap
 */
class MockTranslationProviderTest {

    private MockTranslationProvider provider;

    @BeforeEach
    void setUp() {
        AITranslationConfig config = AITranslationConfig.builder()
                .providerName("mock")
                .enabled(true)
                .priority(999)
                .qualityThreshold(5)
                .supportedLanguages(new String[]{"zh-CN", "en-US", "ja-JP"})
                .build();
        
        provider = new MockTranslationProvider(config);
    }

    @Test
    void testGetProviderName() {
        assertEquals("mock", provider.getProviderName());
    }

    @Test
    void testGetSupportedModels() {
        String[] models = provider.getSupportedModels();
        assertNotNull(models);
        assertTrue(models.length > 0);
    }

    @Test
    void testSupportsLanguagePair() {
        assertTrue(provider.supportsLanguagePair("zh-CN", "en-US"));
        assertTrue(provider.supportsLanguagePair("en-US", "zh-CN"));
        assertTrue(provider.supportsLanguagePair("zh-CN", "ja-JP"));
    }

    @Test
    void testIsAvailable() {
        assertTrue(provider.isAvailable());
    }

    @Test
    void testTranslateText() {
        AITranslationResult result = provider.translateText("你好", "zh-CN", "en-US", "greeting");
        
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("Hello", result.getTranslatedText());
        assertEquals("mock", result.getProvider());
        assertNotNull(result.getModel());
        assertNotNull(result.getQualityScore());
        assertTrue(result.getQualityScore() >= 1 && result.getQualityScore() <= 10);
    }

    @Test
    void testTranslateTextWithUnknownText() {
        AITranslationResult result = provider.translateText("未知文本", "zh-CN", "en-US", "test");
        
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertTrue(result.getTranslatedText().startsWith("EN:"));
        assertEquals("mock", result.getProvider());
    }

    @Test
    void testTranslateTextWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> {
            provider.translateText("", "zh-CN", "en-US", "test");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            provider.translateText("测试", "", "en-US", "test");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            provider.translateText("测试", "zh-CN", "", "test");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            provider.translateText("测试", "zh-CN", "zh-CN", "test");
        });
    }

    @Test
    void testEvaluateTranslationQuality() {
        int quality1 = provider.evaluateTranslationQuality("你好", "Hello", "zh-CN", "en-US");
        assertTrue(quality1 >= 6); // 预定义翻译质量较高
        
        int quality2 = provider.evaluateTranslationQuality("测试", "EN:[测试]", "zh-CN", "en-US");
        assertTrue(quality2 >= 5); // 生成的翻译质量中等
        
        int quality3 = provider.evaluateTranslationQuality("测试", "", "zh-CN", "en-US");
        assertEquals(1, quality3); // 空翻译质量最低
    }

    @Test
    void testGetConfig() {
        AITranslationConfig config = provider.getConfig();
        assertNotNull(config);
        assertEquals("mock", config.getProviderName());
        assertTrue(config.getEnabled());
        assertEquals(999, config.getPriority());
    }
}