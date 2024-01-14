package org.openea.eap.extj.base.service.impl;

import org.openea.eap.extj.base.entity.DictionaryTypeEntity;
import org.openea.eap.extj.base.mapper.DictionaryTypeMapper;
import org.openea.eap.extj.base.service.DictionaryTypeService;
import org.openea.eap.extj.base.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 迁移到 eap字典表
 */
@Service
public class DictionaryTypeServiceImpl extends SuperServiceImpl<DictionaryTypeMapper, DictionaryTypeEntity> implements DictionaryTypeService {


    @Override
    public DictionaryTypeEntity getInfoByEnCode(String enCode) {
        DictionaryTypeEntity entity = new DictionaryTypeEntity();
        entity.setEnCode(enCode);
        entity.setId(enCode);
        return entity;
    }

    @Override
    public DictionaryTypeEntity getInfo(String id) {
        return getInfoByEnCode(id);
    }
}
