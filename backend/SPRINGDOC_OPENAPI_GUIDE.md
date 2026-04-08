# SpringDoc OpenAPI 集成指南

## 📖 概述

本项目已成功集成 SpringDoc OpenAPI，提供完整的 REST API 文档和 Swagger UI 界面。

## 🚀 访问地址

### Swagger UI 界面
```
http://localhost:9090/api/swagger-ui.html
```

### OpenAPI JSON 文档
```
http://localhost:9090/api/v3/api-docs
```

## 📦 依赖配置

已在 `pom.xml` 中添加以下依赖：

```xml
<!-- SpringDoc OpenAPI (Swagger) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

## ⚙️ 配置文件

### application.yml 配置

```yaml
# SpringDoc OpenAPI 配置
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: alpha  # 按字母顺序排列操作
    tags-sorter: alpha        # 按字母顺序排列标签
  default-flat-param-object: true
```

## 🏷️ 注解使用示例

### 1. Controller 级别注解

```java
@RestController
@RequestMapping("/health")
@Tag(name = "健康检查", description = "服务状态监控和健康检查接口")
public class HealthController {
    // ...
}
```

### 2. Method 级别注解

```java
@GetMapping
@Operation(
    summary = "服务健康检查", 
    description = "检查服务运行状态，返回服务基本信息和版本号"
)
public ApiResponse<Map<String, Object>> healthCheck() {
    // ...
}
```

### 3. 参数注解

```java
@PostMapping("/set")
@Operation(summary = "设置缓存", description = "存储键值对到 Redis，无过期时间")
public Map<String, Object> setValue(
    @Parameter(description = "缓存键", example = "test:key:1") @RequestParam String key, 
    @Parameter(description = "缓存值", example = "testValue") @RequestParam String value) {
    // ...
}
```

## 📋 API 分组说明

### 1. 健康检查 (Health Check)
- **路径**: `/health`
- **接口**:
  - `GET /health` - 服务健康检查
  - `GET /ping` - 简单心跳检测
  - `GET /redis` - Redis 连接检查

### 2. 数据采集 (Data Collection)
- **路径**: `/api/data`
- **接口**:
  - `POST /stocks` - 采集股票基本信息
  - `POST /quote/{symbol}` - 采集单只股票行情
  - `POST /funds` - 采集基金基本信息
  - `POST /fund-quote/{fundCode}` - 采集基金净值

### 3. 采集目标管理 (Data Collection Target Management)
- **路径**: `/api/data-collection-targets`
- **接口**:
  - `POST /` - 创建采集目标
  - `PUT /{id}` - 更新采集目标 (按 ID)
  - `PUT /code/{code}` - 更新采集目标 (按代码)
  - `DELETE /{id}` - 删除采集目标 (按 ID)
  - `DELETE /code/{code}` - 删除采集目标 (按代码)
  - `GET /{id}` - 查询采集目标 (按 ID)
  - `GET /code/{code}` - 查询采集目标 (按代码)
  - `GET /` - 查询所有采集目标
  - `GET /type/{type}` - 按类型查询采集目标
  - `GET /active` - 查询激活的采集目标
  - `GET /category/{category}` - 按分类查询采集目标
  - `GET /needing-collection` - 查询需要采集的目标
  - `POST /{id}/activate` - 激活采集目标 (按 ID)
  - `POST /{id}/deactivate` - 停用采集目标 (按 ID)
  - `POST /code/{code}/activate` - 激活采集目标 (按代码)
  - `POST /code/{code}/deactivate` - 停用采集目标 (按代码)
  - `GET /count` - 统计采集目标总数
  - `GET /count/type/{type}` - 按类型统计数量
  - `GET /count/active` - 统计激活目标数量
  - `POST /add-fund` - 添加基金采集目标

### 4. 缓存测试 (Cache Test)
- **路径**: `/api/cache`
- **接口**:
  - `POST /set` - 设置缓存
  - `POST /set-expire` - 设置带过期时间的缓存
  - `GET /get` - 获取缓存
  - `DELETE /delete` - 删除缓存
  - `GET /exists` - 检查缓存是否存在
  - `GET /test-connection` - 测试 Redis 连接

## 🔧 自定义 OpenAPI 配置

### OpenApiConfig.java

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("股票基金数据采集系统 API")
                        .version("1.0.0")
                        .description("""
                                ## 系统简介
                                
                                提供股票、基金数据的实时采集、处理和管理功能。
                                
                                ### 核心功能
                                - **数据采集**: 支持股票基本信息、实时行情、基金净值等数据采集
                                - **目标管理**: 动态管理数据采集目标，支持增删改查和激活/停用控制
                                - **健康检查**: 服务状态监控和 Redis 连接检测
                                """)
                        .contact(new Contact()
                                .name("Stock Fund Team")
                                .email("support@stockfund.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

## 🎯 常用注解说明

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Tag` | Controller 分组 | `@Tag(name = "用户管理", description = "用户 CRUD 操作")` |
| `@Operation` | 方法说明 | `@Operation(summary = "创建用户", description = "...")` |
| `@Parameter` | 参数说明 | `@Parameter(description = "用户 ID", example = "1")` |
| `@ApiResponse` | 响应说明 | `@ApiResponse(responseCode = "200", description = "成功")` |
| `@Schema` | DTO 字段说明 | `@Schema(description = "用户名", example = "张三")` |

## ✅ 最佳实践

1. **所有 Controller 都应添加 `@Tag` 注解**，便于分类和查找
2. **所有公开接口都应添加 `@Operation` 注解**，说明功能用途
3. **所有参数应添加 `@Parameter` 注解**，提供示例值
4. **复杂 DTO 应使用 `@Schema` 注解**，说明字段含义
5. **保持文档与实际代码同步更新**

## 🎨 Swagger UI 功能

- **Try it out**: 可直接在界面上测试 API
- **Models**: 查看数据结构定义
- **Authorize**: 配置认证信息（如需要）
- **Download**: 导出 OpenAPI 规范文件

## 🔍 故障排查

### Swagger UI 无法访问？

1. 确认应用已启动且端口正确（默认 9090）
2. 确认 context-path 为 `/api`
3. 检查浏览器控制台是否有错误信息

### 注解不生效？

1. 确认使用的是 `io.swagger.v3.oas.annotations` 包
2. 确认 Bean 已被 Spring 扫描并注册
3. 重启应用使配置生效

---

**最后更新时间**: 2026-03-25
