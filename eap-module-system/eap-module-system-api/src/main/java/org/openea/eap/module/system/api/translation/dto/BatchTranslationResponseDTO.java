package org.openea.eap.module.system.api.translation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 批量翻译响应DTO
 *
 * @author eap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTranslationResponseDTO {

    /**
     * 批量翻译是否整体成功
     */
    private Boolean success;

    /**
     * 翻译结果映射
     * key: 文本标识符
     * value: 翻译结果
     */
    private Map<String, TranslationResponseDTO> results;

    /**
     * 成功翻译数量
     */
    private Integer successCount;

    /**
     * 失败翻译数量
     */
    private Integer failureCount;

    /**
     * 总翻译数量
     */
    private Integer totalCount;

    /**
     * 缓存命中数量
     */
    private Integer cacheHitCount;

    /**
     * 总耗时（毫秒）
     */
    private Long totalDuration;

    /**
     * 平均质量评分
     */
    private Double averageQuality;

    /**
     * 翻译时间
     */
    private LocalDateTime translateTime;

    /**
     * 业务标识
     */
    private String businessId;

    /**
     * 整体错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 创建成功的批量翻译响应
     */
    public static BatchTranslationResponseDTO success(Map<String, TranslationResponseDTO> results) {
        int successCount = 0;
        int failureCount = 0;
        int cacheHitCount = 0;
        long totalDuration = 0;
        double totalQuality = 0;
        int qualityCount = 0;

        for (TranslationResponseDTO result : results.values()) {
            if (Boolean.TRUE.equals(result.getSuccess())) {
                successCount++;
                if (Boolean.TRUE.equals(result.getFromCache())) {
                    cacheHitCount++;
                }
                if (result.getQualityScore() != null) {
                    totalQuality += result.getQualityScore();
                    qualityCount++;
                }
            } else {
                failureCount++;
            }
            if (result.getDuration() != null) {
                totalDuration += result.getDuration();
            }
        }

        return BatchTranslationResponseDTO.builder()
                .success(true)
                .results(results)
                .successCount(successCount)
                .failureCount(failureCount)
                .totalCount(results.size())
                .cacheHitCount(cacheHitCount)
                .totalDuration(totalDuration)
                .averageQuality(qualityCount > 0 ? totalQuality / qualityCount : null)
                .translateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败的批量翻译响应
     */
    public static BatchTranslationResponseDTO failure(String errorMessage) {
        return BatchTranslationResponseDTO.builder()
                .success(false)
                .errorMessage(errorMessage)
                .translateTime(LocalDateTime.now())
                .build();
    }
}