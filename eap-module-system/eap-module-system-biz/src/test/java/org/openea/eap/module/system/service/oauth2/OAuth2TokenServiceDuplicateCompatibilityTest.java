package org.openea.eap.module.system.service.oauth2;

import cn.hutool.jwt.JWTUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.openea.eap.framework.common.exception.ErrorCode;
import org.openea.eap.framework.security.config.SecurityProperties;
import org.openea.eap.framework.test.core.ut.BaseDbUnitTest;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import org.openea.eap.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import org.openea.eap.module.system.dal.mysql.oauth2.OAuth2RefreshTokenMapper;
import org.openea.eap.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import org.openea.eap.module.system.service.user.AdminUserService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@Import(OAuth2TokenServiceImpl.class)
class OAuth2TokenServiceDuplicateCompatibilityTest extends BaseDbUnitTest {

    @Test
    void oldJwtMustBeRejectedBeforeCacheEvenWhenCleanupFails() {
        String oldJwt = JWTUtil.createToken(Map.of("username", "legacy-user"), "test-secret".getBytes());
        doThrow(new IllegalStateException("simulated cache outage")).when(accessTokenRedisDAO).delete(oldJwt);

        assertServiceException(() -> tokenService.checkAccessToken(oldJwt),
                new ErrorCode(401, "访问令牌不存在"));
        verify(accessTokenRedisDAO, never()).get(oldJwt);
    }

    @Test
    void cachedRefreshAliasMustNotAuthenticateEvenWhenCleanupFails() {
        String refresh = randomString();
        OAuth2AccessTokenDO alias = randomPojo(OAuth2AccessTokenDO.class)
                .setAccessToken(refresh).setRefreshToken(refresh)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        when(accessTokenRedisDAO.get(refresh)).thenReturn(alias);
        doThrow(new IllegalStateException("simulated cache outage")).when(accessTokenRedisDAO).delete(refresh);

        assertServiceException(() -> tokenService.checkAccessToken(refresh),
                new ErrorCode(401, "访问令牌不存在"));
    }

    @Test
    void refreshMustRejectLegacyJwtAncestryIncludingDeletedAccessRows() {
        String refresh = randomString();
        String clientId = randomString();
        refreshTokenMapper.insert(randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(refresh).setClientId(clientId)
                .setExpiresTime(LocalDateTime.now().plusDays(1)));
        String oldJwt = JWTUtil.createToken(Map.of("username", "legacy-user"), "test-secret".getBytes());
        OAuth2AccessTokenDO old = randomPojo(OAuth2AccessTokenDO.class)
                .setAccessToken(oldJwt).setRefreshToken(refresh);
        accessTokenMapper.insert(old);
        accessTokenMapper.deleteById(old.getId());
        when(clientService.validOAuthClientFromCache(clientId)).thenReturn(new OAuth2ClientDO().setClientId(clientId));

        assertEquals(List.of(oldJwt), accessTokenMapper.selectAccessTokensByRefreshTokenIncludingDeleted(refresh));
        assertServiceException(() -> tokenService.refreshAccessToken(refresh, clientId),
                new ErrorCode(401, "旧JWT关联会话不再续期，请重新登录"));
        assertEquals(1, refreshTokenMapper.selectListByRefreshToken(refresh).size());
    }

    @Resource
    private OAuth2TokenServiceImpl tokenService;
    @Resource
    private OAuth2AccessTokenMapper accessTokenMapper;
    @Resource
    private OAuth2RefreshTokenMapper refreshTokenMapper;

    @MockitoBean
    private OAuth2AccessTokenRedisDAO accessTokenRedisDAO;
    @MockitoBean
    private OAuth2ClientService clientService;
    @MockitoBean
    private AdminUserService adminUserService;
    @MockitoBean
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
