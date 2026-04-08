# 数据库批量操作优化规范

##概

本规范定义了数据库批量操作的实现标准和最佳实践，旨在提高数据处理效率，减少数据库连接开销，确保事务一致性。

##批操作优化场景

### 1. 数据采集处理场景⭐⭐⭐
**现状问题**: 
- 数据处理服务中大量使用单条记录循环处理
-记录单独执行数据库操作，效率低下
-缺批量操作优化

**优化方案**:
```java
//原代码（单条处理）
@Override
public void processStockBasics(List<Stock> stockBasics) {
    for (Stock stock : stockBasics) {
        try {
            //单条查询
            var existingStockOpt = stockRepository.findBySymbol(stock.getSymbol());
            if (existingStockOpt.isPresent()) {
                //单条更新
                stockRepository.save(existingStock);
            } else {
                //单条插入
                stockRepository.save(stock);
            }
        } catch (Exception e) {
            logger.error("处理股票失败", e);
        }
    }
}

// 优化后代码（批量处理）
@Override
@Transactional
public void processStockBasics(List<Stock> stockBasics) {
    if (stockBasics == null || stockBasics.isEmpty()) {
        return;
    }
    
    // 1. 批量查询已存在的股票
    List<String> symbols = stockBasics.stream()
        .map(Stock::getSymbol)
        .distinct()
        .toList();
    Map<String, Stock> existingStocks = stockRepository.findBySymbols(symbols)
        .stream()
        .collect(Collectors.toMap(Stock::getSymbol, Function.identity()));
    
    // 2. 分类处理
    List<Stock> toInsert = new ArrayList<>();
    List<Stock> toUpdate = new ArrayList<>();
    
    for (Stock stock : stockBasics) {
        if (existingStocks.containsKey(stock.getSymbol())) {
            // 更新现有记录
            Stock existing = existingStocks.get(stock.getSymbol());
            existing.updateFrom(stock);
            toUpdate.add(existing);
        } else {
            // 新增记录
            toInsert.add(stock);
        }
    }
    
    // 3. 批量操作
    if (!toInsert.isEmpty()) {
        stockRepository.saveAll(toInsert);
        logger.info("批量新增股票记录: {}条", toInsert.size());
    }
    
    if (!toUpdate.isEmpty()) {
        stockRepository.updateAll(toUpdate);
        logger.info("批量更新股票记录: {}条", toUpdate.size());
    }
}
```

### 2.行数据处理场景⭐⭐⭐
**现状问题**:
-行数据按条处理，数据库压力大
-缺批量插入优化

**优化方案**:
```java
//原始代码
@Override
public void processStockQuotes(List<StockQuote> stockQuotes) {
    for (StockQuote stockQuote : stockQuotes) {
        try {
            stockQuoteRepository.save(stockQuote);
        } catch (Exception e) {
            logger.error("处理股票行情失败", e);
        }
    }
}

// 优化后代码
@Override
@Transactional
public void processStockQuotes(List<StockQuote> stockQuotes) {
    if (stockQuotes == null || stockQuotes.isEmpty()) {
        return;
    }
    
    //批量插入行情数据
    stockQuoteRepository.saveAll(stockQuotes);
    logger.info("批量处理股票行情数据: {}条", stockQuotes.size());
}
```

### 3.定时任务数据更新场景 ⭐⭐
**现状问题**:
-定时任务中逐条更新采集目标状态
- 数据库连接频繁开闭

**优化方案**:
```java
// 原始代码
stockTargets.forEach(target -> {
    target.updateCollectionTime();
    dataCollectionTargetAppService.updateTargetByCode(target.getCode(), target);
});

// 优化后代码
List<DataCollectionTarget> updatedTargets = stockTargets.stream()
    .peek(target -> target.updateCollectionTime())
    .toList();
dataCollectionTargetAppService.updateTargetsBatch(updatedTargets);
```

##批量操作实现标准

### 1. 仓储层批量操作接口

#### 1.1 基础批量操作接口
```java
public interface StockRepository {
    //批量查询
    List<Stock> findBySymbols(List<String> symbols);
    List<Stock> findByIds(List<Long> ids);
    
    // 批量保存
    List<Stock> saveAll(List<Stock> stocks);
    
    // 批量更新
    int updateAll(List<Stock> stocks);
    int updateBatchById(List<Stock> stocks);
    
    // 批量删除
    int deleteByIds(List<Long> ids);
    int deleteBySymbols(List<String> symbols);
}
```

#### 1.2 Mapper层批量操作
```java
@Repository
public interface StockBasicMapper extends BaseMapper<StockBasicPO> {
    // MyBatis-Plus内置批量操作
    int insertBatchSomeColumn(List<StockBasicPO> entityList);
    int updateBatchById(List<StockBasicPO> entityList);
    
    // 自定义批量查询
    List<StockBasicPO> findBySymbols(@Param("symbols") List<String> symbols);
    List<StockBasicPO> findByIds(@Param("ids") List<Long> ids);
}
```

