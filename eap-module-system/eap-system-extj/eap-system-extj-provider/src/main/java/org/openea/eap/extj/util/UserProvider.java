package org.openea.eap.extj.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.extj.base.UserInfo;
import org.openea.eap.extj.consts.DeviceType;
import org.openea.eap.framework.common.enums.UserTypeEnum;
import org.openea.eap.framework.common.exception.ServiceException;
import org.openea.eap.framework.common.util.spring.EapAppUtil;
import org.openea.eap.framework.security.core.LoginUser;
import org.openea.eap.framework.security.core.util.SecurityFrameworkUtils;
import org.openea.eap.module.system.api.oauth2.OAuth2TokenApi;
import org.openea.eap.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.springframework.security.access.AccessDeniedException;


@Slf4j
//@Component
//@ConditionalOnMissingBean
public class UserProvider {

    protected static RedisUtil redisUtil;
    protected static CacheKeyUtil cacheKeyUtil;

    public UserProvider(RedisUtil redisUtil, CacheKeyUtil cacheKeyUtil) {
        UserProvider.redisUtil = redisUtil;
        UserProvider.cacheKeyUtil = cacheKeyUtil;
    }

    /// eap

    public static EapUserProvider getEapUserProvider() {
        EapUserProvider eapUserProvider = EapAppUtil.getBean(EapUserProvider.class);
        return eapUserProvider;
    }

    public static LoginUser getEapLoginUser(){
        return SecurityFrameworkUtils.getLoginUser();
    }

    public static String getToken() {
        return getEapUserProvider().getToken();
    }

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



    // =================== 用户ID拼接相关操作 ===================








    /**
     * 获取用户缓存
     *
     * @return
     */
    public UserInfo get() {
        return UserProvider.getUser();
    }


    /**
     * 获取用户缓存
     *
     * @param token
     * @return
     */
    public static UserInfo getUser(String token) {
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

    /**
     * 获取用户缓存
     *
     * @return
     */
    public static UserInfo getUser() {
        UserInfo userInfo = UserProvider.getLoginUserInfo();
        return userInfo;
    }

    public static DeviceType getDeviceForAgent() {
        if (ServletUtil.getIsMobileDevice()) {
            return DeviceType.APP;
        } else {
            return DeviceType.PC;
        }
    }



}
