package org.openea.eap.module.system.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.openea.eap.framework.common.enums.CommonStatusEnum;
import org.openea.eap.framework.common.exception.util.ServiceExceptionUtil;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.util.collection.CollectionUtils;
import org.openea.eap.framework.common.util.date.DateUtils;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.framework.tenant.config.TenantProperties;
import org.openea.eap.framework.tenant.core.context.TenantContextHolder;
import org.openea.eap.framework.tenant.core.util.TenantUtils;
import org.openea.eap.module.system.dal.mysql.tenant.TenantMapper;
import org.openea.eap.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import org.openea.eap.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import org.openea.eap.module.system.controller.admin.tenant.vo.tenant.TenantSaveReqVO;
import org.openea.eap.module.system.convert.tenant.TenantConvert;
import org.openea.eap.module.system.dal.dataobject.permission.MenuDO;
import org.openea.eap.module.system.dal.dataobject.permission.RoleDO;
import org.openea.eap.module.system.dal.dataobject.tenant.TenantDO;
import org.openea.eap.module.system.enums.ErrorCodeConstants;
import org.openea.eap.module.system.enums.permission.RoleCodeEnum;
import org.openea.eap.module.system.enums.permission.RoleTypeEnum;
import org.openea.eap.module.system.service.permission.MenuService;
import org.openea.eap.module.system.service.permission.PermissionService;
import org.openea.eap.module.system.service.permission.RoleService;
import org.openea.eap.module.system.service.tenant.handler.TenantInfoHandler;
import org.openea.eap.module.system.service.tenant.handler.TenantMenuHandler;
import org.openea.eap.module.system.service.user.AdminUserService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.openea.eap.framework.common.exception.util.ServiceExceptionUtil.exception;
import static java.util.Collections.singleton;

/**
 * 租户 Service 实现类
 *
 */
@Service
@ConditionalOnMissingBean(name = "tenantService")
@Validated
@Slf4j
public class TenantServiceImpl implements TenantService {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 eap.tenant.enable 配置项，可以关闭多租户的功能，所以这里只能不强制注入
    protected TenantProperties tenantProperties;

    @Resource
    protected TenantMapper tenantMapper;



    @Resource
    @Lazy // 延迟，避免循环依赖报错
    protected AdminUserService userService;
    @Resource
    protected RoleService roleService;
    @Resource
    protected MenuService menuService;
    @Resource
    protected PermissionService permissionService;

    @Override
    public List<Long> getTenantIdList() {
        List<TenantDO> tenants = tenantMapper.selectList();
        return CollectionUtils.convertList(tenants, TenantDO::getId);
    }

    @Override
    public void validTenant(Long id) {
        TenantDO tenant = getTenant(id);
        if (tenant == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.TENANT_NOT_EXISTS);
        }
        if (tenant.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.TENANT_DISABLE, tenant.getName());
        }
        if (DateUtils.isExpired(tenant.getExpireTime())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.TENANT_EXPIRE, tenant.getName());
        }
    }





    protected TenantDO validateUpdateTenant(Long id) {
        TenantDO tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.TENANT_NOT_EXISTS);
        }
        // 内置租户，不允许删除
        if (isSystemTenant(tenant)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.TENANT_CAN_NOT_UPDATE_SYSTEM);
        }
        return tenant;
    }

    @Override
    public TenantDO getTenant(Long id) {
        return tenantMapper.selectById(id);
    }

    @Override
    public PageResult<TenantDO> getTenantPage(TenantPageReqVO pageReqVO) {
        return tenantMapper.selectPage(pageReqVO);
    }

    @Override
    public TenantDO getTenantByName(String name) {
        return tenantMapper.selectByName(name);
    }

    @Override
    public TenantDO getTenantByWebsite(String website) {
        return tenantMapper.selectByWebsite(website);
    }



    @Override
    public void handleTenantInfo(TenantInfoHandler handler) {
        // 如果禁用，则不执行逻辑
        if (isTenantDisable()) {
            return;
        }
        // 获得租户
        TenantDO tenant = getTenant(TenantContextHolder.getRequiredTenantId());
        // 执行处理器
        handler.handle(tenant);
    }

    @Override
    public void handleTenantMenu(TenantMenuHandler handler) {
        // 如果禁用，则不执行逻辑
        if (isTenantDisable()) {
            return;
        }
        // 获得租户，然后获得菜单
        TenantDO tenant = getTenant(TenantContextHolder.getRequiredTenantId());
        Set<Long> menuIds;
        if (isSystemTenant(tenant)) { // 系统租户，菜单是全量的
            menuIds = CollectionUtils.convertSet(menuService.getMenuList(), MenuDO::getId);
        } else {
            //menuIds = tenantPackageService.getTenantPackage(tenant.getPackageId()).getMenuIds();
            menuIds = new HashSet<>();
        }
        // 执行处理器
        handler.handle(menuIds);
    }

    protected static boolean isSystemTenant(TenantDO tenant) {
        return Objects.equals(tenant.getPackageId(), TenantDO.PACKAGE_ID_SYSTEM);
    }

    protected boolean isTenantDisable() {
        return tenantProperties == null || Boolean.FALSE.equals(tenantProperties.getEnable());
    }

}
