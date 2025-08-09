# EAP AI翻译模块

## 概述

EAP AI翻译模块为拍卖平台提供了强大的多语言支持和AI自动翻译能力。该模块支持多种AI翻译服务提供商，具备智能缓存、质量评估和回退机制。

## 主要特性

### 🚀 核心功能
- **多提供商支持**: 支持OpenAI、百度、腾讯等多种AI翻译服务
- **智能回退**: 当主要提供商不可用时自动切换到备用提供商
- **质量评估**: 自动评估翻译质量，过滤低质量结果
- **缓存优化**: 基于MD5哈希的智能缓存，显著提升性能
- **批量翻译**: 支持批量翻译和负载均衡
- **统计监控**: 完整的缓存命中率和翻译质量统计

### 🎯 拍卖场景优化
- **专业术语**: 针对拍卖行业术语进行优化
- **上下文感知**: 根据不同场景（商品描述、竞拍提示等）调整翻译策略
- **实时翻译**: 支持竞拍过程中的实时消息翻译

## 快速开始

### 1. 配置文件

在 `application.yml` 中添加以下配置：

```yaml
eap:
  i18n:
    ai:
      enabled: true
      default-provider: mock
      providers:
        mock:
          enabled: true
          priority: 999
        openai:
          enabled: true
          api-key: your-openai-api-key
          priority: 1
      global:
        enable-cache: true
        cache-expire-hours: 24
```

### 2. 基本使用

```java
@Resource
private AITranslationFactory aiTranslationFactory;

// 单个翻译
AITranslationResult result = aiTranslationFactory.translateWithFallback(
    "竞拍成功", "zh-CN", "en-US", "auction");

// 批量翻译
Map<String, String> texts = Map.of(
    "success", "竞拍成功",
    "failed", "竞拍失败"
);
Map<String, AITranslationResult> results = aiTranslationFactory.batchTranslateWithLoadBalance(
    texts, "zh-CN", "en-US", "auction");
```

### 3. 在国际化中使用

系统会自动在 `I18nUtil.getMessage()` 中集成AI翻译：

```java
// 如果找不到翻译，会自动使用AI翻译
String message = I18nUtil.getMessage("auction.bid.success", "竞拍成功");
```

## 架构设计

### 模块结构

```
eap-spring-boot-starter-biz-i18n/
├── src/main/java/org/openea/eap/framework/i18n/
│   ├── ai/                          # AI翻译核心接口
│   │   ├── AITranslationProvider    # 翻译提供商接口
│   │   ├── AITranslationResult      # 翻译结果
│   │   ├── AITranslationConfig      # 配置类
│   │   ├── AbstractAITranslationProvider # 抽象基类
│   │   ├── impl/                    # 具体实现
│   │   │   ├── OpenAITranslationProvider
│   │   │   └── MockTranslationProvider
│   │   └── config/                  # 自动配置
│   │       ├── AITranslationProperties
│   │       └── AITranslationAutoConfiguration
│   ├── core/
│   │   └── I18nUtil                 # 国际化工具类
│   └── config/
│       └── EapI18nAutoConfiguration # 主配置类

eap-module-system-biz/
├── src/main/java/org/openea/eap/module/system/
│   ├── service/language/
│   │   ├── AITranslationFactory     # 翻译工厂服务
│   │   ├── AITranslationCacheService # 缓存服务
│   │   └── AITranslationCacheServiceImpl
│   ├── dal/
│   │   ├── dataobject/language/
│   │   │   ├── AITranslationCacheDO
│   │   │   └── AITranslationCacheStatsDO
│   │   └── mysql/language/
│   │       ├── AITranslationCacheMapper
│   │       └── AITranslationCacheStatsMapper
│   └── controller/admin/language/
│       └── AITranslationTestController # 测试控制器
```

### 设计原则

1. **接口分离**: Framework层定义接口，Business层实现具体逻辑
2. **依赖注入**: 通过Spring容器管理组件生命周期
3. **配置驱动**: 支持通过配置文件灵活配置各种参数
4. **松耦合**: 各组件间通过接口交互，便于扩展和测试

## 翻译提供商

### Mock提供商
- **用途**: 开发和测试环境
- **特点**: 无需外部API，提供预定义翻译
- **配置**: 默认启用，优先级最低

### OpenAI提供商
- **用途**: 生产环境高质量翻译
- **特点**: 支持多种模型，翻译质量高
- **配置**: 需要API密钥，支持自定义端点

### 扩展新提供商

1. 实现 `AITranslationProvider` 接口
2. 继承 `AbstractAITranslationProvider` 获得通用功能
3. 在配置类中注册为Spring Bean

```java
@Component
public class CustomTranslationProvider extends AbstractAITranslationProvider {
    // 实现具体翻译逻辑
}
```

## 缓存机制

### 缓存策略
- **哈希键**: 基于源文本MD5哈希
- **过期时间**: 可配置，默认24小时
- **清理机制**: 自动清理过期和低质量缓存

### 缓存表结构

