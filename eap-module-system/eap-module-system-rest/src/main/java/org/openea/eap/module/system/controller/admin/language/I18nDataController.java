package org.openea.eap.module.system.controller.admin.language;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.i18n.core.I18nUtil;
import org.openea.eap.module.system.service.language.I18nDataService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

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
}
