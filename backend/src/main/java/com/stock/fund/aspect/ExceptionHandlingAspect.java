package com.stock.fund.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *异常处理切面
 *统一处理方法执行中的异常，避免重复的try-catch代码
 */
@Aspect
@Component
public class ExceptionHandlingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingAspect.class);
    
    /**
     * 环绕通知：处理应用服务层的异常
     *所有应用服务实现类的方法
     */
    @Around("execution(* com.stock.fund.application.service.impl.*.*(..))")
    public Object handleApplicationServiceException(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        try {
            logger.debug("开始执行应用服务方法: {}.{}", className, methodName);
            Object result = joinPoint.proceed();
            logger.debug("应用服务方法执行成功: {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            logger.error("应用服务方法执行失败: {}.{} - {}", className, methodName, e.getMessage(), e);
            // 重新抛出异常，让上层处理
            throw e;
        }
    }
    
    /**
     * 环绕通知：处理仓储层的异常
     *所有仓储实现类的方法
     */
/*     @Around("execution(* com.stock.fund.infrastructure.repository.*.*(..))")
    public Object handleRepositoryException(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        try {
            logger.debug("开始执行仓储方法: {}.{}", className, methodName);
            Object result = joinPoint.proceed();
            logger.debug("仓储方法执行成功: {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            logger.error("仓储方法执行失败: {}.{} - {}", className, methodName, e.getMessage(), e);
            // 重新抛出异常，让上层处理
            throw e;
        }
    } */
    
    /**
     * 环绕通知：处理定时任务的异常
     *所有调度器类的方法
     */
    @Around("execution(* com.stock.fund.application.scheduler.*.*(..))")
    public Object handleSchedulerException(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        try {
            logger.info("开始执行定时任务: {}.{}", className, methodName);
            Object result = joinPoint.proceed();
            logger.info("定时任务执行成功: {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            logger.error("定时任务执行失败: {}.{} - {}", className, methodName, e.getMessage(), e);
            //定任务异常不重新抛出，避免影响其他任务
            return null;
        }
    }
}