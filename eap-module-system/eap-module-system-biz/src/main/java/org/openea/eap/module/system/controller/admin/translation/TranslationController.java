package org.openea.eap.module.system.controller.admin.translation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.module.system.api.translation.TranslationApi;
import org.openea.eap.module.system.api.translation.TranslationServiceStatus;
import org.openea.eap.module.system.api.translation.dto.*;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static org.openea.eap.framework.common.pojo.CommonResult.success;

/**
 * 翻译服务控制器
 * 提供统一的翻译API接口
 *
 * @author eap
 */
@Tag(name = "管理后台 - 翻译服务")
@RestController
@RequestMapping("/system/translation")
@Slf4j
public class TranslationController {

    @Resource
    private TranslationApi translationApi;

    @PostMapping("/translate")
    @Operation(summary = "单个文本翻译")
    public CommonResult<TranslationResponseDTO> translate(@Valid @RequestBody TranslationRequestDTO request) {
        TranslationResponseDTO result = translationApi.translate(request);
        return success(result);
    }

    @PostMapping("/batch-translate")
    @Operation(summary = "批量文本翻译")
    public CommonResult<BatchTranslationResponseDTO> batchTranslate(@Valid @RequestBody BatchTranslationRequestDTO request) {
        BatchTranslationResponseDTO result = translationApi.batchTranslate(request);
        return success(result);
    }

    @GetMapping("/translate-menu")
    @Operation(summary = "菜单翻译")
    public CommonResult<TranslationResponseDTO> translateMenu(
            @Parameter(description = "菜单文本") @RequestParam String menuText,
            @Parameter(description = "源语言") @RequestParam(defaultValue = "zh-CN") String fromLang,
            @Parameter(description = "目标语言") @RequestParam(defaultValue = "en-US") String toLang) {
        
        TranslationResponseDTO result = translationApi.translateMenu(menuText, fromLang, toLang);
        return success(result);
    }

    @GetMapping("/translate-ui")
    @Operation(summary = "UI词条翻译")
    public CommonResult<TranslationResponseDTO> translateUI(
            @Parameter(description = "UI文本") @RequestParam String uiText,
            @Parameter(description = "源语言") @RequestParam(defaultValue = "zh-CN") String fromLang,
            @Parameter(description = "目标语言") @RequestParam(defaultValue = "en-US") String toLang,
            @Parameter(description = "最大长度限制") @RequestParam(required = false) Integer maxLength) {
        
        TranslationResponseDTO result = translationApi.translateUI(uiText, fromLang, toLang, maxLength);
        return success(result);
    }

    @GetMapping("/translate-with-context")
    @Operation(summary = "上下文感知翻译")
    public CommonResult<TranslationResponseDTO> translateWithContext(
            @Parameter(description = "待翻译文本") @RequestParam String text,
            @Parameter(description = "源语言") @RequestParam(defaultValue = "zh-CN") String fromLang,
            @Parameter(description = "目标语言") @RequestParam(defaultValue = "en-US") String toLang,
            @Parameter(description = "上下文信息") @RequestParam(required = false) String context,
            @Parameter(description = "显示场景") @RequestParam(required = false) String scene) {
        
        TranslationResponseDTO result = translationApi.translateWithContext(text, fromLang, toLang, context, scene);
        return success(result);
    }

    @PostMapping("/review-and-optimize")
    @Operation(summary = "翻译质量检查和优化")
    public CommonResult<TranslationResponseDTO> reviewAndOptimize(
            @Parameter(description = "原文") @RequestParam String originalText,
            @Parameter(description = "译文") @RequestParam String translatedText,
            @Parameter(description = "源语言") @RequestParam(defaultValue = "zh-CN") String fromLang,
            @Parameter(description = "目标语言") @RequestParam(defaultValue = "en-US") String toLang,
            @Parameter(description = "应用场景") @RequestParam(required = false) String scene) {
        
        TranslationResponseDTO result = translationApi.reviewAndOptimize(originalText, translatedText, fromLang, toLang, scene);
        return success(result);
    }

    @GetMapping("/supported-languages")
    @Operation(summary = "获取支持的语言列表")
    public CommonResult<List<String>> getSupportedLanguages() {
        List<String> languages = translationApi.getSupportedLanguages();
        return success(languages);
    }

    @GetMapping("/service-status")
    @Operation(summary = "获取翻译服务状态")
    public CommonResult<TranslationServiceStatus> getServiceStatus() {
        TranslationServiceStatus status = translationApi.getServiceStatus();
        return success(status);
    }

    @PostMapping("/quick-translate")
    @Operation(summary = "快速翻译（简化接口）")
    public CommonResult<String> quickTranslate(
            @Parameter(description = "待翻译文本") @RequestParam String text,
            @Parameter(description = "源语言") @RequestParam(defaultValue = "zh-CN") String fromLang,
            @Parameter(description = "目标语言") @RequestParam(defaultValue = "en-US") String toLang) {
        
        TranslationRequestDTO request = TranslationRequestDTO.builder()
                .text(text)
                .fromLang(fromLang)
                .toLang(toLang)
                .build();
        
        TranslationResponseDTO result = translationApi.translate(request);
        
        if (result.getSuccess()) {
            return success(result.getTranslatedText());
        } else {
            return CommonResult.error(500, result.getErrorMessage());
        }
    }
}