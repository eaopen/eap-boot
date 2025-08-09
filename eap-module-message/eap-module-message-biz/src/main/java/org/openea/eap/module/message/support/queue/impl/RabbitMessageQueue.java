package org.openea.eap.module.message.support.queue.impl;

import lombok.extern.slf4j.Slf4j;
import org.openea.eap.module.message.support.queue.MessageQueue;
import org.openea.eap.module.message.support.queue.MessageTask;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "message.queue", name = "type", havingValue = "rabbitmq")
@Slf4j
public class RabbitMessageQueue implements MessageQueue {
    @Override
    public EnqueueResult enqueue(MessageTask task) {
        // 将任务投递至 RabbitMQ（后续补全交换机/路由键/延迟队列）
        log.info("[enqueue][RabbitMQ accepted task scene={}, priority={}]", task.getSceneCode(), task.getPriority());
        return new MessageQueue.EnqueueResult(task.getMessageId(), false);
    }
}
