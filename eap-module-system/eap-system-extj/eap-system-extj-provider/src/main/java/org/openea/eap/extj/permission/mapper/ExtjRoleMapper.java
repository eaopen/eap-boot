package org.openea.eap.extj.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.openea.eap.extj.base.mapper.SuperMapper;
import org.openea.eap.extj.permission.entity.RoleEntity;

import java.util.List;
@Mapper
public interface ExtjRoleMapper extends SuperMapper<RoleEntity> {

    /**
     * 通过组织id获取用户信息
     *
     * @param orgId
     * @param orgIdList
     * @return
     */
    List<String> query(@Param("orgId") String orgId, @Param("orgIdList") List<String> orgIdList, @Param("keyword") String keyword, @Param("globalMark") Integer globalMark);

}
