# 股票/基金数据采集系统后端项目目录结构说明

## 项目概述
本项目采用DDD（领域驱动设计）架构，使用Spring Boot + MyBatis-Plus + PostgreSQL技术栈，实现股票和基金数据的采集、存储和管理功能。

## 目录结构总览

```
backend/
├── pom.xml                           # Maven项目配置文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── stock/
│   │   │           └── fund/         # 核心包
│   │   │               ├── Application.java  # Spring Boot启动类
│   │   │               ├── domain/           # 领域层
│   │   │               │   ├── entity/       # 领域实体
│   │   │               │   └── repository/   # 领域仓储接口
│   │   │               ├── infrastructure/   # 基础设施层
│   │   │               │   ├── entity/       # 持久化对象
│   │   │               │   ├── mapper/       # MyBatis-Plus映射接口
│   │   │               │   └── repository/   # 仓储实现
│   │   │               ├── application/      # 应用层
│   │   │               │   ├── service/      # 应用服务接口
│   │   │               │   ├── command/      # 命令对象
│   │   │               │   └── query/        # 查询对象
│   │   │               └── interfaces/       # 接口层
│   │   │                   ├── controller/   # 控制器
│   │   │                   └── dto/          # 数据传输对象
│   │   │               └── config/           # 配置类
│   │   │               └── test/             # 测试类
│   │   │               └── infrastructure/
│   │   │                   └── test/         # 基础设施测试类
│   │   └── resources/
│   │       ├── application.yml               # 应用配置文件
│   │       └── mapper/                       # MyBatis XML映射文件
│   │           ├── StockBasicMapper.xml
│   │           ├── FundBasicMapper.xml
│   │           ├── StockQuoteMapper.xml
│   │           └── FundQuoteMapper.xml
│   └── test/
│       └── java/
│           └── com/
│           └── stock/
│           └── fund/
│               └── domain/
│                   └── entity/               # 实体测试类
└── target/                                   # Maven编译输出目录
```

## 详细模块说明

### 1. domain（领域层）

#### 1.1 entity（领域实体）
- **AggregateRoot.java** - 聚合根基类，定义领域实体公共属性
- **Stock.java** - 股票领域实体，包含股票的基本信息
- **Fund.java** - 基金领域实体，包含基金的基本信息
- **StockQuote.java** - 股票行情实体，包含股票实时行情数据
- **FundQuote.java** - 基金净值实体，包含基金实时净值数据

#### 1.2 repository（领域仓储接口）
- **StockRepository.java** - 股票仓储接口，定义股票数据访问契约
- **FundRepository.java** - 基金仓储接口，定义基金数据访问契约
- **StockQuoteRepository.java** - 股票行情仓储接口，定义股票行情数据访问契约
- **FundQuoteRepository.java** - 基金净值仓储接口，定义基金净值数据访问契约

### 2. infrastructure（基础设施层）

#### 2.1 entity（持久化对象）
- **StockBasicPO.java** - 股票基础持久化对象，对应数据库stock_basic表
- **FundBasicPO.java** - 基金基础持久化对象，对应数据库fund_basic表
- **StockQuotePO.java** - 股票行情持久化对象，对应数据库stock_quote表
- **FundQuotePO.java** - 基金净值持久化对象，对应数据库fund_quote表

#### 2.2 mapper（数据访问映射接口）
- **StockBasicMapper.java** - 股票基础数据访问接口，继承BaseMapper
- **FundBasicMapper.java** - 基金基础数据访问接口，继承BaseMapper
- **StockQuoteMapper.java** - 股票行情数据访问接口，继承BaseMapper
- **FundQuoteMapper.java** - 基金净值数据访问接口，继承BaseMapper

