package org.openea.eap.module.infra.service.translate;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 翻译服务实现
 * 
 * 简化的翻译实现：
 * 1、优先使用ChatGPT引擎（基于LLM，支持上下文感知翻译）
 * 2、备用Google翻译引擎（稳定可靠的传统翻译服务）
 * 3、自动故障转移机制，确保翻译服务的高可用性
 *
 */
@Service
@Slf4j
public class TranslateServiceImpl implements TranslateService{
    
    // 引擎使用统计
    private final AtomicLong chatGptUsageCount = new AtomicLong(0);
    private final AtomicLong googleTranslateUsageCount = new AtomicLong(0);
    private final AtomicLong failoverCount = new AtomicLong(0);
    /**
     * 翻译文本(国际化翻译)
     *
     * @param originText 原始文本
     * @param targetLang 目标语言
     * @param params     参数，可支持
     *                   type: 翻译类型, 可选， menu/button/key/label
     *                   sourceLang：源语言，可选
     *                   length: 优选文本长度，可选
     * @return 翻译结果
     */
    @Override
    public String translateText(String originText, String targetLang, Map<String, String> params) {
        if(ObjectUtil.isEmpty(originText) || ObjectUtil.isEmpty(targetLang)){
            return originText;
        }
        
        // 解析参数
        String type = "text";
        if(params != null && params.containsKey("type")){
            type = params.get("type").trim();
        }
        if(ObjectUtil.isEmpty(type)){
            type = "text";
        }else{
            type = type.toLowerCase();
        }
        
        String sourceLang = "auto";
        if(params != null && params.containsKey("sourceLang")){
            sourceLang = params.get("sourceLang");
        }
        
        // 实现引擎选择逻辑：优先ChatGPT，备用GoogleTranslate
        return translateWithFallback(originText, sourceLang, targetLang, type);
    }
    
    /**
     * 带故障转移的翻译方法
     * 优先使用ChatGPT引擎，失败时自动切换到GoogleTranslate
     *
     * @param text 原始文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param type 翻译类型
     * @return 翻译结果
     */
    private String translateWithFallback(String text, String sourceLang, String targetLang, String type) {
        // 首先尝试使用ChatGPT引擎
        if (isChatGPTAvailable()) {
            try {
                String result = translateWithChatGPT(text, sourceLang, targetLang, type);
                if (ObjectUtil.isNotEmpty(result)) {
                    chatGptUsageCount.incrementAndGet();
                    log.debug("ChatGPT翻译成功: {} -> {}", text, result);
                    return result;
                }
            } catch (Exception e) {
                log.warn("ChatGPT翻译失败，尝试使用GoogleTranslate: {}", e.getMessage());
                failoverCount.incrementAndGet();
            }
        }
        
        // ChatGPT不可用或失败时，使用GoogleTranslate作为备用
        try {
            String result = GoogleTranslate.getInstance().translateText(text, sourceLang, targetLang);
            googleTranslateUsageCount.incrementAndGet();
            log.debug("GoogleTranslate翻译成功: {} -> {}", text, result);
            return result;
        } catch (Exception e) {
            log.error("所有翻译引擎都失败了: {}", e.getMessage());
            return text; // 返回原文本作为最后的备选
        }
    }
    
