package org.openea.eap.module.system.convert.tenant;

import org.openea.eap.framework.common.util.collection.CollectionUtils;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.module.system.controller.admin.tenant.vo.tenant.TenantRespVO;
import org.openea.eap.module.system.controller.admin.tenant.vo.tenant.TenantSaveReqVO;
import org.openea.eap.module.system.controller.admin.user.vo.user.UserRespVO;
import org.openea.eap.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.openea.eap.module.system.dal.dataobject.dept.DeptDO;
import org.openea.eap.module.system.dal.dataobject.tenant.TenantDO;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;

import java.util.List;
import java.util.Map;

/**
 * 租户 Convert
 *
 */
@Mapper
public interface TenantConvert {

    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    default UserSaveReqVO convert02(TenantSaveReqVO bean) {
        UserSaveReqVO reqVO = new UserSaveReqVO();
        reqVO.setUsername(bean.getUsername());
        reqVO.setPassword(bean.getPassword());
        reqVO.setNickname(bean.getContactName()).setMobile(bean.getContactMobile());
        return reqVO;
    }

    default List<TenantRespVO> convertList(List<TenantDO> list, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(list, tenant -> convert(tenant, deptMap.get(tenant.getDeptId())));
    }

    default TenantRespVO convert(TenantDO tenant, DeptDO dept) {
        TenantRespVO tenantVO = BeanUtils.toBean(tenant, TenantRespVO.class);
        if (dept != null) {
            tenantVO.setDeptName(dept.getName());
        }
        return tenantVO;
    }

}
