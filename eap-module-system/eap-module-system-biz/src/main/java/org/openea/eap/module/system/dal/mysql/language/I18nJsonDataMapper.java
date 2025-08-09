package org.openea.eap.module.system.dal.mysql.language;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.ibatis.annotations.Mapper;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataExportReqVO;
import org.openea.eap.module.system.controller.admin.language.vo.I18nJsonDataPageReqVO;
import org.openea.eap.module.system.dal.dataobject.language.I18nJsonDataDO;

import java.util.List;

/**
 * 翻译 Mapper
 *
 * @author eap
 */
@Mapper
public interface I18nJsonDataMapper extends BaseMapperX<I18nJsonDataDO> {

    default I18nJsonDataDO queryI18nJsonDataByKey(String key) {
        List<I18nJsonDataDO> list = selectList(I18nJsonDataDO::getAlias, key);
        if(CollectionUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    default PageResult<I18nJsonDataDO> selectPage(I18nJsonDataPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<I18nJsonDataDO>()
                .likeIfPresent(I18nJsonDataDO::getModule, reqVO.getModule())
                .likeIfPresent(I18nJsonDataDO::getAlias, reqVO.getAlias())
                .likeIfPresent(I18nJsonDataDO::getName, reqVO.getName())
                .likeIfPresent(I18nJsonDataDO::getJson, reqVO.getJson())
                .eqIfPresent(I18nJsonDataDO::getTranslationSource, reqVO.getTranslationSource())
                .eqIfPresent(I18nJsonDataDO::getAiProvider, reqVO.getAiProvider())
                .eqIfPresent(I18nJsonDataDO::getAiModel, reqVO.getAiModel())
                .geIfPresent(I18nJsonDataDO::getTranslationQuality, reqVO.getMinQuality())
                .leIfPresent(I18nJsonDataDO::getTranslationQuality, reqVO.getMaxQuality())
                .betweenIfPresent(I18nJsonDataDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(I18nJsonDataDO::getLastAiUpdate, reqVO.getLastAiUpdateTime())
                .orderByDesc(I18nJsonDataDO::getId));
    }

    default List<I18nJsonDataDO> selectList(I18nJsonDataExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<I18nJsonDataDO>()
                .likeIfPresent(I18nJsonDataDO::getModule, reqVO.getModule())
                .likeIfPresent(I18nJsonDataDO::getAlias, reqVO.getAlias())
                .likeIfPresent(I18nJsonDataDO::getName, reqVO.getName())
                .likeIfPresent(I18nJsonDataDO::getJson, reqVO.getJson())
                .eqIfPresent(I18nJsonDataDO::getTranslationSource, reqVO.getTranslationSource())
                .eqIfPresent(I18nJsonDataDO::getAiProvider, reqVO.getAiProvider())
                .betweenIfPresent(I18nJsonDataDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(I18nJsonDataDO::getId));
    }

    /**
     * 根据AI提供商和模型查询翻译数据
     */
    default List<I18nJsonDataDO> selectByAIProvider(String aiProvider, String aiModel) {
        return selectList(new LambdaQueryWrapperX<I18nJsonDataDO>()
                .eq(I18nJsonDataDO::getAiProvider, aiProvider)
                .eqIfPresent(I18nJsonDataDO::getAiModel, aiModel)
                .orderByDesc(I18nJsonDataDO::getLastAiUpdate));
    }

    /**
     * 查询需要AI翻译的数据（质量分数低于阈值的）
     */
    default List<I18nJsonDataDO> selectLowQualityTranslations(Integer qualityThreshold) {
        return selectList(new LambdaQueryWrapperX<I18nJsonDataDO>()
                .lt(I18nJsonDataDO::getTranslationQuality, qualityThreshold)
                .in(I18nJsonDataDO::getTranslationSource, "AI_AUTO", "LLM_AUTO")
                .orderByAsc(I18nJsonDataDO::getTranslationQuality));
    }

    /**
     * 统计AI翻译数据
     */
    default Long countByTranslationSource(String translationSource) {
        return selectCount(new LambdaQueryWrapperX<I18nJsonDataDO>()
                .eq(I18nJsonDataDO::getTranslationSource, translationSource));
    }

}
