> 历史记录：2026-11-13 已授权将 boot4 合入 dev；当前规则见 [分支与发布策略](分支与发布策略.md)，下文描述迁移前状态。

# EAP Boot4 稳定版本与认证兼容候选（2026-11-05）

> 候选分支 `feature/boot4-stable-refresh-20261105`，从原 `boot4` HEAD `ebf80d6` 派生。目标为 JDK21 / Boot4.1.2；依赖同版本候选 `eap-common:3.0.1-SNAPSHOT`。本候选不得直接覆盖 `dev`。

## 对齐的稳定基线

- 版本由配套 `eap-common/eap-dependencies` 和 `eap-cloud-dependencies` 管理：Boot4.1.2、springdoc3.2.0、MyBatis-Plus3.5.18.1、Spring Cloud2025.1.4、Spring Cloud Alibaba2025.1.0.0、Druid1.2.28。
- eap-boot 根 POM 调整为 `3.0.1-SNAPSHOT` / Java21 / Boot4.1.2，不把 Boot3 Starter 整包引入新的 Boot4 依赖树。

## dev 与维护分支近期修复的 Boot4 适配

- 移植 access token / refresh token 不可互相冒用、重复 token fail-closed、撤销全部关联凭证、拒绝旧无 jti JWT 再次续期、Redis 新的 access-token v2 命名空间。
- 移植 OAuth2 Access/Refresh Mapper 对重复历史记录、租户绕过和逻辑删除记录的处理，与 token service 同批测试，避免仅迁业务层造成 API 不一致。
- 适当恢复历史 JWT/重复 token 回归测试与运行时检查脚本；底层签发由配套 eap-common 的 `JwtUtil` 提供随机 jti。
- 旧用户、角色、租户、历史数据和业务表不需为 Boot4 强制迁移；不符合安全规则的历史登录凭证可以重新登录。保持数据兼容与认证安全边界不能混为一谈。
- 已同步 `dev` 对 SonarQube 的 CI 修复：去掉未配置时自动执行且必失败的占位流程，改为需明确 project_key 的手动触发；RabbitMQ 的默认转换器/受信类型映射器已在配套 `eap-common` Boot4 候选中移植并通过单测，但本仓实际消息生产与旧消息消费仍需业务级验证。

## 需实际验证的门禁

1. JDK21 + Maven：先构建安装 eap-common 3.0.1-SNAPSHOT 及 Cloud BOM，之后运行 `mvn -B -Dflatten.skip=true test` 和 `mvn -B -U -DskipTests -Dflatten.skip=true compile`。
2. 使用历史访问/刷新 JWT 及重复记录夹具验证认证 fail-closed、撤销、重登、同秒多会话唯一性；按授权要求检查 Redis v1/v2。
3. 使用旧数据库快照验证所有 EAP 旧业务的读写、权限和启动，不以旧业务转 Schema v2 为条件。Springdoc API 文档生产匿名访问默认应拒绝。
4. 检查工作流/动态 SQL 与 Boot4 JDBC/MyBatis-Plus 依赖、实际 EAP 服务启动、必要消息协议与发布回滚。

## 本轮 JDK21 验证证据（2026-11-05）

- 本机 GraalVM JDK 21.0.7 / Maven 3.9.13；使用对应 `eap-common 3.0.1-SNAPSHOT` **经 Flatten 的本机安装制品**。
- `mvn -B -nsu -Dspring.data.redis.port=26379 -Dflatten.skip=true test`：**13/13 Reactor 模块 BUILD SUCCESS**。
- 系统业务模块 **460** 项测试，0 failure/0 error/8 skipped；基础设施模块 **84** 项测试，0 failure/0 error/10 skipped。
- JWT/OAuth: 包括 `OAuth2TokenServiceImplTest` 13 项以及 `OAuth2TokenServiceDuplicateCompatibilityTest` 9 项，均通过。
- 测试时用 Docker 临时容器提供仅绑定 loopback 的 Redis 7，避免原测试端口上的 Redis 认证设置干扰；测试完成后清理容器。**没有启动实际业务数据库或 EAP 生产服务。**
- 修正两个测试用例的时区假设：七牛短信 epoch 时间使用 JVM 当前 ZoneId 比较；OAuth 分页过期夹具设置为 `LocalDateTime.now().minusMinutes(5)`。均只修改测试、不改变生产逻辑。
- 本地 Maven 安装验证不能代替远端制品仓库的 CI 可复现性；历史业务数据 CRUD、Nacos、MQ 实际消息、Springdoc 匿名访问、部署回滚仍待 DEV 验证。

## 合并策略

本轮 JDK21 25/25（配套 eap-common）及 13/13（eap-boot）本地模块测试已通过，经用户授权可将候选合并入**仅供持续研发的 `boot4` 分支**。本次合并不代表生产可上线：干净 Maven CI、正式依赖发布、数据库旧业务完整读写、真实认证和 MQ、回滚演练仍是 `dev` 合并及部署的强制门禁；不自动合并 dev，不触碰生产。