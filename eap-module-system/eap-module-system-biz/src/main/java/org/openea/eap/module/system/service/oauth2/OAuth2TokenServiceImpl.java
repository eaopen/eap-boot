package org.openea.eap.module.system.service.oauth2;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.openea.eap.framework.common.enums.UserTypeEnum;
import org.openea.eap.framework.common.exception.enums.GlobalErrorCodeConstants;
import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.common.util.date.DateUtils;
import org.openea.eap.framework.common.util.object.BeanUtils;
import org.openea.eap.framework.security.config.SecurityProperties;
import org.openea.eap.framework.security.core.LoginUser;
import org.openea.eap.framework.security.core.util.JwtUtil;
import org.openea.eap.framework.tenant.core.context.TenantContextHolder;
import org.openea.eap.framework.tenant.core.util.TenantUtils;
import org.openea.eap.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import org.openea.eap.module.system.dal.dataobject.user.AdminUserDO;
import org.openea.eap.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import org.openea.eap.module.system.dal.mysql.oauth2.OAuth2RefreshTokenMapper;
import org.openea.eap.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import org.openea.eap.module.system.service.user.AdminUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openea.eap.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static org.openea.eap.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * OAuth2.0 Token Service 实现类
 *
 */
@Service
@Slf4j
public class OAuth2TokenServiceImpl implements OAuth2TokenService {

    @Resource
    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    @Resource
    private OAuth2RefreshTokenMapper oauth2RefreshTokenMapper;

    @Resource
    private OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;

    @Resource
    protected OAuth2ClientService oauth2ClientService;
    @Resource
    @Lazy // 懒加载，避免循环依赖
    protected AdminUserService adminUserService;


    @Resource
    private SecurityProperties securityProperties;

