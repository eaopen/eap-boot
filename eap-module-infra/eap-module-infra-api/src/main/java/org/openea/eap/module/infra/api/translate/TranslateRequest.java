package org.openea.eap.module.infra.api.translate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openea.eap.module.infra.enums.translate.TranslateScenario;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 翻译请求对象
 * 封装翻译请求的所有参数，替代Map参数传递方式，提供类型安全的API
 *
 * @author EAP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslateRequest {

    /**
     * 翻译上下文信息
     * 包含源文本、源语言、目标语言、场景等核心信息
     */
    private TranslateContext context;

    /**
     * 目标语言列表
     * 支持一次请求翻译成多种语言
     */
    @Builder.Default
    private List<String> targetLanguages = new ArrayList<>();

    /**
     * 翻译选项配置
     * 包含引擎选择、质量要求、缓存策略等配置
     */
    @Builder.Default
    private TranslateOptions options = new TranslateOptions();

    /**
     * 创建单语言翻译请求
     *
     * @param context 翻译上下文
     * @param targetLanguage 目标语言
     * @return 翻译请求对象
     */
    public static TranslateRequest of(TranslateContext context, String targetLanguage) {
        return TranslateRequest.builder()
                .context(context)
                .targetLanguages(Collections.singletonList(targetLanguage))
                .build();
    }

    /**
     * 创建多语言翻译请求
     *
     * @param context 翻译上下文
     * @param targetLanguages 目标语言列表
     * @return 翻译请求对象
     */
    public static TranslateRequest of(TranslateContext context, List<String> targetLanguages) {
        return TranslateRequest.builder()
                .context(context)
                .targetLanguages(new ArrayList<>(targetLanguages))
                .build();
    }

    /**
     * 创建带选项的翻译请求
     *
     * @param context 翻译上下文
     * @param targetLanguage 目标语言
     * @param options 翻译选项
     * @return 翻译请求对象
     */
    public static TranslateRequest of(TranslateContext context, String targetLanguage, TranslateOptions options) {
        return TranslateRequest.builder()
                .context(context)
                .targetLanguages(Collections.singletonList(targetLanguage))
                .options(options)
                .build();
    }

    /**
     * 验证请求参数的有效性
     *
     * @throws IllegalArgumentException 当参数无效时抛出异常
     */
    public void validate() {
        if (context == null) {
            throw new IllegalArgumentException("翻译上下文不能为空");
        }
        
        context.validate();
        
        if (targetLanguages == null || targetLanguages.isEmpty()) {
            throw new IllegalArgumentException("目标语言列表不能为空");
        }
        
        for (String lang : targetLanguages) {
            if (lang == null || lang.trim().isEmpty()) {
                throw new IllegalArgumentException("目标语言不能为空");
            }
        }
        
        if (options != null) {
            options.validate();
        }
    }

    /**
     * 获取源文本
     *
     * @return 源文本
     */
    public String getSourceText() {
        return context != null ? context.getSourceText() : null;
    }

    /**
     * 获取源语言
     *
     * @return 源语言
     */
    public String getSourceLanguage() {
        return context != null ? context.getSourceLang() : null;
    }

    /**
     * 获取翻译场景
     *
     * @return 翻译场景
     */
    public TranslateScenario getScenario() {
        return context != null ? context.getScenario() : null;
    }

    /**
     * 是否为批量翻译请求
     *
     * @return 如果目标语言超过1种则为批量翻译
     */
    public boolean isBatchTranslation() {
        return targetLanguages != null && targetLanguages.size() > 1;
    }

    /**
     * 获取第一个目标语言
     *
     * @return 第一个目标语言，如果列表为空则返回null
     */
    public String getFirstTargetLanguage() {
        return targetLanguages != null && !targetLanguages.isEmpty() ? targetLanguages.get(0) : null;
    }

    /**
     * 添加目标语言
     *
     * @param language 要添加的语言
     */
    public void addTargetLanguage(String language) {
        if (language != null && !language.trim().isEmpty()) {
            if (targetLanguages == null) {
                targetLanguages = new ArrayList<>();
            }
            if (!targetLanguages.contains(language)) {
                targetLanguages.add(language);
            }
        }
    }

    /**
     * 获取翻译选项，如果为null则返回默认选项
     *
     * @return 翻译选项
     */
    public TranslateOptions getOptionsOrDefault() {
        return options != null ? options : new TranslateOptions();
    }
}