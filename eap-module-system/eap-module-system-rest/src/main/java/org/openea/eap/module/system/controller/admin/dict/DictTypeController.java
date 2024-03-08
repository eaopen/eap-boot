package org.openea.eap.module.system.controller.admin.dict;

import cn.hutool.core.map.MapUtil;
import io.swagger.v3.oas.annotations.Parameters;
import org.apache.commons.lang3.math.NumberUtils;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.pojo.PageParam;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.framework.excel.core.util.ExcelUtils;
import org.openea.eap.framework.operatelog.core.annotations.OperateLog;
import org.openea.eap.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import org.openea.eap.module.system.controller.admin.dict.vo.type.DictTypeRespVO;
import org.openea.eap.module.system.controller.admin.dict.vo.type.DictTypeSaveReqVO;
import org.openea.eap.module.system.controller.admin.dict.vo.type.DictTypeSimpleRespVO;
import org.openea.eap.module.system.dal.dataobject.dict.DictDataDO;
import org.openea.eap.module.system.dal.dataobject.dict.DictTypeDO;
import org.openea.eap.module.system.service.dict.DictDataService;
import org.openea.eap.module.system.service.dict.DictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

import static org.openea.eap.framework.common.pojo.CommonResult.success;
import static org.openea.eap.framework.common.util.collection.CollectionUtils.filterList;
import static org.openea.eap.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static org.openea.eap.module.system.dal.dataobject.permission.MenuDO.ID_ROOT;

@Tag(name = "管理后台 - 字典类型")
@RestController
@RequestMapping("/system/dict-type")
@Validated
public class DictTypeController {

    @Resource
    private DictTypeService dictTypeService;

    @Resource
    private DictDataService dictDataService;

