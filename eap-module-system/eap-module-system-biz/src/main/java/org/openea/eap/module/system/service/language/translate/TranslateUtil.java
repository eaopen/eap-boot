package org.openea.eap.module.system.service.language.translate;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class TranslateUtil {

    /**
     * 翻译文本
     * @param originText 原始文本
     * @param targetLang 目标语言
     * @param params 参数，可支持type、sourceLang
     * @return 翻译结果
     */
    public static String translateText(String originText, String targetLang, Map<String, String> params) {
        if(ObjectUtil.isEmpty(originText) || ObjectUtil.isEmpty(targetLang)){
            return originText;
        }
        String targetText = originText;
        // check type
        String type = "text";
        if(params != null && params.containsKey("type")){
            type = params.get("type").trim();
        }
        if(ObjectUtil.isEmpty(type)){
            type = "text";
        }else{
            type = type.toLowerCase();
        }
        // check source lang
        String sourceLang = "auto";
        if(params != null && params.containsKey("sourceLang")){
            sourceLang = params.get("sourceLang");
        }
        // todo 根据 type 不同选择不同的翻译机制
        // 优先大语言模型（默认chatgpt），其次才是翻译（默认google翻译）
        // 需要考虑国内环境的可用性，翻译待增加有道翻译
        try {
            targetText =  GT.getInstance().translateText(originText, sourceLang, targetLang);
        } catch (Exception e) {
            log.warn("google translate error", e);
            //throw new RuntimeException(e);
        }
        //

        return targetText;
    }

    /**
     * 国际化词条
     * @param type 类型
     *             menu/label 翻译保持词性，默认名词，保持大致长度（长度要求更严格）
     *             button/action 翻译保持词性，默认动词，保持大致长度
     *             message/tip/placeholder 一般翻译，支持变量替换
     * @param key  i18nKey 可选，用于辅助翻译
     * @param originText 原始词条
     * @param params 参数，可支持sourceLang 原始语言, targetLangs 多目标语言, length
     * @return 翻译结果json {lang1:target1Text,lang2:target2Text,key:i18nKey}
     */
    public static JSONObject translateI18nJson(String type, String key, String originText, Map<String, String> params){
        JSONObject targetJson = new JSONObject();
        // check type
        if(ObjectUtil.isEmpty(type)){
            if(params != null && params.containsKey("type")){
                type = params.get("type");
            }
        }
        if(ObjectUtil.isEmpty(type)){
            type = "text";
        }else{
            type = type.toLowerCase();
        }
        // check source lang
        String sourceLang = "auto";
        if(params != null && params.containsKey("sourceLang")){
            sourceLang = params.get("sourceLang");
        }
        if(!"auto".equals(sourceLang)){
            targetJson.set(sourceLang, originText);
        }

        // chatgpt
        if("menu".equals(type) || "button".equals(type)){
            // todo len
            JSONObject resultJson = queryMenuI18n(type, key, originText, 0);
            if(ObjectUtil.isNotEmpty(resultJson)){
                targetJson.putAll(resultJson);
            }
        }
        if(!"auto".equals(sourceLang)){
            targetJson.set(sourceLang, originText);
        }

        // google translate
        if(ObjectUtil.isNotEmpty(targetJson)){
            Set<String> langs = new HashSet<>();
            langs.add("zh-CN");
            langs.add("en-US");
            langs.add("ja-JP");
            for(String lang : langs){
                if(targetJson.containsKey(lang)){
                    continue;
                }
                String result = translateText(originText, lang);
                targetJson.set(lang, result);
            };
        }
        return targetJson;
    }

    public static String translateText(String originText, String targetLang) {
        return translateText(originText, targetLang, Collections.EMPTY_MAP);
    }

    public static String translateText(String originText, String sourceLang, String targetLang){
        if(ObjectUtil.isNotEmpty(sourceLang) && !"auto".equals(sourceLang)){
            Map<String, String> params = new HashMap<>();
            params.put("sourceLang", sourceLang);
            return translateText(originText, targetLang, params);
        }
        return translateText(originText, targetLang);
    }


    public static JSONObject queryMenuI18n(String type, String key, String name, int len) {
        JSONObject json = null;
        String strJson = null;
        try{
            strJson = ChatGPT.getInstance().queryMenuI18n(type, key, name, len);
            json = JSONUtil.parseObj(strJson);
        }catch (Exception e){
            log.warn(e.getMessage()+"\r\n"+strJson);
            log.debug(String.format("queryMenuI18n key=%s name=%s",key, name),e);
        }
        return json;
    }
}
