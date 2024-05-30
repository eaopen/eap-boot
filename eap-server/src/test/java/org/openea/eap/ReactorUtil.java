package org.openea.eap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static java.io.File.separator;

public class ReactorUtil {

    static Logger log = LoggerFactory.getLogger(ReactorUtil.class);

    private static String GROUP_ID = "cn.iocoder.boot";
    private static String ARTIFACT_ID = "yudao";
    private static String PACKAGE_NAME = "cn.iocoder.yudao";
    private static String TITLE = "芋道管理系统";
    private static String KEYWORD = "";
    /**
     * 白名单文件，不进行重写，避免出问题
     */
    private static final Set<String> WHITE_FILE_TYPES = asSet("gif", "jpg", "svg", "png", // 图片
            "eot", "woff2", "ttf", "woff"); // 字体

    @SafeVarargs
    public static <T> Set<T> asSet(T... objs) {
        return CollUtil.newHashSet(objs);
    }

    /**
     * 项目重写
     * @param projectBaseDir 项目跟目录（全路径）
     * @param newDir 新目录名（非全路径）
     * @param newParams String[]{group, artifact, package, title, keyword, others}
     * @param oldParams String[]{group, artifact, package, title, keyword, others}
     */
    public static void projectReactor(String projectBaseDir, String newDir, String[] newParams, String[] oldParams) {
        long start = System.currentTimeMillis();
        log.info("[main][原项目路劲改地址 ({})]", projectBaseDir);
        // 旧参数
        if(oldParams!=null && oldParams.length>=3){
            // 必须参数
            GROUP_ID = oldParams[0];
            ARTIFACT_ID = oldParams[1];
            PACKAGE_NAME = oldParams[2];
            // 可选参数
            if(oldParams.length>=4){
                TITLE = oldParams[3];
            }
            if(oldParams.length>=5){
                KEYWORD = oldParams[4];
            }
        }
        // 新参数
        if(newParams==null || newParams.length<3){
            log.error("[main][必须配置参数newParams，请检查参数是否正确，程序退出]");
            return;
        }
        // 必须参数
        String groupIdNew = newParams[0];
        String artifactIdNew = newParams[1];
        String packageNameNew = newParams[2];
        // 可选参数
        String titleNew;
        if(newParams.length>=4){
            titleNew = newParams[3];
        } else {
            titleNew = TITLE;
        }
        String keywordNew;
        if(newParams.length>=5){
            keywordNew = newParams[4];
        } else {
            keywordNew = KEYWORD;
        }

        String projectBaseDirNew = projectBaseDir.substring(0, projectBaseDir.lastIndexOf("/")) + "/" + newDir ; // 一键改名后，“新”项目所在的目录
        log.info("[main][检测新项目目录 ({})是否存在]", projectBaseDirNew);
        if (FileUtil.exist(projectBaseDirNew)) {
            log.error("[main][新项目目录检测 ({})已存在，请更改新的目录！程序退出]", projectBaseDirNew);
            return;
        }
        // 如果新目录中存在 PACKAGE_NAME，ARTIFACT_ID 等关键字，路径会被替换，导致生成的文件不在预期目录
        if (StrUtil.containsAny(projectBaseDirNew, PACKAGE_NAME, ARTIFACT_ID, StrUtil.upperFirst(ARTIFACT_ID))) {
            log.error("[main][新项目目录 `projectBaseDirNew` 检测 ({}) 存在冲突名称「{}」或者「{}」，请更改新的目录！程序退出]",
                    projectBaseDirNew, PACKAGE_NAME, ARTIFACT_ID);
            return;
        }
        log.info("[main][完成新项目目录检测，新项目路径地址 ({})]", projectBaseDirNew);
        // 获得需要复制的文件
        log.info("[main][开始获得需要重写的文件，预计需要 10-20 秒]");
        Collection<File> files = listFiles(projectBaseDir);
        int filesSize = files.size();
        log.info("[main][需要重写的文件数量：{}，预计需要 {}-{} 秒]", filesSize, filesSize/100, filesSize/30);
        // 写入文件
        AtomicInteger index = new AtomicInteger(0);
        files.forEach(file -> {
            int currentIndex = index.getAndIncrement();
            // 如果是白名单的文件类型，不进行重写，直接拷贝
            String fileType = getFileType(file);
            if (WHITE_FILE_TYPES.contains(fileType)) {
                copyFile(file, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew, keywordNew);
                return;
            }
            // 如果非白名单的文件类型，重写内容，在生成文件
            String content = replaceFileContent(file, groupIdNew, artifactIdNew, packageNameNew, titleNew, keywordNew);
            writeFile(file, content, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew, keywordNew);
            if(currentIndex%100 == 0){
                log.info("index="+currentIndex);
            }
        });
        log.info("[main][重写完成]共耗时：{} 秒", (System.currentTimeMillis() - start) / 1000);
    }

