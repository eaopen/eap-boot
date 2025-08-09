package org.openea.eap.module.message.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Schema(description = "RPC 服务 - 消息发送请求")
public class MessageSendReqDTO {

    @Schema(description = "场景编码", example = "ORDER_PAID")
    @NotEmpty(message = "sceneCode 不能为空")
    private String sceneCode;

    @Schema(description = "接收人用户编号", example = "1001")
    private Long userId;

    @Schema(description = "接收人用户类型", example = "1")
    private Integer userType;

    @Schema(description = "直发目标：邮箱")
    private String email;

    @Schema(description = "直发目标：手机号")
    private String mobile;

    @Schema(description = "直发目标：微信 OpenId / UnionId / openid")
    private String wechatOpenId;

    @Schema(description = "模板变量")
    @NotNull(message = "variables 不能为空")
    private Map<String, Object> variables;

    @Schema(description = "强制通道覆盖，不填按路由策略", example = "[\"SMS\",\"INBOX\"]")
    private Set<String> overrideChannels;

    @Schema(description = "优先级，数值越大优先级越高", example = "5")
    private Integer priority;

    @Schema(description = "预定发送时间，留空表示尽快发送")
    private OffsetDateTime scheduleAt;

    @Schema(description = "幂等键，重复提交直接返回历史结果")
    private String dedupKey;

    @Schema(description = "最大重试次数", example = "5")
    private Integer maxAttempts;

    @Schema(description = "退避策略：EXPONENTIAL 或 FIXED", example = "EXPONENTIAL")
    private String backoffStrategy;

    @Schema(description = "退避基础秒数", example = "60")
    private Integer backoffBaseSeconds;
}
