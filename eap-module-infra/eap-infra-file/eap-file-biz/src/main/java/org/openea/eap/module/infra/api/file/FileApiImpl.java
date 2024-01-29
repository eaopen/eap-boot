package org.openea.eap.module.infra.api.file;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.openea.eap.module.infra.api.file.dto.FileDTO;
import org.openea.eap.module.infra.dal.dataobject.file.FileDO;
import org.openea.eap.module.infra.service.file.FileService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 文件 API 实现类
 */
@Service
@Validated
public class FileApiImpl implements FileApi {

    @Resource
    private FileService fileService;

    @Override
    public String createFile(String name, String path, byte[] content) {
        return fileService.createFile(name, path, content);
    }

    /**
     * 保存文件，并返回文件的信息
     *
     * @param name    文件名称
     * @param path    文件路径
     * @param content 文件内容
     * @return 文件路径
     */
    @Override
    public JSONObject updateFile(String name, String path, byte[] content) {
        FileDO fileDO = fileService.uploadFile(name, path, content);
        return JSONUtil.parseObj(fileDO);
    }

    /**
     * 删除文件
     *
     * @param id 编号
     */
    @Override
    public void deleteFile(Long id) throws Exception {
        fileService.deleteFile(id);
    }

    /**
     * 获得文件内容
     *
     * @param configId 配置编号
     * @param path     文件路径
     * @return 文件内容
     */
    @Override
    public byte[] getFileContent(Long configId, String path) throws Exception {
        return fileService.getFileContent(configId, path);
    }

    /**
     * 根据你文件id获取文件
     *
     * @param id 文件id
     * @return 文件内容
     */
    @Override
    public FileDTO getById(Long id) {
        FileDO fileDO = fileService.getById(id);
        if (fileDO == null) {
            return null;
        }
        return JSONUtil.toBean(JSONUtil.parseObj(fileDO), FileDTO.class);
    }

}
