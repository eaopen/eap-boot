package org.openea.eap;

import cn.hutool.core.util.StrUtil;


/**
 * 项目修改器，一键替换 Maven 的 groupId、artifactId，项目的 package 等
 * <p>
 * 通过修改 groupIdNew、artifactIdNew、projectBaseDirNew 三个变量
 *
 */
//@Slf4j
public class ProjectReactor {

    public static void main(String[] args) {
        //reactorEap();
        //reactorExtn();
    }

    public static void reactorEap(){
        String projectBaseDir = getProjectBaseDir();
        //projectBaseDir = "/Users/dev/workspace/lowcode/ruoyi-pro";
        ReactorUtil.projectReactor(projectBaseDir, "eap22",
                new String[]{"openea.eap","eap","org.openea.eap","OpenEAP"}, null);
    }

    public static void reactorExtn(){
        String projectBaseDir = getProjectBaseDir();
        projectBaseDir = "/Users/dev/workspace/lowcode/jnpf-boot36";
        ReactorUtil.projectReactor(projectBaseDir, "extn3b",
                new String[]{"openea.extn","extn-","extn","OpenEAP-Extn"},
                new String[]{"com.jnpf","jnpf-","jnpf","JNPF快速开发平台"});
    }

    private static String getProjectBaseDir() {
        String baseDir = System.getProperty("user.dir");
        if (StrUtil.isEmpty(baseDir)) {
            throw new NullPointerException("项目基础路径不存在");
        }
        return baseDir;
    }




}
