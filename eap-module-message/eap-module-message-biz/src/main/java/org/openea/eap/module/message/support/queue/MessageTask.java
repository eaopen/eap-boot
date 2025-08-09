package org.openea.eap.module.message.support.queue;

import lombok.Data;
import org.openea.eap.module.message.api.dto.MessageSendReqDTO;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
public class MessageTask {
    private Long messageId; // 简化：由 DB 实现生成；这里先占位
    private String sceneCode;
    private Long userId;
    private Integer userType;
    private String email;
    private String mobile;
    private String wechatOpenId;
    private String variablesJson;
    private Set<String> overrideChannels;
    private Integer priority;
    private OffsetDateTime scheduleAt;
    private String dedupKey;
    private Integer maxAttempts;
    private String backoffStrategy;
    private Integer backoffBaseSeconds;

    public static MessageTask from(MessageSendReqDTO dto) {
        MessageTask t = new MessageTask();
        t.setSceneCode(dto.getSceneCode());
        t.setUserId(dto.getUserId());
        t.setUserType(dto.getUserType());
        t.setEmail(dto.getEmail());
        t.setMobile(dto.getMobile());
        t.setWechatOpenId(dto.getWechatOpenId());
        t.setVariablesJson("{}"); // 占位，真实实现将 Map 序列化
        t.setOverrideChannels(dto.getOverrideChannels());
        t.setPriority(dto.getPriority() == null ? 0 : dto.getPriority());
        t.setScheduleAt(dto.getScheduleAt());
        t.setDedupKey(dto.getDedupKey());
        t.setMaxAttempts(dto.getMaxAttempts());
        t.setBackoffStrategy(dto.getBackoffStrategy());
        t.setBackoffBaseSeconds(dto.getBackoffBaseSeconds());
        return t;
    }
}