    /**
     * 检查ChatGPT引擎是否可用
     *
     * @return true如果可用，false如果不可用
     */
    private boolean isChatGPTAvailable() {
        try {
            return ChatGPT.getInstance().isAvailable();
        } catch (Exception e) {
            log.warn("检查ChatGPT可用性时出错: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 使用ChatGPT进行翻译
     *
     * @param text 原始文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param type 翻译类型
     * @return 翻译结果
     */
    private String translateWithChatGPT(String text, String sourceLang, String targetLang, String type) {
        // 根据翻译类型选择不同的翻译策略
         if ("menu".equals(type) || "button".equals(type)) {
             // 对于菜单和按钮，使用专门的菜单翻译方法
             try {
                 String jsonResult = ChatGPT.getInstance().queryMenuI18n(type, "", text, 0);
                 if (ObjectUtil.isNotEmpty(jsonResult)) {
                     JSONObject result = JSONUtil.parseObj(jsonResult);
                     if (result != null && result.containsKey(targetLang)) {
                         return result.getStr(targetLang);
                     }
                 }
             } catch (Exception e) {
                 log.debug("菜单翻译失败，使用通用翻译: {}", e.getMessage());
             }
         }
        
        // 使用通用翻译方法（这里需要实现一个简单的翻译调用）
        // 由于ChatGPT类目前没有直接的文本翻译方法，我们使用chat2方法
        String prompt = buildSimpleTranslatePrompt(text, sourceLang, targetLang, type);
        return ChatGPT.getInstance().chat2(prompt);
    }
    
    /**
     * 构建简单的翻译提示词
     *
     * @param text 原始文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @param type 翻译类型
     * @return 提示词
     */
    private String buildSimpleTranslatePrompt(String text, String sourceLang, String targetLang, String type) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请将以下文本翻译成").append(getLanguageName(targetLang)).append("：\n");
        prompt.append("原文：").append(text).append("\n");
        
        if ("menu".equals(type) || "button".equals(type)) {
            prompt.append("注意：这是一个").append(type).append("文本，请保持简洁和专业。\n");
        }
        
        prompt.append("只返回翻译结果，不要包含其他内容。");
        return prompt.toString();
    }
    
    /**
     * 获取语言名称
     *
     * @param langCode 语言代码
     * @return 语言名称
     */
    private String getLanguageName(String langCode) {
        switch (langCode.toLowerCase()) {
            case "en":
            case "en-us":
                return "英文";
            case "zh":
            case "zh-cn":
                return "中文";
            case "ja":
            case "ja-jp":
                return "日文";
            case "ko":
            case "ko-kr":
                return "韩文";
            case "fr":
                return "法文";
            case "de":
                return "德文";
            case "es":
                return "西班牙文";
            default:
                return langCode;
        }
    }

    @Override
    public List<String> translateText(List<String> originTexts, String targetLang, Map<String, String> params) {
        return null;
    }

    /**
     * 菜单翻译
     *
     * @param key
     * @param label
     * @param params 参数
     *               - module: 所属模块，可选
     *               - type : menu/button，可选，默认menu
     *               - len : 建议翻译长度，默认同源语言保持一致
     *               - targetLang: 多个目标语言，默认英中日
     * @return
     */
    @Override
    public JSONObject queryMenuI18n(String key, String label, Map<String, String> params) {
        if (ObjectUtil.isEmpty(label)) {
            return null;
        }
        
        String type = MapUtil.getStr(params, "type", "menu");
        int len = MapUtil.getInt(params, "len", 0);
        
        // 优先使用ChatGPT进行菜单翻译
        if (isChatGPTAvailable()) {
            try {
                String strJson = ChatGPT.getInstance().queryMenuI18n(type, key, label, len);
                if (ObjectUtil.isNotEmpty(strJson)) {
                    JSONObject json = JSONUtil.parseObj(strJson);
                    if (json != null && !json.isEmpty()) {
                        chatGptUsageCount.incrementAndGet();
                        log.debug("ChatGPT菜单翻译成功: {} -> {}", label, strJson);
                        return json;
                    }
                }
            } catch (Exception e) {
                log.warn("ChatGPT菜单翻译失败，尝试使用GoogleTranslate降级: {}", e.getMessage());
                failoverCount.incrementAndGet();
            }
        }
        
        // ChatGPT不可用或失败时，使用GoogleTranslate进行降级翻译
        return fallbackMenuTranslation(label, type);
    }
    
    /**
     * 菜单翻译的降级方案
     * 使用GoogleTranslate为常见语言提供基本的菜单翻译
     *
     * @param label 菜单标签
     * @param type 类型
     * @return 翻译结果JSON
     */
    private JSONObject fallbackMenuTranslation(String label, String type) {
        JSONObject result = new JSONObject();
        
        try {
            // 默认支持的目标语言
            String[] targetLangs = {"en-US", "zh-CN", "ja-JP"};
            
            for (String targetLang : targetLangs) {
                try {
                    String translated = GoogleTranslate.getInstance().translateText(label, "auto", targetLang);
                    if (ObjectUtil.isNotEmpty(translated)) {
                        result.put(targetLang, translated);
                    } else {
                        result.put(targetLang, label); // 翻译失败时保持原文
                    }
                } catch (Exception e) {
                    log.debug("GoogleTranslate翻译{}到{}失败: {}", label, targetLang, e.getMessage());
                    result.put(targetLang, label); // 翻译失败时保持原文
                }
            }
            
            googleTranslateUsageCount.incrementAndGet();
            log.debug("GoogleTranslate菜单翻译完成: {} -> {}", label, result.toString());
            
        } catch (Exception e) {
            log.error("菜单翻译降级方案也失败了: {}", e.getMessage());
            // 返回包含原文的基本结果
            result.put("zh-CN", label);
            result.put("en-US", label);
            result.put("ja-JP", label);
        }
        
        return result;
    }
}
