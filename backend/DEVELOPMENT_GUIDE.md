# 股票/基金数据采集系统开发指南

## 1. 环境准备

### 1.1 开发环境要求
- **JDK**: 21或更高版本
- **Maven**: 3.8或更高版本
- **IDE**: IntelliJ IDEA 或 Eclipse
- **数据库**: PostgreSQL 12或更高版本
- **Redis**: 6或更高版本

### 1.2 依赖安装
```bash
# 确保JDK 21已安装
java -version

# 确保Maven已安装
mvn -version

# PostgreSQL安装（如果未安装）
# Windows: 通过官网下载安装包
# Linux: sudo apt-get install postgresql postgresql-contrib

# Redis安装（如果未安装）
# Windows: 通过Chocolatey安装
# Linux: sudo apt-get install redis-server
```

### 1.3 数据库配置
1. 启动PostgreSQL服务
2. 创建数据库:
```sql
CREATE DATABASE stock_fund_db;
CREATE USER postgres WITH PASSWORD '168168';
GRANT ALL PRIVILEGES ON DATABASE stock_fund_db TO postgres;
```

### 1.4 Redis配置
1. 启动Redis服务
2. 确保Redis监听端口32768（根据配置文件）
3. 设置密码为'168168'

## 2. 项目结构

### 2.1 包结构说明
```
com.stock.fund/
├── Application.java                    # Spring Boot启动类
├── domain/                            # 领域层
│   ├── entity/                        # 领域实体
│   └── repository/                    # 领域仓储接口
├── infrastructure/                    # 基础设施层
│   ├── entity/                        # 持久化对象
│   ├── mapper/                        # MyBatis映射接口
│   └── repository/                    # 仓储实现
├── application/                       # 应用层
│   ├── scheduler/                     # 调度器模块
│   │   ├── SchedulerConfig.java       # 定时任务配置
│   │   ├── DataCollectionScheduler.java # 数据采集调度器
│   │   └── AlertScheduler.java        # 提醒检查调度器
│   └── service/                       # 应用服务
├── interfaces/                        # 接口层
│   ├── controller/                    # 控制器
│   └── dto/                          # 数据传输对象
└── config/                           # 配置类
```

### 2.2 文件结构
- **src/main/java**: 源代码
- **src/main/resources**: 配置文件
- **src/test/java**: 测试代码
- **pom.xml**: 项目依赖配置

## 3. 核心开发指南

### 3.1 领域实体开发

#### 3.1.1 创建领域实体
```java
package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewEntity extends AggregateRoot<Long> {
    private String field1;
    private Integer field2;
    // 更多字段...
}
```

