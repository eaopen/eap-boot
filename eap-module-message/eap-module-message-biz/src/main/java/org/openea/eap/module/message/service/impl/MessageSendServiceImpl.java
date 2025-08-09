package org.openea.eap.module.message.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openea.eap.module.message.api.dto.MessageSendReqDTO;
import org.openea.eap.module.message.api.dto.MessageSendRespDTO;
import org.openea.eap.module.message.service.MessageSendService;
import org.openea.eap.module.message.support.queue.MessageQueue;
import org.openea.eap.module.message.support.queue.MessageTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Slf4j
public class MessageSendServiceImpl implements MessageSendService {

    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public MessageSendRespDTO submit(MessageSendReqDTO reqDTO) {
        // 简化：仅做必要校验与任务入队
        MessageTask task = MessageTask.from(reqDTO);
        try {
            if (reqDTO.getVariables() != null) {
                task.setVariablesJson(objectMapper.writeValueAsString(reqDTO.getVariables()));
            }
        } catch (JsonProcessingException e) {
            log.warn("[submit][variables serialize failed]", e);
            task.setVariablesJson("{}");
        }
        MessageQueue.EnqueueResult result = messageQueue.enqueue(task);
        MessageSendRespDTO resp = new MessageSendRespDTO();
        resp.setMessageId(result.getMessageId());
        resp.setStatus(result.isDedupUsed() ? "DEDUP_REUSED" : "ACCEPTED");
        return resp;
    }
}
