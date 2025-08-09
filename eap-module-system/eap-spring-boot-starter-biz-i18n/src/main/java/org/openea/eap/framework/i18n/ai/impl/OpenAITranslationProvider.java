package org.openea.eap.framework.i18n.ai.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.i18n.ai.AITranslationConfig;
import org.openea.eap.framework.i18n.ai.AITranslationResult;
import org.openea.eap.framework.i18n.ai.AbstractAITranslationProvider;

/**
 * OpenAI翻译提供商实现
 *
 * @author eap
 */
@Slf4j
public class OpenAITranslationProvider extends AbstractAITranslationProvider {

    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    private static final String[] SUPPORTED_MODELS = {"gpt-3.5-turbo", "gpt-4", "gpt-4-turbo"};

    public OpenAITranslationProvider(AITranslationConfig config) {
        super(config);
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public String[] getSupportedModels() {
        return SUPPORTED_MODELS;
    }

    @Override
    public AITranslationResult translateText(String text, String fromLang, String toLang, String context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 验证参数
            validateTranslationParams(text, fromLang, toLang);
            
            // 记录请求
            recordTranslationRequest();
            
            // 构建提示词
            String prompt = buildTranslationPrompt(text, fromLang, toLang, context);
            
            // 调用OpenAI API
            String translatedText = callOpenAIAPI(prompt);
            
            if (StrUtil.isEmpty(translatedText)) {
                return createFailureResult("OpenAI返回空结果", startTime);
            }
            
            // 创建成功结果
            AITranslationResult result = createResult(translatedText, getModelName(), startTime);
            result.setConfidence(0.85); // OpenAI通常有较高的置信度
            
            return result;
            
        } catch (Exception e) {
            log.error("OpenAI翻译失败", e);
            return createFailureResult(e.getMessage(), startTime);
        }
    }

    @Override
    protected boolean checkServiceHealth() {
        try {
            // 简单的健康检查：发送一个测试请求
            String testPrompt = "Translate 'hello' from English to Chinese.";
            String response = callOpenAIAPI(testPrompt);
            return StrUtil.isNotEmpty(response);
        } catch (Exception e) {
            log.warn("OpenAI服务健康检查失败", e);
            return false;
        }
    }

    /**
     * 构建翻译提示词
     */
    private String buildTranslationPrompt(String text, String fromLang, String toLang, String context) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("You are a professional translator. ");
        promptBuilder.append("Please translate the following text from ")
                    .append(getLanguageName(fromLang))
                    .append(" to ")
                    .append(getLanguageName(toLang))
                    .append(".\n\n");
        
        if (StrUtil.isNotEmpty(context)) {
            promptBuilder.append("Context: ").append(context).append("\n\n");
        }
        
        promptBuilder.append("Requirements:\n");
        promptBuilder.append("1. Maintain the original meaning and tone\n");
        promptBuilder.append("2. Use natural and fluent expressions in the target language\n");
        promptBuilder.append("3. Preserve any technical terms or proper nouns appropriately\n");
        promptBuilder.append("4. Return only the translated text without explanations\n\n");
        
        promptBuilder.append("Text to translate:\n").append(text);
        
        return promptBuilder.toString();
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAIAPI(String prompt) {
        String apiEndpoint = config.getApiEndpoint();
        if (StrUtil.isEmpty(apiEndpoint)) {
            apiEndpoint = "https://api.openai.com/v1/chat/completions";
        }
        
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", getModelName());
        requestBody.set("messages", new Object[]{
            new JSONObject()
                .set("role", "user")
                .set("content", prompt)
        });
        requestBody.set("max_tokens", 2000);
        requestBody.set("temperature", 0.3); // 较低的温度以获得更一致的翻译
        
        // 发送请求
        HttpResponse response = HttpRequest.post(apiEndpoint)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(config.getTimeout() != null ? config.getTimeout() : 30000)
                .execute();
        
        if (!response.isOk()) {
            throw new RuntimeException("OpenAI API请求失败: " + response.getStatus() + " - " + response.body());
        }
        
        // 解析响应
        JSONObject responseJson = JSONUtil.parseObj(response.body());
        if (responseJson.containsKey("error")) {
            JSONObject error = responseJson.getJSONObject("error");
            throw new RuntimeException("OpenAI API错误: " + error.getStr("message"));
        }
        
        if (!responseJson.containsKey("choices") || 
            responseJson.getJSONArray("choices").isEmpty()) {
            throw new RuntimeException("OpenAI API返回无效响应");
        }
        
        JSONObject firstChoice = responseJson.getJSONArray("choices").getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        
        return message.getStr("content").trim();
    }

    /**
     * 获取使用的模型名称
     */
    private String getModelName() {
        return config.getDefaultModel() != null ? config.getDefaultModel() : DEFAULT_MODEL;
    }

    /**
     * 获取语言名称
     */
    private String getLanguageName(String langCode) {
        return switch (langCode) {
            case "zh_CN", "zh-CN" -> "Chinese (Simplified)";
            case "zh_HK", "zh-HK" -> "Chinese (Traditional)";
            case "en_US", "en-US", "en" -> "English";
            case "ja_JP", "ja-JP", "ja" -> "Japanese";
            case "ko_KR", "ko-KR", "ko" -> "Korean";
            case "fr_FR", "fr-FR", "fr" -> "French";
            case "de_DE", "de-DE", "de" -> "German";
            case "es_ES", "es-ES", "es" -> "Spanish";
            case "pt_PT", "pt-PT", "pt" -> "Portuguese";
            case "ru_RU", "ru-RU", "ru" -> "Russian";
            case "ar_SA", "ar-SA", "ar" -> "Arabic";
            case "th_TH", "th-TH", "th" -> "Thai";
            case "vi_VN", "vi-VN", "vi" -> "Vietnamese";
            default -> langCode;
        };
    }

    @Override
    public int evaluateTranslationQuality(String originalText, String translatedText, String fromLang, String toLang) {
        // OpenAI特定的质量评估
        int baseScore = super.evaluateTranslationQuality(originalText, translatedText, fromLang, toLang);
        
        // OpenAI通常质量较高，给予额外分数
        return Math.min(10, baseScore + 1);
    }
}