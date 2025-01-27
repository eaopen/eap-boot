package org.openea.eap.framework.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "eap.security")
@Validated
@Data
public class SecurityProperties {

    /**
     * HTTP 请求时，访问令牌的请求 Header
     */
    @NotEmpty(message = "Token Header 不能为空")
    private String tokenHeader = "Authorization";
    /**
     * HTTP 请求时，访问令牌的请求参数
     *
     * 初始目的：解决 WebSocket 无法通过 header 传参，只能通过 token 参数拼接
     */
    @NotEmpty(message = "Token Parameter 不能为空")
    private String tokenParameter = "token";

    /**
     * mock 模式的开关
     */
    @NotNull(message = "mock 模式的开关不能为空")
    private Boolean mockEnable = false;
    /**
     * mock 模式的密钥
     * 一定要配置密钥，保证安全性
     */
    @NotEmpty(message = "mock 模式的密钥不能为空") // 这里设置了一个默认值，因为实际上只有 mockEnable 为 true 时才需要配置。
    private String mockSecret = "test";

    /**
     * 免登录的 URL 列表
     */
    private List<String> permitAllUrls = Collections.emptyList();

    /**
     * PasswordEncoder 加密复杂度，越高开销越大
     */
    private Integer passwordEncoderLength = 4;


    // for poc
    /**
     * 是否启用POC验证
     */
    private boolean enablePoc = false;

    private String pocUserHeader = "pocUser";
    private String pocAuthUser = "pocuser";  //默认用户
    private String pocUserPrefix = "poc,op";

    private String pocAuthHeader = "pocToken";
    private String pocAuthToken;

    private String pocSignHeader = "pocSign";
    private String pocSignPasswd = "poc";
}
