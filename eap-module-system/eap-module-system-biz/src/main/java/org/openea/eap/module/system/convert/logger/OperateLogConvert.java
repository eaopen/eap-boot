package org.openea.eap.module.system.convert.logger;

import org.openea.eap.framework.common.util.collection.CollectionUtils;
import org.openea.eap.framework.common.util.collection.MapUtils;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.module.system.controller.admin.logger.vo.operatelog.OperateLogRespVO;
import org.openea.eap.module.system.dal.dataobject.logger.OperateLogDO;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface OperateLogConvert {

    OperateLogConvert INSTANCE = Mappers.getMapper(OperateLogConvert.class);

    default List<OperateLogRespVO> convertList(List<OperateLogDO> list, Map<Long, AdminUserDO> userMap) {
        return CollectionUtils.convertList(list, log -> {
            OperateLogRespVO logVO = BeanUtils.toBean(log, OperateLogRespVO.class);
            MapUtils.findAndThen(userMap, log.getUserId(), user -> logVO.setUserNickname(user.getNickname()));
            return logVO;
        });
    }

}
