package org.openea.eap.extj.base.service.impl;

import cn.hutool.core.util.NumberUtil;
import org.openea.eap.extj.base.entity.DictionaryDataEntity;
import org.openea.eap.extj.base.mapper.DictionaryDataMapper;
import org.openea.eap.extj.base.service.DictionaryDataService;
import org.openea.eap.extj.base.service.SuperServiceImpl;
import org.openea.eap.module.system.dal.dataobject.dict.DictDataDO;
import org.openea.eap.module.system.dal.dataobject.dict.DictTypeDO;
import org.openea.eap.module.system.service.dict.DictDataService;
import org.openea.eap.module.system.service.dict.DictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 迁移到 eap字典表
 */
@Service
public class DictionaryDataServiceImpl extends SuperServiceImpl<DictionaryDataMapper, DictionaryDataEntity> implements DictionaryDataService {


    @Autowired
    private DictTypeService dictTypeService;
    @Autowired
    private DictDataService dictDataService;


    protected DictTypeDO getDictType(String dictionaryTypeId) {
        DictTypeDO dictType = null;
        if(NumberUtil.isNumber(dictionaryTypeId)){
            dictType = dictTypeService.getDictTypeById(Long.valueOf(dictionaryTypeId));
        }else{
            dictType = dictTypeService.getDictType(dictionaryTypeId);
        }
        return dictType;
    }

    public static DictionaryDataEntity convert(DictDataDO dictDataDO){
        if(dictDataDO==null) return null;
        DictionaryDataEntity entity = new DictionaryDataEntity();
        entity.setId(""+dictDataDO.getId());
        entity.setEnCode(dictDataDO.getValue());
        entity.setFullName(dictDataDO.getLabel());
        return entity;
    }

    public static List<DictionaryDataEntity> convert(List<DictDataDO> listData){
        if(listData==null) return null;
        return listData.stream().map(t -> {
            DictionaryDataEntity entity = new DictionaryDataEntity();
            entity.setId(""+t.getId());
            entity.setEnCode(t.getValue());
            entity.setFullName(t.getLabel());
            return entity;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DictionaryDataEntity> getList(String dictionaryTypeId, Boolean enable) {
        DictTypeDO dictType = getDictType(dictionaryTypeId);
        List<DictDataDO> list = dictDataService.getDictData(dictType.getType());
        return convert(list);
    }


    @Override
    public List<DictionaryDataEntity> getList(String dictionaryTypeId) {
        DictTypeDO dictType = getDictType(dictionaryTypeId);
        if(dictType != null){
            List<DictDataDO> list = dictDataService.getDictData(dictType.getType());
            if(list!=null){
                return list.stream().map(t -> {
                    DictionaryDataEntity entity = new DictionaryDataEntity();
                    entity.setId(""+t.getId());
                    entity.setFullName(t.getLabel());
                    entity.setEnCode(t.getValue());
                    return entity;
                }).collect(Collectors.toList());
            }
        }

        return null;
    }

    @Override
    public List<DictionaryDataEntity> getDicList(String dictionaryTypeId) {
        DictTypeDO dictType = getDictType(dictionaryTypeId);
        if(dictType!=null){
            List<DictDataDO> list = dictDataService.getDictData(dictType.getType());
            return list.stream().map(t -> {
               return convert(t);
            }).collect(Collectors.toList());
        }
        return null;
    }


    @Override
    public DictionaryDataEntity getInfo(String id) {
        if (id == null) {
            return null;
        }
        DictDataDO dictDataDO = dictDataService.getDictData(new Long(id));
        return convert(dictDataDO);
    }
}
