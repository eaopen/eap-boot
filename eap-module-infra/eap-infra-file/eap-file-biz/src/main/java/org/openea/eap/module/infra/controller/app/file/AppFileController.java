package org.openea.eap.module.infra.controller.app.file;

import cn.hutool.core.io.IoUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.module.infra.controller.app.file.vo.AppFileUploadReqVO;
import org.openea.eap.module.infra.service.file.FileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Tag(name = "用户 App - 文件存储")
@RestController
@RequestMapping("/infra/file")
@Validated
@Slf4j
public class AppFileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public CommonResult<String> uploadFile(AppFileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        String path = uploadReqVO.getPath();
        return CommonResult.success(fileService.createFile(file.getOriginalFilename(), path, IoUtil.readBytes(file.getInputStream())));
    }

}
