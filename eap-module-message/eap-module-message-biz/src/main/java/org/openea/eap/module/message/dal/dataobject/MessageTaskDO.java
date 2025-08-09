package org.openea.eap.module.message.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

import java.time.LocalDateTime;

/**
 * 消息任务（用于 DB 队列）
 */
@TableName("msg_task")
@KeySequence("msg_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTaskDO extends BaseDO {
    private Long id;
    private Long messageId;
    private String channel; // EMAIL/SMS/INBOX/WEBSOCKET/WECHAT_MINI

    private Integer status; // 0 READY, 1 PROCESSING, 2 DONE, 3 FAILED

    private Integer retryCount;
    private LocalDateTime nextRetryAt;

    private Integer maxAttempts; // 最大重试次数，含首次（例如 5 次：首发+4次重试）
    private String backoffStrategy; // EXPONENTIAL / FIXED
    private Integer backoffBaseSeconds; // 退避基数秒

    private Integer priority;
    private LocalDateTime scheduleAt;

    private String errorCode;
    private String errorMsg;

    private String providerRequestId;
    private String providerSerialNo;
}
