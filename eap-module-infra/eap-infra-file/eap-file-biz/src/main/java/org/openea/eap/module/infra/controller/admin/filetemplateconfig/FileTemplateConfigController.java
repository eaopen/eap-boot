package org.openea.eap.module.infra.controller.admin.filetemplateconfig;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.framework.common.util.servlet.ServletUtils;
import org.openea.eap.module.infra.api.filetemplateconfig.FileTemplateConfigApi;
import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.FileTemplateConfigPageReqVO;
import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.FileTemplateConfigRespVO;
import org.openea.eap.module.infra.controller.admin.filetemplateconfig.vo.FileTemplateConfigSaveReqVO;
import org.openea.eap.module.infra.dal.dataobject.filetemplateconfig.FileTemplateConfigDO;
import org.openea.eap.module.infra.service.filetemplateconfig.FileTemplateConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static org.openea.eap.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件模板配置")
@RestController
@RequestMapping("/infra/file-template-config")
@Validated
public class FileTemplateConfigController {

    @Resource
    private FileTemplateConfigService fileTemplateConfigService;
    @Resource
    private FileTemplateConfigApi fileTemplateConfigApi;

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

    @GetMapping("/download/{fileId}")
    @PermitAll
    @Operation(summary = "下载文件")
    @Parameter(name = "fileId", description = "模板文件Id", required = true)
    public void download(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable("fileId") Long fileId) throws Exception {
        FileTemplateConfigDO fileTemplateConfigDO = fileTemplateConfigService.getFileTemplateConfig(fileId);
        if (fileTemplateConfigDO == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        String templatePath = fileTemplateConfigApi.getTemplatePath(fileTemplateConfigDO.getTempKey());
        if (StrUtil.isEmpty(templatePath) || !FileUtil.exist(templatePath)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        // 读取内容
        byte[] content = FileUtil.readBytes(templatePath);
        if (content == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        ServletUtils.writeAttachment(response, fileTemplateConfigDO.getTempName(), content);
    }

    @GetMapping("/download/key/{fileKey}")
    @PermitAll
    @Operation(summary = "下载文件")
    @Parameter(name = "fileKey", description = "模板文件Key", required = true)
    public void downloadByKey(HttpServletRequest request,
                              HttpServletResponse response,
                              @PathVariable("fileKey") String fileKey) throws Exception {
        FileTemplateConfigDO fileTemplateConfigDO = fileTemplateConfigService.getFileTemplateConfigByKey(fileKey);

        String templatePath = fileTemplateConfigApi.getTemplatePath(fileKey);
        if (StrUtil.isEmpty(templatePath) || !FileUtil.exist(templatePath)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        // 读取内容
        byte[] content = FileUtil.readBytes(templatePath);
        if (content == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        ServletUtils.writeAttachment(response, fileTemplateConfigDO.getTempName(), content);
    }

}