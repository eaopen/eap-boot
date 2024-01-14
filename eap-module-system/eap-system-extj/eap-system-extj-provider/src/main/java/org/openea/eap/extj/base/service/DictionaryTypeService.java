package org.openea.eap.extj.base.service;

import org.openea.eap.extj.base.entity.DictionaryTypeEntity;

public interface DictionaryTypeService extends SuperService<DictionaryTypeEntity>{

    /**
     * 信息
     *
     * @param enCode 代码
     * @return ignore
     */
    DictionaryTypeEntity getInfoByEnCode(String enCode);

    /**
     * 信息
     *
     * @param id 主键值
     * @return ignore
     */
    DictionaryTypeEntity getInfo(String id);

}
