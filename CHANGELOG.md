# 2.9.3 发布候选（尚未上传 Maven Central）

- 回流维护线已验证的认证边界与运行依赖修复，保留 dev 现有第三方版本，不回退平台。
- 本分支从 2.9.3-SNAPSHOT 开发线构建 2.9.3 候选；不覆盖已有版本，二奢商城/Spider 验收前不得发布 Maven Central。

# 更新日志（Changelog）

本文件记录 eap-boot 各版本的对外变更，格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

- 本仓是 eap-common 的消费方：**框架与 BOM 层变更以 [eap-common 更新日志](../eap-common/CHANGELOG.md) 为准**，本文件不重复记录。
- 升级操作见 [升级指南](doc/升级指南.md)（正文在 eap-common）。

## [2.9.3] - 待正式发布

### Fixed

- OAuth2 access token 查询与撤销兼容历史重复 JWT：读取时拒绝身份不明确的重复 token，登出时批量撤销全部
  access-token 与关联 refresh-token；无 jti 的历史 JWT 在新鉴权链路中直接拒绝并要求重新登录，
  refresh-token 不得作为 Bearer 访问凭证；Redis 访问缓存使用独立 v2 命名空间，
  新 JWT 与普通 UUID access-token 保留缓存快路径，避免管理端和会员端触发 TooManyResultsException。
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
