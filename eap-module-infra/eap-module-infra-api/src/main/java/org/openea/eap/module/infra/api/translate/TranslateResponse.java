package org.openea.eap.module.infra.api.translate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 翻译响应对象
 * 封装翻译请求的响应结果，包括翻译结果、元数据和错误信息
 *
 * @author EAP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslateResponse {

    /**
     * 翻译是否成功
     */
    private Boolean success;

    /**
     * 翻译结果列表
     * 按照请求中目标语言的顺序返回
     */
    private List<TranslateResult> results;

    /**
     * 翻译元数据
     */
    private TranslateMetadata metadata;

    /**
     * 错误信息（当翻译失败时）
     */
    private TranslateError error;

    /**
     * 响应时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 请求追踪ID
     * 用于日志追踪和问题排查
     */
    private String traceId;

    /**
     * 扩展属性
     * 用于存储额外的响应信息
     */
    private Map<String, Object> extensions;

    /**
     * 翻译结果内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslateResult {
        
        /**
         * 目标语言
         */
        private String targetLang;
        
        /**
         * 翻译后的文本
         */
        private String translatedText;
        
        /**
         * 翻译置信度（0.0-1.0）
         */
        private Double confidence;
        
        /**
         * 使用的翻译引擎
         */
        private String engine;
        
        /**
         * 是否来自缓存
         */
        @Builder.Default
        private Boolean fromCache = false;
        
        /**
         * 翻译耗时（毫秒）
         */
        private Long duration;
        
        /**
         * 检测到的源语言（如果与请求不同）
         */
        private String detectedSourceLang;
        
        /**
         * 翻译质量评分（0.0-1.0）
         */
        private Double qualityScore;
        
        /**
         * 术语一致性检查结果
         */
        private List<String> terminologyIssues;
        
        /**
         * 格式保留状态
         */
        private Boolean formatPreserved;
    }

    /**
     * 创建成功响应
     *
     * @param results 翻译结果列表
     * @return 成功响应对象
     */
    public static TranslateResponse success(List<TranslateResult> results) {
        return TranslateResponse.builder()
                .success(true)
                .results(results)
                .build();
    }

    /**
     * 创建成功响应（单个结果）
     *
     * @param result 翻译结果
     * @return 成功响应对象
     */
    public static TranslateResponse success(TranslateResult result) {
        return success(List.of(result));
    }

    /**
     * 创建失败响应
     *
     * @param error 错误信息
     * @return 失败响应对象
     */
    public static TranslateResponse failure(TranslateError error) {
        return TranslateResponse.builder()
                .success(false)
                .error(error)
                .build();
    }

    /**
     * 创建失败响应
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     * @return 失败响应对象
     */
    public static TranslateResponse failure(String errorCode, String errorMessage) {
        TranslateError error = TranslateError.builder()
                .code(errorCode)
                .message(errorMessage)
                .build();
        return failure(error);
    }

    /**
     * 检查响应是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * 检查响应是否失败
     *
     * @return 是否失败
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    /**
     * 获取第一个翻译结果
     *
     * @return 第一个翻译结果，如果没有则返回null
     */
    public TranslateResult getFirstResult() {
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }

    /**
     * 根据目标语言获取翻译结果
     *
     * @param targetLang 目标语言
     * @return 对应的翻译结果，如果没有则返回null
     */
    public TranslateResult getResultByLang(String targetLang) {
        if (results == null || targetLang == null) {
            return null;
        }
        return results.stream()
                .filter(result -> targetLang.equals(result.getTargetLang()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有翻译结果的文本
     *
     * @return 翻译文本列表
     */
    public List<String> getAllTranslatedTexts() {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .map(TranslateResult::getTranslatedText)
                .toList();
    }

    /**
     * 获取平均置信度
     *
     * @return 平均置信度，如果没有结果则返回0.0
     */
    public double getAverageConfidence() {
        if (results == null || results.isEmpty()) {
            return 0.0;
        }
        return results.stream()
                .filter(result -> result.getConfidence() != null)
                .mapToDouble(TranslateResult::getConfidence)
                .average()
                .orElse(0.0);
    }

    /**
     * 获取总翻译耗时
     *
     * @return 总耗时（毫秒），如果没有结果则返回0
     */
    public long getTotalDuration() {
        if (results == null) {
            return 0L;
        }
        return results.stream()
                .filter(result -> result.getDuration() != null)
                .mapToLong(TranslateResult::getDuration)
                .sum();
    }

    /**
     * 检查是否有任何结果来自缓存
     *
     * @return 是否有缓存结果
     */
    public boolean hasAnyFromCache() {
        if (results == null) {
            return false;
        }
        return results.stream()
                .anyMatch(result -> Boolean.TRUE.equals(result.getFromCache()));
    }

    /**
     * 检查是否所有结果都来自缓存
     *
     * @return 是否全部来自缓存
     */
    public boolean isAllFromCache() {
        if (results == null || results.isEmpty()) {
            return false;
        }
        return results.stream()
                .allMatch(result -> Boolean.TRUE.equals(result.getFromCache()));
    }

    /**
     * 添加扩展属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void addExtension(String key, Object value) {
        if (extensions == null) {
            extensions = new java.util.HashMap<>();
        }
        extensions.put(key, value);
    }

    /**
     * 获取扩展属性
     *
     * @param key 属性键
     * @return 属性值
     */
    public Object getExtension(String key) {
        return extensions != null ? extensions.get(key) : null;
    }

    /**
     * 设置追踪ID并返回当前对象（链式调用）
     *
     * @param traceId 追踪ID
     * @return 当前响应对象
     */
    public TranslateResponse withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 设置元数据并返回当前对象（链式调用）
     *
     * @param metadata 元数据
     * @return 当前响应对象
     */
    public TranslateResponse withMetadata(TranslateMetadata metadata) {
        this.metadata = metadata;
        return this;
    }
}