# 发布记录

## 2.9.0.1-rc3

- 从 v2.9.0 回移 f665dd7 的重复令牌校验和原生撤销修复，与同版 common 的 JWT jti 修复配套。
- 通用认证恢复 access-only；旧无 jti JWT 和历史 refresh 别名缓存要求重新登录。
- 正常 access、POST refresh 和登出保留框架原生入口；不增加业务仓认证旁路。
- 原第三方平台依赖保持 v2.9.0 基线。候选测试、制品和迁移要求见同版 common 升级指南。
