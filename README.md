# EAP Boot 项目

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE.txt)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)

## 项目简介

EAP Boot 是一个基于 Spring Boot 3.x 的企业级应用开发脚手架（yudao二次开发），提供了完整的后端解决方案。
项目采用模块化设计，支持单体应用和微服务架构，为企业级应用开发提供了坚实的基础。

## 人机协作

开发人员与编码智能体共用 [AGENTS.md](AGENTS.md) 作为仓库协作规范：其中说明模块边界、兼容性要求、测试和交付标准。Codex 直接读取该文件；Claude Code 通过 [CLAUDE.md](CLAUDE.md) 指向同一规则，避免多份指引发生偏差。

推荐协作流程：先确认模块、接口调用方和已有测试 → 做最小范围修改 → 运行相关测试 → 更新 README/技术文档 → 在交付说明中记录验证结果。涉及共享框架时，先安装同级 `eap-common`，再编译或测试本仓。

## 核心特性

- 🚀 **现代化技术栈**：基于 Spring Boot 3.5 + Java 17
- 🏗️ **模块化架构**：清晰的模块划分，易于维护和扩展
- 🔐 **完整的权限系统**：用户、角色、权限管理
- 📊 **基础设施支持**：定时任务、代码生成、接口文档等
- 🌐 **微服务支持**：支持 Spring Cloud Gateway 网关
- 🐳 **容器化部署**：提供 Docker 部署方案
- 📝 **丰富的文档**：完整的开发和部署文档

## 技术栈

### 后端技术
- **核心框架**：Spring Boot 3.5
- **数据访问**：MyBatis Plus
- **安全框架**：Spring Security
- **缓存**：Redis
- **消息队列**：RabbitMQ
- **数据库**：MySQL、PostgreSQL、Oracle 等
- **构建工具**：Maven 3.6+
- **Java版本**：JDK 17

### 微服务技术
- **服务网关**：Spring Cloud Gateway
- **服务注册**：Nacos
- **配置中心**：Nacos Config
- **负载均衡**：Spring Cloud LoadBalancer

## 项目结构

```
eap-boot-pom/
├── eap-server/                 # 主服务模块（单体应用）
├── eap-gateway/               # API网关模块（微服务）
├── eap-module-system/         # 系统管理模块
│   ├── eap-module-system-api/     # 系统API接口
│   ├── eap-module-system-biz/     # 系统业务逻辑
│   └── eap-module-system-rest/    # 系统REST接口
├── eap-module-infra/          # 基础设施模块
│   ├── eap-module-infra-api/      # 基础设施API
│   ├── eap-module-infra-biz/      # 基础设施业务
│   └── eap-module-infra-rest/     # 基础设施REST接口
├── eap-ui/                    # 前端项目
├── doc/                       # 项目文档
├── script/                    # 部署脚本
│   ├── docker/                    # Docker相关脚本
│   ├── sql/                       # 数据库脚本
│   └── shell/                     # Shell脚本
└── nacos/                     # Nacos配置
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+ / PostgreSQL 12+
- Redis 6.0+
- Node.js 16+ (前端开发)

### 共享框架依赖

`eap-boot` 依赖同级 `eap-common` 的 EAP BOM 与框架组件。修改共享框架后，先安装它再验证本仓：

```bash
cd ../eap-common
mvn -q -Dflatten.skip=true test
mvn -q -DskipTests install
cd ../eap-boot
mvn -q -Dflatten.skip=true test
```

### 本地开发

1. **克隆项目**
```bash
git clone https://github.com/eaopen/openea-eap.git
cd openea-eap
```

2. **数据库初始化**
```bash
# 执行数据库脚本
mysql -u root -p < script/sql/mysql/schema.sql
```

3. **配置修改**
```bash
# 修改数据库连接配置
vim eap-server/src/main/resources/application-dev.yml
```

4. **启动应用**
```bash
# 单体应用启动
mvn clean install
cd eap-server
mvn spring-boot:run

# 微服务启动（需要先启动Nacos）
mvn clean install -P cloud
cd eap-gateway
mvn spring-boot:run
```

5. **访问应用**
- 后端接口：http://localhost:48080
- 接口文档：http://localhost:48080/doc.html

### Docker 部署

1. **构建镜像**
```bash
# 构建服务镜像
./script/docker/build-image-server.sh

# 或使用开发环境构建
./script/docker/build-image-server-dev.sh
```

2. **启动服务**
```bash
cd script/docker/eap
docker-compose up -d
```

## 模块说明

### eap-server
主服务模块，单体应用的入口点。集成了所有业务模块，提供完整的 RESTful API。

### eap-gateway
API网关模块，基于 Spring Cloud Gateway 实现。提供路由转发、负载均衡、限流等功能。

### eap-module-system
系统管理模块，包含：
- 用户管理
- 角色权限
- 菜单管理
- 部门管理
- 数据字典
- 国际化支持

### eap-module-infra
基础设施模块，包含：
- 定时任务管理
- 代码生成器
- 接口文档
- 系统监控
- 文件管理

## 开发指南

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用 Lombok 简化代码
- 统一的异常处理
- 完善的日志记录

### 数据库支持
项目支持多种数据库：
- MySQL 8.0+
- PostgreSQL 12+
- Oracle 11g+
- SQL Server 2017+
- 达梦数据库
- DB2

### API文档
项目集成了 Knife4j，提供了完整的 API 文档：
- 访问地址：http://localhost:8080/doc.html
- 支持在线测试
- 自动生成接口文档

## 部署指南

### 传统部署
```bash
# 打包应用
mvn clean package -P prod

# 启动应用
java -jar eap-server/target/eap-server.jar
```

### Docker 部署
```bash
# 使用提供的Docker脚本
cd script/docker/eap
docker-compose up -d
```

### Kubernetes 部署
```bash
# 构建镜像并推送到镜像仓库
./script/docker/build-image-server.sh

# 部署到K8s集群
kubectl apply -f k8s/
```

## 配置说明

### 环境配置
项目支持多环境配置：
- `dev`：开发环境（默认）
- `test`：测试环境
- `prod`：生产环境

### Maven Profiles
- `dev`：开发环境配置
- `cloud`：微服务模式
- `ossrh`：发布到Maven中央仓库

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目基于 [MIT License](LICENSE.txt) 开源协议。

## 联系我们

- 项目地址：https://github.com/eaopen/openea-eap
- 问题反馈：https://github.com/eaopen/openea-eap/issues
- 邮箱：github@eaopen.github.io

## 致谢
感谢众多个人和组织的支持和帮助，未一一列名。
### 参考项目
* Yudao Boot 版本, RuoYi-Vue 全新 Pro 版本。   
https://github.com/YunaiV/ruoyi-vue-pro

* Yudao Cloud 版本, ruoyi-vue-pro 全新 Cloud 版本     
https://github.com/YunaiV/yudao-cloud

* Jeecg Boot 版本    
https://github.com/jeecgboot/jeecg-boot

### 贡献者
感谢所有为本项目做出贡献的开发者！

---

⭐ 如果这个项目对你有帮助，请给我们一个 Star！
