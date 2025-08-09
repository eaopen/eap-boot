package org.openea.eap.module.message.api;

import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.module.message.api.dto.MessageSendReqDTO;
import org.openea.eap.module.message.api.dto.MessageSendRespDTO;
import org.openea.eap.module.message.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 消息发送")
public interface MessageSendApi {

    String PREFIX = ApiConstants.PREFIX;

    @PostMapping(PREFIX + "/send")
    @Operation(summary = "发送消息：多通道/优先级/定时")
    CommonResult<MessageSendRespDTO> send(@Valid @RequestBody MessageSendReqDTO reqDTO);
}
