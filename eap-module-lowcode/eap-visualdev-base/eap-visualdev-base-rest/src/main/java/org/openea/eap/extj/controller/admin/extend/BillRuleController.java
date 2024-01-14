package org.openea.eap.extj.controller.admin.extend;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.openea.eap.extj.annotation.HandleLog;
import org.openea.eap.extj.base.ActionResult;
import org.openea.eap.extj.base.controller.SuperController;
import org.openea.eap.extj.base.model.billrule.BillRuleCrForm;
import org.openea.eap.extj.base.model.billrule.BillRuleInfoVO;
import org.openea.eap.extj.base.model.billrule.BillRuleListVO;
import org.openea.eap.extj.base.model.billrule.BillRuleUpForm;
import org.openea.eap.extj.base.model.dataInterface.PaginationDataInterface;
import org.openea.eap.extj.base.vo.DownloadVO;
import org.openea.eap.extj.base.vo.PageListVO;
import org.openea.eap.extj.base.vo.PaginationVO;
import org.openea.eap.extj.config.ConfigValueUtil;
import org.openea.eap.extj.constant.MsgCode;
import org.openea.eap.extj.exception.DataException;
import org.openea.eap.extj.extend.entity.BillRuleEntity;
import org.openea.eap.extj.extend.service.BillRuleService;
import org.openea.eap.extj.permission.entity.UserEntity;
import org.openea.eap.extj.permission.service.UserService;
import org.openea.eap.extj.util.*;
import org.openea.eap.extj.util.enums.ModuleTypeEnum;
import org.openea.eap.module.system.service.dict.DictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 单据规则
 *
 */
@Tag(name = "单据规则", description = "BillRule")
@RestController
@RequestMapping("/vdsys/BillRule")
public class BillRuleController extends SuperController<BillRuleService, BillRuleEntity> {

    @Autowired
    private DataFileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private UserService userService;

