package org.openea.eap.module.system.dal.mysql.sys;

import java.util.*;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.module.system.dal.dataobject.sys.SystemDO;
import org.apache.ibatis.annotations.Mapper;
import org.openea.eap.module.system.controller.admin.sys.vo.*;

/**
 * 系统应用 Mapper
 *
 * @author eap
 */
@Mapper
public interface SystemMapper extends BaseMapperX<SystemDO> {

    default PageResult<SystemDO> selectPage(SystemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SystemDO>()
                .eqIfPresent(SystemDO::getCode, reqVO.getCode())
                .likeIfPresent(SystemDO::getFullName, reqVO.getFullName())
                .eqIfPresent(SystemDO::getIcon, reqVO.getIcon())
                .eqIfPresent(SystemDO::getIsMain, reqVO.getIsMain())
                .eqIfPresent(SystemDO::getPropertyJson, reqVO.getPropertyJson())
                .eqIfPresent(SystemDO::getDescription, reqVO.getDescription())
                .eqIfPresent(SystemDO::getNavigationIcon, reqVO.getNavigationIcon())
                .eqIfPresent(SystemDO::getWorkLogoIcon, reqVO.getWorkLogoIcon())
                .eqIfPresent(SystemDO::getSort, reqVO.getSort())
                .eqIfPresent(SystemDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SystemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SystemDO::getId));
    }

}