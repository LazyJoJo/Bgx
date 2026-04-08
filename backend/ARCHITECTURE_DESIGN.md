# 股票/基金数据采集系统架构设计文档

## 1. 项目概述

### 1.1 项目背景
本项目是一个股票/基金数据采集系统，旨在实时获取股票和基金数据，将其存储在数据库中，并提供API接口供前端或其他系统使用。

### 1.2 项目目标
- 实时采集股票和基金数据
- 数据存储和管理
- 提供统一的API接口
- 支持定时数据采集
- 确保数据一致性和完整性

## 2. 架构设计

### 2.1 架构风格
采用**六边形架构（Hexagonal Architecture）**，也称为**端口和适配器架构**，结合**领域驱动设计（DDD）**原则。

```
┌─────────────────────────────────────────────────────────┐
│                    外部系统/用户                         │
└─────────────────────┬───────────────────────────────────┘
                      │
        ┌─────────────▼─────────────┐
        │        接口层(Interfaces)   │
        │  REST API / GraphQL API   │
        └─────────────┬─────────────┘
                      │
        ┌─────────────▼─────────────┐
        │        应用层(Application)  │
        │    服务编排/用例实现        │
        └─────────────┬─────────────┘
                      │
        ┌─────────────▼─────────────┐
        │        领域层(Domain)      │
        │    领域模型/业务逻辑        │
        └─────────────┬─────────────┘
                      │
        ┌─────────────▼─────────────┐
        │      基础设施层(Infrastructure) │
        │      数据库/消息队列等        │
        └─────────────────────────────┘
```

### 2.2 分层架构说明

#### 2.2.1 领域层（Domain Layer）
- **职责**: 核心业务逻辑和领域模型
- **组成**:
  - 领域实体（Entity）
  - 值对象（Value Object）
  - 聚合根（Aggregate Root）
  - 领域服务（Domain Service）
  - 仓储接口（Repository Interface）
- **特点**: 独立于技术实现，纯业务逻辑

#### 2.2.2 应用层（Application Layer）
- **职责**: 协调领域层和基础设施层，处理应用逻辑
- **组成**:
  - 应用服务（Application Service）
  - 命令对象（Command）
  - 查询对象（Query）
  - DTO转换器
- **特点**: 不包含业务逻辑，只负责编排和协调

#### 2.2.3 基础设施层（Infrastructure Layer）
- **职责**: 提供技术支撑和外部依赖
- **组成**:
  - 持久化对象（PO）
  - 数据访问对象（DAO/Repository Implementation）
  - 外部服务适配器
  - 配置类
- **特点**: 实现领域层定义的接口

#### 2.2.4 接口层（Interfaces Layer）
- **职责**: 处理外部交互
- **组成**:
  - 控制器（Controller）
  - 数据传输对象（DTO）
  - API网关适配器
- **特点**: 将外部请求转换为应用层可处理的格式

## 3. 设计模式应用

### 3.1 领域驱动设计模式

