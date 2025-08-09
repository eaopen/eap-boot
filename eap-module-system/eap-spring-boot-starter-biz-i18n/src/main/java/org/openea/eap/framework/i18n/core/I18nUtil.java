package org.openea.eap.framework.i18n.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.common.util.spring.SpringUtils;
import org.openea.eap.module.system.api.i18n.I18nDataApi;
import org.openea.eap.module.system.framework.i18n.core.EapMessageResource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;


import java.util.*;

@Slf4j
public class I18nUtil {


    private static I18nDataApi i18nDataApi;
    private static EapMessageResource _messageSource;

    public static void init(I18nDataApi i18nDataApi, EapMessageResource messageResource) {
        I18nUtil.i18nDataApi = i18nDataApi;
        I18nUtil._messageSource = messageResource;
        try {
            reloadI18nApiData();
        } catch (Exception e) {
            //throw new RuntimeException("reloadI18nApiData fail",e);
            log.error("[init][初始化 I18nUtil - reloadI18nApiData fail]", e);
            // todo disable i18n
        }
        log.info("[init][初始化 I18nUtil 成功]");
    }

    public static void reloadI18nApiData() throws Exception {
        // 从API获取i18n数据更新到messageResource中
        if (I18nUtil.i18nDataApi == null || I18nUtil._messageSource==null) {
            return;
        }
        // current language, current modules

        // 1. init language
        Map<Locale, Map<String, String>> mLocal = new HashMap();
        List<String> langList = I18nUtil.i18nDataApi.getI18nSupportLangs();
        for(String lang : langList){
            Locale locale = Locale.forLanguageTag(lang);
            mLocal.put(locale, new HashMap<String,String>());
        }
        // 2. load data from api
        JSONObject i18nJson = I18nUtil.i18nDataApi.getI18nDataJson("", "");
        i18nJson.keySet().forEach(lang->{
            Locale locale = Locale.forLanguageTag(lang);
            if(!mLocal.containsKey(locale)){
                mLocal.put(locale, new HashMap<String,String>());
            }
            JSONObject langJson = i18nJson.getJSONObject(lang);
            if (langJson != null) {
                langJson.keySet().forEach(key -> {
                    mLocal.get(locale).put(key, langJson.getStr(key));
                });
            }
        });

        // 3. save message to static
        mLocal.keySet().forEach(locale ->{
            I18nUtil._messageSource.addMessages(mLocal.get(locale), locale);
        });

        // zh-CN -> zh, en-US -> en
        Set<Locale> setLocale = mLocal.keySet();
        for(Locale locale: setLocale){
            if(StrUtil.isNotEmpty(locale.getCountry())){
                Locale noCountryLocale = Locale.forLanguageTag(locale.getLanguage());
                if(!mLocal.containsKey(noCountryLocale)){
                    I18nUtil._messageSource.addMessages(mLocal.get(locale), noCountryLocale);
                }
            }
        }
    }

    public static EapMessageResource getMessageResource(){
        if(_messageSource==null){
            _messageSource = SpringUtils.getBean(EapMessageResource.class);
        }
        return _messageSource;
    }

    public static boolean enableI18n(){
        // TODO 从配置服务或文件获取使用启用国际化
        return true;
    }

    public static Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    public static String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage){
        String label = null;
        try {
            Locale locale = getLocale();
            label = getMessageResource().getMessage(code, args, locale);
            if (StrUtil.isEmpty(label) && StrUtil.isNotEmpty(defaultMessage)) {
                label = defaultMessage;
            }
        }catch(NoSuchMessageException e){
            log.debug(e.getMessage()+" (getMessage code="+code+")");
            // 尝试AI自动翻译
            if (StrUtil.isEmpty(label)) {
                label = tryAITranslation(code, defaultMessage, getLocale());
            }
            // 使用默认消息
            if (StrUtil.isEmpty(label) && StrUtil.isNotEmpty(defaultMessage)) {
                label = defaultMessage;
            }
        }catch(Exception e){
            log.warn(e.getMessage()+" (getMessage code="+code+")", e);
        }
        
        if(StrUtil.isEmpty(label) && StrUtil.isNotEmpty(defaultMessage)){
            label = defaultMessage;
        }
        if(StrUtil.isEmpty(label)){
            label = code;
        }
        return label;
    }

    /**
     * 尝试AI自动翻译
     */
    private static String tryAITranslation(String code, String defaultMessage, Locale locale) {
        if (StrUtil.isEmpty(defaultMessage)) {
            return null;
        }
        
        try {
            // 从Spring容器获取AITranslationFactory
            Object aiTranslationFactory = SpringUtils.getBean("AITranslationFactory", false);
            if (aiTranslationFactory == null) {
                return null;
            }
            
            String targetLang = locale.toLanguageTag();
            if ("zh".equals(locale.getLanguage()) && StrUtil.isEmpty(locale.getCountry())) {
                targetLang = "zh-CN";
            } else if ("en".equals(locale.getLanguage()) && StrUtil.isEmpty(locale.getCountry())) {
                targetLang = "en-US";
            }
            
            // 假设默认消息是中文，翻译到目标语言
            if (!"zh-CN".equals(targetLang)) {
                // 使用反射调用translateWithFallback方法
                java.lang.reflect.Method method = aiTranslationFactory.getClass()
                    .getMethod("translateWithFallback", String.class, String.class, String.class, String.class);
                Object result = method.invoke(aiTranslationFactory, defaultMessage, "zh-CN", targetLang, "system_message");
                
                if (result != null) {
                    // 检查翻译是否成功
                    java.lang.reflect.Method getSuccessMethod = result.getClass().getMethod("getSuccess");
                    Boolean success = (Boolean) getSuccessMethod.invoke(result);
                    
                    if (Boolean.TRUE.equals(success)) {
                        java.lang.reflect.Method getTranslatedTextMethod = result.getClass().getMethod("getTranslatedText");
                        String translatedText = (String) getTranslatedTextMethod.invoke(result);
                        
                        if (StrUtil.isNotEmpty(translatedText)) {
                            log.debug("AI翻译成功: {} -> {} ({}->{})", defaultMessage, translatedText, "zh-CN", targetLang);
                            return translatedText;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("AI翻译失败: {}", e.getMessage());
        }
        
        return null;
    }

    public static String getMessage(String code, String defaultMessage){
        return getMessage(code, null, defaultMessage);
    }

    public static String getMessage(String code){
        return getMessage(code, null, null);
    }

    public static String t(String code, @Nullable Object[] args){
        return getMessage(code, args, null);
    }

    public static String t(String code, String defaultMessage){
        return getMessage(code, null, defaultMessage);
    }

    public static String t(String code){
        return getMessage(code);
    }
}
