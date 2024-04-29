package org.openea.eap.module.system.service.tenant;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.tenant.core.context.TenantContextHolder;
import org.openea.eap.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import org.openea.eap.module.system.dal.dataobject.tenant.TenantDO;
import org.openea.eap.module.system.service.tenant.handler.TenantInfoHandler;
import org.openea.eap.module.system.service.tenant.handler.TenantMenuHandler;

import java.util.List;


/**
 * 租户 Service 接口
 *
 */
public interface TenantService {


    /**
     * 获得租户
     *
     * @param id 编号
     * @return 租户
     */
    TenantDO getTenant(Long id);

    /**
     * 获得租户分页
     *
     * @param pageReqVO 分页查询
     * @return 租户分页
     */
    PageResult<TenantDO> getTenantPage(TenantPageReqVO pageReqVO);

    /**
     * 获得名字对应的租户
     *
     * @param name 租户名
     * @return 租户
     */
    TenantDO getTenantByName(String name);

    /**
     * 获得域名对应的租户
     *
     * @param website 域名
     * @return 租户
     */
    TenantDO getTenantByWebsite(String website);


    /**
     * 获得所有租户
     *
     * @return 租户编号数组
     */
    List<Long> getTenantIdList();

    /**
     * 校验租户是否合法
     *
     * @param id 租户编号
     */
    void validTenant(Long id);


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
