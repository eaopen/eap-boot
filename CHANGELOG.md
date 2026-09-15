# 更新日志（Changelog）

本文件记录 eap-boot 各版本的对外变更，格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

- 本仓是 eap-common 的消费方：**框架与 BOM 层变更以 [eap-common 更新日志](../eap-common/CHANGELOG.md) 为准**，本文件不重复记录。
- 升级操作见 [升级指南](doc/升级指南.md)（正文在 eap-common）。

## [Unreleased]

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
