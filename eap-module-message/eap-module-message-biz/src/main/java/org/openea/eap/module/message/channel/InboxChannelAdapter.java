package org.openea.eap.module.message.channel;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.module.message.dal.dataobject.MessageTaskDO;
import org.openea.eap.module.message.dal.mysql.MessageTaskMapper;
import org.openea.eap.module.system.dal.dataobject.notify.NotifyTemplateDO;
import org.openea.eap.module.system.service.notify.NotifyMessageService;
import org.openea.eap.module.system.service.notify.NotifyTemplateService;
import org.openea.eap.module.infra.api.websocket.WebSocketSenderApi;
import org.openea.eap.framework.common.enums.UserTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class InboxChannelAdapter {

    @Resource
    private NotifyTemplateService notifyTemplateService;
    @Resource
    private NotifyMessageService notifyMessageService;
    @Resource
    private MessageTaskMapper messageTaskMapper;
    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    public void deliver(MessageTaskDO task, String sceneCode, Long userId, Integer userType, Map<String, Object> variables) {
        NotifyTemplateDO template = notifyTemplateService.getNotifyTemplateByCodeFromCache(sceneCode);
        if (template == null) {
            fail(task, "NOTIFY_TEMPLATE_NOT_EXISTS", "站内信模板不存在");
            return;
        }
        String content = notifyTemplateService.formatNotifyTemplateContent(template.getContent(), variables);
        Long id = notifyMessageService.createNotifyMessage(userId, userType, template, content, variables);
        // 可选联动：通过 WebSocket 做实时提醒，不影响主流程
        try {
            webSocketSenderApi.sendObject(userType, userId, "notify-message", java.util.Map.of(
                    "id", id,
                    "title", template.getName(),
                    "content", content
            ));
        } catch (Throwable ignored) {
        }
        messageTaskMapper.updateById(MessageTaskDO.builder().id(task.getId()).status(2).build());
    }

    private void fail(MessageTaskDO task, String code, String msg) {
        messageTaskMapper.updateById(MessageTaskDO.builder().id(task.getId()).status(3).errorCode(code).errorMsg(msg).build());
    }
}