    @PostMapping("/create")
    @Operation(summary = "创建字典类型")
    @PreAuthorize("@ss.hasPermission('system:dict:create')")
    public CommonResult<Long> createDictType(@Valid @RequestBody DictTypeSaveReqVO createReqVO) {
        Long dictTypeId = dictTypeService.createDictType(createReqVO);
        return success(dictTypeId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改字典类型")
    @PreAuthorize("@ss.hasPermission('system:dict:update')")
    public CommonResult<Boolean> updateDictType(@Valid @RequestBody DictTypeSaveReqVO updateReqVO) {
        dictTypeService.updateDictType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除字典类型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> deleteDictType(Long id) {
        dictTypeService.deleteDictType(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得字典类型的分页列表")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<PageResult<DictTypeRespVO>> pageDictTypes(@Valid DictTypePageReqVO pageReqVO) {
        PageResult<DictTypeDO> pageResult = dictTypeService.getDictTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DictTypeRespVO.class));
    }

    @Operation(summary = "/查询字典类型详细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @GetMapping(value = "/get")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<DictTypeRespVO> getDictType(@RequestParam("id") Long id) {
        DictTypeDO dictType = dictTypeService.getDictTypeById(id);
        return success(BeanUtils.toBean(dictType, DictTypeRespVO.class));
    }

    @GetMapping(value = {"/list-all-simple", "simple-list"})
    @Operation(summary = "获得全部字典类型列表", description = "包括开启 + 禁用的字典类型，主要用于前端的下拉选项")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<List<DictTypeSimpleRespVO>> getSimpleDictTypeList() {
        List<DictTypeDO> list = dictTypeService.getDictTypeList();
        return success(BeanUtils.toBean(list, DictTypeSimpleRespVO.class));
    }

    @Operation(summary = "导出数据类型")
    @GetMapping("/export")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    @OperateLog(type = EXPORT)
    public void export(HttpServletResponse response, @Valid DictTypePageReqVO exportReqVO) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DictTypeDO> list = dictTypeService.getDictTypePage(exportReqVO).getList();
        // 导出
        ExcelUtils.write(response, "字典类型.xls", "数据", DictTypeRespVO.class,
                BeanUtils.toBean(list, DictTypeRespVO.class));
    }



    @GetMapping("/list-all-parent/{id}")
    @Operation(summary = "获取所有字典分类下拉框列表", description = "包括开启 + 禁用的字典类型，主要用于前端的下拉选项")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<Map<String,Object>> getParentDictTypeList(@PathVariable(value = "id", required = true) String id) {
        List<DictTypeDO> list = dictTypeService.getDictTypeList();
        if (!"0".equals(id)){
            list.remove(dictTypeService.getDictType(id));
        }
        Map<Long, Map> treeNodeMap = new LinkedHashMap<>();
        list.forEach(dictTypeDO -> treeNodeMap.put(dictTypeDO.getId(), transToMap(dictTypeDO)));
        treeNodeMap.values().stream().filter(node -> !MapUtil.getStr(node,"parentId").equals(ID_ROOT)).forEach(childNode -> {
            // 获得父节点
            Map parentNode = treeNodeMap.get(Long.parseLong(MapUtil.getStr(childNode,"parentId")));
            if (parentNode == null) {
                String parentId = MapUtil.getStr(childNode,"parentId");
                if(!"-1".equals(parentId) && !"0".equals(parentId)) {
                    LoggerFactory.getLogger(getClass()).warn("[buildRouterTree][resource({}) 找不到父资源({})]",
                            MapUtil.getStr(childNode, "id"), MapUtil.getStr(childNode, "parentId"));
                }
                return;
            }
            // 将自己添加到父节点中
            if (parentNode.get("children") == null) {
                parentNode.put("children",new ArrayList<>());
            }
            List<Map<String,Object>> children = (List<Map<String, Object>>) parentNode.get("children");
            children.add(childNode);
            parentNode.put("children",children);
        });
        List<Map> rootMenue = filterList(treeNodeMap.values(), node -> ("-1").equals(MapUtil.getStr(node, "parentId")));
        Map map=new HashMap();
        map.put("list",rootMenue);
        return success(map);
    }

    /**
     * 获取字典分类
     *
     * @param dictionaryTypeId 分类id、分类编码
     * @return ignore
     */
    @Operation(summary = "获取某个字典数据下拉框列表")
    @Parameters({
            @Parameter(name = "dictionaryTypeId", description = "数据分类id", required = true)
    })
    @GetMapping("/{dictionaryTypeId}/Data/Selector")
    public CommonResult<Map> selectorOneTreeView(@PathVariable("dictionaryTypeId") String dictionaryTypeId) {
        DictTypeDO dictType = null;
        if(NumberUtils.isDigits(dictionaryTypeId)){
            dictType = dictTypeService.getDictTypeById(new Long(dictionaryTypeId));
        }
        if(dictType==null){
            dictType = dictTypeService.getDictType(dictionaryTypeId);
        }
        List<Map<String,Object>> listV1=new ArrayList<>();
        if(dictType!=null){
            List<DictDataDO> collect = dictDataService.getDictData(dictType.getType());
//        List<DictDataDO> collect = dictDataService.getDictDataList().stream().filter(t -> dictType.getType().equals(t.getDictType())).collect(Collectors.toList());
            for (DictDataDO dictDataDO : collect) {
                Map<String,Object> map=new HashMap<>();
                // fix id same as value
                map.put("id",dictDataDO.getId());
                //map.put("id",dictDataDO.getValue());
                map.put("enCode",dictDataDO.getValue());
                //map.put("parentId",dictionaryTypeId);
                map.put("fullName",dictDataDO.getLabel());
                map.put("hasChildren",false);
                listV1.add(map);
            }
        }
        Map map=new HashMap();
        map.put("list",listV1);
        return success(map);
    }

    Map transToMap(DictTypeDO dictTypeDO){
        Map<String,Object> map=new HashMap<>();
        map.put("id",dictTypeDO.getId());
        map.put("parentId",(dictTypeDO.getParentId()==null || dictTypeDO.getParentId()==0)?-1:dictTypeDO.getParentId());
        map.put("enCode",dictTypeDO.getType());  //兼容extn
        map.put("fullName",dictTypeDO.getName()); //兼容extn
        map.put("value",dictTypeDO.getType());
        map.put("label",dictTypeDO.getName());
        map.put("dataType",dictTypeDO.getDataType());
        map.put("hasChildren",false);
        map.put("children",null);
        return map;
    }


}
