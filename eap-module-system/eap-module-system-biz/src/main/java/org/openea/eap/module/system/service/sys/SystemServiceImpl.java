package org.openea.eap.module.system.service.sys;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import org.openea.eap.module.system.controller.admin.sys.vo.*;
import org.openea.eap.module.system.dal.dataobject.sys.SystemDO;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.pojo.PageParam;
import org.openea.eap.framework.common.util.object.BeanUtils;

import org.openea.eap.module.system.dal.mysql.sys.SystemMapper;

import static org.openea.eap.framework.common.exception.util.ServiceExceptionUtil.exception;
import static org.openea.eap.module.system.enums.ErrorCodeConstants.*;

/**
 * 系统应用 Service 实现类
 *
 * @author eap
 */
@Service
@Validated
public class SystemServiceImpl implements SystemService {

    @Resource
    private SystemMapper systemMapper;

    @Override
    public Long createSystem(SystemSaveReqVO createReqVO) {
        // 插入
        SystemDO system = BeanUtils.toBean(createReqVO, SystemDO.class);
        systemMapper.insert(system);
        // 返回
        return system.getId();
    }

    @Override
    public void updateSystem(SystemSaveReqVO updateReqVO) {
        // 校验存在
        validateSystemExists(updateReqVO.getId());
        // 更新
        SystemDO updateObj = BeanUtils.toBean(updateReqVO, SystemDO.class);
        systemMapper.updateById(updateObj);
    }

    @Override
    public void deleteSystem(Long id) {
        // 校验存在
        validateSystemExists(id);
        // 删除
        systemMapper.deleteById(id);
    }

    private void validateSystemExists(Long id) {
        if (systemMapper.selectById(id) == null) {
            throw exception(SYSTEM_NOT_EXISTS);
        }
    }

    @Override
    public SystemDO getSystem(Long id) {
        return systemMapper.selectById(id);
    }

    @Override
    public PageResult<SystemDO> getSystemPage(SystemPageReqVO pageReqVO) {
        return systemMapper.selectPage(pageReqVO);
    }

}