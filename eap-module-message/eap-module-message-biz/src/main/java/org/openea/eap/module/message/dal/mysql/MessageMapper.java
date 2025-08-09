package org.openea.eap.module.message.dal.mysql;

import org.apache.ibatis.annotations.Mapper;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.module.message.dal.dataobject.MessageDO;

@Mapper
public interface MessageMapper extends BaseMapperX<MessageDO> {
    default int markState(Long id, int state) {
        MessageDO update = new MessageDO();
        update.setId(id);
        update.setState(state);
        return updateById(update);
    }
}
