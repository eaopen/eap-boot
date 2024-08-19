package org.openea.eap.module.system.convert.language;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.system.controller.admin.language.vo.LangTypeCreateReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.LangTypeExcelVO;
import org.openea.eap.module.system.controller.admin.language.vo.LangTypeRespVO;
import org.openea.eap.module.system.controller.admin.language.vo.LangTypeUpdateReqVO;
import org.openea.eap.module.system.dal.dataobject.language.LangTypeDO;

import java.util.List;

/**
 * 语言 Convert
 *
 * @author eap
 */
@Mapper
public interface LangTypeConvert {

    LangTypeConvert INSTANCE = Mappers.getMapper(LangTypeConvert.class);

    LangTypeDO convert(LangTypeCreateReqVO bean);

    LangTypeDO convert(LangTypeUpdateReqVO bean);

    LangTypeRespVO convert(LangTypeDO bean);

    List<LangTypeRespVO> convertList(List<LangTypeDO> list);

    PageResult<LangTypeRespVO> convertPage(PageResult<LangTypeDO> page);

    List<LangTypeExcelVO> convertList02(List<LangTypeDO> list);

}
