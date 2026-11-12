# 主干开发 3.0.1-SNAPSHOT

- 将 boot4 合并到 dev，采用 Java 21 / Spring Boot 4.1.2；2.x 转为独立维护分支。
- 完整保留 2.9.3.1、2.9.4-SNAPSHOT 的认证安全修复和依赖补丁，测试迁移到 MockitoBean。
- 增加双线 CI 与发布策略；3.x 仍未正式发布。

# 维护线 2.9.4-SNAPSHOT

- 合并 `main/2.9.3.1` 发布历史，完整继承热修复；保留 dev 已有的后续升级。

- 在 `2.9.3.1` 正式发布后推进开发版本，保持 Java 17 / Spring Boot 3.5.16；不包含 Boot 4。
- 与同版 eap-common 对齐；保留 Redis 旧 Key 读取、Access/Refresh Token 隔离及重复 Token 拒绝逻辑，并补充旧缓存回归验证。
- 默认前缀为 `oauth2_access_token:%s`，保留 `eap.oauth2.access-token-key-prefix` 配置；v2 缓存不会自动迁移。

# 更新日志（Changelog）

本文件记录 eap-boot 各版本的对外变更，格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

- 本仓是 eap-common 的消费方：**框架与 BOM 层变更以 [eap-common 更新日志](../eap-common/CHANGELOG.md) 为准**，本文件不重复记录。
- 升级操作见 [升级指南](doc/升级指南.md)（正文在 eap-common）。

- 补齐正式线已有的 H2 时间精度、七牛短信回执时区和过期 Token 测试边界修正。

## [2.9.3.1] - 2026-11-13

### Fixed

- 沿用 main 已包含的最新 Redis Access Token 前缀配置修复，默认 Key 恢复旧格式 oauth2_access_token:{token}。
- 修正旧前缀缓存回归测试：有效 Access Token 可继续读取，旧 Refresh Token 别名仍必须拒绝并清理。
- 与 eap-common 2.9.3.1 同版发布；不改变认证安全边界，不要求清空 Redis。

## [2.9.3] - 2026-11-06

### Fixed

- OAuth2 access token 查询与撤销兼容历史重复 JWT：读取时拒绝身份不明确的重复 token，登出时批量撤销全部
  access-token 与关联 refresh-token；无 jti 的历史 JWT 在新鉴权链路中直接拒绝并要求重新登录，
  refresh-token 不得作为 Bearer 访问凭证；
  新 JWT 与普通 UUID access-token 保留缓存快路径，避免管理端和会员端触发 TooManyResultsException。
- OAuth2 RPC 签发恢复透传 `userKey`；保留仅传 `userId` 的原有调用方式，兼容紧密系统与外部集成系统。

### Fixed

- OAuth2 access token 查询与撤销兼容历史重复 JWT：读取时拒绝身份不明确的重复 token，登出时批量撤销全部
  access-token 与关联 refresh-token；Redis 命中无 `jti` 的历史 token 时仍回查数据库，避免缓存覆盖绕过重复检测，
  被当作 access-token 缓存的 refresh-token 同样执行重复检测；新 JWT 与普通 UUID access-token 保留缓存快路径，
  同时避免管理端和会员端触发 `TooManyResultsException`。
- OAuth2 RPC 签发恢复透传 `userKey`；保留仅传 `userId` 的原有调用方式，兼容紧密系统与外部集成系统。

## [2.9.2] - 2026-09-15

### Changed

- 根 pom `revision` 升级到 2.9.2，与 eap-common 2.9.2 BOM 对齐。
- `eap-gateway` 配置与 `doc/技术架构分析.md` 的版本标识同步为 2.9.2。

## [2.9.1] - 2026-08-30

### Added

- 对齐 eap-common JDK 17 基线，升级到 Spring Boot 3.5.16。

### Changed

- 根 pom `revision` 统一到 2.9.1 版本线；补发 **2.9.1b**、**2.9.1c**（仅 `revision` 与开发环境 Nexus 仓库地址调整）。

### Fixed

- OSSRH 部署配置迁移到 Central Publishing Portal。

## [2.8.5] - 2025-10-24

### Changed

- Spring Boot 升级到 3.5.6，移除 dev106 profile 配置。

## 更早版本

2.8.5 之前的版本记录见 git tag 与提交历史。

- 将独立 SonarQube 扫描改为显式手动入口，要求项目键及凭据校验；自动检查继续使用仓库已连接的 SonarCloud 和 CodeQL。
