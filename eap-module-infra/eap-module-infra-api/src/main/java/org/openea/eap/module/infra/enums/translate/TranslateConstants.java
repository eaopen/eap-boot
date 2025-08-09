package org.openea.eap.module.infra.enums.translate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 翻译相关常量定义
 * 包含支持的语言代码、默认配置等常量
 *
 * @author EAP
 */
public class TranslateConstants {

    /**
     * 默认源语言
     */
    public static final String DEFAULT_SOURCE_LANG = "zh";

    /**
     * 默认目标语言
     */
    public static final String DEFAULT_TARGET_LANG = "en";

    /**
     * 支持的语言代码列表
     */
    public static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "zh",    // 中文
            "en",    // 英文
            "ja",    // 日文
            "ko",    // 韩文
            "fr",    // 法文
            "de",    // 德文
            "es",    // 西班牙文
            "it",    // 意大利文
            "pt",    // 葡萄牙文
            "ru",    // 俄文
            "ar",    // 阿拉伯文
            "th",    // 泰文
            "vi",    // 越南文
            "id",    // 印尼文
            "ms",    // 马来文
            "hi",    // 印地文
            "tr",    // 土耳其文
            "pl",    // 波兰文
            "nl",    // 荷兰文
            "sv"     // 瑞典文
    );

    /**
     * 支持的语言代码集合（用于快速查找）
     */
    public static final Set<String> SUPPORTED_LANGUAGE_SET = new HashSet<>(SUPPORTED_LANGUAGES);

    /**
     * 语言代码到语言名称的映射
     */
    public static final class LanguageNames {
        public static final String ZH = "中文";
        public static final String EN = "English";
        public static final String JA = "日本語";
        public static final String KO = "한국어";
        public static final String FR = "Français";
        public static final String DE = "Deutsch";
        public static final String ES = "Español";
        public static final String IT = "Italiano";
        public static final String PT = "Português";
        public static final String RU = "Русский";
        public static final String AR = "العربية";
        public static final String TH = "ไทย";
        public static final String VI = "Tiếng Việt";
        public static final String ID = "Bahasa Indonesia";
        public static final String MS = "Bahasa Melayu";
        public static final String HI = "हिन्दी";
        public static final String TR = "Türkçe";
        public static final String PL = "Polski";
        public static final String NL = "Nederlands";
        public static final String SV = "Svenska";
    }

    /**
     * 翻译引擎类型
     */
    public static final class EngineType {
        public static final String GOOGLE = "google";
        public static final String CHATGPT = "chatgpt";
        public static final String BAIDU = "baidu";
        public static final String YOUDAO = "youdao";
        public static final String TENCENT = "tencent";
    }

    /**
     * 翻译策略类型
     */
    public static final class StrategyType {
        public static final String MENU = "menu";
        public static final String GENERAL = "general";
        public static final String BATCH = "batch";
        public static final String CONTEXT = "context";
    }

    /**
     * 翻译缓存相关常量
     */
    public static final class Cache {
        /**
         * 缓存键前缀
         */
        public static final String KEY_PREFIX = "translate:";
        
        /**
         * 默认缓存过期时间（秒）
         */
        public static final long DEFAULT_TTL = 3600L; // 1小时
        
        /**
         * 长期缓存过期时间（秒）
         */
        public static final long LONG_TTL = 86400L; // 24小时
        
        /**
         * 最大缓存大小
         */
        public static final int MAX_CACHE_SIZE = 10000;
    }

    /**
     * 翻译限制常量
     */
    public static final class Limits {
        /**
         * 单次翻译最大文本长度
         */
        public static final int MAX_TEXT_LENGTH = 5000;
        
        /**
         * 批量翻译最大条目数
         */
        public static final int MAX_BATCH_SIZE = 100;
        
        /**
         * 翻译超时时间（毫秒）
         */
        public static final long TRANSLATE_TIMEOUT = 30000L; // 30秒
        
        /**
         * 重试次数
         */
        public static final int MAX_RETRY_COUNT = 3;
    }

    /**
     * 翻译质量等级
     */
    public static final class Quality {
        public static final String HIGH = "high";
        public static final String MEDIUM = "medium";
        public static final String LOW = "low";
        public static final String AUTO = "auto";
    }

    /**
     * 翻译模式
     */
    public static final class Mode {
        /**
         * 同步翻译
         */
        public static final String SYNC = "sync";
        
        /**
         * 异步翻译
         */
        public static final String ASYNC = "async";
        
        /**
         * 批量翻译
         */
        public static final String BATCH = "batch";
    }

    /**
     * 错误代码
     */
    public static final class ErrorCode {
        public static final String UNSUPPORTED_LANGUAGE = "UNSUPPORTED_LANGUAGE";
        public static final String TEXT_TOO_LONG = "TEXT_TOO_LONG";
        public static final String ENGINE_UNAVAILABLE = "ENGINE_UNAVAILABLE";
        public static final String TRANSLATION_FAILED = "TRANSLATION_FAILED";
        public static final String INVALID_CONTEXT = "INVALID_CONTEXT";
        public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    }

    /**
     * 检查语言代码是否支持
     *
     * @param languageCode 语言代码
     * @return 是否支持
     */
    public static boolean isSupportedLanguage(String languageCode) {
        return languageCode != null && SUPPORTED_LANGUAGE_SET.contains(languageCode.toLowerCase());
    }

    /**
     * 获取语言名称
     *
     * @param languageCode 语言代码
     * @return 语言名称
     */
    public static String getLanguageName(String languageCode) {
        if (languageCode == null) {
            return "未知语言";
        }
        
        switch (languageCode.toLowerCase()) {
            case "zh": return LanguageNames.ZH;
            case "en": return LanguageNames.EN;
            case "ja": return LanguageNames.JA;
            case "ko": return LanguageNames.KO;
            case "fr": return LanguageNames.FR;
            case "de": return LanguageNames.DE;
            case "es": return LanguageNames.ES;
            case "it": return LanguageNames.IT;
            case "pt": return LanguageNames.PT;
            case "ru": return LanguageNames.RU;
            case "ar": return LanguageNames.AR;
            case "th": return LanguageNames.TH;
            case "vi": return LanguageNames.VI;
            case "id": return LanguageNames.ID;
            case "ms": return LanguageNames.MS;
            case "hi": return LanguageNames.HI;
            case "tr": return LanguageNames.TR;
            case "pl": return LanguageNames.PL;
            case "nl": return LanguageNames.NL;
            case "sv": return LanguageNames.SV;
            default: return "未知语言";
        }
    }

    /**
     * 私有构造函数，防止实例化
     */
    private TranslateConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}