#### 3.1.1 聚合模式（Aggregate Pattern）
- **Stock聚合**: [Stock](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/Stock.java#L10-L43) + [StockQuote](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/StockQuote.java#L10-L36)
- **Fund聚合**: [Fund](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/Fund.java#L10-L46) + [FundQuote](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/FundQuote.java#L10-L30)
- **聚合根**: [Stock](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/Stock.java#L10-L43)、[Fund](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/Fund.java#L10-L46)继承[AggregateRoot](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/AggregateRoot.java#L8-L11)

#### 3.1.2 仓储模式（Repository Pattern）
- **领域层定义接口**: [StockRepository](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/repository/StockRepository.java#L8-L24)、[FundRepository](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/repository/FundRepository.java#L8-L24)
- **基础设施层实现**: [StockRepositoryImpl](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/repository/StockRepositoryImpl.java#L12-L100)、[FundRepositoryImpl](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/repository/FundRepositoryImpl.java#L9-L102)

#### 3.1.3 工厂模式（Factory Pattern）
- 实体创建逻辑封装在应用服务中

### 3.2 Spring框架模式

#### 3.2.1 依赖注入（Dependency Injection）
- 通过[@Autowired](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/test/DatabaseConnectionTest.java#L19-L44)注解实现依赖注入
- 降低组件间耦合度

#### 3.2.2 控制反转（Inversion of Control）
- Spring容器管理对象生命周期
- 配置驱动的对象创建

### 3.3 数据访问模式

#### 3.3.1 活动记录模式（Active Record Pattern）
- 使用MyBatis-Plus的[BaseMapper](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/mapper/StockBasicMapper.java#L8-L21)提供CRUD操作

#### 3.3.2 数据传输对象模式（DTO Pattern）
- [ApiResponse](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/interfaces/dto/response/ApiResponse.java#L8-L55)封装统一响应格式

## 4. 技术架构

### 4.1 技术栈选择

| 技术类别 | 技术选型 | 选择理由 |
|---------|---------|----------|
| 框架 | Spring Boot 3.2.0 | 快速开发、自动配置、生态丰富 |
| ORM | MyBatis-Plus 3.5.5 | SQL控制灵活、性能优秀、功能丰富 |
| 数据库 | PostgreSQL | ACID特性、扩展性好、开源免费 |
| 开发工具 | Lombok | 减少样板代码、提高开发效率 |
| 消息队列 | Spring Kafka | 解耦系统组件、异步处理 |
| 缓存 | Redis | 高性能缓存、会话管理 |

### 4.2 数据库设计

#### 4.2.1 表结构设计
- **stock_basic**: 股票基本信息表
- **fund_basic**: 基金基本信息表
- **stock_quote**: 股票行情表
- **fund_quote**: 基金净值表

#### 4.2.2 关系设计
- 股票与行情：一对多关系
- 基金与净值：一对多关系

### 4.3 配置管理

#### 4.3.1 外部化配置
- application.yml: 主配置文件
- 环境变量: 敏感信息配置
- 配置类: Java配置

#### 4.3.2 配置层次
- 默认配置
- 环境配置
- 用户配置

## 5. 业务流程

### 5.1 数据采集流程
```
外部数据源 → 数据采集服务 → 数据验证 → 数据处理 → 数据存储 → API接口
```

### 5.2 数据处理流程
```
接收数据 → 验证数据 → 转换格式 → 业务逻辑处理 → 持久化 → 发送通知
```

### 5.3 API调用流程
```
HTTP请求 → Controller → Application Service → Domain Service → Repository → Response
```

## 6. 安全设计

### 6.1 认证授权
- JWT Token认证
- Spring Security框架

### 6.2 数据安全
- 敏感信息加密
- SQL注入防护
- XSS防护

## 7. 性能优化

### 7.1 数据库优化
- 索引设计
- 查询优化
- 分页处理

### 7.2 缓存策略
- Redis缓存高频数据
- 本地缓存减少数据库压力

### 7.3 异步处理
- Kafka消息队列
- 定时任务调度

## 8. 监控和运维

### 8.1 应用监控
- Spring Boot Actuator
- 自定义健康检查
- 指标收集

### 8.2 日志管理
- SLF4J日志框架
- 结构化日志
- 日志级别管理

## 9. 部署架构

### 9.1 微服务部署
- 容器化部署（Docker）
- Kubernetes编排
- 服务发现和负载均衡

### 9.2 数据库部署
- PostgreSQL集群
- 读写分离
- 数据备份策略

## 10. 测试策略

### 10.1 单元测试
- JUnit 5框架
- Mockito模拟依赖
- 覆盖率要求80%+

### 10.2 集成测试
- 嵌入式数据库
- API接口测试
- 端到端测试

## 11. 项目特点

### 11.1 架构特点
- 高内聚低耦合
- 关注点分离
- 可测试性良好
- 可扩展性强

### 11.2 代码特点
- 使用Lombok减少样板代码
- 统一的异常处理
- 规范的命名约定
- 详细的文档注释

### 11.3 运维特点
- 配置外置化
- 监控完备
- 日志结构化
- 部署自动化