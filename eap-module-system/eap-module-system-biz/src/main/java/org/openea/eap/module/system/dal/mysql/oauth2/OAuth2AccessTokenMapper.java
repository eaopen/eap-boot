package org.openea.eap.module.system.dal.mysql.oauth2;

import org.openea.eap.framework.common.pojo.PageResult;
import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.openea.eap.framework.tenant.core.aop.TenantIgnore;
import org.openea.eap.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface OAuth2AccessTokenMapper extends BaseMapperX<OAuth2AccessTokenDO> {

    @TenantIgnore // 获取 token 的时候，需要忽略租户编号。原因是：一些场景下，可能不会传递 tenant-id 请求头，例如说文件上传、积木报表等等
    default OAuth2AccessTokenDO selectByAccessToken(String accessToken) {
        List<OAuth2AccessTokenDO> accessTokens = selectListByAccessToken(accessToken);
        return accessTokens.isEmpty() ? null : accessTokens.get(0);
    }

    /**
     * 查询同一访问令牌的全部记录，兼容历史版本可能产生的重复 JWT。
     */
    @TenantIgnore
    default List<OAuth2AccessTokenDO> selectListByAccessToken(String accessToken) {
        return selectList(new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .eq(OAuth2AccessTokenDO::getAccessToken, accessToken)
                .orderByDesc(OAuth2AccessTokenDO::getId));
    }

    @TenantIgnore
    default List<OAuth2AccessTokenDO> selectListByRefreshToken(String refreshToken) {
        return selectList(OAuth2AccessTokenDO::getRefreshToken, refreshToken);
    }

    @TenantIgnore
    default int deleteByIdsIgnoringTenant(Collection<Long> ids) {
        return delete(new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .in(OAuth2AccessTokenDO::getId, ids));
    }

    default PageResult<OAuth2AccessTokenDO> selectPage(OAuth2AccessTokenPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .eqIfPresent(OAuth2AccessTokenDO::getUserId, reqVO.getUserId())
                .eqIfPresent(OAuth2AccessTokenDO::getUserType, reqVO.getUserType())
                .likeIfPresent(OAuth2AccessTokenDO::getClientId, reqVO.getClientId())
                .gt(OAuth2AccessTokenDO::getExpiresTime, LocalDateTime.now())
                .orderByDesc(OAuth2AccessTokenDO::getId));
    }

}
