package org.openea.eap.framework.file.core.client.git;

//import org.eclipse.jgit.api.Git;
import org.openea.eap.framework.file.core.client.AbstractFileClient;

/**
 * Git 文件客户端
 */
public class GitFileClient extends AbstractFileClient<GitFileClientConfig> {

    //protected Git git;

    public GitFileClient(Long id, GitFileClientConfig config) {
        super(id, config);
    }

    /**
     * 自定义初始化
     */
    @Override
    protected void doInit() {
        // load local git or clone git
    }

    /**
     * 上传文件
     *
     * @param content 文件流
     * @param path    相对路径
     * @param type
     * @return 完整路径，即 HTTP 访问地址
     * @throws Exception 上传文件时，抛出 Exception 异常
     */
    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        // pull
        // add or modify file
        // commit and push
        return "";
    }

    /**
     * 删除文件
     *
     * @param path 相对路径
     * @throws Exception 删除文件时，抛出 Exception 异常
     */
    @Override
    public void delete(String path) throws Exception {
        // pull
        // delete file
        // commit and push
    }

    /**
     * 获得文件的内容
     *
     * @param path 相对路径
     * @return 文件的内容
     */
    @Override
    public byte[] getContent(String path) throws Exception {
        // pull
        return new byte[0];
    }
}
