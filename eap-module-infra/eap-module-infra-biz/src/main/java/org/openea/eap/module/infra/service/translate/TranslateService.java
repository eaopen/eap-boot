package org.openea.eap.module.infra.service.translate;

import cn.hutool.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface TranslateService {

    /**
     * 翻译文本(国际化翻译)
     * @param originText 原始文本
     * @param targetLang 目标语言
     * @param params 参数，可支持
     *               type: 翻译类型, 可选， menu/button/key/label
     *               sourceLang：源语言，可选
     *               length: 优选文本长度，可选
     * @return 翻译结果
     */
    String translateText(String originText, String targetLang, Map<String, String> params);
    List<String> translateText(List<String> originTexts, String targetLang, Map<String, String> params);


    // 模块/菜单
    // 表单/类名/表名
    // 属性名/字段名
    // 链接/按钮/操作

    /**
     * 菜单翻译
     * @param key
     * @param label
     * @param params 参数
     *    - module: 所属模块，可选
     *    - type : menu/button，可选，默认menu
     *    - len : 建议翻译长度，默认同源语言保持一致
     *    - targetLang: 多个目标语言，默认英中日
     * @return
     */
    JSONObject queryMenuI18n(String key, String label, Map<String, String> params);
}
