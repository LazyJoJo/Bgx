package com.stock.fund.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *性能监控切面
 *监控方法执行性能，统计调用次数和平均执行时间
 */
@Aspect
@Component
public class PerformanceMonitoringAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
    
    // 方法执行统计信息
    private final ConcurrentHashMap<String, MethodStats> methodStats = new ConcurrentHashMap<>();
    
    /**
     * 环绕通知：监控数据采集方法的性能
     */
    @Around("execution(* com.stock.fund.application.service.*DataCollection*.*(..))")
    public Object monitorDataCollectionPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodKey = getMethodKey(joinPoint);
        MethodStats stats = methodStats.computeIfAbsent(methodKey, k -> new MethodStats());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            // 更新统计信息
            stats.update(stopWatch.getTotalTimeMillis());
            
            // 记录性能警告（超过阈值时）
            if (stopWatch.getTotalTimeMillis() > 5000) { // 5秒阈值
                logger.warn("数据采集方法执行时间过长: {}，耗时: {}ms", methodKey, stopWatch.getTotalTimeMillis());
            }
            
            //定输出统计信息
            if (stats.getCallCount() % 100 == 0) {
                logger.info("方法性能统计 [{}]:调用次数={},平耗时={}ms, 最大耗时={}ms", 
                    methodKey, stats.getCallCount(), stats.getAverageTime(), stats.getMaxTime());
            }
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("数据采集方法执行异常: {}，耗时: {}ms，异常: {}", 
                methodKey, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 环绕通知：监控定时任务方法的性能
     */
    @Around("execution(* com.stock.fund.application.scheduler.*.*(..))")
    public Object monitorSchedulerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodKey = getMethodKey(joinPoint);
        MethodStats stats = methodStats.computeIfAbsent(methodKey, k -> new MethodStats());
        
        logger.info("开始执行定时任务: {}", methodKey);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            stats.update(stopWatch.getTotalTimeMillis());
            
            logger.info("定时任务执行完成: {}，耗时: {}ms", methodKey, stopWatch.getTotalTimeMillis());
            
            // 记录性能警告
            if (stopWatch.getTotalTimeMillis() > 30000) { // 30秒阈值
                logger.warn("定时任务执行时间过长: {}，耗时: {}ms", methodKey, stopWatch.getTotalTimeMillis());
            }
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("定时任务执行异常: {}，耗时: {}ms，异常: {}", 
                methodKey, stopWatch.getTotalTimeMillis(), e.getMessage());
            //定时任务异常不重新抛出，避免影响其他任务
            return null;
        }
    }
    
    /**
     * 环绕通知：监控数据库操作方法的性能
     */
    @Around("execution(* com.stock.fund.infrastructure.repository.*.*(..))")
    public Object monitorDatabasePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodKey = getMethodKey(joinPoint);
        MethodStats stats = methodStats.computeIfAbsent(methodKey, k -> new MethodStats());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            stats.update(stopWatch.getTotalTimeMillis());
            
            // 记录性能警告（数据库操作超过1秒）
            if (stopWatch.getTotalTimeMillis() > 1000) {
                logger.warn("数据库操作耗时较长: {}，耗时: {}ms", methodKey, stopWatch.getTotalTimeMillis());
            }
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("数据库操作异常: {}，耗时: {}ms，异常: {}", 
                methodKey, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     *环绕通知：监控HTTP请求方法的性能
     */
    @Around("execution(* com.stock.fund.application.service.*Http*.*(..)) || " +
            "execution(* com.stock.fund.utils.*Http*.*(..))")
    public Object monitorHttpPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodKey = getMethodKey(joinPoint);
        MethodStats stats = methodStats.computeIfAbsent(methodKey, k -> new MethodStats());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            stats.update(stopWatch.getTotalTimeMillis());
            
            // 记录性能警告（HTTP请求超过5秒）
            if (stopWatch.getTotalTimeMillis() > 5000) {
                logger.warn("HTTP请求耗时较长: {}，耗时: {}ms", methodKey, stopWatch.getTotalTimeMillis());
            }
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("HTTP请求异常: {}，耗时: {}ms，异常: {}", 
                methodKey, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 获取方法唯一标识
     */
    private String getMethodKey(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }
    
    /**
     * 方法统计信息内部类
     */
    private static class MethodStats {
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private volatile long maxTime = 0;
        
        public void update(long executionTime) {
            callCount.incrementAndGet();
            totalTime.addAndGet(executionTime);
            
            // 更新最大执行时间
            long currentMax = maxTime;
            while (executionTime > currentMax) {
                if (java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater(
                    MethodStats.class, "maxTime").compareAndSet(this, currentMax, executionTime)) {
                    break;
                }
                currentMax = maxTime;
            }
        }
        
        public long getCallCount() {
            return callCount.get();
        }
        
        public long getAverageTime() {
            long count = callCount.get();
            return count > 0 ? totalTime.get() / count : 0;
        }
        
        public long getMaxTime() {
            return maxTime;
        }
    }
}