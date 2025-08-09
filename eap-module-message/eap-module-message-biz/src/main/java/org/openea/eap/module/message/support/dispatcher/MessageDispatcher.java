package org.openea.eap.module.message.support.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.openea.eap.module.message.channel.EmailChannelAdapter;
import org.openea.eap.module.message.channel.SmsChannelAdapter;
import org.openea.eap.module.message.channel.InboxChannelAdapter;
import org.openea.eap.module.message.dal.dataobject.MessageDO;
import org.openea.eap.module.message.dal.dataobject.MessageTaskDO;
import org.openea.eap.module.message.dal.mysql.MessageMapper;
import org.openea.eap.module.message.dal.mysql.MessageTaskMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务调度：从队列/DB 中提取到期任务并派发到通道适配器
 * 初版：占位，后续补充 DB 查询与通道调用
 */
@Component
@Slf4j
public class MessageDispatcher {

    @Resource
    private MessageTaskMapper messageTaskMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private EmailChannelAdapter emailChannelAdapter;
    @Resource
    private SmsChannelAdapter smsChannelAdapter;
    @Resource
    private InboxChannelAdapter inboxChannelAdapter;

    @Scheduled(fixedDelay = 3000L, initialDelay = 5000L)
    public void dispatch() {
        try {
            // 拉取最多 20 条 READY 且到期的任务（nextRetryAt 到期，或 nextRetryAt 为空且 scheduleAt 到期/为空），按优先级倒序
            LocalDateTime now = LocalDateTime.now();
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MessageTaskDO> query =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MessageTaskDO>()
                            .eq(MessageTaskDO::getStatus, 0)
                            .and(w -> w.le(MessageTaskDO::getNextRetryAt, now)
                                    .or(w2 -> w2.isNull(MessageTaskDO::getNextRetryAt)
                                            .and(w3 -> w3.isNull(MessageTaskDO::getScheduleAt)
                                                    .or().le(MessageTaskDO::getScheduleAt, now))))
                            .orderByDesc(MessageTaskDO::getPriority)
                            .last("limit 20");
            List<MessageTaskDO> tasks = messageTaskMapper.selectList(query);
            for (MessageTaskDO task : tasks) {
                // 尝试占用（CAS）：仅当仍为 READY 时置为 PROCESSING，避免并发重复发送
                boolean locked = messageTaskMapper.tryLockReady(task.getId());
                if (!locked) continue;
                MessageDO message = messageMapper.selectById(task.getMessageId());
                if (message == null) {
                    messageTaskMapper.updateById(MessageTaskDO.builder().id(task.getId()).status(3).errorCode("NOT_FOUND").errorMsg("message missing").build());
                    continue;
                }
                try {
                    switch (task.getChannel()) {
                        case "EMAIL" -> {
                            emailChannelAdapter.deliver(task, message.getSceneCode(), message.getUserId(), message.getUserType(), message.getEmail(), parseVariables(message.getVariablesJson()));
                        }
                        case "SMS" -> {
                            smsChannelAdapter.deliver(task, message.getSceneCode(), message.getMobile(), message.getUserId(), message.getUserType(), parseVariables(message.getVariablesJson()));
                        }
                        case "INBOX" -> {
                            inboxChannelAdapter.deliver(task, message.getSceneCode(), message.getUserId(), message.getUserType(), parseVariables(message.getVariablesJson()));
                        }
                        default -> handleFailure(task, "UNSUPPORTED_CHANNEL", task.getChannel());
                    }
                } catch (Throwable ex) {
                    handleFailure(task, "EXCEPTION", ex.getMessage());
                }
                // 若该消息已无进行中/待执行任务，则根据是否有成功任务来更新主消息状态
                long remaining = messageTaskMapper.selectRemainingCount(message.getId());
                if (remaining == 0) {
                    long successCnt = messageTaskMapper.selectSuccessCount(message.getId());
                    messageMapper.markState(message.getId(), successCnt > 0 ? 1 : 2); // DONE or FAILED
                }
            }
        } catch (Throwable ex) {
            log.warn("[dispatch][error]", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> parseVariables(String json) {
        try {
            if (json == null) return java.util.Collections.emptyMap();
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, java.util.Map.class);
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }

    private void handleFailure(MessageTaskDO task, String code, String msg) {
        int attemptsSoFar = (task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1; // 包含本次
        int max = task.getMaxAttempts() == null ? 5 : task.getMaxAttempts();
        if (attemptsSoFar >= max) {
            messageTaskMapper.updateById(MessageTaskDO.builder()
                    .id(task.getId()).status(3).retryCount(attemptsSoFar).errorCode(code).errorMsg(msg).build());
            return;
        }
        int base = task.getBackoffBaseSeconds() == null ? 60 : task.getBackoffBaseSeconds();
        String strategy = task.getBackoffStrategy() == null ? "EXPONENTIAL" : task.getBackoffStrategy();
        long delaySec = "FIXED".equalsIgnoreCase(strategy) ? base : (long) (base * Math.pow(2, attemptsSoFar - 1));
        LocalDateTime next = LocalDateTime.now().plusSeconds(delaySec);
        messageTaskMapper.updateById(MessageTaskDO.builder()
                .id(task.getId()).status(0).retryCount(attemptsSoFar).nextRetryAt(next)
                .errorCode(code).errorMsg(msg).build());
    }
}
