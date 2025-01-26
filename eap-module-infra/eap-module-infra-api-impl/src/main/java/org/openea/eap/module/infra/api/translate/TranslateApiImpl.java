package org.openea.eap.module.infra.api.translate;

import cn.hutool.json.JSONObject;
import org.openea.eap.module.infra.service.translate.TranslateService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TranslateApiImpl implements TranslateApi{

    @Resource
    private TranslateService translateService;

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
        return translateService.translateText(originText, targetLang, params);
    }

    @Override
    public List<String> translateText(List<String> originTexts, String targetLang, Map<String, String> params) {
        return translateService.translateText(originTexts, targetLang, params);
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
        return translateService.queryMenuI18n(key, label, params);
    }
}
