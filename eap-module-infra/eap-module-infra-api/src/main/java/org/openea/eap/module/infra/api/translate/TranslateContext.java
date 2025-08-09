package org.openea.eap.module.infra.api.translate;

import org.openea.eap.module.infra.enums.translate.TranslateScenario;
import org.openea.eap.module.infra.enums.translate.TranslateConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 移除javax.validation依赖，使用自定义验证
import java.util.HashMap;
import java.util.Map;

/**
 * 翻译上下文类
 * 封装翻译请求的所有上下文信息，包括源文本、语言、场景等
 *
 * @author EAP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslateContext {

    /**
     * 源文本（必填）
     */
    private String sourceText;

    /**
     * 源语言代码（可选，默认为中文）
     */
    @Builder.Default
    private String sourceLang = TranslateConstants.DEFAULT_SOURCE_LANG;

    /**
     * 目标语言代码（必填）
     */
    private String targetLang;

    /**
     * 翻译场景（必填）
     */
    @Builder.Default
    private TranslateScenario scenario = TranslateScenario.GENERAL_TEXT;

    /**
     * 所属模块（可选）
     */
    private String module;

    /**
     * 国际化键值（可选）
     */
    private String i18nKey;

    /**
     * 最大长度限制（可选，默认使用场景的建议长度）
     */
    private Integer maxLength;

    /**
     * 翻译质量要求（可选，默认为自动）
     */
    @Builder.Default
    private String quality = TranslateConstants.Quality.AUTO;

    /**
     * 翻译模式（可选，默认为同步）
     */
    @Builder.Default
    private String mode = TranslateConstants.Mode.SYNC;

    /**
     * 是否使用缓存（可选，默认为true）
     */
    @Builder.Default
    private Boolean useCache = true;

    /**
     * 扩展元数据（可选）
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 创建翻译上下文的便捷方法
     *
     * @param sourceText 源文本
     * @param targetLang 目标语言
     * @return 翻译上下文
     */
    public static TranslateContext of(String sourceText, String targetLang) {
        return TranslateContext.builder()
                .sourceText(sourceText)
                .targetLang(targetLang)
                .build();
    }

    /**
     * 创建翻译上下文的便捷方法（指定场景）
     *
     * @param sourceText 源文本
     * @param targetLang 目标语言
     * @param scenario   翻译场景
     * @return 翻译上下文
     */
    public static TranslateContext of(String sourceText, String targetLang, TranslateScenario scenario) {
        return TranslateContext.builder()
                .sourceText(sourceText)
                .targetLang(targetLang)
                .scenario(scenario)
                .build();
    }

    /**
     * 创建翻译上下文的便捷方法（完整参数）
     *
     * @param sourceText 源文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param scenario   翻译场景
     * @return 翻译上下文
     */
    public static TranslateContext of(String sourceText, String sourceLang, String targetLang, TranslateScenario scenario) {
        return TranslateContext.builder()
                .sourceText(sourceText)
                .sourceLang(sourceLang)
                .targetLang(targetLang)
                .scenario(scenario)
                .build();
    }

    /**
     * 验证上下文的有效性
     *
     * @throws IllegalArgumentException 如果上下文无效
     */
    public void validate() {
        if (sourceText == null || sourceText.trim().isEmpty()) {
            throw new IllegalArgumentException("源文本不能为空");
        }

        if (targetLang == null || targetLang.trim().isEmpty()) {
            throw new IllegalArgumentException("目标语言不能为空");
        }

        if (!TranslateConstants.isSupportedLanguage(sourceLang)) {
            throw new IllegalArgumentException("不支持的源语言: " + sourceLang);
        }

        if (!TranslateConstants.isSupportedLanguage(targetLang)) {
            throw new IllegalArgumentException("不支持的目标语言: " + targetLang);
        }

        if (scenario == null) {
            throw new IllegalArgumentException("翻译场景不能为空");
        }

        // 检查文本长度
        int effectiveMaxLength = getEffectiveMaxLength();
        if (sourceText.length() > effectiveMaxLength) {
            throw new IllegalArgumentException(
                    String.format("源文本长度(%d)超过限制(%d)", sourceText.length(), effectiveMaxLength));
        }
    }

    /**
     * 获取有效的最大长度限制
     *
     * @return 有效的最大长度
     */
    public int getEffectiveMaxLength() {
        if (maxLength != null && maxLength > 0) {
            return maxLength;
        }
        if (scenario != null) {
            return scenario.getMaxLength();
        }
        return TranslateConstants.Limits.MAX_TEXT_LENGTH;
    }

    /**
     * 检查是否需要保持简洁
     *
     * @return 是否需要保持简洁
     */
    public boolean shouldKeepConcise() {
        return scenario != null && scenario.getKeepConcise();
    }

    /**
     * 添加元数据
     *
     * @param key   键
     * @param value 值
     * @return 当前对象（支持链式调用）
     */
    public TranslateContext addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    /**
     * 获取元数据
     *
     * @param key 键
     * @return 值
     */
    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }

    /**
     * 获取元数据（指定类型）
     *
     * @param key   键
     * @param clazz 值的类型
     * @param <T>   泛型类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> clazz) {
        Object value = getMetadata(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 生成缓存键
     *
     * @return 缓存键
     */
    public String generateCacheKey() {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(TranslateConstants.Cache.KEY_PREFIX);
        keyBuilder.append(sourceLang).append(":");
        keyBuilder.append(targetLang).append(":");
        keyBuilder.append(scenario.getCode()).append(":");
        keyBuilder.append(sourceText.hashCode());
        
        // 如果有模块信息，加入缓存键
        if (module != null && !module.trim().isEmpty()) {
            keyBuilder.append(":").append(module);
        }
        
        return keyBuilder.toString();
    }

    /**
     * 复制当前上下文并修改目标语言
     *
     * @param newTargetLang 新的目标语言
     * @return 新的翻译上下文
     */
    public TranslateContext withTargetLang(String newTargetLang) {
        return TranslateContext.builder()
                .sourceText(this.sourceText)
                .sourceLang(this.sourceLang)
                .targetLang(newTargetLang)
                .scenario(this.scenario)
                .module(this.module)
                .i18nKey(this.i18nKey)
                .maxLength(this.maxLength)
                .quality(this.quality)
                .mode(this.mode)
                .useCache(this.useCache)
                .metadata(new HashMap<>(this.metadata))
                .build();
    }

    /**
     * 复制当前上下文并修改场景
     *
     * @param newScenario 新的翻译场景
     * @return 新的翻译上下文
     */
    public TranslateContext withScenario(TranslateScenario newScenario) {
        return TranslateContext.builder()
                .sourceText(this.sourceText)
                .sourceLang(this.sourceLang)
                .targetLang(this.targetLang)
                .scenario(newScenario)
                .module(this.module)
                .i18nKey(this.i18nKey)
                .maxLength(this.maxLength)
                .quality(this.quality)
                .mode(this.mode)
                .useCache(this.useCache)
                .metadata(new HashMap<>(this.metadata))
                .build();
    }

    /**
     * 获取上下文的描述信息
     *
     * @return 描述信息
     */
    public String getDescription() {
        return String.format("翻译上下文[%s->%s, 场景:%s, 文本:%s]",
                TranslateConstants.getLanguageName(sourceLang),
                TranslateConstants.getLanguageName(targetLang),
                scenario.getName(),
                sourceText.length() > 50 ? sourceText.substring(0, 50) + "..." : sourceText);
    }
}