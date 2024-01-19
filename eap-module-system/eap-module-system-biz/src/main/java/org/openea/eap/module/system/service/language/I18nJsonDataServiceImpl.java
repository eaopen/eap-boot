package org.openea.eap.module.system.service.language;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataCreateReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataExportReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataPageReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataUpdateReqVO;
import org.openea.eap.module.system.convert.language.I18nJsonDataConvert;
import org.openea.eap.module.system.dal.dataobject.language.I18nJsonDataDO;
import org.openea.eap.module.system.dal.mysql.language.I18nJsonDataMapper;
import org.openea.eap.module.system.service.language.translate.TranslateUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openea.eap.framework.common.exception.util.ServiceExceptionUtil.exception;
import static org.openea.eap.module.system.enums.ErrorCodeConstants.I18N_JSON_DATA_NOT_EXISTS;

/**
 * 翻译 Service 实现类
 *
 * @author eap
 */
@Service
@Validated
public class I18nJsonDataServiceImpl implements I18nJsonDataService {

    @Resource
    private I18nJsonDataMapper i18nJsonDataMapper;

    @Override
    public Long createI18nJsonData(I18nJsonDataCreateReqVO createReqVO) {
        // 插入
        I18nJsonDataDO i18nJsonData = I18nJsonDataConvert.INSTANCE.convert(createReqVO);
        i18nJsonDataMapper.insert(i18nJsonData);
        // 返回
        return i18nJsonData.getId();
    }

    @Override
    public void updateI18nJsonData(I18nJsonDataUpdateReqVO updateReqVO) {
        // 校验存在
        validateI18nJsonDataExists(updateReqVO.getId());
        // 更新
        I18nJsonDataDO updateObj = I18nJsonDataConvert.INSTANCE.convert(updateReqVO);
        i18nJsonDataMapper.updateById(updateObj);
    }

    @Override
    public void deleteI18nJsonData(Long id) {
        // 校验存在
        validateI18nJsonDataExists(id);
        // 删除
        i18nJsonDataMapper.deleteById(id);
    }

    private void validateI18nJsonDataExists(Long id) {
        if (i18nJsonDataMapper.selectById(id) == null) {
            throw exception(I18N_JSON_DATA_NOT_EXISTS);
        }
    }

    @Override
    public I18nJsonDataDO getI18nJsonData(Long id) {
        return i18nJsonDataMapper.selectById(id);
    }

    @Override
    public List<I18nJsonDataDO> getI18nJsonDataList(Collection<Long> ids) {
        return i18nJsonDataMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<I18nJsonDataDO> getI18nJsonDataPage(I18nJsonDataPageReqVO pageReqVO) {
        return i18nJsonDataMapper.selectPage(pageReqVO);
    }

    @Override
    public List<I18nJsonDataDO> getI18nJsonDataList(I18nJsonDataExportReqVO exportReqVO) {
        return i18nJsonDataMapper.selectList(exportReqVO);
    }

    /**
     * 批量添加国际化条目
     *
     * @param module 模块
     * @param itemList map(key, desc, label)
     */
    @Async
    public void createI18nItemList(String module, List<Map<String, String>> itemList) {
        if(CollectionUtil.isEmpty(itemList)) return;
        for(Map<String, String> map : itemList){
            String key = MapUtil.getStr(map, "key");
            String desc = MapUtil.getStr(map, "desc");
            String label = MapUtil.getStr(map, "label");
            createI18nItem(module, key, desc, label);
        }
    }
    @Override
    public void createI18nItem(String module, String i18nKey, String i18nDesc, String originValue) {
        I18nJsonDataDO.I18nJsonDataDOBuilder i18nJsonDataDO = I18nJsonDataDO.builder();
        i18nJsonDataDO.module(module);
        i18nJsonDataDO.alias(i18nKey);
        i18nJsonDataDO.name(i18nDesc);
        JSONObject json = new JSONObject();
        //json.put("default", originValue);
        if(containsChinese(originValue)){
            json.put("zh-CN", originValue);
            json.put("en-US",translate(originValue, "en-US"));
            json.put("ja-JP",translate(originValue, "ja-JP"));
        } else  if(containsJapanese(originValue)){
            json.put("ja-JP", originValue);
            json.put("en-US",translate(originValue, "en-US"));
            json.put("zh-CN",translate(originValue, "zh-CN"));
        } else {
            json.put("en-US", originValue);
            json.put("zh-CN",translate(originValue, "zh-CN"));
            json.put("ja-JP",translate(originValue, "ja-JP"));
        }

        i18nJsonDataDO.json(json.toString());
        i18nJsonDataDO.remark("auto");
        i18nJsonDataMapper.insert(i18nJsonDataDO.build());
    }
    private static String translate(String text, String targetLang){
        return TranslateUtil.translateText(text, "auto", targetLang);
    }

    @Override
    public boolean checkI18nExist(String module, String i18nKey) {
        I18nJsonDataDO i18nJsonData =i18nJsonDataMapper.queryI18nJsonDataByKey(i18nKey);
        if(i18nJsonData!=null){
            return true;
        }
        return false;
    }

    private static boolean containsChinese(String input) {
        // 使用正则表达式匹配中文字符
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pattern.matcher(input);

        // 如果找到匹配的中文字符，则返回 true
        return matcher.find();
    }

    private static boolean containsJapanese(String input) {
        // 使用正则表达式匹配日文字符
        Pattern pattern = Pattern.compile("[\\p{Script=Hiragana}\\p{Script=Katakana}\\p{Script=Han}]");
        Matcher matcher = pattern.matcher(input);

        // 如果找到匹配的日文字符，则返回 true
        return matcher.find();
    }

}
