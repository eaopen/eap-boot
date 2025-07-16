package org.openea.eap.server.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.util.servlet.ServletUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.openea.eap.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;

/**
 * 默认 Controller，解决部分 module 未开启时的 404 提示。
 * 例如说，/bpm/** 路径，工作流
 *
 */
@RestController
@Slf4j
public class DefaultController {

//    @RequestMapping("/admin-api/bpm/**")
//    public CommonResult<Boolean> bpm404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[工作流模块 eap-module-bpm - 已禁用]");
//    }

//    @RequestMapping(value = {"/admin-api/product/**", // 商品中心
//            "/admin-api/trade/**", // 交易中心
//            "/admin-api/promotion/**"})  // 营销中心
//    public CommonResult<Boolean> mall404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[商城系统 eap-module-mall - 已禁用]");
//    }

//    @RequestMapping(value = {"/admin-api/ai/**"})
//    public CommonResult<Boolean> ai404() {
//        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
//                "[AI 大模型 eap-module-ai - 已禁用]");
//    }

    /**
     * 测试接口：打印 query、header、body
     */
    @RequestMapping(value = { "/test" })
    @PermitAll
    public CommonResult<Boolean> test(HttpServletRequest request) {
        // 打印查询参数
        log.info("Query: {}", ServletUtils.getParamMap(request));
        // 打印请求头
        log.info("Header: {}", ServletUtils.getHeaderMap(request));
        // 打印请求体
        log.info("Body: {}", ServletUtils.getBody(request));
        return CommonResult.success(true);
    }
}
