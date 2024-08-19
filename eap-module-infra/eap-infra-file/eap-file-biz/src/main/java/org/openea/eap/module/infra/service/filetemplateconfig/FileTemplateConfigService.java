package org.openea.eap.module.infra.service.filetemplateconfig;

import java.util.*;
import javax.validation.*;
import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.*;
import org.openea.eap.module.infra.dal.dataobject.filetemplateconfig.FileTemplateConfigDO;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.pojo.PageParam;

/**
 * 文件模板配置 Service 接口
 *
 * @author admin
 */
public interface FileTemplateConfigService {

    /**
     * 创建文件模板配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFileTemplateConfig(@Valid FileTemplateConfigSaveReqVO createReqVO);

    /**
     * 更新文件模板配置
     *
     * @param updateReqVO 更新信息
     */
    void updateFileTemplateConfig(@Valid FileTemplateConfigSaveReqVO updateReqVO);

    /**
     * 删除文件模板配置
     *
     * @param id 编号
     */
    void deleteFileTemplateConfig(Long id);

    /**
     * 获得文件模板配置
     *
     * @param id 编号
     * @return 文件模板配置
     */
    FileTemplateConfigDO getFileTemplateConfig(Long id);

    /**
     * 获得文件模板配置
     *
     * @param tempKey 模板key
     * @return 文件模板配置
     */
    FileTemplateConfigDO getFileTemplateConfigByKey(String tempKey);

    /**
     * 获得文件模板配置分页
     *
     * @param pageReqVO 分页查询
     * @return 文件模板配置分页
     */
    PageResult<FileTemplateConfigDO> getFileTemplateConfigPage(FileTemplateConfigPageReqVO pageReqVO);

}