    @Override
    @Transactional
    public OAuth2AccessTokenDO createAccessToken(Long userId, Integer userType, String clientId, List<String> scopes) {
       return createAccessToken(userId, null, userType, clientId, scopes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuth2AccessTokenDO createAccessToken(Long userId, String userKey, Integer userType, String clientId, List<String> scopes) {
        OAuth2ClientDO clientDO = oauth2ClientService.validOAuthClientFromCache(clientId);
        // 创建刷新令牌
        OAuth2RefreshTokenDO refreshTokenDO = createOAuth2RefreshToken(userId, userKey, userType, clientDO, scopes);
        // 创建访问令牌
        return createOAuth2AccessToken(refreshTokenDO, clientDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuth2AccessTokenDO refreshAccessToken(String refreshToken, String clientId) {
        // 查询访问令牌
        List<OAuth2RefreshTokenDO> refreshTokenDOs = oauth2RefreshTokenMapper.selectListByRefreshToken(refreshToken);
        if (refreshTokenDOs.size() != 1) {
            if (refreshTokenDOs.size() > 1) {
                log.error("拒绝使用存在重复记录的刷新令牌，记录数：{}", refreshTokenDOs.size());
            }
            throw exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "无效的刷新令牌");
        }
        OAuth2RefreshTokenDO refreshTokenDO = refreshTokenDOs.get(0);

        // 校验 Client 匹配
        OAuth2ClientDO clientDO = oauth2ClientService.validOAuthClientFromCache(clientId);
        if (ObjectUtil.notEqual(clientId, refreshTokenDO.getClientId())) {
            throw exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "刷新令牌的客户端编号不正确");
        }

        // todo 检查不允许频繁刷新token

        // 移除相关的访问令牌
        List<OAuth2AccessTokenDO> accessTokenDOs = oauth2AccessTokenMapper.selectListByRefreshToken(refreshToken);
        if (CollUtil.isNotEmpty(accessTokenDOs)) {
            oauth2AccessTokenMapper.deleteByIdsIgnoringTenant(convertSet(accessTokenDOs, OAuth2AccessTokenDO::getId));
            oauth2AccessTokenRedisDAO.deleteList(convertSet(accessTokenDOs, OAuth2AccessTokenDO::getAccessToken));
        }

        // 已过期的情况下，删除刷新令牌
        if (DateUtils.isExpired(refreshTokenDO.getExpiresTime())) {
            oauth2RefreshTokenMapper.deleteByRefreshToken(refreshToken);
            throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "刷新令牌已过期");
        }

        // 创建访问令牌
        return createOAuth2AccessToken(refreshTokenDO, clientDO);
    }

    @Override
    public OAuth2AccessTokenDO getAccessToken(String accessToken) {
        // 优先从 Redis 中获取
        OAuth2AccessTokenDO accessTokenDO = oauth2AccessTokenRedisDAO.get(accessToken);
        boolean refreshTokenAlias = accessTokenDO != null
                && ObjectUtil.equal(accessTokenDO.getAccessToken(), accessTokenDO.getRefreshToken());
        if (accessTokenDO != null && !JwtUtil.isJwtWithoutId(accessToken) && !refreshTokenAlias) {
            return accessTokenDO;
        }

        // 无 jti 的历史 JWT 可能由旧版本重复签发。
        // 被当作 access token 缓存的 refresh token 也可能存在历史重复行。
        // 这两种情况即使 Redis 已命中，也必须回查数据库确认 token 只对应一条身份记录。

        // 缓存未命中，或历史令牌需要兼容校验时，从 MySQL 中获取访问令牌
        List<OAuth2AccessTokenDO> accessTokenDOs = oauth2AccessTokenMapper.selectListByAccessToken(accessToken);
        if (accessTokenDOs.size() > 1) {
            // 相同 token 可能属于不同用户或租户，不能通过 max(id) 猜测持有者，必须拒绝认证。
            log.error("拒绝使用存在重复记录的访问令牌，记录数：{}", accessTokenDOs.size());
            oauth2AccessTokenRedisDAO.delete(accessToken);
            return null;
        }
        accessTokenDO = CollUtil.getFirst(accessTokenDOs);
        if (accessTokenDO == null) {
            // 特殊：从 MySQL 中获取刷新令牌。原因：解决部分场景不方便刷新访问令牌场景
            // 例如说，积木报表只允许传递 token，不允许传递 refresh_token，导致无法刷新访问令牌
            // 再例如说，前端 WebSocket 的 token 直接跟在 url 上，无法传递 refresh_token
            List<OAuth2RefreshTokenDO> refreshTokenDOs = oauth2RefreshTokenMapper.selectListByRefreshToken(accessToken);
            if (refreshTokenDOs.size() > 1) {
                log.error("拒绝将存在重复记录的刷新令牌作为访问令牌使用，记录数：{}", refreshTokenDOs.size());
                oauth2AccessTokenRedisDAO.delete(accessToken);
                return null;
            }
            OAuth2RefreshTokenDO refreshTokenDO = CollUtil.getFirst(refreshTokenDOs);
            if (refreshTokenDO != null && !DateUtils.isExpired(refreshTokenDO.getExpiresTime())) {
                accessTokenDO = convertToAccessToken(refreshTokenDO);
            }
        }

        // 如果在 MySQL 存在，则往 Redis 中写入
        if (accessTokenDO != null && !DateUtils.isExpired(accessTokenDO.getExpiresTime())) {
            oauth2AccessTokenRedisDAO.set(accessTokenDO);
        }
        return accessTokenDO;
    }

    @Override
    public OAuth2AccessTokenDO checkAccessToken(String accessToken) {
        OAuth2AccessTokenDO accessTokenDO = getAccessToken(accessToken);
        if (accessTokenDO == null) {
            throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "访问令牌不存在");
        }
        if (DateUtils.isExpired(accessTokenDO.getExpiresTime())) {
            throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "访问令牌已过期");
        }
        return accessTokenDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuth2AccessTokenDO removeAccessToken(String accessToken) {
        // 删除访问令牌
        List<OAuth2AccessTokenDO> accessTokenDOs = oauth2AccessTokenMapper.selectListByAccessToken(accessToken);
        if (accessTokenDOs.isEmpty()) {
            return null;
        }
        OAuth2AccessTokenDO accessTokenDO = accessTokenDOs.get(0);
        if (accessTokenDOs.size() > 1) {
            log.warn("撤销访问令牌时发现重复记录，将全部撤销，记录数：{}", accessTokenDOs.size());
        }
        oauth2AccessTokenMapper.deleteByIdsIgnoringTenant(convertSet(accessTokenDOs, OAuth2AccessTokenDO::getId));
        oauth2AccessTokenRedisDAO.delete(accessToken);
        // 删除全部关联刷新令牌，避免被收敛掉的旧访问令牌通过 refresh_token 再次复活
        Set<String> refreshTokens = new LinkedHashSet<>();
        for (OAuth2AccessTokenDO item : accessTokenDOs) {
            if (StrUtil.isNotBlank(item.getRefreshToken())) {
                refreshTokens.add(item.getRefreshToken());
            }
        }
        refreshTokens.forEach(oauth2RefreshTokenMapper::deleteByRefreshToken);
        return accessTokenDO;
    }

