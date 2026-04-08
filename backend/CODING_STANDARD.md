# 股票/基金数据采集系统编码规范

## 1. 项目概述

本规范定义了股票/基金数据采集系统的编码标准和最佳实践，确保代码质量和一致性。

## 2. 项目结构规范

### 2.1 包结构
```
src/main/java/com/stock/fund/
├── domain/                    # 领域层
│   ├── entity/               # 实体类
│   └── repository/           # 仓储接口
├── application/              # 应用层
│   ├── scheduler/            # 调度器模块
│   │   ├── SchedulerConfig.java       # 定时任务配置类
│   │   ├── XxxScheduler.java          # 具体调度器类
│   │   └── SCHEDULER_GUIDE.md         # 调度器使用指南
│   ├── service/              # 应用服务接口和实现
│   └── dto/                  # 数据传输对象
├── infrastructure/           # 基础设施层
│   ├── entity/              # 持久化对象(PO)
│   ├── mapper/              # 数据访问对象(Mapper)
│   └── repository/          # 仓储实现
└── interfaces/               # 接口层
    ├── controller/          # 控制器
    └── dto/                 # 数据传输对象
```

### 2.2 领域驱动设计(DDD)分层规范
- **Domain Layer (领域层)**: 包含业务实体、值对象、聚合根、领域服务和仓储接口
- **Application Layer (应用层)**: 包含应用服务、DTOs和应用配置
- **Infrastructure Layer (基础设施层)**: 包含数据访问实现、外部服务适配器、消息队列等
- **Interface Layer (接口层)**: 包含API控制器、Web页面、消息监听器等

## 3. 命名规范

### 3.1 类命名
- 实体类: `CamelCase` + `Entity` 后缀 (如 `Stock`, `FundQuote`)
- 持久化对象: `CamelCase` + `PO` 后缀 (如 `StockPO`, `FundQuotePO`)
- 仓储接口: `CamelCase` + `Repository` 后缀 (如 `StockRepository`)
- 仓储实现: `CamelCase` + `RepositoryImpl` 后缀 (如 `StockRepositoryImpl`)
- 应用服务: `CamelCase` + `AppService` 后缀 (如 `DataCollectionAppService`)
- 控制器: `CamelCase` + `Controller` 后缀 (如 `DataCollectionController`)

### 3.2 方法命名
- Getter/Setter: `getXxx()`/`setXxx()`
- 业务方法: 动词开头，描述具体行为 (如 `collectStockQuotes`, `processFundBasics`)
- 查询方法: `findByXxx`, `getByXxx`, `listXxx`
- 布尔方法: `isXxx`, `hasXxx`, `canXxx`

### 3.3 变量命名
- 局部变量: 小写字母开头的驼峰命名 (如 `stockCode`, `fundList`)
- 常量: 全大写字母，单词间用下划线分隔 (如 `DEFAULT_PAGE_SIZE`, `MAX_RETRY_COUNT`)

## 4. 实体类编码与映射规范

### 4.1 注解使用
- 所有领域层和基础设施层实体类必须使用Lombok `@Data` 注解
- 禁止手写 getter/setter 方法
- 使用 `@EqualsAndHashCode(callSuper = true)` 继承父类的 equals/hashCode

### 4.2 字段命名
- 实体类字段名必须与 PostgreSQL 数据库表列名严格一致，大小写敏感
- 禁止依赖 MyBatis/JPA 等框架的驼峰转下划线等隐式映射机制
- PO类中的字段名必须与数据库表列名完全一致

### 4.3 示例
``java
// 正确示例
@Data
@EqualsAndHashCode(callSuper = true)
public class StockQuotePO {
    private Long id;
    private LocalDateTime quote_time;  // 与数据库列名一致
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
    private Double amount;
    private Double change;
    private Double change_percent;  // 与数据库列名一致
    private LocalDateTime created_at;
}

// 错误示例
public class StockQuotePO {
    private Long id;
    private LocalDateTime quoteTime;  // 与数据库列名不一致
    
    // 禁止手写getter/setter
    public LocalDateTime getQuoteTime() {
        return quoteTime;
    }
    
    public void setQuoteTime(LocalDateTime quoteTime) {
        this.quoteTime = quoteTime;
    }
}
```

## 5. 导入语句规范

### 5.1 遵循原则
- **禁止使用全限定类名**: 如 `List<com.stock.fund.domain.entity.Stock>` 
- **必须使用import语句**: 将需要的类导入后再使用简短名称
- **避免wildcard imports**: 不使用 `import java.util.*` 形式

### 5.2 导入顺序
1. 标准库导入 (java.*)
2. 第三方库导入 (org.*, com.*)
3. 项目内导入 (com.stock.fund.*)

### 5.3 示例
```
// 正确示例
package com.stock.fund.application.service;