    private static String getProjectBaseDir() {
        String baseDir = System.getProperty("user.dir");
        if (StrUtil.isEmpty(baseDir)) {
            throw new NullPointerException("项目基础路径不存在");
        }
        return baseDir;
    }

    private static Collection<File> listFiles(String projectBaseDir) {
        Collection<File> files = FileUtil.loopFiles(projectBaseDir);
        // 移除 IDEA、Git 自身的文件、Node 编译出来的文件
        files = files.stream()
                .filter(file -> !file.getPath().contains(separator + "target" + separator)
                        && !file.getPath().contains(separator + "node_modules" + separator)
                        && !file.getPath().contains(separator + ".idea" + separator)
                        && !file.getPath().contains(separator + ".git" + separator)
                        && !file.getPath().contains(separator + "dist" + separator)
                        && !file.getPath().contains(".iml")
                        && !file.getPath().contains(".html.gz"))
                .collect(Collectors.toList());
        return files;
    }

    private static String replaceFileContent(File file, String groupIdNew,
                                             String artifactIdNew, String packageNameNew,
                                             String titleNew, String keywordNew) {
        String content = FileUtil.readString(file, StandardCharsets.UTF_8);
        // 如果是白名单的文件类型，不进行重写
        String fileType = getFileType(file);
        if (WHITE_FILE_TYPES.contains(fileType)) {
            return content;
        }
        // 执行文件内容都重写
        content = content.replaceAll(GROUP_ID, groupIdNew);
        if(ObjectUtils.isNotEmpty(artifactIdNew) && ARTIFACT_ID.contains("-")){
            content = content.replaceAll(ARTIFACT_ID, artifactIdNew) // - 可区分package
                    .replaceAll(StrUtil.upperFirst(ARTIFACT_ID), StrUtil.upperFirst(artifactIdNew));
        }
        // replace keyword + key
        if(ObjectUtils.isNotEmpty(keywordNew) && !keywordNew.equals(KEYWORD)){
            content = content.replaceAll(KEYWORD.toLowerCase()+"Key", keywordNew.toLowerCase()+"Key")
                    .replaceAll(StrUtil.upperFirst(KEYWORD)+"Key", StrUtil.upperFirst(keywordNew)+"Key");
        }
        if(ObjectUtils.isNotEmpty(packageNameNew) && !packageNameNew.equals(PACKAGE_NAME)){
            if(PACKAGE_NAME.contains(".")){
                content = content.replaceAll(PACKAGE_NAME, packageNameNew);
                content = content.replaceAll(PACKAGE_NAME.replaceAll("\\.","/"), packageNameNew.replaceAll("\\.","/"));
            }else{
                content = content.replaceAll(PACKAGE_NAME+"\\.", packageNameNew+".");
                content = content.replaceAll(PACKAGE_NAME+"/", packageNameNew+"/");
                //content = content.replaceAll(PACKAGE_NAME, packageNameNew);
            }
        }
        content = content.replaceAll(TITLE, titleNew);
        if(ObjectUtils.isNotEmpty(artifactIdNew) && !ARTIFACT_ID.contains("-")){
            content = content.replaceAll(ARTIFACT_ID, artifactIdNew)
                    .replaceAll(StrUtil.upperFirst(ARTIFACT_ID), StrUtil.upperFirst(artifactIdNew));
        }
        // replace keyword
        if(ObjectUtils.isNotEmpty(keywordNew) && !keywordNew.equals(KEYWORD)){
            content = content.replaceAll(KEYWORD.toUpperCase(), keywordNew.toUpperCase())
                    .replaceAll(KEYWORD.toLowerCase(), keywordNew.toLowerCase())
                    .replaceAll(StrUtil.upperFirst(KEYWORD), StrUtil.upperFirst(keywordNew));
        }
        // replace 注释
        content = removeContentCust(content);
        return content;
    }