#### 3.1.2 领域实体规范
- 继承[AggregateRoot](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/AggregateRoot.java#L8-L11)基类
- 使用[@Data](file:///d:/workspace/Bgx/backend/src/test/java/com/stock/fund/domain/entity/EntityLombokTest.java#L5-L86)注解自动生成getter/setter
- 使用[@EqualsAndHashCode](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/domain/entity/Stock.java#L10-L43)注解处理相等性比较
- 不要添加业务逻辑方法到实体类

### 3.2 仓储接口开发

#### 3.2.1 定义仓储接口
```java
package com.stock.fund.domain.repository;

import com.stock.fund.domain.entity.NewEntity;
import java.util.List;
import java.util.Optional;

public interface NewEntityRepository {
    Optional<NewEntity> findById(Long id);
    List<NewEntity> findAll();
    NewEntity save(NewEntity entity);
    void deleteById(Long id);
    // 业务特定查询方法...
}
```

#### 3.2.2 仓储接口规范
- 只定义方法签名，不包含实现
- 使用泛型类型安全
- 返回Optional处理空值情况
- 遵循CRUD操作命名规范

### 3.3 持久化对象开发

#### 3.3.1 创建PO类
```java
package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("new_entity_table")
@Data
public class NewEntityPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String field1;
    private Integer field2;
    private LocalDateTime createdAt;
    // 更多字段...
}
```

#### 3.3.2 PO类规范
- 使用[@TableName](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/mapper/StockBasicMapper.java#L8-L21)注解映射表名
- 使用[@TableId](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/mapper/StockBasicMapper.java#L8-L21)注解标识主键
- 使用[@Data](file:///d:/workspace/Bgx/backend/src/test/java/com/stock/fund/domain/entity/EntityLombokTest.java#L5-L86)注解生成方法
- 字段类型与数据库表结构对应

### 3.4 Mapper接口开发

#### 3.4.1 创建Mapper接口
```java
package com.stock.fund.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.NewEntityPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewEntityMapper extends BaseMapper<NewEntityPO> {
    NewEntityPO findByField1(@Param("field1") String field1);
    List<NewEntityPO> findByField2(@Param("field2") Integer field2);
}
```

#### 3.4.2 Mapper接口规范
- 继承BaseMapper获得CRUD方法
- 使用[@Param](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/mapper/StockBasicMapper.java#L8-L21)注解绑定参数
- 使用[@Repository](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/mapper/StockBasicMapper.java#L8-L21)注解标记为Spring组件
- 自定义查询方法使用MyBatis注解或XML配置

### 3.5 仓储实现开发

#### 3.5.1 创建仓储实现
```java
package com.stock.fund.infrastructure.repository;

import com.stock.fund.domain.entity.NewEntity;
import com.stock.fund.domain.repository.NewEntityRepository;
import com.stock.fund.infrastructure.entity.NewEntityPO;
import com.stock.fund.infrastructure.mapper.NewEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class NewEntityRepositoryImpl implements NewEntityRepository {

    @Autowired
    private NewEntityMapper newEntityMapper;

    @Override
    public Optional<NewEntity> findById(Long id) {
        NewEntityPO po = newEntityMapper.selectById(id);
        return po != null ? Optional.of(mapToDomainEntity(po)) : Optional.empty();
    }

    @Override
    public List<NewEntity> findAll() {
        List<NewEntityPO> pos = newEntityMapper.selectList(null);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public NewEntity save(NewEntity entity) {
        NewEntityPO po = mapToPO(entity);
        if (entity.getId() == null) {
            newEntityMapper.insert(po);
            entity.setId(po.getId());
        } else {
            newEntityMapper.updateById(po);
        }
        return entity;
    }

    @Override
    public void deleteById(Long id) {
        newEntityMapper.deleteById(id);
    }

    // 私有方法：PO与领域实体转换
    private NewEntity mapToDomainEntity(NewEntityPO po) {
        // 转换逻辑
    }

    private NewEntityPO mapToPO(NewEntity entity) {
        // 转换逻辑
    }
}
```

#### 3.5.2 仓储实现规范
- 实现领域层定义的接口
- 使用[@Repository](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/mapper/StockBasicMapper.java#L8-L21)注解标记为Spring组件
- 使用[@Autowired](file:///d:/workspace/Bgx/backend/src/main/java/com/stock/fund/infrastructure/test/DatabaseConnectionTest.java#L19-L44)注入Mapper依赖
- 处理PO与领域实体的相互转换

### 3.6 应用服务开发

#### 3.6.1 创建应用服务接口
```java
package com.stock.fund.application.service;

import com.stock.fund.domain.entity.NewEntity;
import java.util.List;

public interface NewEntityAppService {
    List<NewEntity> getAllEntities();
    NewEntity getEntityById(Long id);
    NewEntity createEntity(NewEntity entity);
    NewEntity updateEntity(Long id, NewEntity entity);
    void deleteEntity(Long id);
}
```

#### 3.6.2 创建应用服务实现
```java
package com.stock.fund.application.service.impl;

import com.stock.fund.application.service.NewEntityAppService;
import com.stock.fund.domain.entity.NewEntity;
import com.stock.fund.domain.repository.NewEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NewEntityAppServiceImpl implements NewEntityAppService {

    @Autowired
    private NewEntityRepository newEntityRepository;

    @Override
    public List<NewEntity> getAllEntities() {
        return newEntityRepository.findAll();
    }

    @Override
    public NewEntity getEntityById(Long id) {
        return newEntityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found"));
    }

    @Override
    public NewEntity createEntity(NewEntity entity) {
        return newEntityRepository.save(entity);
    }

    @Override
    public NewEntity updateEntity(Long id, NewEntity entity) {
        entity.setId(id);
        return newEntityRepository.save(entity);
    }

    @Override
    public void deleteEntity(Long id) {
        newEntityRepository.deleteById(id);
    }
}
```

### 3.7 控制器开发

#### 3.7.1 创建控制器
```java
package com.stock.fund.interfaces.controller;

import com.stock.fund.application.service.NewEntityAppService;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/new-entities")
public class NewEntityController {
    
    @Autowired
    private NewEntityAppService newEntityAppService;

    @GetMapping
    public ApiResponse<List<NewEntity>> getAllEntities() {
        List<NewEntity> entities = newEntityAppService.getAllEntities();
        return ApiResponse.success(entities);
    }

    @GetMapping("/{id}")
    public ApiResponse<NewEntity> getEntity(@PathVariable Long id) {
        NewEntity entity = newEntityAppService.getEntityById(id);
        return ApiResponse.success(entity);
    }

    @PostMapping
    public ApiResponse<NewEntity> createEntity(@RequestBody NewEntity entity) {
        NewEntity savedEntity = newEntityAppService.createEntity(entity);
        return ApiResponse.success("Entity created successfully", savedEntity);
    }

    @PutMapping("/{id}")
    public ApiResponse<NewEntity> updateEntity(@PathVariable Long id, @RequestBody NewEntity entity) {
        NewEntity updatedEntity = newEntityAppService.updateEntity(id, entity);
        return ApiResponse.success("Entity updated successfully", updatedEntity);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteEntity(@PathVariable Long id) {
        newEntityAppService.deleteEntity(id);
        return ApiResponse.success("Entity deleted successfully");
    }
}
```

## 4. 配置管理

### 4.1 application.yml配置
```yaml
spring:
  application:
    name: stock-fund-data-collector
  
  # 数据库配置
  datasource:
    url: jdbc:postgresql://localhost:5432/stock_fund_db
    username: postgres
    password: 168168
    driver-class-name: org.postgresql.Driver
  
  # MyBatis-Plus配置
  mybatis-plus:
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    global-config:
      db-config:
        logic-delete-field: deleted
        logic-delete-value: 1
        logic-not-delete-value: 0

  # Redis配置
  redis:
    host: localhost
    port: 32768
    password: 168168
    database: 0

# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /api

# 日志配置
logging:
  level:
    root: info
    com.stock.fund: debug
```

### 4.2 配置类开发
```java
package com.stock.fund.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(DbType.POSTGRE_SQL);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}
```

## 5. 测试开发

### 5.1 单元测试
```java
package com.stock.fund.domain.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NewEntityTest {
    
    @Test
    public void testNewEntityCreation() {
        NewEntity entity = new NewEntity();
        entity.setField1("test");
        entity.setField2(123);
        
        assertEquals("test", entity.getField1());
        assertEquals(123, entity.getField2());
    }
}
```

### 5.2 集成测试
```java
package com.stock.fund.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@SpringJUnitConfig
public class NewEntityIntegrationTest {
    
    @Test
    public void testNewEntityFullFlow() {
        // 集成测试逻辑
    }
}
```

## 6. 运行和部署

### 6.1 本地运行
```bash
# 编译项目
cd d:\workspace\Bgx\backend
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

### 6.2 打包部署
```bash
# 打包
mvn clean package

# 运行jar包
java -jar target/data-collector-1.0-SNAPSHOT.jar
```

### 6.3 Docker部署
```dockerfile
FROM openjdk:21-jdk-slim

COPY target/data-collector-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 7. 最佳实践

### 7.1 代码规范
- 使用Lombok减少样板代码
- 遵循DDD分层架构
- 使用有意义的命名
- 添加适当的注释

### 7.2 异常处理
```java
try {
    // 业务逻辑
} catch (SpecificException e) {
    log.error("业务操作失败", e);
    throw new BusinessException("操作失败");
}
```

### 7.3 日志记录
```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

logger.info("操作成功: {}", param);
logger.error("操作失败", exception);
```

### 7.4 事务管理
```java
@Transactional
public void businessMethod() {
    // 事务性操作
}
```

## 8. 常见问题和解决方案

### 8.1 数据库连接问题
- 检查PostgreSQL服务是否启动
- 验证数据库连接参数
- 检查防火墙设置

### 8.2 Redis连接问题
- 检查Redis服务是否启动
- 验证Redis连接参数
- 确认密码配置正确

### 8.3 依赖冲突
- 使用mvn dependency:tree分析依赖树
- 排除冲突的依赖
- 使用统一的版本管理

### 8.4 性能问题
- 优化SQL查询
- 添加适当索引
- 使用缓存减少数据库访问