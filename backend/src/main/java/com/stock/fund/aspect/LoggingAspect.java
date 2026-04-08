package com.stock.fund.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * 日志记录切面
 *统处理方法执行的日志记录，包括执行时间、参数、返回值等
 */
@Aspect
@Component
public class LoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    
    /**
     * 环绕通知：记录定时任务方法的执行日志
     */
    @Around("execution(* com.stock.fund.application.scheduler.*.*(..))")
    public Object logSchedulerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        logger.info("开始执行定时任务: {}.{}，参数: {}", className, methodName, formatArgs(args));
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            // 记录方法结束
            logger.info("定时任务执行完成: {}.{}，耗时: {}ms", 
                className, methodName, stopWatch.getTotalTimeMillis());
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("定时任务执行异常: {}.{}，耗时: {}ms，异常: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 环绕通知：记录应用服务方法的执行日志
     */
    @Around("execution(* com.stock.fund.application.service.*.*(..))")
    public Object logApplicationServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        //记方法开始
        logger.info("开始执行应用服务方法: {}.{}，参数: {}", className, methodName, formatArgs(args));
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            // 记录方法结束
            logger.info("应用服务方法执行完成: {}.{}，耗时: {}ms，返回值: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), formatResult(result));
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("应用服务方法执行异常: {}.{}，耗时: {}ms，异常: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 环绕通知：记录控制器方法的执行日志
     */
    @Around("execution(* com.stock.fund.interfaces.controller.*.*(..))")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        logger.info("开始处理请求: {}.{}，参数: {}", className, methodName, formatArgs(args));
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            logger.info("请求处理完成: {}.{}，耗时: {}ms", 
                className, methodName, stopWatch.getTotalTimeMillis());
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("请求处理异常: {}.{}，耗时: {}ms，异常: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 环绕通知：记录数据处理方法的执行日志
     */
    @Around("execution(* com.stock.fund.application.service.impl.*DataProcessing*.*(..))")
    public Object logDataProcessingExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        logger.info("开始数据处理: {}.{}，数据量: {}", className, methodName, getDataSize(args));
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            logger.info("数据处理完成: {}.{}，耗时: {}ms，处理结果: {}", 
                className, methodName, stopWatch.getTotalTimeMillis(), formatResult(result));
            
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("数据处理异常: {}.{}，耗时: {}ms，异常: {}", 
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
     *格化返回结果
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof java.util.Collection) {
            return "集合(size=" + ((java.util.Collection<?>) result).size() + ")";
        }
        return formatObject(result);
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
            return str.length() > 50 ? str.substring(0, 50) + "..." : str;
        }
        return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
    }
}