# 配置文件说明文档

## 1. application.yml 配置详解

### 1.1 应用配置
```yaml
spring:
  application:
    name: stock-fund-data-collector  # 应用名称
```
- **作用**: 定义应用的基本信息
- **用途**: 服务注册与发现、监控系统识别

### 1.2 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stock_fund_db  # 数据库连接URL
    username: postgres                                  # 数据库用户名
    password: 168168                                    # 数据库密码
    driver-class-name: org.postgresql.Driver            # JDBC驱动类
```
- **作用**: 配置数据库连接信息
- **注意事项**:
  - 确保PostgreSQL服务已启动
  - 数据库stock_fund_db已创建
  - 用户postgres具有相应权限

### 1.3 MyBatis-Plus配置
```yaml
spring:
  mybatis-plus:
    configuration:
      map-underscore-to-camel-case: true    # 下划线转驼峰命名
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL日志输出
    global-config:
      db-config:
        logic-delete-field: deleted        # 逻辑删除字段名
        logic-delete-value: 1              # 逻辑删除值
        logic-not-delete-value: 0          # 未删除值
```
- **作用**: 配置MyBatis-Plus框架行为
- **功能**:
  - 自动映射数据库字段到实体属性
  - 输出SQL语句便于调试
  - 逻辑删除支持

### 1.4 Redis配置
```yaml
spring:
  redis:
    host: localhost          # Redis主机地址
    port: 32768              # Redis端口号
    password: 168168         # Redis密码
    database: 0              # 数据库索引
```
- **作用**: 配置Redis连接信息
- **用途**: 缓存、会话管理、分布式锁

### 1.5 服务器配置
```yaml
server:
  port: 8080                    # 应用端口
  servlet:
    context-path: /api          # 应用上下文路径
```
- **作用**: 配置Web服务器参数
- **说明**: API访问路径为 http://localhost:8080/api/

### 1.6 数据采集配置
```yaml
data:
  collection:
    api:
      tushare-token: ${TUSHARE_TOKEN:your_tushare_token}  # Tushare API令牌
      sina-enabled: true                                  # 新浪财经API启用
      eastmoney-enabled: true                            # 东方财富API启用
      stocktv-enabled: false                             # StockTV API启用
    schedule:
      stock-basic-cron: "0 0 2 * * ?"                    # 股票基础数据采集定时
      fund-basic-cron: "0 0 3 * * ?"                     # 基金基础数据采集定时
      stock-quote-cron: "0 */1 9-15 * * MON-FRI"        # 股票行情数据采集定时
      fund-quote-cron: "0 */15 9-15 * * MON-FRI"        # 基金净值数据采集定时
      daily-collection-cron: "0 0 16 * * MON-FRI"       # 每日数据采集定时
    retry:
      max-attempts: 3                                     # 最大重试次数
      delay-ms: 1000                                      # 重试延迟(毫秒)
      max-delay-ms: 10000                                 # 最大延迟(毫秒)
      multiplier: 1.5                                     # 延迟倍数
    validation:
      enabled: true                                       # 验证启用
      strict-mode: false                                  # 严格模式
      max-pe-ratio: 100.0                                 # 最大市盈率
      max-pb-ratio: 10.0                                  # 最大市净率
      max-price-change-percent: 20.0                      # 最大价格变动百分比
```
- **作用**: 配置数据采集相关参数
- **说明**:
  - cron表达式定义定时任务执行时间
  - 重试机制确保数据采集可靠性
  - 验证机制保证数据质量

### 1.7 日志配置
```yaml
logging:
  level:
    root: info                                          # 根日志级别
    com.stock.fund: debug                               # 应用包日志级别
    org.springframework.web: debug                      # Spring Web日志级别
    org.springframework.security: debug                 # Spring Security日志级别
```
- **作用**: 配置日志输出级别
- **说明**: 便于调试和监控应用运行状态

## 2. Mapper XML 配置文件

### 2.1 StockBasicMapper.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.stock.fund.infrastructure.mapper.StockBasicMapper">

    <select id="findBySymbol" parameterType="string" resultType="com.stock.fund.infrastructure.entity.StockBasicPO">
        SELECT * FROM stock_basic WHERE symbol = #{symbol}
    </select>

    <select id="findByIndustry" parameterType="string" resultType="com.stock.fund.infrastructure.entity.StockBasicPO">
        SELECT * FROM stock_basic WHERE industry = #{industry}
    </select>

    <select id="findByMarket" parameterType="string" resultType="com.stock.fund.infrastructure.entity.StockBasicPO">
        SELECT * FROM stock_basic WHERE market = #{market}
    </select>

</mapper>
```
- **作用**: 定义股票基础数据的自定义查询SQL
- **功能**:
  - 根据股票代码查询
  - 根据行业查询
  - 根据市场查询

