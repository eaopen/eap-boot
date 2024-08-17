package org.openea.eap.module.system.framework.sms.core.client.dto;

import lombok.Data;
import org.openea.eap.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;

/**
 * 短信模板 Response DTO
 *
 */
@Data
public class SmsTemplateRespDTO {

    /**
     * 模板编号
     */
    private String id;
    /**
     * 短信内容
     */
    private String content;
    /**
     * 审核状态
     *
     * 枚举 {@link SmsTemplateAuditStatusEnum}
     */
    private Integer auditStatus;
    /**
     * 审核未通过的理由
     */
    private String auditReason;

}
