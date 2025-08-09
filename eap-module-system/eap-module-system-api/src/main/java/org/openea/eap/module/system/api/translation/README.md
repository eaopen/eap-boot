# 翻译服务API使用指南

## 概述

翻译服务API提供了统一的翻译接口，支持多种翻译场景，包括菜单翻译、UI词条翻译、内容翻译等。服务具备智能缓存、质量评估、长度控制等特性。

## 核心特性

- **多场景支持**: 菜单、UI、按钮、表单、消息等不同场景的专业翻译
- **长度控制**: 针对UI元素提供长度限制功能
- **上下文感知**: 根据使用场景和上下文优化翻译质量
- **质量评估**: 自动评估翻译质量并提供优化建议
- **批量处理**: 支持批量翻译和并行处理
- **缓存优化**: 智能缓存机制提升响应速度
- **审核优化**: 支持翻译结果的二次审核和优化

## 快速开始

### 1. 依赖注入

```java
@Resource
private TranslationApi translationApi;
```

### 2. 基本使用

#### 单个文本翻译
```java
TranslationRequestDTO request = TranslationRequestDTO.builder()
    .text("你好，世界")
    .fromLang("zh-CN")
    .toLang("en-US")
    .scene("content")
    .build();

TranslationResponseDTO result = translationApi.translate(request);
if (result.getSuccess()) {
    System.out.println("翻译结果: " + result.getTranslatedText());
}
```

#### 菜单翻译
```java
TranslationResponseDTO result = translationApi.translateMenu("用户管理", "zh-CN", "en-US");
// 结果: "User Management"
```

#### UI词条翻译（带长度限制）
```java
TranslationResponseDTO result = translationApi.translateUI("确认删除", "zh-CN", "en-US", 15);
// 结果: "Confirm Delete" (长度控制在15字符内)
```

#### 批量翻译
```java
Map<String, String> texts = new HashMap<>();
texts.put("menu1", "首页");
texts.put("menu2", "用户管理");
texts.put("menu3", "系统设置");

BatchTranslationRequestDTO request = BatchTranslationRequestDTO.builder()
    .texts(texts)
    .fromLang("zh-CN")
    .toLang("en-US")
    .scene("menu")
    .parallel(true)
    .build();

BatchTranslationResponseDTO result = translationApi.batchTranslate(request);
```

## 翻译场景

### 支持的场景类型

| 场景代码 | 场景名称 | 推荐长度 | 特点 |
|---------|---------|---------|------|
| `menu` | 菜单翻译 | 20字符 | 简洁、一致、易理解 |
| `ui` | UI词条翻译 | 30字符 | 界面友好、长度限制 |
| `button` | 按钮翻译 | 15字符 | 动作导向、简洁有力 |
| `form_label` | 表单标签 | 25字符 | 准确描述、专业术语 |
| `message` | 提示信息 | 100字符 | 友好提示、易理解 |
| `error` | 错误信息 | 150字符 | 准确描述问题 |
| `content` | 内容翻译 | 1000字符 | 保持原意、流畅自然 |
| `auction` | 拍卖术语 | 50字符 | 专业准确、行业标准 |

### 场景使用示例

```java
// 菜单翻译 - 简洁明了
TranslationResponseDTO menuResult = translationApi.translateMenu("商品管理", "zh-CN", "en-US");

// 按钮翻译 - 动作导向
TranslationRequestDTO buttonRequest = TranslationRequestDTO.builder()
    .text("立即购买")
    .fromLang("zh-CN")
    .toLang("en-US")
    .scene("button")
    .maxLength(15)
    .build();

// 错误信息翻译 - 详细准确
TranslationRequestDTO errorRequest = TranslationRequestDTO.builder()
    .text("用户名或密码错误，请重新输入")
    .fromLang("zh-CN")
    .toLang("en-US")
    .scene("error")
    .build();
```

## 高级功能

### 1. 上下文感知翻译

```java
String context = "这是一个电商平台的商品详情页面";
TranslationResponseDTO result = translationApi.translateWithContext(
    "立即购买", "zh-CN", "en-US", context, "button");
```

### 2. 翻译质量审核

```java
TranslationResponseDTO optimized = translationApi.reviewAndOptimize(
    "用户管理", "User Manage", "zh-CN", "en-US", "menu");
// 可能优化为: "User Management"
```

### 3. 服务状态监控

```java
TranslationServiceStatus status = translationApi.getServiceStatus();
System.out.println("服务可用性: " + status.getAvailable());
System.out.println("缓存命中率: " + status.getCacheStatus().getHitRate());
```

