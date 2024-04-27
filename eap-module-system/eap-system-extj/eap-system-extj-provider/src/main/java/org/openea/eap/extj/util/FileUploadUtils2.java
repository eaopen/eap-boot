package org.openea.eap.extj.util;

import org.openea.eap.extj.model.FileListVO;
import org.openea.eap.module.infra.framework.file.core.client.FileClientFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * todo eap待处理
 */
public class FileUploadUtils2  {

    @Resource
    private FileClientFactory fileClientFactory;

    /**
     * 上传文件，通过字节数组
     *
     * @param bytes    内容
     * @param path     路径
     * @param fileName 文件名
     */
    public static FileInfo uploadFile(byte[] bytes, String path, String fileName) {
        FileInfo fileInfo=new FileInfo();
        //fileInfo.setId(RandomUtil.uuId());
        fileInfo.setPath(path);
        fileInfo.setFilename(fileName);
        // todo 上传文件
        return fileInfo;
    }

    /**
     * 上传文件，MultipartFile
     *
     * @param multipartFile 文件
     * @param path          路径
     * @param fileName      文件名
     */
    public static FileInfo uploadFile(MultipartFile multipartFile, String path, String fileName) {
        FileInfo fileInfo=new FileInfo();
        // todo 上传文件
        return fileInfo;
    }

    /**
     * 上传文件，File
     *
     * @param file 文件
     * @param path          路径
     * @param fileName      文件名
     */
    public static FileInfo uploadFile(File file, String path, String fileName) {
        FileInfo fileInfo=new FileInfo();
        // todo 上传文件
        return fileInfo;
    }

    /**
     * 获取文件列表
     *
     * @param path          路径
     */
    public static List<FileListVO> getFileList(String path) {
        return Collections.emptyList();
    }


    public static String getLocalBasePath() {
        // todo 集成文件系统
        return "/Users/dev/workspace/openea/temp/";
    }


    /**
     * 下载文件得到字节数组
     *
     * @param path
     * @param fileName
     * @param origin
     */
    public static byte[] downloadFileByte(String path, String fileName, boolean origin) {
        return null;
    }




    /**
     * 下载到本地
     *
     * @param folderName  文件夹名
     * @param filePath   下载到本地文件路径
     * @param objectName 文件名
     */
    public static void downLocal(String folderName, String filePath, String objectName) {
        // todo
    }

    /**
     * 下载到本地
     *
     * @param folderName  文件夹名
     */
    public static List<FileListVO> getDefaultFileList(String folderName) {
        List<FileListVO> fileList = new ArrayList<>();
        getFileList(Thread.currentThread().getContextClassLoader(), folderName, fileList);
        return fileList;
    }

    private static  List<FileListVO> getFileList(ClassLoader classLoader, String folderName, List<FileListVO> fileList){
        try {
            Enumeration<URL> urls = classLoader.getResources(folderName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                File file = new File(url.getFile());
                if (file.isDirectory()) {
                    fileList = getFileList(file.getPath());
                }else{
                    // file
                    FileListVO fileVO =new FileListVO();
                    fileVO.setFileName(file.getName());
                    fileVO.setFileSize(String.valueOf(file.length()));
                    fileVO.setFileTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.lastModified()));
                    fileVO.setFileType(file.isDirectory() ? "Directory" : "File");
                    fileList.add(fileVO);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fileList;
    }



}
