# EAP Boot 协作指南

## 适用范围与入口

本文件适用于整个 `eap-boot` 仓库，是开发人员、Codex、Claude Code 及其他编码智能体的统一协作约定。规则仅在此维护，避免不同工具的说明重复或冲突。

- 人工开发：开始修改前阅读本文件与 [README.md](README.md)。
- Codex：将本文件作为仓库级工作指引。
- Claude Code：从 [CLAUDE.md](CLAUDE.md) 跳转到本文件，不在 `CLAUDE.md` 重复维护规则。

## 仓库定位

- `eap-boot` 是基于 Java 17 的 EAP 应用仓库；保持现有模块目录和全部 `org.openea.eap` 包名不变。
- 本仓依赖同级 `eap-common` 的 BOM 和共享框架。共享框架变更后，必须先在 `eap-common` 安装新产物再验证本仓。
- 参考 yudao 时仅适配实现，保留 EAP 模块、包名、集成和业务行为；禁止复制 `cn.iocoder.yudao` 包名或覆盖 EAP 定制代码。
- 依赖与插件版本优先由根 POM 和 `eap-common` BOM 管理。子模块只有在存在已说明的兼容性要求时才能覆盖版本。

## 修改流程

1. 先定位模块、接口调用方、数据模型与已有测试，再做最小范围修改。
2. 保留无关的工作区改动；不执行未经要求的回退、批量格式化或删除；不修改 `target/`、`.flattened-pom.xml` 等生成文件。
3. 控制器、API、DTO、数据库字段和包名默认保持兼容。需要破坏性修改时，必须说明迁移方案和影响范围。
4. Spring 测试必须显式提供所需的 mock；数据库空值规范化等持久化差异应在测试夹具或断言中处理。
5. 修改模块职责、依赖版本、构建命令或运行前提时，同步更新 README 和相关技术文档。

## 验证与交付

- 先运行受影响模块的测试；跨模块、共享框架或依赖升级必须运行完整测试与编译。
- Mockito 在 JDK 17 上的动态 agent 提示为警告；测试或编译失败才是阻塞项。
- 交付时说明：修改范围、验证命令、验证结果，以及尚未验证的风险（如有）。

```bash
# 在 ../eap-common：共享框架变更后先安装
mvn -q -DskipTests install

# 在 eap-boot 根目录：跨模块或依赖变更
mvn -q -Dflatten.skip=true test
mvn -q -U -DskipTests -Dflatten.skip=true compile
```
