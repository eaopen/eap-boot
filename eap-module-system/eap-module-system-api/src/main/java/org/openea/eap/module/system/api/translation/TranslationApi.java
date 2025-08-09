package org.openea.eap.module.system.api.translation;

import org.openea.eap.module.system.api.translation.dto.TranslationRequestDTO;
import org.openea.eap.module.system.api.translation.dto.TranslationResponseDTO;
import org.openea.eap.module.system.api.translation.dto.BatchTranslationRequestDTO;
import org.openea.eap.module.system.api.translation.dto.BatchTranslationResponseDTO;

import java.util.List;

/**
 * 翻译服务API
 * 提供统一的翻译接口，支持菜单翻译、UI词条翻译等多种场景
 *
 * @author eap
 */
public interface TranslationApi {

    /**
     * 单个文本翻译
     * @param request 翻译请求
     * @return 翻译结果
     */
    TranslationResponseDTO translate(TranslationRequestDTO request);

    /**
     * 批量文本翻译
     * @param request 批量翻译请求
     * @return 批量翻译结果
     */
    BatchTranslationResponseDTO batchTranslate(BatchTranslationRequestDTO request);

    /**
     * 菜单翻译
     * 针对菜单项进行优化的翻译，考虑简洁性和一致性
     * @param menuText 菜单文本
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @return 翻译结果
     */
    TranslationResponseDTO translateMenu(String menuText, String fromLang, String toLang);

    /**
     * UI词条翻译
     * 针对UI界面词条的翻译，支持长度限制
     * @param uiText UI文本
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @param maxLength 最大长度限制
     * @return 翻译结果
     */
    TranslationResponseDTO translateUI(String uiText, String fromLang, String toLang, Integer maxLength);

    /**
     * 上下文感知翻译
     * 根据显示场景和上下文进行翻译优化
     * @param text 待翻译文本
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @param context 上下文信息
     * @param scene 显示场景
     * @return 翻译结果
     */
    TranslationResponseDTO translateWithContext(String text, String fromLang, String toLang, 
                                               String context, String scene);

    /**
     * 翻译质量检查和优化
     * @param originalText 原文
     * @param translatedText 译文
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @param scene 应用场景
     * @return 优化后的翻译结果
     */
    TranslationResponseDTO reviewAndOptimize(String originalText, String translatedText, 
                                           String fromLang, String toLang, String scene);

    /**
     * 获取支持的语言列表
     * @return 支持的语言代码列表
     */
    List<String> getSupportedLanguages();

    /**
     * 检查翻译服务状态
     * @return 服务状态信息
     */
    TranslationServiceStatus getServiceStatus();
}