### 2. 事务管理要求

#### 2.1批操作操作事务边界
- **批量插入**: 使用`@Transactional`确保数据一致性
- **批量更新**:必须在事务中执行
- **混合操作**:整批量处理过程保持在同一事务中

#### 2.2 事务配置示例
```java
@Service
@Transactional(timeout = 300, rollbackFor = Exception.class)
public class DataProcessingAppServiceImpl implements DataProcessingAppService {
    
    @Override
    public void processBatchData(List<Stock> stocks, List<StockQuote> quotes) {
        // 所有操作在同一事务中
        stockRepository.saveAll(stocks);
        stockQuoteRepository.saveAll(quotes);
    }
}
```

### 3.性能优化策略

#### 3.1批量大小控制
```java
//建议的批量大小
private static final int BATCH_SIZE = 1000;

public void processLargeDataset(List<Stock> stocks) {
    if (stocks.size() > BATCH_SIZE) {
        // 分批处理
        List<List<Stock>> batches = Lists.partition(stocks, BATCH_SIZE);
        for (List<Stock> batch : batches) {
            processBatch(batch);
        }
    } else {
        processBatch(stocks);
    }
}
```

#### 3.2内存优化
```java
@Override
@Transactional
public void processStockBasics(List<Stock> stockBasics) {
    // 使用流式处理减少内存占用
    stockBasics.parallelStream()
        .collect(Collectors.groupingBy(
            stock -> stock.getSymbol().substring(0, 1), //按首字母分组
            Collectors.toList()
        ))
        .values()
        .parallelStream()
        .forEach(this::processBatch);
}
```

##批操作最佳实践

### 1. 数据预处理
```java
// 数据去重和验证
private List<Stock> preprocessStocks(List<Stock> rawStocks) {
    return rawStocks.stream()
        .filter(Objects::nonNull)
        .filter(stock -> StringUtils.isNotBlank(stock.getSymbol()))
        .distinct() //去
        .collect(Collectors.toList());
}
```

### 2. 错误处理策略
```java
@Override
@Transactional
public BatchResult processStockBatch(List<Stock> stocks) {
    BatchResult result = new BatchResult();
    try {
        List<Stock> validStocks = preprocessStocks(stocks);
        stockRepository.saveAll(validStocks);
        result.setSuccessCount(validStocks.size());
        result.setTotalCount(stocks.size());
    } catch (Exception e) {
        //记录失败详情，但不中断整个批处理
        logger.error("批量处理部分失败", e);
        result.setError(e.getMessage());
        //可以选择回滚或继续处理其他批次
    }
    return result;
}
```

### 3.监控和日志
```java
@Around("@annotation(BatchOperation)")
public Object monitorBatchOperation(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();
    int dataSize = getDataSize(args);
    
    logger.info("开始批量操作: {}, 数据量: {}", methodName, dataSize);
    
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    
    try {
        Object result = joinPoint.proceed();
        stopWatch.stop();
        
        logger.info("批量操作完成: {}, 数据量: {},耗: {}ms,平每条: {}ms", 
            methodName, dataSize, stopWatch.getTotalTimeMillis(), 
            dataSize > 0 ? stopWatch.getTotalTimeMillis() / dataSize : 0);
        
        return result;
    } catch (Exception e) {
        stopWatch.stop();
        logger.error("批量操作失败: {}, 数据量: {},耗: {}ms, 错误: {}", 
            methodName, dataSize, stopWatch.getTotalTimeMillis(), e.getMessage());
        throw e;
    }
}
```

## 代码规范要求

### 1.强规范规范
-⚠ **禁止**在循环中执行单条数据库操作
-⚠ **必须**使用批量操作处理10条以上数据
- ⚠️ **必须**在批量操作上添加事务注解
- ⚠️ **必须**进行数据预处理和验证

### 2.推荐规范
-✅ 优先使用MyBatis-Plus内置批量操作方法
- ✅合设置批量大小（建议100-1000条）
- ✅ 添加批量操作的监控和日志记录
- ✅ 实现优雅的错误处理机制

### 3.禁止规范
-❌禁止在事务外执行批量更新操作
-❌禁一次性处理过大数据集（建议分批）
- ❌禁止忽略批量操作的异常处理
- ❌禁止在批量操作中混用不同类型的业务逻辑

##性能指标要求

### 1.响应时间标准
-单条记录处理：< 10ms
-批量100条处理：< 1000ms
- 批量1000条处理：< 5000ms

### 2.吞量要求
-批量插入：> 1000条/秒
-批量更新：> 500条/秒
- 批量查询：> 2000条/秒

## 实施计划

### 第一阶段：基础批量操作实现
1. 实现仓储层批量操作接口
2. 优化数据处理服务中的批量操作
3. 添加批量操作监控

### 第二阶段：性能优化
1.调整批量大小参数
2. 实现分批处理机制
3. 添加性能监控告警

### 第三阶段：完善规范
1.制详细的批量操作编码规范
2.建立代码审查检查清单
3. 提供批量操作模板代码