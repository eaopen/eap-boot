package org.openea.eap.module.message.api;

import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.module.message.api.dto.MessageSendReqDTO;
import org.openea.eap.module.message.api.dto.MessageSendRespDTO;
import org.openea.eap.module.message.service.MessageSendService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static org.openea.eap.framework.common.pojo.CommonResult.success;

@RestController
@Validated
public class MessageSendApiImpl implements MessageSendApi {

    @Resource
    private MessageSendService messageSendService;

    @Override
    public CommonResult<MessageSendRespDTO> send(MessageSendReqDTO reqDTO) {
        return success(messageSendService.submit(reqDTO));
    }
}
