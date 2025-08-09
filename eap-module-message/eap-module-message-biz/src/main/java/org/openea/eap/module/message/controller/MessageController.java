package org.openea.eap.module.message.controller;

import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.module.message.api.dto.MessageSendReqDTO;
import org.openea.eap.module.message.api.dto.MessageSendRespDTO;
import org.openea.eap.module.message.service.MessageSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.openea.eap.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/admin-api/message")
@Tag(name = "管理后台 - 消息发送")
public class MessageController {

    @Resource
    private MessageSendService messageSendService;

    @PostMapping("/send")
    @Operation(summary = "发送消息：多通道/优先级/定时")
    public CommonResult<MessageSendRespDTO> send(@Valid @RequestBody MessageSendReqDTO reqDTO) {
        return success(messageSendService.submit(reqDTO));
    }
}
