package org.openea.eap.extj.base.util;

import lombok.extern.slf4j.Slf4j;
import org.openea.eap.extj.base.entity.DictionaryDataEntity;
import org.openea.eap.extj.base.entity.DictionaryTypeEntity;
import org.openea.eap.extj.base.entity.VisualdevEntity;
import org.openea.eap.extj.base.service.DictionaryDataService;
import org.openea.eap.extj.base.service.DictionaryTypeService;
import org.openea.eap.extj.constant.MsgCode;
import org.openea.eap.extj.exception.WorkFlowException;
import org.openea.eap.extj.form.entity.FlowFormEntity;
import org.openea.eap.extj.form.service.FlowFormService;
import org.openea.eap.extj.model.visualJson.OnlineDevData;
import org.openea.eap.extj.util.EapUserProvider;
import org.openea.eap.extj.util.StringUtil;
import org.openea.eap.extj.util.enums.DictionaryDataEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 在线开发流程及表单相关方法
 *
 */
@Component
@Slf4j
public class VisualFlowFormUtil {
    @Autowired
    private FlowFormService flowFormService;

    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private EapUserProvider userProvider;


    /**
     * 保存或修改流程表单信息
     *
     * @param
     * @return
     */
    public void saveOrUpdateForm(VisualdevEntity entity, int enabledMark, boolean isSave) throws WorkFlowException {
        String userId = userProvider.get().getUserId();
        FlowFormEntity flowFormEntity = Optional.ofNullable(flowFormService.getById(entity.getId())).orElse(new FlowFormEntity());
        flowFormEntity.setId(entity.getId());
//        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        flowFormEntity.setEnCode(entity.getEnCode());
        flowFormEntity.setFullName(entity.getFullName());
        //功能流程（在线开发-自定义表单-隐藏）
        flowFormEntity.setFlowType(OnlineDevData.FLOW_TYPE_DEV);
        flowFormEntity.setFormType(OnlineDevData.FORM_TYPE_DEV);
        if (entity.getType() == 4) {//功能系统表单，代码生成-功能系统表单
            flowFormEntity.setFormType(OnlineDevData.FORM_TYPE_SYS);
        }
        if (entity.getType() == 3) {//发起系统表单，代码生成-发起系统表单
            flowFormEntity.setFlowType(OnlineDevData.FLOW_TYPE_FLOW);
            flowFormEntity.setFormType(OnlineDevData.FORM_TYPE_SYS);
        }
        flowFormEntity.setCategory(entity.getCategory());
        flowFormEntity.setPropertyJson(entity.getFormData());
        flowFormEntity.setDescription(entity.getDescription());
        flowFormEntity.setSortCode(entity.getSortCode());
        flowFormEntity.setEnabledMark(enabledMark);
        if (isSave) {
            flowFormEntity.setCreatorTime(new Date());
            flowFormEntity.setCreatorUserId(userId);
        } else {

            flowFormEntity.setLastModifyTime(new Date());
            flowFormEntity.setLastModifyUserId(userId);
        }
        flowFormEntity.setTableJson(entity.getVisualTables());
        flowFormEntity.setDbLinkId(entity.getDbLinkId());
        if (isSave) {
            flowFormEntity.setFlowId(entity.getId());
        }
        //判断名称是否重复
        if (flowFormService.isExistByFullName(flowFormEntity.getFullName(), flowFormEntity.getId())) {
            throw new WorkFlowException(MsgCode.EXIST001.get());
        }
        //判断编码是否重复
        if (flowFormService.isExistByEnCode(flowFormEntity.getEnCode(), flowFormEntity.getId())) {
            throw new WorkFlowException(MsgCode.EXIST002.get());
        }
        flowFormService.saveOrUpdate(flowFormEntity);
    }


    /**
     * 获取字典相关列表
     *
     * @param
     * @return
     */
    public List<DictionaryDataEntity> getListByTypeDataCode(Integer type) {
        return getListByTypeDataCode(DictionaryDataEnum.getTypeId(type));
    }

    /**
     * 获取字典数据
     *
     * @param
     * @return
     */
    public DictionaryDataEntity getdictionaryDataInfo(String category) {
        return dictionaryDataService.getInfo(category);
    }


    /**
     * 将在线开发分类字段转换成流程分类字段id
     *
     * @param
     * @return
     */
    public String categaryMapping(String devCateId) {
        //流程分类
        String flowCateId = "";
        try {
            List<DictionaryDataEntity> flowDictionList = getListByTypeDataCode(DictionaryDataEnum.FLOWWOEK_ENGINE.getDictionaryTypeId());
            List<DictionaryDataEntity> devDictionList = getListByTypeDataCode(DictionaryDataEnum.VISUALDEV.getDictionaryTypeId());
            for (DictionaryDataEntity devItem : devDictionList) {
                if (devItem.getId().equals(devCateId)) {
                    for (DictionaryDataEntity flowItem : flowDictionList) {
                        if (flowItem.getEnCode().equals(devItem.getEnCode())) {
                            flowCateId = flowItem.getId();
                        }
                        if (StringUtil.isEmpty(flowCateId) && OnlineDevData.DEFAULT_CATEGATY_ENCODE.equals(flowItem.getEnCode())) {//没值，给默认
                            flowCateId = flowItem.getId();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("分类字段转换失败！:{}", e.getMessage());
        }
        return flowCateId;
    }

    /**
     * 获取字典数据信息列表
     *
     * @param typeCode 字典分类code
     * @return
     */
    public List<DictionaryDataEntity> getListByTypeDataCode(String typeCode) {
        DictionaryTypeEntity dictionaryTypeEntity = dictionaryTypeService.getInfoByEnCode(typeCode);
        List<DictionaryDataEntity> list = null;
        if (dictionaryTypeEntity != null) {
            list = dictionaryDataService.getList(dictionaryTypeEntity.getId());
        }else{
            list = Collections.emptyList();
        }
        return list;
    }

    /**
     * 删除流程表单信息
     *
     * @param
     * @return
     */
    public void deleteFlowForm(String id) {
        try {
            flowFormService.removeById(id);
        } catch (Exception e) {
        }
    }
}