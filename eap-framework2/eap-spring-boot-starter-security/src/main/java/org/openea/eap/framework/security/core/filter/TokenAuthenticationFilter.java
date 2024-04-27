package org.openea.eap.framework.security.core.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.openea.eap.framework.common.enums.UserTypeEnum;
import org.openea.eap.framework.common.exception.ServiceException;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.openea.eap.framework.common.util.servlet.ServletUtils;
import org.openea.eap.framework.security.config.SecurityProperties;
import org.openea.eap.framework.security.core.LoginUser;
import org.openea.eap.framework.security.core.util.PocAuthUtil;
import org.openea.eap.framework.security.core.util.SecurityFrameworkUtils;
import org.openea.eap.framework.web.core.handler.GlobalExceptionHandler;
import org.openea.eap.framework.web.core.util.WebFrameworkUtils;
import org.openea.eap.module.system.api.oauth2.OAuth2TokenApi;
import org.openea.eap.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import org.openea.eap.module.system.api.user.AdminUserApi;
import org.openea.eap.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Token 过滤器，验证 token 的有效性
 * 验证通过后，获得 {@link LoginUser} 信息，并加入到 Spring Security 上下文
 *
 */
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final OAuth2TokenApi oauth2TokenApi;

    private final AdminUserApi adminUserApi;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());

        if (StrUtil.isNotEmpty(token)) {
            Integer userType = WebFrameworkUtils.getLoginUserType(request);
            try {
                // 1.1 基于 token 构建登录用户
                LoginUser loginUser = buildLoginUserByToken(token, userType);
                // 1.2 模拟 Login 功能，方便日常开发调试
                if (loginUser == null) {
                    loginUser = mockLoginUser(request, token, userType);
                }

                // 2. 设置当前用户
                if (loginUser != null) {
                    SecurityFrameworkUtils.setLoginUser(loginUser, request);
                }
            } catch (Throwable ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }else{
            checkPocAuth(request, response);
        }
        // 继续过滤链
        chain.doFilter(request, response);
    }
    private void checkPocAuth(HttpServletRequest request, HttpServletResponse response) {
        // 0 prepare
        if(SecurityFrameworkUtils.getLoginUser()!=null) return;
        // poc only
        boolean enablePoc = securityProperties.isEnablePoc();
        if(!enablePoc) return;
        if(request.getRequestURI().indexOf("/auth/login")>0) return;

        // 1 get poc user
        String pocUser = request.getHeader(securityProperties.getPocUserHeader());
        if(StrUtil.isEmpty(pocUser)){
            pocUser = request.getHeader(StrUtil.upperFirst(securityProperties.getPocUserHeader()));
        }
        if(StrUtil.isEmpty(pocUser)){
            pocUser = request.getParameter(securityProperties.getPocUserHeader());
            if(StrUtil.isNotEmpty(pocUser)){
                // 增加限制只支持op或poc开头的用户
                boolean withPrefix = false;
                String pocUserPrefix = securityProperties.getPocUserPrefix();
                if(StrUtil.isEmpty(pocUserPrefix)){
                    pocUserPrefix="poc,op";
                }
                String[] prefixes =pocUserPrefix.split(",");
                for(String prefix:prefixes){
                    if(pocUser.startsWith(prefix)){
                        withPrefix = true;
                        break;
                    }
                }
                if(!withPrefix){
                    // 不符合前缀要求的，强制改为默认poc用户
                    pocUser = securityProperties.getPocAuthUser();
                }
            }
        }
        // 必须指定poc用户
        if(StrUtil.isEmpty(pocUser)){
            return;
        }
        // 2 check
        boolean pocPass = false;
        String pocToken = null;
        // 2.1 poc 1: check poc token
        String pocAuthToken = securityProperties.getPocAuthToken();
        if(StrUtil.isNotEmpty(pocAuthToken)){
            pocToken = SecurityFrameworkUtils.obtainAuthorization(request,
                    securityProperties.getPocAuthHeader(), securityProperties.getPocAuthHeader());
            if(StrUtil.isEmpty(pocToken)){
                pocToken = SecurityFrameworkUtils.obtainAuthorization(request,
                        StrUtil.upperFirst(securityProperties.getPocAuthHeader()), StrUtil.upperFirst(securityProperties.getPocAuthHeader()));
            }
            if(StrUtil.isNotEmpty(pocToken)
                    && pocToken.equals(pocAuthToken)){
                pocPass = true;
            }
        }

        // 2.2 poc 2: check poc sign
        if(!pocPass){
            String sign = request.getHeader(securityProperties.getPocSignHeader());
            if(StrUtil.isEmpty(sign)){
                sign = request.getParameter(securityProperties.getPocSignHeader());
            }
            if(StrUtil.isNotEmpty(sign)){
                String userSign = PocAuthUtil.authSignToday(pocUser, securityProperties.getPocSignPasswd());
                if(sign.equals(userSign)){
                    pocPass = true;
                    pocToken = sign;
                }
            }
        }
        // 3 login
        if(pocPass){
            Integer userType = UserTypeEnum.ADMIN.getValue();
            try{
                AdminUserRespDTO userResp = adminUserApi.getUserByAccount(pocUser);
                Long userId = userResp.getId();

                // 构建登录用户
                LoginUser loginUser = new LoginUser();
                loginUser.setId(userId);
                loginUser.setUserType(userType);
                loginUser.setUserKey(pocUser);
                loginUser.setTenantId(0L);

                // 设置当前用户
                SecurityFrameworkUtils.setLoginUser(loginUser, request);

            } catch (Throwable ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }
    }

    private LoginUser buildLoginUserByToken(String token, Integer userType) {
        try {
            OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token);
            if (accessToken == null) {
                return null;
            }
            // 用户类型不匹配，无权限
            // 注意：只有 /admin-api/* 和 /app-api/* 有 userType，才需要比对用户类型
            // 类似 WebSocket 的 /ws/* 连接地址，是不需要比对用户类型的
            if (userType != null
                    && ObjectUtil.notEqual(accessToken.getUserType(), userType)) {
                throw new AccessDeniedException("错误的用户类型");
            }
            // 构建登录用户
            return new LoginUser().setId(accessToken.getUserId()).setUserType(accessToken.getUserType())
                    .setTenantId(accessToken.getTenantId()).setScopes(accessToken.getScopes());
        } catch (ServiceException serviceException) {
            // 校验 Token 不通过时，考虑到一些接口是无需登录的，所以直接返回 null 即可
            return null;
        }
    }

    /**
     * 模拟登录用户，方便日常开发调试
     *
     * 注意，在线上环境下，一定要关闭该功能！！！
     *
     * @param request 请求
     * @param token 模拟的 token，格式为 {@link SecurityProperties#getMockSecret()} + 用户编号
     * @param userType 用户类型
     * @return 模拟的 LoginUser
     */
    private LoginUser mockLoginUser(HttpServletRequest request, String token, Integer userType) {
        if (!securityProperties.getMockEnable()) {
            return null;
        }
        // 必须以 mockSecret 开头
        if (!token.startsWith(securityProperties.getMockSecret())) {
            return null;
        }
        // 构建模拟用户
        Long userId = Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
        return new LoginUser().setId(userId).setUserType(userType)
                .setTenantId(WebFrameworkUtils.getTenantId(request));
    }

}
