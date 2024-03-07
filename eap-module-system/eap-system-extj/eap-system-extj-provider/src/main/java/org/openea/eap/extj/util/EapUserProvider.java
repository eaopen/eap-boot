package org.openea.eap.extj.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.openea.eap.extj.base.UserInfo;
import org.openea.eap.framework.common.enums.UserTypeEnum;
import org.openea.eap.framework.common.exception.ServiceException;
import org.openea.eap.framework.common.util.spring.EapAppUtil;
import org.openea.eap.framework.security.core.LoginUser;
import org.openea.eap.framework.security.core.util.SecurityFrameworkUtils;
import org.openea.eap.module.system.api.oauth2.OAuth2TokenApi;
import org.openea.eap.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * todo auth
 */

@Component("userProvider")
public class EapUserProvider extends UserProvider {
    private static final Logger log = LoggerFactory.getLogger(EapUserProvider.class);

    public EapUserProvider(RedisUtil redisUtil, CacheKeyUtil cacheKeyUtil) {
        super(redisUtil, cacheKeyUtil);
    }

    public static LoginUser getEapLoginUser(){
        return SecurityFrameworkUtils.getLoginUser();
    }

    public static UserInfo get(String token) {
        return getUser(token);
    }

    public static UserInfo getUser(String token) {
        // 参考 TokenAuthenticationFilter
        UserInfo userInfo = null;
        if (StrUtil.isNotEmpty(token)){
            LoginUser loginUser = buildLoginUserByToken(token);
            if(loginUser!=null){
                userInfo = userInfo(loginUser);
                return userInfo;
            }
        }
        if (userInfo == null || userInfo.getId()==null || StrUtil.isEmpty(userInfo.getUserName())) {
            LoginUser loginUser = getEapLoginUser();
            if(loginUser!=null){
                userInfo = userInfo(loginUser);
            }
        }
        return userInfo;
    }

    public static String getToken() {
        // TODO 获取当前线程的thread
        return "";
    }

    public static UserInfo getLoginUserInfo(){
        UserInfo user = new UserInfo();
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        user.setId(""+loginUser.getId());
        user.setUserId(""+loginUser.getId());
        String account = loginUser.getUserKey();
        AdminUserService adminUserApi = EapAppUtil.getBean(AdminUserService.class);
        AdminUserDO userDo = null;
        // TODO 是否有其他字段需要补充
        if(StrUtil.isNotEmpty(account)){
            user.setUserAccount(loginUser.getUserKey());
            user.setUserName(loginUser.getUserKey());
            userDo = adminUserApi.getUserByUsername(account);
        }else{
            userDo = adminUserApi.getUser(new Long(loginUser.getId()));
        }
        if(ObjectUtil.isNotNull(userDo)){
            user.setUserAccount(userDo.getUsername());
            user.setUserName(userDo.getNickname());
        }
        return user;
    }





    /**
     * 获取用户缓存

     * @return
     */
    public static UserInfo userInfo(LoginUser loginUser){
        if(loginUser==null) return null;

        UserInfo user = new UserInfo();
        user.setId(""+loginUser.getId());
        user.setUserId(""+loginUser.getId());
        String account = loginUser.getUserKey();
        if(StrUtil.isNotEmpty(account)){
            user.setUserAccount(loginUser.getUserKey());
            user.setUserName(loginUser.getUserKey());
        }else{
            throw new RuntimeException("loginUser lost key, id="+loginUser.getId());
        }
        return user;
    }


    private static LoginUser buildLoginUserByToken(String token) {
        try {
            if(StrUtil.isNotEmpty(token)){
                return getEapLoginUser();
            }
            Integer userType = UserTypeEnum.ADMIN.getValue();
            OAuth2TokenApi oauth2TokenApi = (OAuth2TokenApi)EapAppUtil.getBean(OAuth2TokenApi.class);
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
}
