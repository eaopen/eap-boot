package org.openea.eap.module.infra.api.filetemplateconfig;

public interface FileTemplateConfigApi {
    /**
     * 根据模板文件key获取模板文件
     *
     * @param tempKey
     * @return
     */
    String getTemplatePath(String tempKey);
}
