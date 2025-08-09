package org.openea.eap.module.message.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

import java.time.LocalDateTime;

/**
 * 消息主记录
 */
@TableName("msg_message")
@KeySequence("msg_message_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDO extends BaseDO {
    private Long id;
    private Long tenantId;

    private String sceneCode;

    private Long userId;
    private Integer userType;

    private String email;
    private String mobile;
    private String wechatOpenId;

    private String variablesJson;
    private String overrideChannelsJson;

    private Integer priority;
    private LocalDateTime scheduleAt;

    private String dedupKey;

    private Integer state; // 0: PENDING, 1: DONE, 2: FAILED
}