import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.StockQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataCollectionService {
    public List<Stock> getStockList() { ... }
    public List<StockQuote> getStockQuotes() { ... }
}

// 错误示例
package com.stock.fund.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataCollectionService {
    public List<com.stock.fund.domain.entity.Stock> getStockList() { ... }
    public List<com.stock.fund.domain.entity.StockQuote> getStockQuotes() { ... }
}
```

## 6. 仓储层规范

### 6.1 接口设计
- 仓储接口命名: `XxxRepository`
- 仓储实现命名: `XxxRepositoryImpl`
- 实现类使用 `@Repository` 注解
- 使用泛型定义实体类型

### 6.2 方法命名
- 查询单个: `findById`, `findByCode`
- 查询多个: `findByXxx`, `findAll`
- 保存: `save`, `saveAll`
- 删除: `deleteById`, `deleteByXxx`

## 7. 应用服务层规范

### 7.1 接口设计
- 应用服务接口命名: `XxxAppService`
- 应用服务实现命名: `XxxAppServiceImpl`
- 实现类使用 `@Service` 注解
- 方法名体现业务意图

### 7.2 事务管理
- 使用 `@Transactional` 注解管理事务
- 在类级别或方法级别声明事务

## 8. 控制器层规范

### 8.1 注解使用
- 使用 `@RestController` 注解
- 使用 `@RequestMapping` 定义路径前缀
- 使用 `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` 等

### 8.2 响应处理
- 使用统一的响应格式 `ApiResponse<T>`
- 正确处理异常并返回合适的HTTP状态码

## 9. 日志规范

### 9.1 日志级别
- `ERROR`: 错误日志，记录异常信息
- `WARN`: 警告日志，记录潜在问题
- `INFO`: 信息日志，记录关键业务流程
- `DEBUG`: 调试日志，记录详细过程信息

### 9.2 日志格式
- 使用SLF4J日志框架
- 记录有意义的信息，避免记录敏感数据
- 使用参数化日志消息

## 10. 异常处理规范

### 10.1 自定义异常
- 创建业务特定的异常类
- 异常类名以 `Exception` 结尾
- 提供有意义的错误信息

### 10.2 异常传播
- 适当处理异常，避免异常向上传播
- 在控制器层统一处理异常

## 11. 测试规范

### 11.1 单元测试
- 测试类命名: `XxxTest`
- 使用JUnit 5进行单元测试
- 测试覆盖率目标: 80%以上

### 11.2 集成测试
- 使用 `@SpringBootTest` 进行集成测试
- 测试业务逻辑的正确性

## 12. 数据库设计规范

### 12.1 表命名
- 使用小写字母和下划线分隔
- 表名使用复数形式 (如 `stocks`, `fund_quotes`)

### 12.2 字段命名
- 使用小写字母和下划线分隔
- 字段名与实体类字段保持一致

## 13. 工具类使用规范

### 13.1 避免重复造轮子
- 优先使用 Hutool 等成熟工具库
- 禁止重复创建 Hutool 已提供的通用工具类
- 只保留业务特有工具类

### 13.2 标准库选择
- HTTP请求: 使用 OkHttp3
- 日期处理: 使用 Hutool DateUtil
- JSON处理: 使用 Hutool JSONUtil
- 字符串处理: 使用 Hutool StrUtil
- 集合处理: 使用 Hutool CollUtil
- 加密解密: 使用 Hutool SecureUtil

## 14. 编码实践

### 14.1 代码可读性
- 方法长度不超过50行
- 类长度不超过500行
- 适当添加注释说明业务逻辑

### 14.2性能考虑
-在循环中执行数据库查询
- 使用批量操作处理大量数据
-合使用缓存
- **批量操作要求**：
  -⚠禁止在循环中执行单条数据库操作
  -⚠️必须使用批量操作处理10条以上数据
  -⚠️必须在批量操作上添加事务注解
  -⚠️必须进行数据预处理和验证

### 14.3安全性
-验证输入参数
-防SQL注入
- 保护敏感信息

## 15. 代码审查清单

- [ ] 遵循命名规范
- [ ] 使用了正确的注解
- [ ] 实体类使用了@Data注解
- [ ] 没有使用全限定类名
- [ ] 字段名与数据库列名一致
- [ ] 代码风格一致
- [ ] 添加了必要的注释
- [ ] 遵循了DDD分层架构
- [ ] 异常处理得当
- [ ] 日志记录恰当


