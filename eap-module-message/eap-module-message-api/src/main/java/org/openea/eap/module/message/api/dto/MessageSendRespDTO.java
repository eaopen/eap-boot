package org.openea.eap.module.message.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "RPC 服务 - 消息发送响应")
public class MessageSendRespDTO {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "状态：ACCEPTED/DEDUP_REUSED")
    private String status;
}
