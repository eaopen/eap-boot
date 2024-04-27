package org.openea.eap.module.system.api.logger;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.module.system.api.logger.dto.OperateLogCreateReqDTO;
import org.openea.eap.module.system.api.logger.dto.OperateLogPageReqDTO;
import org.openea.eap.module.system.api.logger.dto.OperateLogRespDTO;
import org.openea.eap.module.system.dal.dataobject.logger.OperateLogDO;
import org.openea.eap.module.system.service.logger.OperateLogService;
import com.fhs.core.trans.anno.TransMethodResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 操作日志 API 实现类
 *
 */
@Service
@Validated
public class OperateLogApiImpl implements OperateLogApi {

    @Resource
    private OperateLogService operateLogService;

    @Override
    @Async
    public void createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        operateLogService.createOperateLog(createReqDTO);
    }

    @Override
    @TransMethodResult
    public PageResult<OperateLogRespDTO> getOperateLogPage(OperateLogPageReqDTO pageReqVO) {
        PageResult<OperateLogDO> operateLogPage = operateLogService.getOperateLogPage(pageReqVO);
        return BeanUtils.toBean(operateLogPage, OperateLogRespDTO.class);
    }

}
