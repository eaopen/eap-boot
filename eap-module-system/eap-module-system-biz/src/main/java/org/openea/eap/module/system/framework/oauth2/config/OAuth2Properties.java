package org.openea.eap.module.system.framework.oauth2.config;

import lombok.Data;
import org.openea.eap.module.system.dal.redis.RedisKeyConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OAuth2 令牌缓存配置
 */
@ConfigurationProperties(prefix = "eap.oauth2")
@Data
public class OAuth2Properties {

    /**
     * 访问令牌的 Redis key 前缀，默认与历史版本一致
     */
    private String accessTokenKeyPrefix = RedisKeyConstants.OAUTH2_ACCESS_TOKEN;

}
