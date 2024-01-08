package org.openea.eap.module.obpm.service.eap;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openea.eap.framework.common.enums.CommonStatusEnum;
import org.openea.eap.module.obpm.service.obpm.ObmpClientService;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.openea.eap.module.system.enums.logger.LoginLogTypeEnum;
import org.openea.eap.module.system.enums.logger.LoginResultEnum;
import org.openea.eap.module.system.service.auth.AdminAuthService;
import org.openea.eap.module.system.service.auth.AdminAuthServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static org.openea.eap.framework.common.exception.util.ServiceExceptionUtil.exception;
import static org.openea.eap.module.system.enums.ErrorCodeConstants.*;

@Primary
@Service
@Slf4j
public class ObpmAuthServiceImpl extends AdminAuthServiceImpl implements AdminAuthService {

    @Resource
    private ObmpClientService obmpClientService;

    @Resource
    @Lazy
    ObpmUserServiceImpl obpmUserService;

    @Override
    public AdminUserDO authenticate(String username, String password) {
        final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
        // 校验账号是否存在
        AdminUserDO user = userService.getUserByUsername(username);
        if (user == null) {
            try{
                //可增加实时查询未同步用户
                JSONObject jsonUser = queryObpmUser(username, true);
                // 创建用户并保存密码
                if(jsonUser!=null){
                    user = createAdminUser(jsonUser);
                }
            }catch (Exception e){
                log.error(String.format("queryObpmUser fail, user=%s, exception=%s",username, e.getMessage()));
                createLoginLog(null, username, logTypeEnum, LoginResultEnum.THIRD_FAIL);
                throw exception(AUTH_THIRD_LOGIN_FAIL);
            }
        }
        if (user == null) {
            createLoginLog(null, username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (!userService.isPasswordMatch(password, user.getPassword())) {
            createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 校验是否禁用
        if (CommonStatusEnum.isDisable(user.getStatus())) {
            createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }
        return user;
    }


    /**
     * 查询Obpm用户信息
     * @param username
     * @param withPassword
     * @return
     */
    private JSONObject queryObpmUser(String username, boolean withPassword){
        JSONObject jsonUser = obmpClientService.queryUserInfo(username, withPassword);
        return jsonUser;
    }

    /**
     * 根据obpm用户创建本地用户
     * @param jsonUser
     * @return
     */
    protected AdminUserDO createAdminUser(JSONObject jsonUser) {
        return obpmUserService.createAdminUser(jsonUser);
    }



}
