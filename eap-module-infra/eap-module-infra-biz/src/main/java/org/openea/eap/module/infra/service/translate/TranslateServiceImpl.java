package org.openea.eap.module.infra.service.translate;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;


/**
 * 翻译服务实现
 *
 * todo 可支持翻译实现，可配置优先顺序
 * 1、调用eap在线翻译服务（优化了翻译效果）
 * 2、直接调用google translate（已内建支持）
 * 3、调用chatgpt或兼容API(需要设置API key)
 *
 */
@Service
@Slf4j
public class TranslateServiceImpl implements TranslateService{
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
            targetText =  GoogleTranslate.getInstance().translateText(originText, sourceLang, targetLang);
        } catch (Exception e) {
            log.warn("google translate error", e);
            //throw new RuntimeException(e);
        }
        //
        return targetText;
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
        JSONObject json = null;
        String strJson = null;
        try{
            String type = MapUtil.getStr(params, "type", "menu");
            int len = MapUtil.getInt(params, "len", 0);
            strJson = ChatGPT.getInstance().queryMenuI18n(type, key, label, len);
            json = JSONUtil.parseObj(strJson);
        }catch (Exception e){
            log.warn(e.getMessage()+"\r\n"+strJson);
            log.debug(String.format("queryMenuI18n key=%s name=%s",key, label),e);
        }
        return json;
    }
}
