package org.openea.eap.module.infra.api.translate;

/**
 * 翻译引擎接口
 * 简化设计：只支持大语言模型翻译和传统翻译两种类型
 * 配置通过配置文件管理，不需要复杂的运行时管理
 *
 * @author EAP
 */
public interface TranslateEngine {

    /**
     * 获取引擎名称
     *
     * @return 引擎名称
     */
    String getName();

    /**
     * 获取引擎类型
     * 只支持两种类型：LLM（大语言模型）、TRADITIONAL（传统翻译）
     *
     * @return 引擎类型
     */
    String getType();

    /**
     * 检查引擎是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 翻译文本
     *
     * @param text 原始文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @return 翻译结果
     */
    String translateText(String text, String sourceLang, String targetLang);


}