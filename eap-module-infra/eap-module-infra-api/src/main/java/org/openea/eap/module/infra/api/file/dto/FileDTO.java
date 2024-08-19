package org.openea.eap.module.infra.api.file.dto;

import lombok.Data;

@Data
public class FileDTO {
    /**
     * 编号，数据库自增
     */
    private Long id;

    /**
     * 模块
     * 不同模块采用不同存储策略和权限策略
     */
    private Long moduleId;

    /**
     * 配置编号
     *
     * 关联
     */
    private Long configId;  //platform
    /**
     * 原文件名
     */
    private String name;  //fileName
    /**
     * 路径，即文件名
     */
    private String path;
    /**
     * 访问地址
     */
    private String url;
    /**
     * 文件的 MIME 类型，例如 "application/octet-stream"
     */
    private String type;
    /**
     * 文件大小
     */
    private Integer size;

}
