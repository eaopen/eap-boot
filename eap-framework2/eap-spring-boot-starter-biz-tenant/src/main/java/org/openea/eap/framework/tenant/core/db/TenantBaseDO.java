package org.openea.eap.framework.tenant.core.db;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openea.eap.framework.mybatis.core.dataobject.BaseDO;

/**
 * 拓展多租户的 BaseDO 基类
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TenantBaseDO extends BaseDO {

    /**
     * 多租户编号
     */
    private Long tenantId;

}
