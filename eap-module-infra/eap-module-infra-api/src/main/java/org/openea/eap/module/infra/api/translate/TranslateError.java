package org.openea.eap.module.infra.api.translate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 翻译错误信息
 * 封装翻译过程中发生的错误详情
 *
 * @author EAP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslateError {

    /**
     * 错误代码
     */
    private String code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 详细错误描述
     */
    private String details;

    /**
     * 错误发生时间
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 错误类型/分类
     */
    private String type;

    /**
     * 错误严重级别
     */
    @Builder.Default
    private String severity = "ERROR";

    /**
     * 失败的翻译引擎
     */
    private String failedEngine;

    /**
     * 源文本（导致错误的文本）
     */
    private String sourceText;

    /**
     * 目标语言
     */
    private String targetLang;

    /**
     * 是否可重试
     */
    @Builder.Default
    private Boolean retryable = false;

    /**
     * 建议的解决方案
     */
    private String suggestion;

    /**
     * 错误堆栈信息（调试用）
     */
    private String stackTrace;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 相关的HTTP状态码（如果适用）
     */
    private Integer httpStatus;

    /**
     * 上游服务错误代码（如果适用）
     */
    private String upstreamErrorCode;

    /**
     * 扩展错误信息
     */
    private Map<String, Object> extensions;

    // 常见错误代码常量
    public static final String ERROR_INVALID_REQUEST = "INVALID_REQUEST";
    public static final String ERROR_UNSUPPORTED_LANGUAGE = "UNSUPPORTED_LANGUAGE";
    public static final String ERROR_TEXT_TOO_LONG = "TEXT_TOO_LONG";
    public static final String ERROR_QUOTA_EXCEEDED = "QUOTA_EXCEEDED";
    public static final String ERROR_ENGINE_UNAVAILABLE = "ENGINE_UNAVAILABLE";
    public static final String ERROR_NETWORK_TIMEOUT = "NETWORK_TIMEOUT";
    public static final String ERROR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String ERROR_RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String ERROR_INTERNAL_SERVER = "INTERNAL_SERVER_ERROR";
    public static final String ERROR_TRANSLATION_FAILED = "TRANSLATION_FAILED";
    public static final String ERROR_INVALID_LANGUAGE_PAIR = "INVALID_LANGUAGE_PAIR";
    public static final String ERROR_CONTENT_FILTERED = "CONTENT_FILTERED";
    public static final String ERROR_CACHE_FAILURE = "CACHE_FAILURE";
    public static final String ERROR_VALIDATION_FAILED = "VALIDATION_FAILED";

    // 错误严重级别常量
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_ERROR = "ERROR";
    public static final String SEVERITY_CRITICAL = "CRITICAL";

    // 错误类型常量
    public static final String TYPE_VALIDATION = "VALIDATION";
    public static final String TYPE_AUTHENTICATION = "AUTHENTICATION";
    public static final String TYPE_AUTHORIZATION = "AUTHORIZATION";
    public static final String TYPE_NETWORK = "NETWORK";
    public static final String TYPE_ENGINE = "ENGINE";
    public static final String TYPE_QUOTA = "QUOTA";
    public static final String TYPE_CONTENT = "CONTENT";
    public static final String TYPE_SYSTEM = "SYSTEM";

    /**
     * 创建验证错误
     *
     * @param message 错误消息
     * @return 错误对象
     */
    public static TranslateError validationError(String message) {
        return TranslateError.builder()
                .code(ERROR_VALIDATION_FAILED)
                .type(TYPE_VALIDATION)
                .message(message)
                .severity(SEVERITY_ERROR)
                .retryable(false)
                .build();
    }

    /**
     * 创建不支持语言错误
     *
     * @param language 不支持的语言
     * @return 错误对象
     */
    public static TranslateError unsupportedLanguageError(String language) {
        return TranslateError.builder()
                .code(ERROR_UNSUPPORTED_LANGUAGE)
                .type(TYPE_VALIDATION)
                .message("不支持的语言: " + language)
                .severity(SEVERITY_ERROR)
                .retryable(false)
                .suggestion("请检查支持的语言列表")
                .build();
    }

    /**
     * 创建文本过长错误
     *
     * @param actualLength 实际长度
     * @param maxLength 最大长度
     * @return 错误对象
     */
    public static TranslateError textTooLongError(int actualLength, int maxLength) {
        return TranslateError.builder()
                .code(ERROR_TEXT_TOO_LONG)
                .type(TYPE_VALIDATION)
                .message(String.format("文本长度超出限制: %d > %d", actualLength, maxLength))
                .severity(SEVERITY_ERROR)
                .retryable(false)
                .suggestion("请缩短文本长度或分段翻译")
                .build();
    }

    /**
     * 创建引擎不可用错误
     *
     * @param engine 引擎名称
     * @return 错误对象
     */
    public static TranslateError engineUnavailableError(String engine) {
        return TranslateError.builder()
                .code(ERROR_ENGINE_UNAVAILABLE)
                .type(TYPE_ENGINE)
                .message("翻译引擎不可用: " + engine)
                .failedEngine(engine)
                .severity(SEVERITY_ERROR)
                .retryable(true)
                .suggestion("请稍后重试或使用其他引擎")
                .build();
    }

    /**
     * 创建网络超时错误
     *
     * @param timeoutMs 超时时间
     * @return 错误对象
     */
    public static TranslateError networkTimeoutError(long timeoutMs) {
        return TranslateError.builder()
                .code(ERROR_NETWORK_TIMEOUT)
                .type(TYPE_NETWORK)
                .message(String.format("网络请求超时: %d ms", timeoutMs))
                .severity(SEVERITY_ERROR)
                .retryable(true)
                .suggestion("请检查网络连接或增加超时时间")
                .build();
    }

    /**
     * 创建配额超限错误
     *
     * @return 错误对象
     */
    public static TranslateError quotaExceededError() {
        return TranslateError.builder()
                .code(ERROR_QUOTA_EXCEEDED)
                .type(TYPE_QUOTA)
                .message("翻译配额已用完")
                .severity(SEVERITY_ERROR)
                .retryable(false)
                .suggestion("请联系管理员增加配额或等待配额重置")
                .build();
    }

    /**
     * 创建认证失败错误
     *
     * @return 错误对象
     */
    public static TranslateError authenticationFailedError() {
        return TranslateError.builder()
                .code(ERROR_AUTHENTICATION_FAILED)
                .type(TYPE_AUTHENTICATION)
                .message("翻译服务认证失败")
                .severity(SEVERITY_ERROR)
                .retryable(false)
                .suggestion("请检查API密钥配置")
                .build();
    }

    /**
     * 创建速率限制错误
     *
     * @return 错误对象
     */
    public static TranslateError rateLimitExceededError() {
        return TranslateError.builder()
                .code(ERROR_RATE_LIMIT_EXCEEDED)
                .type(TYPE_QUOTA)
                .message("请求频率超出限制")
                .severity(SEVERITY_WARNING)
                .retryable(true)
                .suggestion("请降低请求频率后重试")
                .build();
    }

    /**
     * 创建通用翻译失败错误
     *
     * @param cause 失败原因
     * @return 错误对象
     */
    public static TranslateError translationFailedError(String cause) {
        return TranslateError.builder()
                .code(ERROR_TRANSLATION_FAILED)
                .type(TYPE_ENGINE)
                .message("翻译失败: " + cause)
                .severity(SEVERITY_ERROR)
                .retryable(true)
                .build();
    }

    /**
     * 创建系统内部错误
     *
     * @param details 错误详情
     * @return 错误对象
     */
    public static TranslateError internalServerError(String details) {
        return TranslateError.builder()
                .code(ERROR_INTERNAL_SERVER)
                .type(TYPE_SYSTEM)
                .message("系统内部错误")
                .details(details)
                .severity(SEVERITY_CRITICAL)
                .retryable(true)
                .build();
    }

    /**
     * 添加扩展信息
     *
     * @param key 键
     * @param value 值
     */
    public void addExtension(String key, Object value) {
        if (this.extensions == null) {
            this.extensions = new java.util.HashMap<>();
        }
        this.extensions.put(key, value);
    }

    /**
     * 获取扩展信息
     *
     * @param key 键
     * @return 值
     */
    public Object getExtension(String key) {
        return this.extensions != null ? this.extensions.get(key) : null;
    }

    /**
     * 检查是否为严重错误
     *
     * @return 是否为严重错误
     */
    public boolean isCritical() {
        return SEVERITY_CRITICAL.equals(this.severity);
    }

    /**
     * 检查是否为警告级别
     *
     * @return 是否为警告
     */
    public boolean isWarning() {
        return SEVERITY_WARNING.equals(this.severity);
    }

    /**
     * 检查是否可重试
     *
     * @return 是否可重试
     */
    public boolean isRetryable() {
        return this.retryable != null && this.retryable;
    }

    /**
     * 设置请求ID并返回当前对象（链式调用）
     *
     * @param requestId 请求ID
     * @return 当前错误对象
     */
    public TranslateError withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * 设置堆栈信息并返回当前对象（链式调用）
     *
     * @param stackTrace 堆栈信息
     * @return 当前错误对象
     */
    public TranslateError withStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }
}