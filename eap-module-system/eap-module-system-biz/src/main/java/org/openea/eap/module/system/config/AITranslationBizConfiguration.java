package org.openea.eap.module.system.config;

import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.i18n.ai.AITranslationProvider;
import org.openea.eap.framework.i18n.ai.config.AITranslationProperties;
import org.openea.eap.module.system.service.language.AITranslationFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AI翻译业务配置
 *
 * @author eap
 */
@Configuration
@ConditionalOnProperty(prefix = "eap.i18n.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class AITranslationBizConfiguration {

    @Resource
    private AITranslationFactory aiTranslationFactory;

    @Resource
    @Lazy
    private List<AITranslationProvider> translationProviders;

    @Resource
    private AITranslationProperties properties;

    @PostConstruct
    public void initializeProviders() {
        if (translationProviders == null || translationProviders.isEmpty()) {
            log.warn("未找到任何AI翻译提供商");
            return;
        }

        // 注册所有可用的翻译提供商
        for (AITranslationProvider provider : translationProviders) {
            try {
                aiTranslationFactory.registerProvider(provider);
                log.info("成功注册AI翻译提供商: {}", provider.getProviderName());
            } catch (Exception e) {
                log.error("注册AI翻译提供商失败: {}", provider.getProviderName(), e);
            }
        }

        // 设置回退链
        setupFallbackChains();

        log.info("AI翻译服务初始化完成，已注册 {} 个提供商", translationProviders.size());
    }

    /**
     * 设置回退链
     */
    private void setupFallbackChains() {
        for (Map.Entry<String, String[]> entry : properties.getFallbackChains().entrySet()) {
            String languagePair = entry.getKey();
            String[] fallbackProviders = entry.getValue();
            
            aiTranslationFactory.setFallbackChain(languagePair, Arrays.asList(fallbackProviders));
            log.debug("设置回退链: {} -> {}", languagePair, Arrays.toString(fallbackProviders));
        }
    }

    /**
     * 将AITranslationFactory注册为Bean，供其他组件使用
     */
    @Bean("AITranslationFactory")
    public AITranslationFactory aiTranslationFactoryBean() {
        return aiTranslationFactory;
    }
}