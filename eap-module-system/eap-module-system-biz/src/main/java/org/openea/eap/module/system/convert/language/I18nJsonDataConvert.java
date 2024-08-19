package org.openea.eap.module.system.convert.language;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataCreateReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataExcelVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataRespVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataUpdateReqVO;
import org.openea.eap.module.system.dal.dataobject.language.I18nJsonDataDO;

import java.util.List;

/**
 * 翻译 Convert
 *
 * @author eap
 */
@Mapper
public interface I18nJsonDataConvert {

    I18nJsonDataConvert INSTANCE = Mappers.getMapper(I18nJsonDataConvert.class);

    I18nJsonDataDO convert(I18nJsonDataCreateReqVO bean);

    I18nJsonDataDO convert(I18nJsonDataUpdateReqVO bean);

    I18nJsonDataRespVO convert(I18nJsonDataDO bean);

    List<I18nJsonDataRespVO> convertList(List<I18nJsonDataDO> list);

    PageResult<I18nJsonDataRespVO> convertPage(PageResult<I18nJsonDataDO> page);

    List<I18nJsonDataExcelVO> convertList02(List<I18nJsonDataDO> list);

}
