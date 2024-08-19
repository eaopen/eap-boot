package org.openea.eap.module.infra.api.filetemplateconfig;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import org.openea.eap.module.infra.dal.dataobject.file.FileDO;
import org.openea.eap.module.infra.dal.dataobject.filetemplateconfig.FileTemplateConfigDO;
import org.openea.eap.module.infra.dal.mysql.file.FileMapper;
import org.openea.eap.module.infra.service.filetemplateconfig.FileTemplateConfigService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.io.File;

import static org.openea.eap.framework.common.exception.util.ServiceExceptionUtil.exception;
import static org.openea.eap.module.infra.enums.ErrorCodeConstants.FILE_TEMPLATE_CONFIG_NOT_EXISTS;

@Service
@Validated
public class FileTemplateConfigApiImpl implements FileTemplateConfigApi {

    @Resource
    FileTemplateConfigService fileTemplateConfigService;
    @Resource
    FileMapper fileMapper;

    @Override
    public String getTemplatePath(String tempKey) {
        FileTemplateConfigDO fileTemplateConfigDO = fileTemplateConfigService.getFileTemplateConfigByKey(tempKey);
        if (fileTemplateConfigDO == null) {
            throw exception(FILE_TEMPLATE_CONFIG_NOT_EXISTS);
        }
        if (fileTemplateConfigDO.getFileId() != null) {
            try {
                FileDO fileDo = fileMapper.selectById(fileTemplateConfigDO.getFileId());
                if (fileDo != null) {
                    File file = HttpUtil.downloadFileFromUrl(fileDo.getUrl(), FileUtil.getTmpDirPath() + "/"
                            + RandomUtil.randomString(12)
                            + "."
                            + FileUtil.getSuffix(fileDo.getName()));
                    return FileUtil.getAbsolutePath(file);
                }
            } catch (Exception e) {
                throw exception(FILE_TEMPLATE_CONFIG_NOT_EXISTS);
            }
        }
        return null;
    }
}
