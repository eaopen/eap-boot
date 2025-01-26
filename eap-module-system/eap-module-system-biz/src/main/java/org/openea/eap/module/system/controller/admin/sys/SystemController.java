package org.openea.eap.module.system.controller.admin.sys;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import org.openea.eap.framework.common.pojo.PageParam;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.util.object.BeanUtils;
import static org.openea.eap.framework.common.pojo.CommonResult.success;

import org.openea.eap.framework.excel.core.util.ExcelUtils;

import org.openea.eap.framework.apilog.core.annotation.ApiAccessLog;
import static org.openea.eap.framework.apilog.core.enums.OperateTypeEnum.*;

import org.openea.eap.module.system.controller.admin.sys.vo.*;
import org.openea.eap.module.system.dal.dataobject.sys.SystemDO;
import org.openea.eap.module.system.service.sys.SystemService;

@Tag(name = "管理后台 - 系统应用")
@RestController
@RequestMapping("/system/system")
@Validated
public class SystemController {

    @Resource
    private SystemService systemService;

    @PostMapping("/create")
    @Operation(summary = "创建系统应用")
    @PreAuthorize("@ss.hasPermission('system:system:create')")
    public CommonResult<Long> createSystem(@Valid @RequestBody SystemSaveReqVO createReqVO) {
        return success(systemService.createSystem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新系统应用")
    @PreAuthorize("@ss.hasPermission('system:system:update')")
    public CommonResult<Boolean> updateSystem(@Valid @RequestBody SystemSaveReqVO updateReqVO) {
        systemService.updateSystem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除系统应用")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:system:delete')")
    public CommonResult<Boolean> deleteSystem(@RequestParam("id") Long id) {
        systemService.deleteSystem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得系统应用")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:system:query')")
    public CommonResult<SystemRespVO> getSystem(@RequestParam("id") Long id) {
        SystemDO system = systemService.getSystem(id);
        return success(BeanUtils.toBean(system, SystemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得系统应用分页")
    @PreAuthorize("@ss.hasPermission('system:system:query')")
    public CommonResult<PageResult<SystemRespVO>> getSystemPage(@Valid SystemPageReqVO pageReqVO) {
        PageResult<SystemDO> pageResult = systemService.getSystemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SystemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出系统应用 Excel")
    @PreAuthorize("@ss.hasPermission('system:system:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSystemExcel(@Valid SystemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SystemDO> list = systemService.getSystemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "系统应用.xls", "数据", SystemRespVO.class,
                        BeanUtils.toBean(list, SystemRespVO.class));
    }

}