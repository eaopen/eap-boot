package org.openea.eap.extj.util;

import lombok.extern.slf4j.Slf4j;
import org.openea.eap.extj.base.UserInfo;
import org.openea.eap.extj.consts.DeviceType;
import org.openea.eap.framework.common.util.spring.EapAppUtil;


@Slf4j
public abstract class UserProvider {

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

    public static String getToken() {
        return getEapUserProvider().getToken();
    }

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
     * @return
     */
    public static UserInfo getUser() {
        UserInfo userInfo = EapUserProvider.getLoginUserInfo();
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
