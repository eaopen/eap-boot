package org.openea.eap.module.obpm.config;

import org.openea.eap.framework.common.util.spring.EapAppUtil;
import org.openea.eap.module.obpm.service.eap.ObpmAuthServiceImpl;
import org.openea.eap.module.obpm.service.eap.ObpmPermissionServiceImpl;
import org.openea.eap.module.obpm.service.eap.ObpmUserServiceImpl;
import org.openea.eap.module.system.service.auth.AdminAuthService;
import org.openea.eap.module.system.service.permission.PermissionService;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 集成OpenBPM配置类
 *
 * ObmpClientService 有可配置参数，默认不用修改
 * Obpm数据同步需要配置动态数据源 DS "obpm"
 *
 */
@Configurable
public class EapObpmConfiguration  {

    @Bean
    @Primary
    public AdminAuthService authService(){
        return EapAppUtil.getBean(ObpmAuthServiceImpl.class);
    }

    @Bean
    @Primary
    public AdminUserService userService(){
        return EapAppUtil.getBean(ObpmUserServiceImpl.class);
    }

    @Bean
    @Primary
    public PermissionService permissionService() {
        return EapAppUtil.getBean(ObpmPermissionServiceImpl.class);
    }

}
