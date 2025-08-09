package org.openea.eap.framework.i18n.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.i18n.ai.AITranslationConfig;
import org.openea.eap.framework.i18n.ai.AITranslationProvider;
import org.openea.eap.framework.i18n.ai.impl.MockTranslationProvider;
import org.openea.eap.framework.i18n.ai.impl.OpenAITranslationProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * AI翻译自动配置
 *
 * @author eap
 */
@AutoConfiguration
@EnableConfigurationProperties(AITranslationProperties.class)
@ConditionalOnProperty(prefix = "eap.i18n.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class AITranslationAutoConfiguration {

    @Resource
    private AITranslationProperties properties;

    /**
     * 创建OpenAI翻译提供商
     */
    @Bean
    @ConditionalOnProperty(prefix = "eap.i18n.ai.providers.openai", name = "enabled", havingValue = "true")
    public OpenAITranslationProvider openAITranslationProvider() {
        AITranslationProperties.ProviderConfig config = properties.getProviders().get("openai");
        if (config == null) {
            config = new AITranslationProperties.ProviderConfig();
        }
        AITranslationConfig translationConfig = buildTranslationConfig("openai", config);
        return new OpenAITranslationProvider(translationConfig);
    }

    /**
     * 创建Mock翻译提供商
     */
    @Bean
    @ConditionalOnProperty(prefix = "eap.i18n.ai.providers.mock", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MockTranslationProvider mockTranslationProvider() {
        AITranslationProperties.ProviderConfig config = properties.getProviders().get("mock");
        if (config == null) {
            config = new AITranslationProperties.ProviderConfig();
            config.setEnabled(true);
            config.setPriority(999); // 最低优先级
        }
        AITranslationConfig translationConfig = buildTranslationConfig("mock", config);
        return new MockTranslationProvider(translationConfig);
    }



    /**
     * 构建翻译配置
     */
    private AITranslationConfig buildTranslationConfig(String providerName, AITranslationProperties.ProviderConfig config) {
        return AITranslationConfig.builder()
                .providerName(providerName)
                .apiKey(config.getApiKey())
                .apiEndpoint(config.getApiEndpoint())
                .defaultModel(config.getDefaultModel())
                .timeout(config.getTimeout())
                .maxRetries(config.getMaxRetries())
                .enabled(config.getEnabled())
                .priority(config.getPriority())
                .rateLimit(config.getRateLimit())
                .supportedLanguages(config.getSupportedLanguages())
                .qualityThreshold(config.getQualityThreshold() != null ? 
                    config.getQualityThreshold() : properties.getGlobal().getDefaultQualityThreshold())
                .extraParams(config.getExtraParams())
                .build();
    }


}