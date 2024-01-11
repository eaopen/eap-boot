package org.openea.eap.module.infra.service.filetemplateconfig;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.*;
import org.openea.eap.module.infra.dal.dataobject.filetemplateconfig.FileTemplateConfigDO;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.pojo.PageParam;
import org.openea.eap.framework.common.util.object.BeanUtils;

import org.openea.eap.module.infra.dal.mysql.filetemplateconfig.FileTemplateConfigMapper;

import static org.openea.eap.framework.common.exception.util.ServiceExceptionUtil.exception;
import static org.openea.eap.module.infra.enums.ErrorCodeConstants.*;

/**
 * 文件模板配置 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class FileTemplateConfigServiceImpl implements FileTemplateConfigService {

    @Resource
    private FileTemplateConfigMapper fileTemplateConfigMapper;

    @Override
    public Long createFileTemplateConfig(FileTemplateConfigSaveReqVO createReqVO) {
        // 插入
        FileTemplateConfigDO fileTemplateConfig = BeanUtils.toBean(createReqVO, FileTemplateConfigDO.class);
        fileTemplateConfigMapper.insert(fileTemplateConfig);
        // 返回
        return fileTemplateConfig.getId();
    }

    @Override
    public void updateFileTemplateConfig(FileTemplateConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateFileTemplateConfigExists(updateReqVO.getId());
        // 更新
        FileTemplateConfigDO updateObj = BeanUtils.toBean(updateReqVO, FileTemplateConfigDO.class);
        fileTemplateConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteFileTemplateConfig(Long id) {
        // 校验存在
        validateFileTemplateConfigExists(id);
        // 删除
        fileTemplateConfigMapper.deleteById(id);
    }

    private void validateFileTemplateConfigExists(Long id) {
        if (fileTemplateConfigMapper.selectById(id) == null) {
            throw exception(FILE_TEMPLATE_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public FileTemplateConfigDO getFileTemplateConfig(Long id) {
        return fileTemplateConfigMapper.selectById(id);
    }

    @Override
    public FileTemplateConfigDO getFileTemplateConfigByKey(String tempKey) {
        return fileTemplateConfigMapper.selectOne(FileTemplateConfigDO::getTempKey, tempKey);
    }

    @Override
    public PageResult<FileTemplateConfigDO> getFileTemplateConfigPage(FileTemplateConfigPageReqVO pageReqVO) {
        return fileTemplateConfigMapper.selectPage(pageReqVO);
    }

}