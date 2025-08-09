package org.openea.eap.module.system.api.translation;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.i18n.ai.AITranslationResult;
import org.openea.eap.module.system.api.translation.dto.*;
import org.openea.eap.module.system.api.translation.enums.TranslationSceneEnum;
import org.openea.eap.module.system.service.language.AITranslationFactory;
import org.openea.eap.module.system.service.language.AITranslationCacheService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 翻译服务API实现
 *
 * @author eap
 */
@Service
@Slf4j
public class TranslationApiImpl implements TranslationApi {

    @Resource
    private AITranslationFactory aiTranslationFactory;

    @Resource
    private AITranslationCacheService cacheService;

    @Override
    public TranslationResponseDTO translate(TranslationRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 参数验证
            validateTranslationRequest(request);
            
            // 构建上下文
            String context = buildContext(request);
            
            // 执行翻译
            AITranslationResult aiResult = aiTranslationFactory.translateWithFallback(
                request.getText(), request.getFromLang(), request.getToLang(), context);
            
            // 转换结果
            TranslationResponseDTO response = convertToResponse(aiResult, request);
            response.setDuration(System.currentTimeMillis() - startTime);
            
            // 后处理
            postProcessTranslation(response, request);
            
            return response;
            
        } catch (Exception e) {
            log.error("翻译失败: {}", request, e);
            return TranslationResponseDTO.failure(request.getText(), e.getMessage(), "TRANSLATION_ERROR");
        }
    }

    @Override
    public BatchTranslationResponseDTO batchTranslate(BatchTranslationRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 参数验证
            validateBatchTranslationRequest(request);
            
            Map<String, TranslationResponseDTO> results = new ConcurrentHashMap<>();
            
            if (Boolean.TRUE.equals(request.getParallel())) {
                // 并行处理
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                
                for (Map.Entry<String, String> entry : request.getTexts().entrySet()) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        TranslationRequestDTO singleRequest = TranslationRequestDTO.builder()
                                .text(entry.getValue())
                                .fromLang(request.getFromLang())
                                .toLang(request.getToLang())
                                .scene(request.getScene())
                                .context(request.getContext())
                                .maxLength(request.getMaxLength())
                                .enableCache(request.getEnableCache())
                                .qualityLevel(request.getQualityLevel())
                                .businessId(request.getBusinessId())
                                .build();
                        
                        TranslationResponseDTO result = translate(singleRequest);
                        results.put(entry.getKey(), result);
                    });
                    futures.add(future);
                }
                
                // 等待所有任务完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                
            } else {
                // 串行处理
                for (Map.Entry<String, String> entry : request.getTexts().entrySet()) {
                    TranslationRequestDTO singleRequest = TranslationRequestDTO.builder()
                            .text(entry.getValue())
                            .fromLang(request.getFromLang())
                            .toLang(request.getToLang())
                            .scene(request.getScene())
                            .context(request.getContext())
                            .maxLength(request.getMaxLength())
                            .enableCache(request.getEnableCache())
                            .qualityLevel(request.getQualityLevel())
                            .businessId(request.getBusinessId())
                            .build();
                    
                    TranslationResponseDTO result = translate(singleRequest);
                    results.put(entry.getKey(), result);
                }
            }
            
            BatchTranslationResponseDTO response = BatchTranslationResponseDTO.success(results);
            response.setTotalDuration(System.currentTimeMillis() - startTime);
            response.setBusinessId(request.getBusinessId());
            
            return response;
            
        } catch (Exception e) {
            log.error("批量翻译失败: {}", request, e);
            return BatchTranslationResponseDTO.failure(e.getMessage());
        }
    }

    @Override
    public TranslationResponseDTO translateMenu(String menuText, String fromLang, String toLang) {
        TranslationRequestDTO request = TranslationRequestDTO.builder()
                .text(menuText)
                .fromLang(fromLang)
                .toLang(toLang)
                .scene(TranslationSceneEnum.MENU.getCode())
                .maxLength(TranslationSceneEnum.MENU.getMaxLength())
                .qualityLevel(8) // 菜单翻译要求较高质量
                .build();
        
        return translate(request);
    }

    @Override
    public TranslationResponseDTO translateUI(String uiText, String fromLang, String toLang, Integer maxLength) {
        TranslationRequestDTO request = TranslationRequestDTO.builder()
                .text(uiText)
                .fromLang(fromLang)
                .toLang(toLang)
                .scene(TranslationSceneEnum.UI.getCode())
                .maxLength(maxLength != null ? maxLength : TranslationSceneEnum.UI.getMaxLength())
                .qualityLevel(7)
                .build();
        
        return translate(request);
    }

    @Override
    public TranslationResponseDTO translateWithContext(String text, String fromLang, String toLang, 
                                                     String context, String scene) {
        TranslationRequestDTO request = TranslationRequestDTO.builder()
                .text(text)
                .fromLang(fromLang)
                .toLang(toLang)
                .context(context)
                .scene(scene)
                .qualityLevel(6)
                .build();
        
        return translate(request);
    }

    @Override
    public TranslationResponseDTO reviewAndOptimize(String originalText, String translatedText, 
                                                  String fromLang, String toLang, String scene) {
        try {
            // 构建审核上下文
            String reviewContext = String.format(
                "请审核并优化以下翻译：\n原文：%s\n译文：%s\n场景：%s\n请提供更好的翻译版本。",
                originalText, translatedText, scene);
            
            TranslationRequestDTO request = TranslationRequestDTO.builder()
                    .text(reviewContext)
                    .fromLang(fromLang)
                    .toLang(toLang)
                    .scene("review")
                    .qualityLevel(9) // 审核要求最高质量
                    .enableCache(false) // 审核不使用缓存
                    .build();
            
            TranslationResponseDTO result = translate(request);
            
            // 评估翻译质量
            if (result.getSuccess()) {
                result.getSuggestions().add("已通过AI审核优化");
                result.setQualityScore(Math.min(10, result.getQualityScore() + 1));
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("翻译审核失败", e);
            return TranslationResponseDTO.failure(originalText, e.getMessage(), "REVIEW_ERROR");
        }
    }

    @Override
    public List<String> getSupportedLanguages() {
        // 返回系统支持的语言列表
        return Arrays.asList(
            "zh-CN", "zh-HK", "en-US", "ja-JP", "ko-KR", 
            "fr-FR", "de-DE", "es-ES", "pt-PT", "ru-RU",
            "ar-SA", "th-TH", "vi-VN"
        );
    }

    @Override
    public TranslationServiceStatus getServiceStatus() {
        try {
            // 获取提供商状态
            Map<String, Object> providersStatus = aiTranslationFactory.getProvidersStatus();
            
            // 获取缓存状态
            Map<String, Object> cacheStats = cacheService.getCacheStats(null, 1);
            
            // 构建服务状态
            return TranslationServiceStatus.builder()
                    .available(true)
                    .supportedLanguages(getSupportedLanguages())
                    .lastCheckTime(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("获取服务状态失败", e);
            return TranslationServiceStatus.builder()
                    .available(false)
                    .lastCheckTime(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 验证翻译请求
     */
    private void validateTranslationRequest(TranslationRequestDTO request) {
        if (StrUtil.isEmpty(request.getText())) {
            throw new IllegalArgumentException("翻译文本不能为空");
        }
        if (StrUtil.isEmpty(request.getFromLang()) || StrUtil.isEmpty(request.getToLang())) {
            throw new IllegalArgumentException("源语言和目标语言不能为空");
        }
        if (request.getText().length() > 5000) {
            throw new IllegalArgumentException("翻译文本长度不能超过5000字符");
        }
    }

    /**
     * 验证批量翻译请求
     */
    private void validateBatchTranslationRequest(BatchTranslationRequestDTO request) {
        if (request.getTexts() == null || request.getTexts().isEmpty()) {
            throw new IllegalArgumentException("批量翻译文本不能为空");
        }
        if (request.getTexts().size() > 100) {
            throw new IllegalArgumentException("批量翻译最多支持100条文本");
        }
    }

    /**
     * 构建翻译上下文
     */
    private String buildContext(TranslationRequestDTO request) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // 添加场景信息
        if (StrUtil.isNotEmpty(request.getScene())) {
            TranslationSceneEnum scene = TranslationSceneEnum.fromCode(request.getScene());
            contextBuilder.append("翻译场景：").append(scene.getDescription()).append("。");
            
            if (scene.needLengthLimit() && request.getMaxLength() != null) {
                contextBuilder.append("长度限制：").append(request.getMaxLength()).append("字符。");
            }
        }
        
        // 添加自定义上下文
        if (StrUtil.isNotEmpty(request.getContext())) {
            contextBuilder.append(request.getContext());
        }
        
        return contextBuilder.toString();
    }

    /**
     * 转换AI翻译结果为API响应
     */
    private TranslationResponseDTO convertToResponse(AITranslationResult aiResult, TranslationRequestDTO request) {
        TranslationResponseDTO.TranslationResponseDTOBuilder builder = TranslationResponseDTO.builder()
                .success(aiResult.getSuccess())
                .originalText(request.getText())
                .fromLang(request.getFromLang())
                .toLang(request.getToLang())
                .scene(request.getScene())
                .businessId(request.getBusinessId())
                .translateTime(LocalDateTime.now());
        
        if (aiResult.getSuccess()) {
            builder.translatedText(aiResult.getTranslatedText())
                   .qualityScore(aiResult.getQualityScore())
                   .confidence(aiResult.getConfidence())
                   .provider(aiResult.getProvider())
                   .model(aiResult.getModel())
                   .fromCache("cached_result".equals(aiResult.getMetadata()));
        } else {
            builder.errorMessage(aiResult.getErrorMessage())
                   .errorCode("AI_TRANSLATION_FAILED");
        }
        
        return builder.build();
    }

    /**
     * 翻译后处理
     */
    private void postProcessTranslation(TranslationResponseDTO response, TranslationRequestDTO request) {
        if (!response.getSuccess()) {
            return;
        }
        
        List<String> suggestions = new ArrayList<>();
        
        // 长度检查
        if (request.getMaxLength() != null && response.getTranslatedText().length() > request.getMaxLength()) {
            suggestions.add("翻译结果超出长度限制，建议优化");
            // 可以在这里实现自动截断或重新翻译的逻辑
        }
        
        // 质量检查
        if (response.getQualityScore() != null && response.getQualityScore() < request.getQualityLevel()) {
            suggestions.add("翻译质量未达到要求，建议人工审核");
        }
        
        response.setSuggestions(suggestions);
    }
}