### 2.2 FundBasicMapper.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.stock.fund.infrastructure.mapper.FundBasicMapper">

    <select id="findByFundCode" parameterType="string" resultType="com.stock.fund.infrastructure.entity.FundBasicPO">
        SELECT * FROM fund_basic WHERE fund_code = #{fundCode}
    </select>

    <select id="findByType" parameterType="string" resultType="com.stock.fund.infrastructure.entity.FundBasicPO">
        SELECT * FROM fund_basic WHERE type = #{type}
    </select>

    <select id="findByManager" parameterType="string" resultType="com.stock.fund.infrastructure.entity.FundBasicPO">
        SELECT * FROM fund_basic WHERE manager = #{manager}
    </select>

</mapper>
```
- **作用**: 定义基金基础数据的自定义查询SQL
- **功能**:
  - 根据基金代码查询
  - 根据基金类型查询
  - 根据基金经理查询

### 2.3 StockQuoteMapper.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.stock.fund.infrastructure.mapper.StockQuoteMapper">

    <select id="findByStockId" parameterType="long" resultType="com.stock.fund.infrastructure.entity.StockQuotePO">
        SELECT * FROM stock_quote WHERE stock_id = #{stockId} ORDER BY timestamp DESC
    </select>

    <select id="findByStockIdAndTimestampBetween" resultType="com.stock.fund.infrastructure.entity.StockQuotePO">
        SELECT * FROM stock_quote 
        WHERE stock_id = #{stockId} 
        AND timestamp BETWEEN #{start} AND #{end}
        ORDER BY timestamp DESC
    </select>

</mapper>
```
- **作用**: 定义股票行情数据的自定义查询SQL
- **功能**:
  - 根据股票ID查询行情
  - 根据股票ID和时间范围查询行情

### 2.4 FundQuoteMapper.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.stock.fund.infrastructure.mapper.FundQuoteMapper">

    <select id="findByFundId" parameterType="long" resultType="com.stock.fund.infrastructure.entity.FundQuotePO">
        SELECT * FROM fund_quote WHERE fund_id = #{fundId} ORDER BY timestamp DESC
    </select>

    <select id="findByFundIdAndTimestampBetween" resultType="com.stock.fund.infrastructure.entity.FundQuotePO">
        SELECT * FROM fund_quote 
        WHERE fund_id = #{fundId} 
        AND timestamp BETWEEN #{start} AND #{end}
        ORDER BY timestamp DESC
    </select>

</mapper>
```
- **作用**: 定义基金净值数据的自定义查询SQL
- **功能**:
  - 根据基金ID查询净值
  - 根据基金ID和时间范围查询净值

## 3. 环境变量配置

### 3.1 环境变量使用
```yaml
data:
  collection:
    api:
      tushare-token: ${TUSHARE_TOKEN:your_tushare_token}  # 使用环境变量TUSHARE_TOKEN，若未设置则使用默认值
```
- **作用**: 支持通过环境变量配置敏感信息
- **用途**: 生产环境中避免硬编码敏感配置

### 3.2 环境配置文件
- **application-dev.yml**: 开发环境配置
- **application-test.yml**: 测试环境配置
- **application-prod.yml**: 生产环境配置

## 4. 配置生效顺序

配置属性的生效顺序（从高到低）：
1. 命令行参数
2. 环境变量
3. application-{profile}.yml
4. application.yml
5. 默认值

## 5. 配置验证

### 5.1 配置类验证
```java
@Configuration
@Validated
public class AppConfig {
    
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @AssertTrue(message = "Database URL must be configured")
    public boolean isDbUrlConfigured() {
        return dbUrl != null && !dbUrl.isEmpty();
    }
}
```

### 5.2 配置加载验证
- 应用启动时会验证所有必需配置项
- 配置错误会导致应用启动失败
- 日志中会输出配置加载信息

## 6. 配置管理最佳实践

### 6.1 敏感信息处理
- 数据库密码等敏感信息应使用环境变量
- 避免在配置文件中硬编码敏感信息
- 使用加密配置属性

### 6.2 配置文件组织
- 按环境分离配置文件
- 使用profile激活特定配置
- 保持配置文件结构清晰

### 6.3 配置变更管理
- 配置变更需要经过测试验证
- 生产环境配置变更需要审批
- 保留配置变更历史记录