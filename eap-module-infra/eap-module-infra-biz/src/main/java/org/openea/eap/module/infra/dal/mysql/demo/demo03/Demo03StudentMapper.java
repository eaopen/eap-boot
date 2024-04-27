package org.openea.eap.module.infra.dal.mysql.demo.demo03;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;
import org.apache.ibatis.annotations.Mapper;
import org.openea.eap.module.infra.controller.admin.demo.demo03.vo.*;

/**
 * 学生 Mapper
 *
 */
@Mapper
public interface Demo03StudentMapper extends BaseMapperX<Demo03StudentDO> {

    default PageResult<Demo03StudentDO> selectPage(Demo03StudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Demo03StudentDO>()
                .likeIfPresent(Demo03StudentDO::getName, reqVO.getName())
                .eqIfPresent(Demo03StudentDO::getSex, reqVO.getSex())
                .eqIfPresent(Demo03StudentDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(Demo03StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(Demo03StudentDO::getId));
    }

}
