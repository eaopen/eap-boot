package org.openea.eap.module.infra.convert.file;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.openea.eap.module.infra.controller.admin.file.vo.config.FileConfigSaveReqVO;
import org.openea.eap.module.infra.dal.dataobject.file.FileConfigDO;

/**
 * 文件配置 Convert
 *
 */
@Mapper
public interface FileConfigConvert {

    FileConfigConvert INSTANCE = Mappers.getMapper(FileConfigConvert.class);

    @Mapping(target = "config", ignore = true)
    FileConfigDO convert(FileConfigSaveReqVO bean);

}