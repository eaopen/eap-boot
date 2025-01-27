package org.openea.eap.module.system.service.language;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataCreateReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataExportReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataPageReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataUpdateReqVO;
import org.openea.eap.module.system.dal.dataobject.language.I18nJsonDataDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 翻译 Service 接口
 *
 * @author eap
 */
public interface I18nJsonDataService {

    /**
     * 创建翻译
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createI18nJsonData(@Valid I18nJsonDataCreateReqVO createReqVO);

    /**
     * 更新翻译
     *
     * @param updateReqVO 更新信息
     */
    void updateI18nJsonData(@Valid I18nJsonDataUpdateReqVO updateReqVO);

    /**
     * 删除翻译
     *
     * @param id 编号
     */
    void deleteI18nJsonData(Long id);

    /**
     * 获得翻译
     *
     * @param id 编号
     * @return 翻译
     */
    I18nJsonDataDO getI18nJsonData(Long id);


    /**
     * 获得翻译列表
     *
     * @param ids 编号
     * @return 翻译列表
     */
    List<I18nJsonDataDO> getI18nJsonDataList(Collection<Long> ids);

    /**
     * 获得翻译分页
     *
     * @param pageReqVO 分页查询
     * @return 翻译分页
     */
    PageResult<I18nJsonDataDO> getI18nJsonDataPage(I18nJsonDataPageReqVO pageReqVO);

    /**
     * 获得翻译列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 翻译列表
     */
    List<I18nJsonDataDO> getI18nJsonDataList(I18nJsonDataExportReqVO exportReqVO);


    /**
     * 批量添加国际化条目
     *
     * @param module 模块
     * @param itemList map(key, desc, label)
     */
    void createI18nItemList(String module, List<Map<String, String>> itemList);

    void createI18nItem(String module, String i18nKey, String i18nDesc, String originValue);

    boolean checkI18nExist(String module, String i18nKey);
}
