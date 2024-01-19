package org.openea.eap.extj.base.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import org.openea.eap.extj.base.entity.VisualdevEntity;
import org.openea.eap.extj.base.entity.VisualdevReleaseEntity;
import org.openea.eap.extj.base.mapper.VisualdevMapper;
import org.openea.eap.extj.base.model.PaginationVisualdev;
import org.openea.eap.extj.base.service.FilterService;
import org.openea.eap.extj.base.service.SuperServiceImpl;
import org.openea.eap.extj.base.service.VisualdevReleaseService;
import org.openea.eap.extj.base.service.VisualdevService;
import org.openea.eap.extj.database.model.dbfield.DbFieldModel;
import org.openea.eap.extj.database.model.dbtable.DbTableFieldModel;
import org.openea.eap.extj.exception.WorkFlowException;
import org.openea.eap.extj.form.model.form.VisualTableModel;
import org.openea.eap.extj.form.service.FlowFormService;
import org.openea.eap.extj.form.util.ConcurrencyUtils;
import org.openea.eap.extj.form.util.VisualDevTableCre;
import org.openea.eap.extj.model.visual.ExtnKeyConsts;
import org.openea.eap.extj.model.visualJson.FieLdsModel;
import org.openea.eap.extj.model.visualJson.FormCloumnUtil;
import org.openea.eap.extj.model.visualJson.FormDataModel;
import org.openea.eap.extj.model.visualJson.TableModel;
import org.openea.eap.extj.model.visualJson.analysis.FormAllModel;
import org.openea.eap.extj.model.visualJson.analysis.RecursionForm;
import org.openea.eap.extj.model.visualJson.config.ConfigModel;
import org.openea.eap.extj.onlinedev.model.DataInfoVO;
import org.openea.eap.extj.util.EapUserProvider;
import org.openea.eap.extj.util.JsonUtil;
import org.openea.eap.extj.util.RandomUtil;
import org.openea.eap.extj.util.StringUtil;
import org.openea.eap.framework.i18n.core.I18nUtil;
import org.openea.eap.module.system.service.language.I18nJsonDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**

 */
@Service
public class VisualdevServiceImpl extends SuperServiceImpl<VisualdevMapper, VisualdevEntity> implements VisualdevService {

    @Autowired
    private ConcurrencyUtils concurrencyVisualUtils;
    @Autowired
    private FlowFormService flowFormService;
    @Autowired
    private EapUserProvider userProvider;
    @Autowired
    private VisualDevTableCre visualDevTableCreUtil;
    @Autowired
    private ConcurrencyUtils concurrencyUtils;
    @Autowired
    private DbTableServiceImpl dbTableService;
    @Autowired
    private FilterService filterService;
    @Autowired
    private VisualdevReleaseService visualdevReleaseService;