    @Override
    public PageResult<OAuth2AccessTokenDO> getAccessTokenPage(OAuth2AccessTokenPageReqVO reqVO) {
        return oauth2AccessTokenMapper.selectPage(reqVO);
    }


    protected String generateAccessToken(OAuth2RefreshTokenDO refreshTokenDO, OAuth2ClientDO clientDO){
        String accessToken = null;
        if(securityProperties.getJwtEnable()) {
            accessToken = JwtUtil.generateJwtToken(refreshTokenDO.getUserKey(), clientDO.getName(), clientDO.getAccessTokenValiditySeconds());
        }
        if(StringUtils.isEmpty(accessToken)){
            accessToken = generateAccessToken();
        }
        return accessToken;
    }

    private OAuth2AccessTokenDO createOAuth2AccessToken(OAuth2RefreshTokenDO refreshTokenDO, OAuth2ClientDO clientDO) {
        String accessToken = generateAccessToken(refreshTokenDO, clientDO);
        OAuth2AccessTokenDO accessTokenDO = new OAuth2AccessTokenDO().setAccessToken(accessToken)
                .setUserId(refreshTokenDO.getUserId()).setUserType(refreshTokenDO.getUserType())
                .setUserKey(refreshTokenDO.getUserKey())
                .setUserInfo(buildUserInfo(refreshTokenDO.getUserId(), refreshTokenDO.getUserType()))
                .setClientId(clientDO.getClientId()).setScopes(refreshTokenDO.getScopes())
                .setRefreshToken(refreshTokenDO.getRefreshToken())
                .setExpiresTime(LocalDateTime.now().plusSeconds(clientDO.getAccessTokenValiditySeconds()));
        accessTokenDO.setTenantId(TenantContextHolder.getTenantId()); // 手动设置租户编号，避免缓存到 Redis 的时候，无对应的租户编号
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // 记录到 Redis 中
        oauth2AccessTokenRedisDAO.set(accessTokenDO);
        return accessTokenDO;
    }

    private OAuth2RefreshTokenDO createOAuth2RefreshToken(Long userId, String userKey, Integer userType, OAuth2ClientDO clientDO, List<String> scopes) {
        OAuth2RefreshTokenDO refreshToken = new OAuth2RefreshTokenDO().setRefreshToken(generateRefreshToken())
                .setUserId(userId).setUserKey(userKey).setUserType(userType)
                .setClientId(clientDO.getClientId()).setScopes(scopes)
                .setExpiresTime(LocalDateTime.now().plusSeconds(clientDO.getRefreshTokenValiditySeconds()));
        oauth2RefreshTokenMapper.insert(refreshToken);
        return refreshToken;
    }

    private OAuth2AccessTokenDO convertToAccessToken(OAuth2RefreshTokenDO refreshTokenDO) {
        OAuth2AccessTokenDO accessTokenDO = BeanUtils.toBean(refreshTokenDO, OAuth2AccessTokenDO.class)
                .setAccessToken(refreshTokenDO.getRefreshToken());
        TenantUtils.execute(refreshTokenDO.getTenantId(),
                        () -> accessTokenDO.setUserInfo(buildUserInfo(refreshTokenDO.getUserId(), refreshTokenDO.getUserType())));
        return accessTokenDO;
    }

    /**
     * 加载用户信息，方便 {@link org.openea.eap.framework.security.core.LoginUser} 获取到昵称、部门等信息
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @return 用户信息
     */
    private Map<String, String> buildUserInfo(Long userId, Integer userType) {
        if (userType.equals(UserTypeEnum.ADMIN.getValue())) {
            AdminUserDO user = adminUserService.getUser(userId);
            return MapUtil.builder(LoginUser.INFO_KEY_NICKNAME, user.getNickname())
                    .put(LoginUser.INFO_KEY_DEPT_ID, StrUtil.toStringOrNull(user.getDeptId())).build();
        } else if (userType.equals(UserTypeEnum.MEMBER.getValue())) {
            // 注意：目前 Member 暂时不读取，可以按需实现
            return Collections.emptyMap();
        }
        return null;
    }

    private static String generateAccessToken() {
        return IdUtil.fastSimpleUUID();
    }

    private static String generateRefreshToken() {
        return IdUtil.fastSimpleUUID();
    }

}
