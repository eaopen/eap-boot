package org.openea.eap.framework.file.core.client.git;

import lombok.Data;
import org.openea.eap.framework.file.core.client.FileClientConfig;

/**
 * Git 文件客户端的配置类
 *
 */
@Data
public class GitFileClientConfig implements FileClientConfig {

    /**
     * 仓库地址
     */
    private String repoUrl;

    /**
     * 本地路径
     */
    private String localDir;

    /**
     * 分支
     */
    private String branch;

    /**
     * 基础路径
     */
    private String basePath;

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

}
