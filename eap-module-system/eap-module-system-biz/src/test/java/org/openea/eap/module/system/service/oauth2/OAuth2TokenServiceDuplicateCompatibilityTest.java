package org.openea.eap.module.system.service.oauth2;

import cn.hutool.jwt.JWTUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.openea.eap.framework.common.exception.ErrorCode;
import org.openea.eap.framework.security.config.SecurityProperties;
import org.openea.eap.framework.test.core.ut.BaseDbUnitTest;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import org.openea.eap.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import org.openea.eap.module.system.dal.mysql.oauth2.OAuth2RefreshTokenMapper;
import org.openea.eap.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Map;

import static org.openea.eap.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static org.openea.eap.framework.test.core.util.RandomUtils.randomPojo;
import static org.openea.eap.framework.test.core.util.RandomUtils.randomString;
import static org.openea.eap.framework.test.core.util.AssertUtils.assertServiceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(OAuth2TokenServiceImpl.class)
class OAuth2TokenServiceDuplicateCompatibilityTest extends BaseDbUnitTest {

    @Resource
    private OAuth2TokenServiceImpl tokenService;
    @Resource
    private OAuth2AccessTokenMapper accessTokenMapper;
    @Resource
    private OAuth2RefreshTokenMapper refreshTokenMapper;

    @MockBean
    private OAuth2AccessTokenRedisDAO accessTokenRedisDAO;
    @MockBean
    private OAuth2ClientService clientService;
    @MockBean
    private AdminUserService adminUserService;
    @MockBean
    private SecurityProperties securityProperties;

    @Test
    void getAccessTokenShouldFailClosedForDuplicateRows() {
        String legacyAccessToken = JWTUtil.createToken(Map.of("username", "legacy-user"), "test-secret".getBytes());
        OAuth2AccessTokenDO first = randomPojo(OAuth2AccessTokenDO.class)
                .setAccessToken(legacyAccessToken)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        OAuth2AccessTokenDO second = cloneIgnoreId(first, token -> token.setRefreshToken(randomString()));
        accessTokenMapper.insert(first);
        accessTokenMapper.insert(second);
        // 复现旧版本签发后 Redis 已被最后一条重复记录覆盖的真实场景。
        when(accessTokenRedisDAO.get(first.getAccessToken())).thenReturn(second);

        OAuth2AccessTokenDO result = tokenService.getAccessToken(first.getAccessToken());

        assertNull(result);
        verify(accessTokenRedisDAO).delete(first.getAccessToken());
    }

    @Test
    void getAccessTokenShouldKeepFastCachePathForTokenWithJti() {
        String accessToken = JWTUtil.createToken(Map.of("jti", "unique-token-id"), "test-secret".getBytes());
        OAuth2AccessTokenDO cached = randomPojo(OAuth2AccessTokenDO.class)
                .setAccessToken(accessToken)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        when(accessTokenRedisDAO.get(accessToken)).thenReturn(cached);

        OAuth2AccessTokenDO result = tokenService.getAccessToken(accessToken);

        assertSame(cached, result);
    }

    @Test
    void getAccessTokenShouldKeepFastCachePathForOpaqueToken() {
        String accessToken = "0123456789abcdef0123456789abcdef";
        OAuth2AccessTokenDO cached = randomPojo(OAuth2AccessTokenDO.class)
                .setAccessToken(accessToken)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        when(accessTokenRedisDAO.get(accessToken)).thenReturn(cached);

        OAuth2AccessTokenDO result = tokenService.getAccessToken(accessToken);

        assertSame(cached, result);
    }

    @Test
    void getAccessTokenShouldFailClosedForCachedDuplicateRefreshTokenAlias() {
        String refreshToken = "0123456789abcdef0123456789abcdef";
        OAuth2RefreshTokenDO first = randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(refreshToken)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        OAuth2RefreshTokenDO second = cloneIgnoreId(first, token -> token.setUserId(first.getUserId() + 1));
        refreshTokenMapper.insert(first);
        refreshTokenMapper.insert(second);
        OAuth2AccessTokenDO cached = randomPojo(OAuth2AccessTokenDO.class)
                .setAccessToken(refreshToken)
                .setRefreshToken(refreshToken)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        when(accessTokenRedisDAO.get(refreshToken)).thenReturn(cached);

        OAuth2AccessTokenDO result = tokenService.getAccessToken(refreshToken);

        assertNull(result);
        verify(accessTokenRedisDAO).delete(refreshToken);
    }

    @Test
    void removeAccessTokenShouldRevokeWholeDuplicateGroup() {
        OAuth2AccessTokenDO first = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        OAuth2AccessTokenDO second = cloneIgnoreId(first, token -> token.setRefreshToken(randomString()));
        accessTokenMapper.insert(first);
        accessTokenMapper.insert(second);
        refreshTokenMapper.insert(randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(first.getRefreshToken()));
        refreshTokenMapper.insert(randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(second.getRefreshToken()));

        OAuth2AccessTokenDO result = tokenService.removeAccessToken(first.getAccessToken());

        assertEquals(second.getId(), result.getId());
        assertTrue(accessTokenMapper.selectListByAccessToken(first.getAccessToken()).isEmpty());
        assertNull(refreshTokenMapper.selectByRefreshToken(first.getRefreshToken()));
        assertNull(refreshTokenMapper.selectByRefreshToken(second.getRefreshToken()));
        verify(accessTokenRedisDAO).delete(first.getAccessToken());
    }

    @Test
    void refreshAccessTokenShouldRejectDuplicateRows() {
        OAuth2RefreshTokenDO first = randomPojo(OAuth2RefreshTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        OAuth2RefreshTokenDO second = cloneIgnoreId(first, token -> token.setUserId(first.getUserId() + 1));
        refreshTokenMapper.insert(first);
        refreshTokenMapper.insert(second);

        assertServiceException(() -> tokenService.refreshAccessToken(first.getRefreshToken(), "default"),
                new ErrorCode(400, "无效的刷新令牌"));
    }
}
