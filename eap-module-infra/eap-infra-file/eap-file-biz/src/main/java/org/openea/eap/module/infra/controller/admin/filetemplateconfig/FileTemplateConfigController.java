package org.openea.eap.module.infra.controller.admin.filetemplateconfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.FileTemplateConfigPageReqVO;
import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.FileTemplateConfigRespVO;
import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.FileTemplateConfigSaveReqVO;
import org.openea.eap.module.infra.dal.dataobject.filetemplateconfig.FileTemplateConfigDO;
import org.openea.eap.module.infra.service.filetemplateconfig.FileTemplateConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static org.openea.eap.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件模板配置")
@RestController
@RequestMapping("/infra/file-template-config")
@Validated
public class FileTemplateConfigController {

    @Resource
    private FileTemplateConfigService fileTemplateConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建文件模板配置")
    @PreAuthorize("@ss.hasPermission('infra:file-template-config:create')")
    public CommonResult<Long> createFileTemplateConfig(@Valid @RequestBody FileTemplateConfigSaveReqVO createReqVO) {
        return success(fileTemplateConfigService.createFileTemplateConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文件模板配置")
    @PreAuthorize("@ss.hasPermission('infra:file-template-config:update')")
    public CommonResult<Boolean> updateFileTemplateConfig(@Valid @RequestBody FileTemplateConfigSaveReqVO updateReqVO) {
        fileTemplateConfigService.updateFileTemplateConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件模板配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:file-template-config:delete')")
    public CommonResult<Boolean> deleteFileTemplateConfig(@RequestParam("id") Long id) {
        fileTemplateConfigService.deleteFileTemplateConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文件模板配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:file-template-config:query')")
    public CommonResult<FileTemplateConfigRespVO> getFileTemplateConfig(@RequestParam("id") Long id) {
        FileTemplateConfigDO fileTemplateConfig = fileTemplateConfigService.getFileTemplateConfig(id);
        return success(BeanUtils.toBean(fileTemplateConfig, FileTemplateConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文件模板配置分页")
    @PreAuthorize("@ss.hasPermission('infra:file-template-config:query')")
    public CommonResult<PageResult<FileTemplateConfigRespVO>> getFileTemplateConfigPage(@Valid FileTemplateConfigPageReqVO pageReqVO) {
        PageResult<FileTemplateConfigDO> pageResult = fileTemplateConfigService.getFileTemplateConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, FileTemplateConfigRespVO.class));
    }

}