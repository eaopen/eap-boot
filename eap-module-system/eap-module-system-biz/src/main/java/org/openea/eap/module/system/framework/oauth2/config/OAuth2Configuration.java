package org.openea.eap.module.system.framework.oauth2.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2 配置类
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OAuth2Properties.class)
public class OAuth2Configuration {
}
