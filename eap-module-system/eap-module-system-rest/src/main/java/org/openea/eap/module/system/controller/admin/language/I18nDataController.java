package org.openea.eap.module.system.controller.admin.language;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.i18n.core.I18nUtil;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataRespVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataUpdateReqVO;
import org.openea.eap.module.system.convert.language.I18nJsonDataConvert;
import org.openea.eap.module.system.dal.dataobject.language.I18nJsonDataDO;
import org.openea.eap.module.system.service.language.I18nDataService;
import org.openea.eap.module.system.service.language.translate.TranslateUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.openea.eap.framework.common.pojo.CommonResult.error;
import static org.openea.eap.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - i18n国际化数据")
@RestController
@RequestMapping("/system/i18n-data")
@Validated
public class I18nDataController {

    @Resource
    private I18nDataService i18nDataService;

    @GetMapping(value = "getJs")
    @Operation(summary = "获得前端i18n json数据",description = "数据json格式 {language:{key:label}}")
    public CommonResult<JSONObject> getJsJson(){
        return success(i18nDataService.getJsJson());
    }
    @GetMapping(value = "getLocalMessage/{locale}")
    @Operation(summary = "获得指定语言的json数据",description = "数据json格式 {}")
    public CommonResult<JSONObject> getLocalMessage(@PathVariable("locale") String locale, String modules, String lastLoadTime){
        JSONObject message = new JSONObject();
        message.putAll(i18nDataService.getLocaleMessageJson(locale));
        message.put("lastLoadTime", new Date());
        return success(message);
    }

    @GetMapping(value = "reloadI18nUtil")
    @Operation(summary = "刷新API数据",description = "")
    @SneakyThrows
    public CommonResult reloadI18nUtil(){
        I18nUtil.reloadI18nApiData();
        return success("ok");
    }

    @PostMapping("/autoTransItem")
    @Operation(summary = "自动翻译")
    public CommonResult<I18nJsonDataRespVO> autoTransItem(@RequestBody I18nJsonDataUpdateReqVO updateReqVO) {
        I18nJsonDataDO i18nJsonData = I18nJsonDataConvert.INSTANCE.convert(updateReqVO);
        i18nDataService.autoTransItem(i18nJsonData);
        return success(I18nJsonDataConvert.INSTANCE.convert(i18nJsonData));
    }

    @RequestMapping(value={"/autoTrans"}, method={RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "自动翻译单词或句子")
    public CommonResult autoTrans(@RequestBody JSONObject bodyJson) {
        String originText = bodyJson.getStr("originText");
        String targetLang= bodyJson.getStr("targetLang");
        if(ObjectUtil.isEmpty(originText) || ObjectUtil.isEmpty(targetLang)){
            return error(599,"缺少必要参数");
        }
        JSONObject result = new JSONObject();
        Map<String, String> params = new HashMap<>();
        String translateText = TranslateUtil.translateText(originText, targetLang, params);
        if(ObjectUtil.isNotEmpty(translateText)){
            result.put(targetLang, translateText);
        };
        return success(result);
    }

    @GetMapping("/checkI18nItem")
    @Operation(summary = "检查国际化数据", description = "检查及更新菜单国际化数据")
    public CommonResult<I18nJsonDataRespVO> checkI18nItem(@RequestParam(value = "key")String key, @RequestParam(value = "label") String label) {
        I18nJsonDataDO i18nJsonData = new I18nJsonDataDO();
        i18nJsonData.setAlias(key);
        JSONObject json = i18nDataService.getI18nJsonByKey(key);
        if(json == null){
            json = new JSONObject();
            json.set("zh-CN",label);
            i18nJsonData.setJson(JSON.toJSONString(json));
            i18nDataService.autoTransItem(i18nJsonData);
        }else{
            i18nJsonData.setJson(JSON.toJSONString(json));
        }
        return success(I18nJsonDataConvert.INSTANCE.convert(i18nJsonData));
    }
}