    @Autowired
    private DictDataService dictDataService;
    /**
     * 列表
     *
     * @param pagination 分页参数
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "查询")
    @Operation(summary = "获取单据规则列表(带分页)")
    @SaCheckPermission("system.billRule")
    @GetMapping
    public ActionResult<PageListVO<BillRuleListVO>> list(PaginationDataInterface pagination) {
        List<BillRuleEntity> list = billRuleService.getList(pagination);
        List<BillRuleListVO> listVO = new ArrayList<>();
        Map<String, String> mapDict = null;
        list.forEach(entity->{
            BillRuleListVO vo = JsonUtil.getJsonToBean(entity, BillRuleListVO.class);
            if(StringUtil.isNotEmpty(entity.getCategory())){
                vo.setCategory(getCategoryName(entity.getCategory(), mapDict));
            }

            UserEntity userEntity = userService.getInfo(entity.getCreatorUserId());
            if(userEntity != null){
                vo.setCreatorUser(userEntity.getRealName() + "/" + userEntity.getAccount());
            }
            listVO.add(vo);
        });

        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    private String getCategoryName(String category, Map<String, String> mapDict){
        if(mapDict!=null && mapDict.containsKey(category)){
            return mapDict.get(category);
        }
        return category;
    }

    /**
     * 列表
     *
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "查询")
    @Operation(summary = "获取单据规则下拉框")
    @GetMapping("/Selector")
    public ActionResult selectList(PaginationDataInterface pagination) {
        List<BillRuleEntity> list = billRuleService.getListByCategory(pagination.getCategoryId(),pagination);
        List<BillRuleListVO> listVO = new ArrayList<>();
        Map<String, String> mapDict = null;
        list.forEach(entity->{
            BillRuleListVO vo = JsonUtil.getJsonToBean(entity, BillRuleListVO.class);
            if(StringUtil.isNotEmpty(entity.getCategory())){
                vo.setCategory(getCategoryName(entity.getCategory(), mapDict));
            }

            UserEntity userEntity = userService.getInfo(entity.getCreatorUserId());
            if(userEntity != null){
                vo.setCreatorUser(userEntity.getRealName() + "/" + userEntity.getAccount());
            }
            listVO.add(vo);
        });
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }


    /**
     * 更新组织状态
     *
     * @param id 主键值
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "修改")
    @Operation(summary = "更新单据规则状态")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.billRule")
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) {
        BillRuleEntity entity = billRuleService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            billRuleService.update(entity.getId(), entity);
            return ActionResult.success(MsgCode.SU004.get());
        }
        return ActionResult.fail(MsgCode.FA002.get());
    }

    /**
     * 信息
     *
     * @param id 主键值
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "查询")
    @Operation(summary = "获取单据规则信息")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.billRule")
    @GetMapping("/{id}")
    public ActionResult<BillRuleInfoVO> info(@PathVariable("id") String id) throws DataException {
        BillRuleEntity entity = billRuleService.getInfo(id);
        BillRuleInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, BillRuleInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 获取单据流水号
     *
     * @param enCode 参数编码
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "查询")
    @Operation(summary = "获取单据流水号(工作流调用)")
    @Parameters({
            @Parameter(name = "enCode", description = "参数编码", required = true)
    })
    @GetMapping("/BillNumber/{enCode}")
    public ActionResult getBillNumber(@PathVariable("enCode") String enCode) throws DataException {
        String data = billRuleService.getBillNumber(enCode, false);
        return ActionResult.success("获取成功", data);
    }

    /**
     * 新建
     *
     * @param billRuleCrForm 实体对象
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "新增")
    @Operation(summary = "添加单据规则")
    @Parameters({
            @Parameter(name = "billRuleCrForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.billRule")
    @PostMapping
    public ActionResult create(@RequestBody @Valid BillRuleCrForm billRuleCrForm) {

        BillRuleEntity entity = JsonUtil.getJsonToBean(billRuleCrForm, BillRuleEntity.class);
        if (billRuleService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (billRuleService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        billRuleService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 更新
     *
     * @param billRuleUpForm 实体对象
     * @param id             主键值
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "修改")
    @Operation(summary = "修改单据规则")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "billRuleUpForm", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.billRule")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody BillRuleUpForm billRuleUpForm) {
        BillRuleEntity entity = JsonUtil.getJsonToBean(billRuleUpForm, BillRuleEntity.class);
        if (billRuleService.isExistByFullName(entity.getFullName(), id)) {
            return ActionResult.fail(MsgCode.EXIST001.get());
        }
        if (billRuleService.isExistByEnCode(entity.getEnCode(), id)) {
            return ActionResult.fail(MsgCode.EXIST002.get());
        }
        boolean flag = billRuleService.update(id, entity);
        if (!flag) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "删除")
    @Operation(summary = "删除单据规则")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.billRule")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        BillRuleEntity entity = billRuleService.getInfo(id);
        if (entity != null) {
            if (!StringUtil.isEmpty(entity.getOutputNumber())) {
                return ActionResult.fail("单据已经被使用,不允许被删除");
            } else {
                billRuleService.delete(entity);
                return ActionResult.success(MsgCode.SU003.get());
            }
        }
        return ActionResult.fail(MsgCode.FA003.get());
    }

    /**
     * 导出单据规则
     *
     * @param id 打印模板id
     * @return ignore
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "导出")
    @Operation(summary = "导出")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @SaCheckPermission("system.billRule")
    @GetMapping("/{id}/Action/Export")
    public ActionResult<DownloadVO> export(@PathVariable String id) {
        BillRuleEntity entity = billRuleService.getInfo(id);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(entity, configValueUtil.getTemporaryFilePath(), entity.getFullName(), ModuleTypeEnum.SYSTEM_BILLRULE.getTableName());
        return ActionResult.success(downloadVO);
    }

    /**
     * 导入单据规则
     *
     * @param multipartFile 备份json文件
     * @return 执行结果标识
     */
    @HandleLog(moduleName = "单据规则", requestMethod = "导入")
    @Operation(summary = "导入")
    @SaCheckPermission("system.billRule")
    @PostMapping(value = "/Action/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult importData(@RequestPart("file") MultipartFile multipartFile) throws DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.SYSTEM_BILLRULE.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile);
        BillRuleEntity entity = JsonUtil.getJsonToBean(fileContent, BillRuleEntity.class);
        return billRuleService.ImportData(entity);
    }
}
