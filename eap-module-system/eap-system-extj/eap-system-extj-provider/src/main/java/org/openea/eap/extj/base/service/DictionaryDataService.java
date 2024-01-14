package org.openea.eap.extj.base.service;

import org.openea.eap.extj.base.entity.DictionaryDataEntity;

import java.util.List;

/**
 * 字典数据
 *
 * todo eap待处理
 *
 */
public interface DictionaryDataService extends SuperService<DictionaryDataEntity> {

    /**
     * 列表
     *
     * @param dictionaryTypeId 字段分类id
     * @param enable 是否只看有效
     * @return ignore
     */
    List<DictionaryDataEntity> getList(String dictionaryTypeId, Boolean enable);

    /**
     * 列表
     *
     * @param dictionaryTypeId 类别主键
     * @return ignore
     */
    List<DictionaryDataEntity> getList(String dictionaryTypeId);
    /**
     * 列表
     *
     * @param dictionaryTypeId 类别主键(在线开发数据转换)
     * @return ignore
     */
    List<DictionaryDataEntity> getDicList(String dictionaryTypeId);

    /**
     * 信息
     *
     * @param id 主键值
     * @return ignore
     */
    DictionaryDataEntity getInfo(String id);

}