```sql
-- 翻译缓存表
CREATE TABLE ai_translation_cache (
    id BIGINT PRIMARY KEY,
    content_hash VARCHAR(64) NOT NULL,
    source_text TEXT NOT NULL,
    source_lang VARCHAR(10) NOT NULL,
    target_lang VARCHAR(10) NOT NULL,
    translated_text TEXT NOT NULL,
    ai_provider VARCHAR(30) NOT NULL,
    ai_model VARCHAR(50) NOT NULL,
    translation_quality TINYINT DEFAULT 5,
    confidence DECIMAL(3,2),
    context_type VARCHAR(50),
    hit_count INT DEFAULT 1,
    last_hit_time DATETIME,
    expire_time DATETIME,
    -- 标准字段
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0
);

-- 缓存统计表
CREATE TABLE ai_translation_cache_stats (
    id BIGINT PRIMARY KEY,
    stat_date DATE NOT NULL,
    ai_provider VARCHAR(30) NOT NULL,
    ai_model VARCHAR(50),
    source_lang VARCHAR(10) NOT NULL,
    target_lang VARCHAR(10) NOT NULL,
    total_requests INT DEFAULT 0,
    cache_hits INT DEFAULT 0,
    cache_misses INT DEFAULT 0,
    hit_rate DECIMAL(5,2) DEFAULT 0.00,
    avg_quality DECIMAL(3,1) DEFAULT 0.0,
    -- 标准字段
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0
);
```

## API接口

### 测试接口

系统提供了完整的测试接口，访问路径：`/system/ai-translation/test`

- `GET /translate` - 单个翻译测试
- `POST /batch-translate` - 批量翻译测试
- `GET /providers/status` - 获取提供商状态
- `GET /cache/stats` - 获取缓存统计
- `POST /cache/clean` - 清理缓存
- `GET /cache/hit-rate` - 获取缓存命中率
- `POST /demo` - 演示功能

### 使用示例

```bash
# 单个翻译
curl -X GET "http://localhost:8080/admin-api/system/ai-translation/test/translate?text=你好&fromLang=zh-CN&toLang=en-US"

# 批量翻译
curl -X POST "http://localhost:8080/admin-api/system/ai-translation/test/batch-translate?fromLang=zh-CN&toLang=en-US" \
  -H "Content-Type: application/json" \
  -d '{"greeting":"你好","goodbye":"再见"}'

# 获取缓存统计
curl -X GET "http://localhost:8080/admin-api/system/ai-translation/test/cache/stats?days=7"
```

## 配置参考

### 完整配置示例

```yaml
eap:
  i18n:
    ai:
      enabled: true
      default-provider: openai
      providers:
        openai:
          enabled: true
          api-key: ${OPENAI_API_KEY}
          api-endpoint: https://api.openai.com/v1/chat/completions
          default-model: gpt-3.5-turbo
          priority: 1
          timeout: 30000
          max-retries: 3
          rate-limit: 60
          quality-threshold: 7
          supported-languages:
            - zh-CN
            - en-US
            - ja-JP
        mock:
          enabled: true
          priority: 999
          quality-threshold: 5
      fallback-chains:
        "zh-CN-en-US": ["openai", "mock"]
        "en-US-zh-CN": ["openai", "mock"]
      global:
        default-quality-threshold: 5
        enable-cache: true
        cache-expire-hours: 24
        enable-quality-evaluation: true
        max-batch-size: 100
```

### 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `enabled` | 是否启用AI翻译 | `true` |
| `default-provider` | 默认翻译提供商 | `mock` |
| `api-key` | API密钥 | - |
| `priority` | 优先级（数字越小优先级越高） | `10` |
| `timeout` | 请求超时时间（毫秒） | `30000` |
| `rate-limit` | 每分钟最大请求数 | `60` |
| `quality-threshold` | 质量阈值（1-10） | `5` |
| `cache-expire-hours` | 缓存过期时间（小时） | `24` |

## 监控和运维

### 性能监控
- 缓存命中率统计
- 翻译质量分布
- 提供商可用性监控
- 请求响应时间统计

### 运维操作
- 缓存清理和预热
- 提供商状态检查
- 翻译质量评估
- 批量数据导入导出

## 最佳实践

### 1. 提供商配置
- 生产环境建议配置多个提供商作为备份
- 根据成本和质量要求设置合适的优先级
- 定期检查API配额和限制

### 2. 缓存优化
- 合理设置缓存过期时间
- 定期清理低质量缓存
- 监控缓存命中率，优化常用文本

### 3. 质量控制
- 设置合适的质量阈值
- 定期人工审核翻译质量
- 建立专业术语词典

### 4. 性能优化
- 使用批量翻译减少API调用
- 预热常用文本缓存
- 合理设置请求限流

## 故障排查

### 常见问题

1. **翻译失败**
   - 检查API密钥是否正确
   - 确认网络连接正常
   - 查看提供商服务状态

2. **缓存不生效**
   - 检查缓存配置是否启用
   - 确认数据库表是否创建
   - 查看缓存过期时间设置

3. **翻译质量差**
   - 调整质量阈值设置
   - 检查上下文配置
   - 考虑更换翻译提供商

### 日志配置

```yaml
logging:
  level:
    org.openea.eap.framework.i18n: DEBUG
    org.openea.eap.module.system.service.language: DEBUG
```

## 更新日志

### v1.0.0 (2025-02-08)
- ✨ 初始版本发布
- ✨ 支持多翻译提供商
- ✨ 智能缓存机制
- ✨ 质量评估和回退
- ✨ 批量翻译支持
- ✨ 完整的监控统计

## 贡献指南

欢迎提交Issue和Pull Request来改进这个模块。

## 许可证

本项目采用 Apache 2.0 许可证。