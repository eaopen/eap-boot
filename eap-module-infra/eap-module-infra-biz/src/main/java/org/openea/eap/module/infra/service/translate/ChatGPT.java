package org.openea.eap.module.infra.service.translate;

import cn.hutool.core.util.ObjectUtil;
import io.github.asleepyfish.annotation.EnableChatGPT;
import io.github.asleepyfish.config.ChatGPTProperties;
import io.github.asleepyfish.service.OpenAiProxyService;
import io.github.asleepyfish.util.OpenAiUtils;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.module.infra.api.translate.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ChatGPT翻译引擎实现
 * 基于OpenAI ChatGPT API的翻译服务
 *
 * @author EAP
 */
@Component
@Slf4j
public class ChatGPT implements TranslateEngine {

    private static ChatGPT _instance;
    public static ChatGPT getInstance(){
        if(_instance==null){
            _instance = new ChatGPT();
        }
        return _instance;
    }

    private ChatGPTProperties properties;
    
    // TranslateEngine接口所需的成员变量
    private final AtomicLong requestCount = new AtomicLong(0);
    private volatile LocalDateTime lastUsedTime = LocalDateTime.now();
    private volatile boolean available = true;
    private final Map<String, Object> configuration = new HashMap<>();

    public ChatGPTProperties loadChatGPTProperties(){
        if(properties==null){
            properties = ChatGPTProperties.builder().token("sk-")
                    .proxyHost("127.0.0.1")
                    .proxyPort(7890)
                    .build();
        }
        return  properties;
    }
    public void chat() {
        OpenAiProxyService openAiProxyService = new OpenAiProxyService(loadChatGPTProperties());
        System.out.println(openAiProxyService.chatCompletion("Go写个程序"));
    }

    public String chat2(String prompt){
        OpenAiProxyService openAiProxyService = new OpenAiProxyService(loadChatGPTProperties());
        return openAiProxyService.chatCompletion(prompt).get(0);
    }

    public String chat(String content) {
        return OpenAiUtils.createChatCompletion(content).get(0);
    }

    private String promptTemplate4menu = "作为IT专业人员帮助翻译menu菜单(通常为名词)或button按钮(通常为动词)，翻译结果返回格式为单层json " +
            "{\"en-US\":\"System Mgt\",\"zh-CN\":\"系统管理\",\n\"ja-JP\":\"システム\"}  " +
            "输入中文%s \"%s\",中文保持不变，翻译英文和日文，\"%s\"可作为英文翻译参考，" +
            "尽量保留相同的长度，一个中文对应两个英文字符。每次只返回一组翻译。";

    public String queryMenuI18n(String type, String key, String name, int len) {
        String prompt = String.format(promptTemplate4menu, type, name, key);
//        String result = chat2(prompt);
        String result = chat(prompt);
        log.debug("prompt="+prompt);
        log.debug("result="+result);
        if(ObjectUtil.isNotEmpty(result) && !result.startsWith("{")){
            result = result.substring(result.lastIndexOf("{"), result.lastIndexOf("}")+1);
            log.debug("result="+result);
        }
        return result;
    }

    public static void main(String[] args){
        //new ChatGPT().chat();
        new ChatGPT().queryMenuI18n("menu","post","岗位管理",0);
        new ChatGPT().queryMenuI18n("menu","notice","通知公告",0);
        new ChatGPT().queryMenuI18n("button","roleUpdate","角色修改",0);
    }

    // ========== TranslateEngine接口实现 ==========

    @Override
    public String getName() {
        return "ChatGPT";
    }

    @Override
    public String getType() {
        return "CHATGPT";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }



    @Override
    public String translateText(String text, String sourceLang, String targetLang) {
        try {
            requestCount.incrementAndGet();
            lastUsedTime = LocalDateTime.now();
            
            String prompt = buildTranslatePrompt(text, sourceLang, targetLang);
            String result = chat2(prompt);
            
            if (result != null && !result.trim().isEmpty()) {
                return result.trim();
            } else {
                log.warn("ChatGPT翻译结果为空");
                return null;
            }
        } catch (Exception e) {
            log.error("ChatGPT翻译失败: {}", e.getMessage(), e);
            return null;
        }
    }



    /**
     * 构建翻译提示词
     */
    private String buildTranslatePrompt(String text, String sourceLang, String targetLang) {
        return String.format(
            "请将以下%s文本翻译成%s，保持原文的格式和语气：\n%s",
            getLanguageName(sourceLang),
            getLanguageName(targetLang),
            text
        );
    }

    /**
     * 获取语言名称
     */
    private String getLanguageName(String langCode) {
        switch (langCode) {
            case "zh-CN": return "中文";
            case "en-US": return "英文";
            case "ja-JP": return "日文";
            case "ko-KR": return "韩文";
            case "fr-FR": return "法文";
            case "de-DE": return "德文";
            case "es-ES": return "西班牙文";
            case "ru-RU": return "俄文";
            default: return langCode;
        }
    }

}
