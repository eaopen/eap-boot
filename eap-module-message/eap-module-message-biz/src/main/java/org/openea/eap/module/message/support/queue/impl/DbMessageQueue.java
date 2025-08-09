package org.openea.eap.module.message.support.queue.impl;

import lombok.extern.slf4j.Slf4j;
import org.openea.eap.module.message.dal.dataobject.MessageDO;
import org.openea.eap.module.message.dal.dataobject.MessageTaskDO;
import org.openea.eap.module.message.dal.mysql.MessageMapper;
import org.openea.eap.module.message.dal.mysql.MessageTaskMapper;
import org.openea.eap.module.message.support.queue.MessageQueue;
import org.openea.eap.module.message.support.queue.MessageTask;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "message.queue", name = "type", havingValue = "db", matchIfMissing = true)
@Slf4j
public class DbMessageQueue implements MessageQueue {

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private MessageTaskMapper messageTaskMapper;

    @Override
    public MessageQueue.EnqueueResult enqueue(MessageTask task) {
        // 简化持久化：先写消息主记录，再写单通道任务（若未指定通道，默认 INBOX）
        // 幂等：若传入 dedupKey，尝试查询已有消息直接返回
        if (task.getDedupKey() != null) {
            MessageDO existed = messageMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MessageDO>()
                    .eq(MessageDO::getDedupKey, task.getDedupKey()));
            if (existed != null) {
                return new MessageQueue.EnqueueResult(existed.getId(), true);
            }
        }
        MessageDO message = MessageDO.builder()
                .sceneCode(task.getSceneCode())
                .userId(task.getUserId()).userType(task.getUserType())
                .email(task.getEmail()).mobile(task.getMobile()).wechatOpenId(task.getWechatOpenId())
                .variablesJson(task.getVariablesJson())
                .overrideChannelsJson(null)
                .priority(task.getPriority())
                .scheduleAt(task.getScheduleAt() == null ? null : task.getScheduleAt().toLocalDateTime())
                .dedupKey(task.getDedupKey())
                .state(0)
                .build();
        messageMapper.insert(message);
        task.setMessageId(message.getId());

        java.util.Set<String> channels = (task.getOverrideChannels() == null || task.getOverrideChannels().isEmpty())
                ? java.util.Set.of("EMAIL") : task.getOverrideChannels();
        for (String channel : channels) {
            MessageTaskDO taskDO = MessageTaskDO.builder()
                    .messageId(message.getId())
                    .channel(channel)
                    .status(0)
                    .retryCount(0)
                    .nextRetryAt(message.getScheduleAt())
                    .maxAttempts(task.getMaxAttempts() == null ? 5 : task.getMaxAttempts())
                    .backoffStrategy(task.getBackoffStrategy() == null ? "EXPONENTIAL" : task.getBackoffStrategy())
                    .backoffBaseSeconds(task.getBackoffBaseSeconds() == null ? 60 : task.getBackoffBaseSeconds())
                    .priority(task.getPriority())
                    .scheduleAt(message.getScheduleAt())
                    .build();
            messageTaskMapper.insert(taskDO);
            log.info("[enqueue][DB queue accepted task messageId={}, channel={}, priority={}]", message.getId(), channel, task.getPriority());
        }
        return new MessageQueue.EnqueueResult(message.getId(), false);
    }
}
