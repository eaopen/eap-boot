package org.openea.eap.module.message.channel;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.common.core.KeyValue;
import org.openea.eap.framework.common.enums.CommonStatusEnum;
import org.openea.eap.module.message.dal.dataobject.MessageTaskDO;
import org.openea.eap.module.message.dal.mysql.MessageTaskMapper;
import org.openea.eap.module.system.dal.dataobject.sms.SmsChannelDO;
import org.openea.eap.module.system.dal.dataobject.sms.SmsTemplateDO;
import org.openea.eap.module.system.service.sms.SmsChannelService;
import org.openea.eap.module.system.service.sms.SmsLogService;
import org.openea.eap.module.system.service.sms.SmsSendService;
import org.openea.eap.module.system.service.sms.SmsTemplateService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SmsChannelAdapter {

    @Resource
    private SmsChannelService smsChannelService;
    @Resource
    private SmsTemplateService smsTemplateService;
    @Resource
    private SmsLogService smsLogService;
    @Resource
    private SmsSendService smsSendService;
    @Resource
    private MessageTaskMapper messageTaskMapper;

    public void deliver(MessageTaskDO task, String sceneCode, String mobile, Long userId, Integer userType, Map<String, Object> variables) {
        SmsTemplateDO template = smsTemplateService.getSmsTemplateByCodeFromCache(sceneCode);
        if (template == null) {
            fail(task, "SMS_TEMPLATE_NOT_EXISTS", "短信模板不存在");
            return;
        }
        SmsChannelDO channel = smsChannelService.getSmsChannel(template.getChannelId());
        if (channel == null) {
            fail(task, "SMS_CHANNEL_NOT_EXISTS", "短信渠道不存在");
            return;
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus()) ||
                !CommonStatusEnum.ENABLE.getStatus().equals(channel.getStatus())) {
            messageTaskMapper.updateById(MessageTaskDO.builder().id(task.getId()).status(2).build());
            return;
        }
        if (mobile == null || mobile.isEmpty()) {
            fail(task, "SMS_SEND_MOBILE_NOT_EXISTS", "手机号为空");
            return;
        }
        List<KeyValue<String, Object>> params = template.getParams().stream()
                .map(k -> new KeyValue<String, Object>(k, variables.get(k))).collect(Collectors.toList());
        String content = smsTemplateService.formatSmsTemplateContent(template.getContent(), variables);
        Long logId = smsLogService.createSmsLog(mobile, userId, userType, true, template, content, variables);
        // 复用现有发送（同步），异常向外抛出以便调度器处理重试
        smsSendService.doSendSms(new org.openea.eap.module.system.mq.message.sms.SmsSendMessage()
                .setLogId(logId).setMobile(mobile).setChannelId(template.getChannelId())
                .setApiTemplateId(template.getApiTemplateId()).setTemplateParams(params));
        messageTaskMapper.updateById(MessageTaskDO.builder().id(task.getId()).status(2).build());
    }

    private void fail(MessageTaskDO task, String code, String msg) {
        messageTaskMapper.updateById(MessageTaskDO.builder().id(task.getId()).status(3).errorCode(code).errorMsg(msg).build());
    }
}
