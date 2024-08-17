package org.openea.eap.module.system.convert.tenant;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.openea.eap.module.system.controller.admin.tenant.vo.tenant.TenantSaveReqVO;
import org.openea.eap.module.system.controller.admin.user.vo.user.UserSaveReqVO;

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

}
