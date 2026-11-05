package org.openea.eap.module.system.api.oauth2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openea.eap.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.openea.eap.module.system.service.oauth2.OAuth2TokenService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2TokenApiImplTest {

    @InjectMocks
    private OAuth2TokenApiImpl tokenApi;
    @Mock
    private OAuth2TokenService tokenService;

    @Test
    void createAccessTokenShouldPassThroughUserKey() {
        OAuth2AccessTokenCreateReqDTO request = new OAuth2AccessTokenCreateReqDTO()
                .setUserId(10L)
                .setUserKey("external-user")
                .setUserType(2)
                .setClientId("default")
                .setScopes(List.of("user_info"));
        when(tokenService.createAccessToken(10L, "external-user", 2, "default", request.getScopes()))
                .thenReturn(new OAuth2AccessTokenDO());

        tokenApi.createAccessToken(request);

        verify(tokenService).createAccessToken(10L, "external-user", 2, "default", request.getScopes());
    }
}