## REST API 接口

### 基础翻译接口

```bash
# 单个翻译
POST /system/translation/translate
Content-Type: application/json

{
  "text": "你好，世界",
  "fromLang": "zh-CN",
  "toLang": "en-US",
  "scene": "content",
  "maxLength": 100,
  "qualityLevel": 7
}

# 快速翻译（简化接口）
POST /system/translation/quick-translate?text=你好&fromLang=zh-CN&toLang=en-US

# 菜单翻译
GET /system/translation/translate-menu?menuText=用户管理&fromLang=zh-CN&toLang=en-US

# UI翻译
GET /system/translation/translate-ui?uiText=确认删除&fromLang=zh-CN&toLang=en-US&maxLength=15
```

### 批量翻译接口

```bash
POST /system/translation/batch-translate
Content-Type: application/json

{
  "texts": {
    "menu1": "首页",
    "menu2": "用户管理",
    "menu3": "系统设置"
  },
  "fromLang": "zh-CN",
  "toLang": "en-US",
  "scene": "menu",
  "parallel": true
}
```

### 审核优化接口

```bash
POST /system/translation/review-and-optimize
?originalText=用户管理
&translatedText=User Manage
&fromLang=zh-CN
&toLang=en-US
&scene=menu
```

## 最佳实践

### 1. 场景选择

- **菜单项**: 使用 `menu` 场景，保持简洁一致
- **按钮文字**: 使用 `button` 场景，突出动作性
- **表单标签**: 使用 `form_label` 场景，确保专业准确
- **提示消息**: 使用 `message` 场景，友好易懂
- **错误信息**: 使用 `error` 场景，准确描述问题

### 2. 长度控制

```java
// UI元素翻译时设置合适的长度限制
TranslationResponseDTO result = translationApi.translateUI(
    "非常长的按钮文字描述", "zh-CN", "en-US", 12);

// 检查是否超长
if (result.getSuggestions().contains("翻译结果超出长度限制")) {
    // 处理超长情况
}
```

### 3. 质量控制

```java
TranslationRequestDTO request = TranslationRequestDTO.builder()
    .text("重要的系统提示信息")
    .fromLang("zh-CN")
    .toLang("en-US")
    .scene("message")
    .qualityLevel(8) // 设置较高的质量要求
    .needReview(true) // 标记需要人工审核
    .build();
```

### 4. 批量处理优化

```java
// 大量翻译时启用并行处理
BatchTranslationRequestDTO request = BatchTranslationRequestDTO.builder()
    .texts(largeTextMap)
    .fromLang("zh-CN")
    .toLang("en-US")
    .parallel(true) // 启用并行处理
    .enableCache(true) // 启用缓存
    .build();
```

### 5. 错误处理

```java
TranslationResponseDTO result = translationApi.translate(request);
if (!result.getSuccess()) {
    log.error("翻译失败: {} - {}", result.getErrorCode(), result.getErrorMessage());
    // 使用原文或默认文本
    return result.getOriginalText();
}
```

## 性能优化

### 1. 缓存策略
- 系统自动缓存翻译结果，相同文本的重复翻译会直接返回缓存结果
- 可通过 `enableCache` 参数控制是否使用缓存

### 2. 批量处理
- 大量文本翻译时使用批量接口，支持并行处理
- 合理设置批量大小，建议每批不超过100条

### 3. 场景优化
- 根据实际使用场景选择合适的翻译模式
- 专业术语翻译使用对应的专业场景

## 监控和统计

```java
// 获取服务状态
TranslationServiceStatus status = translationApi.getServiceStatus();

// 检查各项指标
System.out.println("今日翻译总数: " + status.getStatistics().getTodayTranslations());
System.out.println("缓存命中率: " + status.getCacheStatus().getHitRate());
System.out.println("平均响应时间: " + status.getStatistics().getAverageResponseTime());
```

## 常见问题

### Q: 如何处理翻译长度超限？
A: 系统会在响应中提供建议，可以调整 `maxLength` 参数或使用更简洁的原文。

### Q: 如何提高翻译质量？
A: 设置更高的 `qualityLevel`，提供准确的 `context` 和 `scene` 信息。

### Q: 批量翻译如何处理失败的项目？
A: 批量翻译会返回每个项目的详细结果，可以单独处理失败的项目。

### Q: 如何自定义翻译场景？
A: 可以通过 `context` 参数提供详细的场景描述，系统会据此优化翻译策略。