#### 2.3 repository（仓储实现）
- **StockRepositoryImpl.java** - 股票仓储实现，实现StockRepository接口
- **FundRepositoryImpl.java** - 基金仓储实现，实现FundRepository接口
- **StockQuoteRepositoryImpl.java** - 股票行情仓储实现，实现StockQuoteRepository接口
- **FundQuoteRepositoryImpl.java** - 基金净值仓储实现，实现FundQuoteRepository接口

### 3. application（应用层）

#### 3.1 service（应用服务接口）
- **DataCollectionAppService.java** - 数据采集应用服务接口，定义数据采集契约
- **DataProcessingAppService.java** - 数据处理应用服务接口，定义数据处理契约

#### 3.2 service/impl（应用服务实现）
- **DataCollectionAppServiceImpl.java** - 数据采集应用服务实现
- **DataProcessingAppServiceImpl.java** - 数据处理应用服务实现

#### 3.3 调度服务
- **DataCollectionScheduler.java** - 数据采集调度器，负责定时数据采集任务

### 4. interfaces（接口层）

#### 4.1 controller（控制器）
- **DataCollectionController.java** - 数据采集控制器，提供REST API接口

#### 4.2 dto（数据传输对象）
- **request/CollectStockRequest.java** - 股票采集请求对象
- **response/ApiResponse.java** - 统一响应对象

### 5. config（配置类）
- **MybatisPlusConfig.java** - MyBatis-Plus配置类
- **DataSourceConfig.java** - 数据源配置类

### 6. test（测试类）
- **EntityLombokTest.java** - 实体类Lombok功能测试
- **DDAArchitectureTest.java** - DDD架构验证测试
- **DatabaseConnectionTest.java** - 数据库连接测试

### 7. resources（资源配置）

#### 7.1 application.yml
应用主配置文件，包含：
- 数据库连接配置
- Redis连接配置
- 服务器配置
- 数据采集配置
- 日志配置

#### 7.2 mapper/（XML映射文件）
- **StockBasicMapper.xml** - 股票基础数据SQL映射
- **FundBasicMapper.xml** - 基金基础数据SQL映射
- **StockQuoteMapper.xml** - 股票行情数据SQL映射
- **FundQuoteMapper.xml** - 基金净值数据SQL映射

## 架构设计原则

### 1. DDD分层架构
- **领域层**: 包含业务逻辑和领域模型，不依赖其他层
- **应用层**: 协调领域对象，处理应用逻辑
- **基础设施层**: 提供技术支撑，如数据访问、消息队列等
- **接口层**: 处理外部请求，提供API接口

### 2. 依赖倒置原则
- 上层不直接依赖下层，通过接口进行依赖
- 领域层定义仓储接口，基础设施层实现接口

### 3. 领域驱动设计
- 领域实体包含业务逻辑
- 聚合根维护聚合内部一致性
- 值对象表示无标识的业务概念

## 技术栈说明

### 1. 核心框架
- **Spring Boot 3.2.0**: 提供快速开发框架
- **MyBatis-Plus 3.5.5**: ORM框架，简化数据库操作
- **PostgreSQL**: 关系型数据库

### 2. 工具库
- **Lombok 1.18.30**: 简化实体类代码
- **Jackson**: JSON处理
- **SLF4J**: 日志框架

### 3. 开发规范
- 使用Lombok [@Data](file:///d:/workspace/Bgx/backend/src/test/java/com/stock/fund/domain/entity/EntityLombokTest.java#L5-L86)注解，避免手写getter/setter
- 遵循DDD分层架构原则
- 统一异常处理和响应格式

## 业务功能模块

### 1. 数据采集模块
- 支持股票基本信息采集
- 支持基金基本信息采集
- 支持股票实时行情采集
- 支持基金实时净值采集

### 2. 数据处理模块
- 数据验证和清洗
- 数据存储和更新
- 数据一致性维护

### 3. 调度模块
- 定时任务调度
- 交易时间适配
- 数据采集策略管理

### 4. API接口模块
- RESTful API设计
- 统一响应格式
- 参数校验和异常处理