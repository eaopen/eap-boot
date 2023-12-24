package org.openea.eap.module.system.service.logger;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.system.api.logger.dto.OperateLogCreateReqDTO;
import org.openea.eap.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import org.openea.eap.module.system.dal.dataobject.logger.OperateLogDO;

/**
 * 操作日志 Service 接口
 *
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 操作日志请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO pageReqVO);

}