    private static String removeContentCust(String content){
        // 去掉部分中文注释
        // 类注释
        content = content.replaceAll("( \\* @.*\\n)* \\* @.*有限公司.*\\n( \\* @.*\\n)*( \\*\\n)* \\*/", " */")
                .replaceAll("( \\* @.*\\n)* \\* @.*EXTN.*\\n( \\* @.*\\n)*( \\*\\n)* \\*/", " */")
                .replaceAll("/\\*\\*\\n( \\* @.*\\n)* \\* @.*有限公司.*\\n( \\* @.*\\n)*( \\*\\n)* \\*/", "")
                .replaceAll("/\\*\\*\\n( \\* @.*\\n)* \\* @.*EXTN.*\\n( \\* @.*\\n)*( \\*\\n)* \\*/", "")
                .replaceAll("(?m)^ \\* .*芋道源码.*\\n", "")
                .replaceAll("example = \"芋道\"", "example = \"eap\"")
                .replaceAll("(?m)^ \\* .*版本.*\\n", "")
                .replaceAll("(?m)^ \\* .*版权.*\\n", "")
                .replaceAll("(?m)^ \\* .*作者.*\\n", "")
                .replaceAll("(?m)^ \\* .*日期.*\\n", "")
                .replaceAll("/\\*\\*\\n( \\*\\n)* \\*/", "");   //空注释
        // 方法注释
        content = content.replaceAll("\n( )*\\* @.*有限公司.*\\n(( )*\\* @.*\\n)*( \\*\\n)*( )*\\*/", "\n */")
                .replaceAll("\n( )*\\* @.*EXTN.*\\n(( )*\\* @.*\\n)*( \\*\\n)*( )*\\*/", "\n */");
        return content;
    }

    private static void writeFile(File file, String fileContent, String projectBaseDir,
                                  String projectBaseDirNew, String packageNameNew, String artifactIdNew, String keywordNew) {
        String newPath = buildNewFilePath(file, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew, keywordNew);
        FileUtil.writeUtf8String(fileContent, newPath);
    }

    private static void copyFile(File file, String projectBaseDir,
                                 String projectBaseDirNew, String packageNameNew, String artifactIdNew, String keywordNew) {
        String newPath = buildNewFilePath(file, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew, keywordNew);
        FileUtil.copyFile(file, new File(newPath));
    }

    private static String buildNewFilePath(File file, String projectBaseDir,
                                           String projectBaseDirNew, String packageNameNew, String artifactIdNew, String keywordNew) {
        String newPath = file.getPath().replace(projectBaseDir, projectBaseDirNew); // 新目录
        if(artifactIdNew.contains("-")){
            newPath = newPath.replace(ARTIFACT_ID, artifactIdNew) //
                    .replaceAll(StrUtil.upperFirst(ARTIFACT_ID), StrUtil.upperFirst(artifactIdNew));
        }
        newPath = newPath.replace(PACKAGE_NAME.replaceAll("\\.", Matcher.quoteReplacement(separator).replace(KEYWORD,keywordNew)),
                packageNameNew.replaceAll("\\.", Matcher.quoteReplacement(separator)));
        if(!artifactIdNew.contains("-")){
            newPath = newPath.replace(ARTIFACT_ID, artifactIdNew) //
                    .replaceAll(StrUtil.upperFirst(ARTIFACT_ID), StrUtil.upperFirst(artifactIdNew));
        }
        // keyword vs package
        if(ObjectUtils.isNotEmpty(keywordNew) && !keywordNew.equals(KEYWORD)){
            newPath = newPath.replaceAll(KEYWORD.toUpperCase(), keywordNew.toUpperCase())
                    .replaceAll(KEYWORD.toLowerCase(), keywordNew.toLowerCase())
                    .replaceAll(StrUtil.upperFirst(KEYWORD), StrUtil.upperFirst(keywordNew));
        }
        return newPath;

    }

    private static String getFileType(File file) {
        return file.length() > 0 ? FileTypeUtil.getType(file) : "";
    }
}
