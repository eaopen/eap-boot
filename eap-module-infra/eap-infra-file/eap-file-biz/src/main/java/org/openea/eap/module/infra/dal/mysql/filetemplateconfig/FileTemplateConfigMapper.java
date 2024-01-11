package org.openea.eap.module.infra.dal.mysql.filetemplateconfig;

import org.apache.ibatis.annotations.Mapper;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.FileTemplateConfigPageReqVO;
import org.openea.eap.module.infra.dal.dataobject.filetemplateconfig.FileTemplateConfigDO;

/**
 * 文件模板配置 Mapper
 *
 * @author admin
 */
@Mapper
public interface FileTemplateConfigMapper extends BaseMapperX<FileTemplateConfigDO> {

    default PageResult<FileTemplateConfigDO> selectPage(FileTemplateConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileTemplateConfigDO>()
                .likeIfPresent(FileTemplateConfigDO::getTempKey, reqVO.getTempKey())
                .likeIfPresent(FileTemplateConfigDO::getTempName, reqVO.getTempName())
                .orderByDesc(FileTemplateConfigDO::getId));
    }

}