package com.stock.fund.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

/**
 *事务管理切面
 *统一处理事务相关的日志记录和监控
 */
@Aspect
@Component
public class TransactionAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionAspect.class);
    
    /**
     * 环绕通知：记录事务方法的执行情况
     *所有带有@Transactional注解的方法
     */
    @Around("@annotation(transactional)")
    public Object monitorTransaction(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        logger.info("开始事务方法: {}.{}，事务配置: readOnly={}, timeout={}s", 
            className, methodName, transactional.readOnly(), transactional.timeout());
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            logger.info("事务方法执行成功: {}.{}，耗时: {}ms", 
                className, methodName, stopWatch.getTotalTimeMillis());
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("事务方法执行失败: {}.{}，耗时: {}ms，异常: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            // 重新抛出异常，让事务管理器处理回滚
            throw e;
        }
    }
    
    /**
     * 环绕通知：记录数据修改操作的事务情况
     *所有修改数据的应用服务方法
     */
    @Around("execution(* com.stock.fund.application.service.*.create*(..)) || " +
            "execution(* com.stock.fund.application.service.*.update*(..)) || " +
            "execution(* com.stock.fund.application.service.*.delete*(..)) || " +
            "execution(* com.stock.fund.application.service.*.save*(..))")
    public Object monitorDataModification(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        logger.info("开始数据修改操作: {}.{}，参数: {}", className, methodName, formatArgs(args));
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            logger.info("数据修改操作成功: {}.{}，耗时: {}ms", 
                className, methodName, stopWatch.getTotalTimeMillis());
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("数据修改操作失败: {}.{}，耗时: {}ms，异常: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 环绕通知：记录批量操作的事务情况
     */
    @Around("execution(* com.stock.fund.application.service.*.batch*(..)) || " +
            "execution(* com.stock.fund.application.service.*.process*(..))")
    public Object monitorBatchOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        logger.info("开始批量操作: {}.{}，数据量: {}", className, methodName, getDataSize(args));
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            logger.info("批量操作完成: {}.{}，耗时: {}ms", 
                className, methodName, stopWatch.getTotalTimeMillis());
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("批量操作失败: {}.{}，耗时: {}ms，异常: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     *格化参数
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "无参数";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("arg").append(i).append(": ").append(formatObject(args[i]));
        }
        return sb.toString();
    }
    
    /**
     * 获取数据大小
     */
    private String getDataSize(Object[] args) {
        if (args == null || args.length == 0) {
            return "0";
        }
        
        for (Object arg : args) {
            if (arg instanceof java.util.Collection) {
                return String.valueOf(((java.util.Collection<?>) arg).size());
            }
            if (arg instanceof java.util.Map) {
                return String.valueOf(((java.util.Map<?, ?>) arg).size());
            }
        }
        return "1";
    }
    
    /**
     *格化对象
     */
    private String formatObject(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            String str = (String) obj;
            return str.length() > 30 ? str.substring(0, 30) + "..." : str;
        }
        return obj.getClass().getSimpleName();
    }
}