    @Autowired
    private I18nJsonDataService i18nJsonDataService;
    @Override
    public List<VisualdevEntity> getList(PaginationVisualdev paginationVisualdev) {
        // 定义变量判断是否需要使用修改时间倒序
        boolean flag = false;
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(paginationVisualdev.getKeyword())) {
            flag = true;
            queryWrapper.lambda().like(VisualdevEntity::getFullName, paginationVisualdev.getKeyword());
        }
        queryWrapper.lambda().eq(VisualdevEntity::getType, paginationVisualdev.getType());
        if (StringUtil.isNotEmpty(paginationVisualdev.getCategory())) {
            flag = true;
            queryWrapper.lambda().eq(VisualdevEntity::getCategory, paginationVisualdev.getCategory());
        }
        // 排序
        queryWrapper.lambda().orderByAsc(VisualdevEntity::getSortCode).orderByDesc(VisualdevEntity::getCreatorTime);
        if (flag) {
            queryWrapper.lambda().orderByDesc(VisualdevEntity::getLastModifyTime);
        }
        Page<VisualdevEntity> page = new Page<>(paginationVisualdev.getCurrentPage(), paginationVisualdev.getPageSize());
        IPage<VisualdevEntity> userPage = this.page(page, queryWrapper);
        return paginationVisualdev.setData(userPage.getRecords(), page.getTotal());
    }


    @Override
    public List<VisualdevEntity> getList() {
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        // 排序
        queryWrapper.lambda().orderByAsc(VisualdevEntity::getSortCode).orderByDesc(VisualdevEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public VisualdevEntity getInfo(String id) {
        QueryWrapper<VisualdevEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevEntity::getId, id);
        VisualdevEntity entity = this.getOne(queryWrapper);
        return entity;
    }


    @Override
    public VisualdevEntity getReleaseInfo(String id) {
        VisualdevReleaseEntity visualdevReleaseEntity = visualdevReleaseService.getById(id);
        VisualdevEntity visualdevEntity = null;
        if (visualdevReleaseEntity != null) {
            visualdevEntity = JsonUtil.getJsonToBean(visualdevReleaseEntity, VisualdevEntity.class);
        }
        if (visualdevEntity == null) {
            visualdevEntity = getById(id);
        }
        return visualdevEntity;
    }

    @Override
    public Map<String, String> getTableMap(String formData) {
        Map<String, String> tableMap = new HashMap<>();
        if (StringUtil.isEmpty(formData)) {
            return tableMap;
        }
        FormDataModel formDataModel = JsonUtil.getJsonToBean(formData, FormDataModel.class);
        String fields = formDataModel.getFields();
        List<FieLdsModel> list = JsonUtil.getJsonToList(fields, FieLdsModel.class);
        list.forEach(item -> {
            this.solveTableName(item, tableMap);
        });
        return tableMap;
    }

    private void solveTableName(FieLdsModel item, Map tableMap) {
        ConfigModel config = item.getConfig();
        if (config != null) {
            List<FieLdsModel> children = config.getChildren();
            if ("table".equals(config.getExtnKey())) {
                if (children != null && children.size() > 0) {
                    FieLdsModel fieLdsModel = children.get(0);
                    String parentVModel = fieLdsModel.getConfig().getParentVModel();
                    String relationTable = fieLdsModel.getConfig().getRelationTable();
                    tableMap.put(parentVModel, relationTable);
                }
            }
            if (children != null) {
                children.forEach(item2 -> {
                    this.solveTableName(item2, tableMap);
                });
            }
        }
    }

    @Override
    @SneakyThrows
    public Boolean create(VisualdevEntity entity) {
        if (StringUtil.isEmpty(entity.getId())) {
            entity.setId(RandomUtil.uuId());
        }

        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);

        if (formDataModel != null) {
            //数据过滤信息
            Map<String, String> tableMap = this.getTableMap(entity.getFormData());
            // 保存app,pc过滤配置
            filterService.saveRuleList(entity.getId(), entity, 1, 1, tableMap);

            //是否开启安全锁
            Boolean concurrencyLock = formDataModel.getConcurrencyLock();
            Boolean logicalDelete = formDataModel.getLogicalDelete();
            int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();

            //判断是否要创表
            List<TableModel> tableModels = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);

            //有表
            if (tableModels.size() > 0) {
                List<TableModel> visualTables = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
                TableModel mainTable = visualTables.stream().filter(f -> f.getTypeId().equals("1")).findFirst().orElse(null);
                //判断自增是否匹配
                concurrencyUtils.checkAutoIncrement(primaryKeyPolicy, entity.getDbLinkId(), visualTables);
                //在主表创建锁字段
                try {
                    if (logicalDelete && mainTable != null) {
                        concurrencyUtils.creDeleteMark(mainTable.getTable(), entity.getDbLinkId());
                    }
                    if (concurrencyLock) {
                        concurrencyUtils.createVersion(mainTable.getTable(), entity.getDbLinkId());
                    }
                    if (primaryKeyPolicy == 2) {
                        concurrencyUtils.createFlowTaskId(mainTable.getTable(), entity.getDbLinkId());
                    }
                    concurrencyUtils.createFlowEngine(mainTable.getTable(), entity.getDbLinkId());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("创建锁字段失败");
                }
            }
        }
        entity.setCreatorUser(userProvider.get().getUserId());
        entity.setSortCode(RandomUtil.parses());
        return this.saveOrUpdateIgnoreLogic(entity);
    }

    @Override
    public boolean update(String id, VisualdevEntity entity) throws Exception {
        entity.setId(id);
        entity.setLastModifyUser(userProvider.get().getUserId());
        boolean b = this.updateById(entity);

        //代码生成修改时就要生成字段-做一些判断
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        if (formDataModel != null) {
            //是否开启安全锁
            Boolean concurrencyLock = formDataModel.getConcurrencyLock();
            Boolean logicalDelete = formDataModel.getLogicalDelete();
            int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();
            //判断是否要创表
            List<TableModel> visualTables = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
            //有表
            if (visualTables.size() > 0) {
                if (formDataModel != null) {
                    try {
                        TableModel mainTable = visualTables.stream().filter(f -> f.getTypeId().equals("1")).findFirst().orElse(null);
                        if (logicalDelete && mainTable != null) {
                            //在主表创建逻辑删除
                            concurrencyVisualUtils.creDeleteMark(mainTable.getTable(), entity.getDbLinkId());
                        }
                        if (concurrencyLock) {
                            //在主表创建锁字段
                            concurrencyVisualUtils.createVersion(mainTable.getTable(), entity.getDbLinkId());
                        }
                        if (primaryKeyPolicy == 2) {
                            concurrencyVisualUtils.createFlowTaskId(mainTable.getTable(), entity.getDbLinkId());
                        }
                        concurrencyVisualUtils.createFlowEngine(mainTable.getTable(), entity.getDbLinkId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //判断自增是否匹配
                concurrencyUtils.checkAutoIncrement(primaryKeyPolicy, entity.getDbLinkId(), visualTables);
            }
        }
        checkVisualdevI18n(entity);
        return b;
    }

    @Override
    public void delete(VisualdevEntity entity) throws WorkFlowException {
        if (entity != null) {

            try {
                //删除表单
                flowFormService.removeById(entity.getId());
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            List<String> ids = new ArrayList<>();
            ids.add(entity.getId());
            this.removeByIds(ids);
        }
    }

    @Override
    public Long getObjByEncode(String encode, Integer type) {
        QueryWrapper<VisualdevEntity> visualWrapper = new QueryWrapper<>();
        visualWrapper.lambda().eq(VisualdevEntity::getEnCode, encode).eq(VisualdevEntity::getType, type);
        Long count = this.baseMapper.selectCount(visualWrapper);
        return count;
    }

    @Override
    public Long getCountByName(String name, Integer type) {
        QueryWrapper<VisualdevEntity> visualWrapper = new QueryWrapper<>();
        visualWrapper.lambda().eq(VisualdevEntity::getFullName, name).eq(VisualdevEntity::getType, type);
        Long count = this.baseMapper.selectCount(visualWrapper);
        return count;
    }

    @Override
    public void createTable(VisualdevEntity entity) throws WorkFlowException {
        boolean isTenant = false; //TenantDataSourceUtil.isTenantColumn();
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        //是否开启安全锁
        Boolean concurrencyLock = formDataModel.getConcurrencyLock();
        int primaryKeyPolicy = formDataModel.getPrimaryKeyPolicy();
        Boolean logicalDelete = formDataModel.getLogicalDelete();

        Map<String, Object> formMap = JsonUtil.stringToMap(entity.getFormData());
        List<FieLdsModel> list = JsonUtil.getJsonToList(formMap.get("fields"), FieLdsModel.class);
        JSONArray formJsonArray = JsonUtil.getJsonToJsonArray(String.valueOf(formMap.get("fields")));
        List<TableModel> visualTables = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);

        List<FormAllModel> formAllModel = new ArrayList<>();
        RecursionForm recursionForm = new RecursionForm();
        recursionForm.setTableModelList(visualTables);
        recursionForm.setList(list);
        FormCloumnUtil.recursionForm(recursionForm, formAllModel);

        String tableName = "mt" + RandomUtil.uuId();

        String dbLinkId = entity.getDbLinkId();
        VisualTableModel model = new VisualTableModel(formJsonArray, formAllModel, tableName, dbLinkId, entity.getFullName(), concurrencyLock, primaryKeyPolicy, logicalDelete);
        List<TableModel> tableModelList = visualDevTableCreUtil.tableList(model);

        if (formDataModel != null) {
            try {
                TableModel mainTable = tableModelList.stream().filter(f -> f.getTypeId().equals("1")).findFirst().orElse(null);
                if (logicalDelete && mainTable != null) {
                    //在主表创建逻辑删除
                    concurrencyUtils.creDeleteMark(mainTable.getTable(), entity.getDbLinkId());
                }
                if (concurrencyLock && mainTable != null) {
                    //在主表创建锁字段
                    concurrencyUtils.createVersion(mainTable.getTable(), entity.getDbLinkId());
                }
                if (formDataModel.getPrimaryKeyPolicy() == 2) {
                    concurrencyUtils.createFlowTaskId(mainTable.getTable(), entity.getDbLinkId());
                }
                if (isTenant) {
                    for (TableModel tableModel : visualTables) {
                        concurrencyUtils.createTenantId(tableModel.getTable(), entity.getDbLinkId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        formMap.put("fields", formJsonArray);
        //更新
        entity.setFormData(JsonUtil.getObjectToString(formMap));
        entity.setVisualTables(JsonUtil.getObjectToString(tableModelList));
    }

    @Override
    public Map<String, String> getTableNameToKey(String modelId) {
        Map<String, String> childKeyMap = new HashMap<>();
        VisualdevEntity info = this.getInfo(modelId);
        FormDataModel formDataModel = JsonUtil.getJsonToBean(info.getFormData(), FormDataModel.class);
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> childFields = fieLdsModels.stream().filter(f -> ExtnKeyConsts.CHILD_TABLE.equals(f.getConfig().getExtnKey())).collect(Collectors.toList());
        childFields.stream().forEach(c ->
                childKeyMap.put(c.getConfig().getTableName().toLowerCase(), c.getVModel())
        );
        return childKeyMap;
    }

    @Override
    public Boolean getPrimaryDbField(String linkId, String table) throws Exception {
        DbTableFieldModel dbTableModel = dbTableService.getDbTableModel(linkId, table);
        List<DbFieldModel> data = dbTableModel.getDbFieldModelList();
        DbFieldModel dbFieldModel = data.stream().filter(DbFieldModel::getIsPrimaryKey).findFirst().orElse(null);
        if (dbFieldModel != null) {
            return dbFieldModel.getIsAutoIncrement() != null && dbFieldModel.getIsAutoIncrement();
        } else {
            return null;
        }
    }


//////////////////////////////////////////////////////////////////    @Override/////////////

    @Override
    public void loadI18nData(DataInfoVO vo){
        if(vo==null || vo.getHasI18n()==null) return;
        boolean hasI18n = (1==vo.getHasI18n());
        if(!hasI18n){
            return;
        }

        Map<String, Object> mapI18nParam = new HashMap();
        mapI18nParam.put("checkLost", false);
        mapI18nParam.put("needChange", true);
        mapI18nParam.put("modelCode", vo.getEnCode());
        mapI18nParam.put("i18nPrefix", vo.getEnCode());

        if (StringUtil.isNotEmpty(vo.getFormData())) {
            vo.setFormData(checkI18nConfigJson(vo.getFormData(), mapI18nParam));
        }
        if (StringUtil.isNotEmpty(vo.getColumnData())) {
            vo.setColumnData(checkI18nConfigJson(vo.getColumnData(), mapI18nParam));
        }
    }

    protected void checkVisualdevI18n(VisualdevEntity entity) throws Exception {
        if(entity==null || entity.getHasI18n()==null) return;
        boolean hasI18n = (1==entity.getHasI18n());
        if (!hasI18n) {
            return;
        }

        Map<String, Object> mapI18nParam = new HashMap();
        mapI18nParam.put("modelName", entity.getFullName());
        mapI18nParam.put("modelCode", entity.getEnCode());
        mapI18nParam.put("i18nPrefix", entity.getEnCode());

        mapI18nParam.put("checkLost", true);
        mapI18nParam.put("needChange", true);
        mapI18nParam.put("lostI18nRes", new HashMap<String, String>());
        if (StringUtil.isNotEmpty(entity.getFormData())) {
            checkI18nConfigJson(entity.getFormData(), mapI18nParam);
        }
        if (StringUtil.isNotEmpty(entity.getColumnData())) {
            checkI18nConfigJson(entity.getColumnData(), mapI18nParam);
        }

        Map<String, String> lostI18nRes = (Map<String, String>)mapI18nParam.get("lostI18nRes");
        if (lostI18nRes != null && !lostI18nRes.isEmpty()) {
            try{
                addI18nData(lostI18nRes, "modelDev", entity.getFullName());
            }catch(Exception e){
                log.error("addI18nData " + e.getMessage(), e);
            }
        }
    }

    @Async
    public void addI18nData(Map<String, String> i18nDataRes, String module, String moduleDesc) throws Exception {
        AtomicInteger needReloadI18n = new AtomicInteger(0);
        if (CollectionUtil.isNotEmpty(i18nDataRes)) {
            List<Map<String, String>> itemList = new ArrayList<>();
            i18nDataRes.keySet().stream().forEach(i18nKey -> {
                String label = i18nDataRes.get(i18nKey);
                // 过滤掉通用或不必要的翻译
                // xx.placeholder -> 请输入/请选择/数字文本
                if(i18nKey.endsWith(".placeholder")){
                    if("请输入".equals(label) || "请选择".equals(label) || "数字文本".equals(label)){
                        return;
                    }
                }
                // 检查是否有添加中的数据(I18nUtil默认获取的是缓存数据)
                if(!i18nJsonDataService.checkI18nExist(module, i18nKey)){
                    String desc = moduleDesc+"-"+label;
                    if(i18nKey.startsWith("onlineDev.")){
                        desc = "onlineDev-"+label;
                    }
                    Map<String, String> item = new HashMap<>();
                    item.put("key", i18nKey);
                    item.put("desc", desc);
                    item.put("label", label);
                    itemList.add(item);
                    //i18nJsonDataService.createI18nData(module, i18nKey, desc, label);
                    needReloadI18n.set(1);
                }
            });
            if(itemList.size()>0){
                i18nJsonDataService.createI18nItemList(module, itemList);
                needReloadI18n.set(1);
            }
        }
        if(1==needReloadI18n.get()){
            I18nUtil.reloadI18nApiData();
        }
    };

    private String checkI18nConfigJson(String configJson, Map<String, Object> mapI18nParam){
        if(ObjectUtil.isEmpty(configJson)) {
            return configJson;
        }
        Map<String, Object> configJsonMap = JsonUtil.stringToMap(configJson.trim());
        if(configJsonMap == null && configJsonMap.isEmpty()) {
            return configJson;
        }

        int isChange = 0;

        //处理字段
        Object fieldsObj = configJsonMap.get("fields");
        List<Map<String, Object>> fieldsList = null;
        if(fieldsObj != null) {
            fieldsList = (List<Map<String, Object>>)fieldsObj;
            if(fieldsList != null && !fieldsList.isEmpty()) {
                //mapI18nParam.put("mapI18nParam", "add");
                int count = checkI18nResource( fieldsList, mapI18nParam);
                if(count>0) {
                    configJsonMap.put("fields", fieldsList);
                    isChange = 1;
                }
            }
        }
        //处理查询条件
        Object searchObj = configJsonMap.get("searchList");
        List<Map<String, Object>> searchList = null;
        if(searchObj != null) {
            searchList = (List<Map<String, Object>>)searchObj;
            if(searchList != null && !searchList.isEmpty()) {
                //mapI18nParam.put("mapI18nParam", "search");
                int count = checkI18nResource( searchList, mapI18nParam);
                if(count>0) {
                    configJsonMap.put("searchList", searchList);
                    isChange = 1;
                }
            }
        }
        //处理列
        Object columnListObj = configJsonMap.get("columnList");
        List<Map<String, Object>> columnList = null;
        if(columnListObj != null) {
            columnList = (List<Map<String, Object>>)columnListObj;
            if(columnList != null && !columnList.isEmpty()) {
                //mapI18nParam.put("mapI18nParam", "add");
                int count = checkI18nResource( columnList, mapI18nParam);
                if(count>0) {
                    configJsonMap.put("columnList", columnList);
                    isChange = 1;
                }
            }
        }

        //处理button columnBtnsList, btnsList
        List<Map<String, Object>> buttonList =  null;
        Object btnsListObj = configJsonMap.get("btnsList");
        if(btnsListObj != null) {
            buttonList = (List<Map<String, Object>>)btnsListObj;
            if(buttonList != null && !buttonList.isEmpty()) {
                int count = checkI18nButton( buttonList, mapI18nParam);
                if(count>0) {
                    configJsonMap.put("btnsList", buttonList);
                    isChange = 1;
                }
            }
        }
        Object colBtnsListObj = configJsonMap.get("columnBtnsList");
        if(colBtnsListObj != null) {
            buttonList = (List<Map<String, Object>>)colBtnsListObj;
            if(buttonList != null && !buttonList.isEmpty()) {
                int count = checkI18nButton( buttonList, mapI18nParam);
                if(count>0){
                    configJsonMap.put("columnBtnsList", buttonList);
                    isChange = 1;
                }
            }
        }

        boolean i18nNeedChange = MapUtil.getBool(mapI18nParam, "needChange", true);
        if(i18nNeedChange && isChange == 1) {
            return JsonUtil.getObjectToString(configJsonMap);
        } else {
            return configJson;
        }
    }

    public static Set<String> normalBtnSet = new HashSet<>();
    public static Set<String> normalFieldSet = new HashSet<>();
    public static Set<String> normalPlaceholderSet = new HashSet<>();
    static {
        normalBtnSet.addAll(Arrays.asList(new String[]{"add", "edit", "remove", "batchRemove", "delete", "save", "cancel", "submit", "detail"}));
        normalBtnSet.addAll(Arrays.asList(new String[]{"search", "query", "reset", "import", "export", "download","upload", "print", "batchPrint"}));
        normalPlaceholderSet.addAll(Arrays.asList(new String[]{"请输入","请选择","数字文本"}));
        normalFieldSet.addAll(Arrays.asList(new String[]{"id", "sort", "createBy", "createTime", "updateBy", "updateTime"}));
        //normalFieldSet.addAll(Arrays.asList(new String[]{"code", "name" }));
    }

    int checkI18nResource( List<Map<String, Object>> itemList, Map<String, Object>  mapI18nParam){
        int count = 0;
        boolean i18nNeedChange = MapUtil.getBool(mapI18nParam, "needChange", true);
        boolean i18nCheckLost = MapUtil.getBool(mapI18nParam, "checkLost", true);
        Map<String, String> lostI18nRes = (Map<String, String>)mapI18nParam.get("lostI18nRes");

        for(int i = 0, len = itemList.size(); i < len; i++) {
            Map<String, Object> itemMap = itemList.get(i);
            if (itemMap == null || itemMap.isEmpty()) {
                continue;
            }
            Map<String, Object> configMap = (Map<String, Object>) itemMap.get("__config__");
            if (configMap == null || configMap.isEmpty()) {
                continue;
            }
            String i18nPrefix = (String) mapI18nParam.get("i18nPrefix");
            // itemMap: __vModel__, label(search)
            String fieldName = (String) itemMap.get("__vModel__");
            String parentKeyPrefix = (String) mapI18nParam.get("parentKeyPrefix");
            if (parentKeyPrefix == null) {
                parentKeyPrefix = i18nPrefix;
            }
            String currentKeyPrefix = parentKeyPrefix;
            if(StringUtil.isNotEmpty(fieldName)) {
                currentKeyPrefix += "." + fieldName;
            }
            String extnKey = (String)configMap.get("extnKey");

            // configMap: label/tipLabel/placeholder
            String[] keys = new String[]{"label", "tipLabel", "placeholder"};
            for (String key : keys) {
                String originValue = null;
                if (itemMap.containsKey(key)) {
                    originValue = (String) itemMap.get(key);
                }
                if (ObjectUtil.isEmpty(originValue) && configMap.containsKey(key)) {
                    originValue = (String) configMap.get(key);
                }

                // 仅检查则跳过无数据的字段
                if(ObjectUtil.isEmpty(originValue)){
                    continue;
                }
                // 跳过不需要更改返回结果的常用字段
                if(normalFieldSet.contains(fieldName) && !i18nNeedChange) {
                    continue;
                }

                // 检查常用placeholder
                if("placeholder".equals(key) && normalPlaceholderSet.contains(originValue)){
                    if(!i18nNeedChange){
                        continue;
                    }
                    String i18nKey = "onlineDev.inputPh";
                    // input / select / 数字文本
                    if(originValue.indexOf("数字文本")>-1){
                        i18nKey = "onlineDev.numberPh";
                    }else if(originValue.indexOf("选择")>-1){
                        i18nKey = "onlineDev.selectPh";
                    }else if(originValue.indexOf("输入")>-1){
                        i18nKey = "onlineDev.inputPh";
                    }
                    String i18nLabel = I18nUtil.getMessage(i18nKey);
                    if(ObjectUtil.isNotEmpty(i18nLabel) && !i18nLabel.equals(i18nKey) && !i18nLabel.equals(originValue)){
                        if(itemMap.containsKey(key)){
                            itemMap.put(key, i18nLabel);
                            count++;
                        }
                        if(configMap.containsKey(key)){
                            configMap.put(key, i18nLabel);
                            count++;
                        }
                    }
                    continue;
                }

                // xx=label, XXTip=tipLabel, XXPh=placeholder
                String i18nKey = currentKeyPrefix;
                if( "tipLabel".equals(key)){
                    i18nKey = currentKeyPrefix+"Tip";
                }else if( "placeholder".equals(key)){
                    i18nKey = currentKeyPrefix+"Ph";
                }else {
                    i18nKey = currentKeyPrefix+"Label";
                }
                String i18nLabel = I18nUtil.getMessage(i18nKey);
                if(ObjectUtil.isEmpty(i18nLabel) || i18nLabel.equals(i18nKey)){
                    // [modelxxx].[field]+[key] => onlineDev.[field]+[key] 或 onlineDev.常用字段
                    i18nKey = "onlineDev."+fieldName;
                    if( "tipLabel".equals(key)){
                        i18nKey = "onlineDev."+fieldName+"Tip";
                    }else if( "placeholder".equals(key)){
                        i18nKey = "onlineDev."+fieldName+"Ph";
                    }
//                    String[] parts = i18nKey.split("\\.");
//                    if(parts.length > 1){
//                        i18nKey = "onlineDev."+parts[parts.length-2];
//                    }
                    i18nLabel = I18nUtil.getMessage(i18nKey);
                }
                if(i18nNeedChange){
                    if(ObjectUtil.isNotEmpty(i18nLabel) && !i18nLabel.equals(i18nKey) && !i18nLabel.equals(originValue)){
                        if(itemMap.containsKey(key)){
                            itemMap.put(key, i18nLabel);
                            count++;
                        }
                        if(configMap.containsKey(key)){
                            configMap.put(key, i18nLabel);
                            count++;
                        }
                    }
                }
                if(i18nCheckLost) {
                    if (ObjectUtil.isEmpty(i18nLabel) || i18nLabel.equals(i18nKey)) {
                        if(!normalFieldSet.contains(fieldName)){
                            if( "tipLabel".equals(key)){
                                i18nKey = currentKeyPrefix+"Tip";
                            }else if( "placeholder".equals(key)){
                                i18nKey = currentKeyPrefix+"Ph";
                            }else {
                                i18nKey = currentKeyPrefix+"Label";
                            }
                        }
                        if(!lostI18nRes.containsKey(i18nKey)){
                            lostI18nRes.put(i18nKey, originValue);
                        }
                    }
                }
            }
            List<Map<String, Object>> childrenList = (List<Map<String, Object>>) configMap.get("children");
            if (childrenList != null && !childrenList.isEmpty()) {
                // 针对 tab/tabItem 特别处理
                if("tab".equals(extnKey)){
                    // tab componentName
                    String componentName = (String) configMap.get("componentName");
                    // tabItem title
                    for(int k=0; k<childrenList.size(); k++){
                        JSONObject tabItem = JSONUtil.parseObj(childrenList.get(k));
                        if(tabItem.containsKey("title")){
                            String title = (String) tabItem.get("title");
                            String i18nKey = currentKeyPrefix + "." + componentName + "." +k;
                            String i18nLabel = I18nUtil.getMessage(i18nKey);
                            if(i18nNeedChange){
                                if (ObjectUtil.isNotEmpty(i18nLabel) && !i18nLabel.equals(i18nKey) && !i18nLabel.equals(title)) {
                                    tabItem.put("title", i18nLabel);
                                    childrenList.set(k, tabItem);
                                    count++;
                                }
                            }
                            if(i18nCheckLost) {
                                if (ObjectUtil.isEmpty(i18nLabel) || i18nLabel.equals(i18nKey)) {
                                    if(!lostI18nRes.containsKey(i18nKey)){
                                        lostI18nRes.put(i18nKey, title);
                                    }
                                }
                            }
                        }
                    }
                }
                mapI18nParam.put("parentKeyPrefix", currentKeyPrefix);
                count += checkI18nResource(childrenList, mapI18nParam);
                mapI18nParam.remove("parentKeyPrefix");
            }
        }
        return count;
    }

    int checkI18nButton( List<Map<String, Object>> itemList,  Map<String, Object>  mapI18nParam){
        int count = 0;
        boolean i18nNeedChange = MapUtil.getBool(mapI18nParam, "needChange", true);
        boolean i18nCheckLost = MapUtil.getBool(mapI18nParam, "checkLost", true);
        Map<String, String> lostI18nRes = (Map<String, String>)mapI18nParam.get("lostI18nRes");
//        "btnsList": [{
//            "icon": "el-icon-plus",
//            "label": "新增",
//            "value": "add"
//        }]
        // add/edit/remove
        for(int i = 0, len = itemList.size(); i < len; i++) {
            Map<String, Object> itemMap = itemList.get(i);
            if (itemMap == null || itemMap.isEmpty()) {
                continue;
            }
            String btnName = MapUtil.getStr(itemMap, "value");
            String i18nKey = "onlineDev."+btnName+"Btn";
            if(!normalBtnSet.contains(btnName)){
                String i18nPrefix = (String) mapI18nParam.get("i18nPrefix");
                i18nKey = i18nPrefix+"."+btnName+"Btn";
            }
            String label = MapUtil.getStr(itemMap, "label");
            String i18nLabel = I18nUtil.getMessage(i18nKey);
            if(i18nNeedChange){
                if(StringUtil.isNotEmpty(i18nLabel)
                        && !i18nLabel.equals(i18nKey)
                        && !label.equals(i18nLabel)) {
                    itemMap.put("label", i18nLabel);
                    count++;
                }
            }
            // && !normalBtnSet.contains(btnName)
            if(i18nCheckLost){
                if(StringUtil.isEmpty(i18nLabel)
                        || i18nLabel.equals(i18nKey)) {
                    if(!lostI18nRes.containsKey(i18nKey)){
                        lostI18nRes.put(i18nKey, label);
                    }
                }
            }
        }
        return count;
    }

}

