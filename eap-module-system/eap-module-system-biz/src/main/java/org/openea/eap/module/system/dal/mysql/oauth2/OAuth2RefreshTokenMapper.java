package org.openea.eap.module.system.dal.mysql.oauth2;

import org.openea.eap.framework.mybatis.core.mapper.BaseMapperX;
import org.openea.eap.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.openea.eap.framework.tenant.core.aop.TenantIgnore;
import org.openea.eap.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OAuth2RefreshTokenMapper extends BaseMapperX<OAuth2RefreshTokenDO> {

    @TenantIgnore
    default int deleteByRefreshToken(String refreshToken) {
        return delete(new LambdaQueryWrapperX<OAuth2RefreshTokenDO>()
                .eq(OAuth2RefreshTokenDO::getRefreshToken, refreshToken));
    }

    @TenantIgnore // 获取 token 的时候，需要忽略租户编号。原因是：一些场景下，可能不会传递 tenant-id 请求头，例如说文件上传、积木报表等等
    default OAuth2RefreshTokenDO selectByRefreshToken(String refreshToken) {
        List<OAuth2RefreshTokenDO> refreshTokens = selectListByRefreshToken(refreshToken);
        return refreshTokens.isEmpty() ? null : refreshTokens.get(0);
    }

    /**
     * 查询同一刷新令牌的全部记录，避免历史脏数据触发 selectOne 异常。
     */
    @TenantIgnore
    default List<OAuth2RefreshTokenDO> selectListByRefreshToken(String refreshToken) {
        return selectList(new LambdaQueryWrapperX<OAuth2RefreshTokenDO>()
                .eq(OAuth2RefreshTokenDO::getRefreshToken, refreshToken)
                .orderByDesc(OAuth2RefreshTokenDO::getId));
    }

}
