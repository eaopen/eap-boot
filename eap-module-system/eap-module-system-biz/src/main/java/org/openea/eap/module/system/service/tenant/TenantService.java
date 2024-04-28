package org.openea.eap.module.system.service.tenant;

import org.openea.eap.framework.tenant.core.context.TenantContextHolder;
import org.openea.eap.module.system.service.tenant.handler.TenantInfoHandler;
import org.openea.eap.module.system.service.tenant.handler.TenantMenuHandler;


/**
 * 租户 Service 接口
 *
 */
public interface TenantService {



    /**
     * 进行租户的信息处理逻辑
     * 其中，租户编号从 {@link TenantContextHolder} 上下文中获取
     *
     * @param handler 处理器
     */
    void handleTenantInfo(TenantInfoHandler handler);

    /**
     * 进行租户的菜单处理逻辑
     * 其中，租户编号从 {@link TenantContextHolder} 上下文中获取
     *
     * @param handler 处理器
     */
    void handleTenantMenu(TenantMenuHandler handler);


}
