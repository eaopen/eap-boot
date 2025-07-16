package org.openea.eap.module.system.api.permission;

import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import org.openea.eap.module.system.service.permission.PermissionService;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Set;

import static org.openea.eap.framework.common.pojo.CommonResult.success;

@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
@Primary // 由于 PermissionCommonApi 的存在，必须声明为 @Primary Bean
public class PermissionApiImpl implements PermissionApi {

    @Resource
    private PermissionService permissionService;

    @Override
    public CommonResult<Set<Long>> getUserRoleIdListByRoleIds(Collection<Long> roleIds) {
        return success(permissionService.getUserRoleIdListByRoleId(roleIds));
    }

    @Override
    public CommonResult<Boolean> hasAnyPermissions(Long userId, String... permissions) {
        return success(permissionService.hasAnyPermissions(userId, permissions));
    }

    @Override
    public CommonResult<Boolean> hasAnyRoles(Long userId, String... roles) {
        return success(permissionService.hasAnyRoles(userId, roles));
    }

    @Override
    public CommonResult<DeptDataPermissionRespDTO> getDeptDataPermission(Long userId) {
        return success(permissionService.getDeptDataPermission(userId));
    }

}
