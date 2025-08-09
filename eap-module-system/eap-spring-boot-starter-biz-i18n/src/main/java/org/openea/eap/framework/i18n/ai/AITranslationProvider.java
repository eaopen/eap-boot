package org.openea.eap.framework.i18n.ai;

import java.util.Map;

/**
 * AI翻译服务提供商接口
 * 支持多种AI引擎的统一抽象层
 *
 * @author eap
 */
public interface AITranslationProvider {

    /**
     * 获取提供商名称
     * @return 提供商名称，如 "openai", "baidu", "tencent"
     */
    String getProviderName();

    /**
     * 获取支持的模型列表
     * @return 支持的模型名称列表
     */
    String[] getSupportedModels();

    /**
     * 检查是否支持指定的语言对
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @return 是否支持
     */
    boolean supportsLanguagePair(String fromLang, String toLang);

    /**
     * 单文本翻译
     * @param text 待翻译文本
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @param context 翻译上下文（可选）
     * @return 翻译结果
     */
    AITranslationResult translateText(String text, String fromLang, String toLang, String context);

    /**
     * 批量文本翻译
     * @param texts 待翻译文本列表
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @param context 翻译上下文（可选）
     * @return 翻译结果列表
     */
    Map<String, AITranslationResult> batchTranslate(Map<String, String> texts, String fromLang, String toLang, String context);

    /**
     * 专业翻译（针对特定领域）
     * @param text 待翻译文本
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @param domain 专业领域，如 "ecommerce", "auction", "legal"
     * @param context 翻译上下文
     * @return 翻译结果
     */
    AITranslationResult translateProfessional(String text, String fromLang, String toLang, String domain, String context);

    /**
     * 获取翻译质量评估
     * @param originalText 原文
     * @param translatedText 译文
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @return 质量评分 (1-10)
     */
    int evaluateTranslationQuality(String originalText, String translatedText, String fromLang, String toLang);

    /**
     * 检查服务可用性
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取服务配置信息
     * @return 配置信息
     */
    AITranslationConfig getConfig();
}