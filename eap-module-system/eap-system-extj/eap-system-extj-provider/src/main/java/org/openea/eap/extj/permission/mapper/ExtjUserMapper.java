package org.openea.eap.extj.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.openea.eap.extj.base.mapper.SuperMapper;
import org.openea.eap.extj.permission.entity.UserEntity;

import java.util.List;

@Mapper
public interface ExtjUserMapper extends SuperMapper<UserEntity> {
    /**
     * 获取用户id
     * @return
     */
    List<String> getListId();

    /**
     * 通过组织id获取用户信息
     *
     * @param orgIdList
     * @return
     */
    List<String> query(@Param("orgIdList") List<String> orgIdList, @Param("account") String account, @Param("dbSchema") String dbSchema);
}
