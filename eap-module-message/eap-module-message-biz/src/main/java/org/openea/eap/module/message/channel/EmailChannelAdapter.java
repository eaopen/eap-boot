package org.openea.eap.module.message.channel;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.common.enums.CommonStatusEnum;
import org.openea.eap.module.message.dal.dataobject.MessageTaskDO;
import org.openea.eap.module.message.dal.mysql.MessageTaskMapper;
import org.openea.eap.module.system.dal.dataobject.mail.MailAccountDO;
import org.openea.eap.module.system.dal.dataobject.mail.MailTemplateDO;
import org.openea.eap.module.system.service.mail.*;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 邮件通道适配器：复用 system 模块既有邮件服务
 */
@Component
@Slf4j
public class EmailChannelAdapter {

    @Resource
    private MailAccountService mailAccountService;
    @Resource
    private MailTemplateService mailTemplateService;
    @Resource
    private MailLogService mailLogService;
    @Resource
    private MailSendService mailSendService;
    @Resource
    private MessageTaskMapper messageTaskMapper;

    public void deliver(MessageTaskDO task, String sceneCode, Long userId, Integer userType,
                        String email, Map<String, Object> variables) {
        // 1) 校验模板与账号
        MailTemplateDO template = mailTemplateService.getMailTemplateByCodeFromCache(sceneCode);
        if (template == null) {
            fail(task, "MAIL_TEMPLATE_NOT_EXISTS", "模板不存在");
            return;
        }
        Long accountId = template.getAccountId();
        MailAccountDO account = mailAccountService.getMailAccountFromCache(accountId);
        if (account == null) {
            fail(task, "MAIL_ACCOUNT_NOT_EXISTS", "邮箱账号不存在");
            return;
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus())) {
            // 模板禁用，直接完成（忽略发送）
            messageTaskMapper.updateById(MessageTaskDO.builder().id(task.getId()).status(2).build());
            return;
        }
        // 2) 校验 email
        if (StrUtil.isEmpty(email)) {
            fail(task, "MAIL_SEND_MAIL_NOT_EXISTS", "收件邮箱为空");
            return;
        }
        // 3) 生成内容并写日志
        String title = mailTemplateService.formatMailTemplateContent(template.getTitle(), variables);
        String content = mailTemplateService.formatMailTemplateContent(template.getContent(), variables);
        Long logId = mailLogService.createMailLog(userId, userType, email, account, template, content, variables, true);
        // 4) 直接调用现有发送实现（同步）
        // 直接复用 MailSendServiceImpl 的 doSendMail 的逻辑（其内部已更新 mail_log 结果），此处不再 try/catch 为重试保留异常
        mailSendService.doSendMail(new org.openea.eap.module.system.mq.message.mail.MailSendMessage()
                .setLogId(logId)
                .setMail(email)
                .setAccountId(account.getId())
                .setNickname(template.getNickname())
                .setTitle(title)
                .setContent(content));
        messageTaskMapper.updateById(MessageTaskDO.builder().id(task.getId()).status(2).build());
    }

    private void fail(MessageTaskDO task, String code, String msg) {
        messageTaskMapper.updateById(MessageTaskDO.builder()
                .id(task.getId()).status(3).errorCode(code).errorMsg(msg).build());
    }
}
