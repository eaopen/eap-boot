package org.openea.eap.module.system.service.language;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.system.controller.admin.language.vo.LangTypeCreateReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.LangTypeExportReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.LangTypePageReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.LangTypeUpdateReqVO;
import org.openea.eap.module.system.dal.dataobject.language.LangTypeDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 语言 Service 接口
 *
 * @author eap
 */
public interface LangTypeService {

    /**
     * 创建语言
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createLangType(@Valid LangTypeCreateReqVO createReqVO);

    /**
     * 更新语言
     *
     * @param updateReqVO 更新信息
     */
    void updateLangType(@Valid LangTypeUpdateReqVO updateReqVO);

    /**
     * 删除语言
     *
     * @param id 编号
     */
    void deleteLangType(Long id);

    /**
     * 获得语言
     *
     * @param id 编号
     * @return 语言
     */
    LangTypeDO getLangType(Long id);

    /**
     * 获得语言列表
     *
     * @param ids 编号
     * @return 语言列表
     */
    List<LangTypeDO> getLangTypeList(Collection<Long> ids);

    /**
     * 获得语言分页
     *
     * @param pageReqVO 分页查询
     * @return 语言分页
     */
    PageResult<LangTypeDO> getLangTypePage(LangTypePageReqVO pageReqVO);

    /**
     * 获得语言列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 语言列表
     */
    List<LangTypeDO> getLangTypeList(LangTypeExportReqVO exportReqVO);

}
