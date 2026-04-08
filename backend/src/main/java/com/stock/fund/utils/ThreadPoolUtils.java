package com.stock.fund.utils;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *线程池工具类
 * 提供线程池管理和异步执行功能
 */
@Slf4j
public class ThreadPoolUtils {
    
    // 默认线程池配置
    private static final int DEFAULT_CORE_POOL_SIZE = 10;
    private static final int DEFAULT_MAX_POOL_SIZE = 20;
    private static final int DEFAULT_QUEUE_SIZE = 100;
    private static final String DEFAULT_THREAD_NAME_PREFIX = "bgx-async-";
    
    //全局线程池
    private static volatile ExecutorService globalExecutor;
    
    //线程工厂
    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = DEFAULT_THREAD_NAME_PREFIX;
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    };

    /**
     *获取全局线程池
     */
    public static ExecutorService getGlobalExecutor() {
        if (globalExecutor == null) {
            synchronized (ThreadPoolUtils.class) {
                if (globalExecutor == null) {
                    globalExecutor = createExecutorService(
                        DEFAULT_CORE_POOL_SIZE,
                        DEFAULT_MAX_POOL_SIZE,
                        DEFAULT_QUEUE_SIZE,
                        DEFAULT_THREAD_FACTORY
                    );
                }
            }
        }
        return globalExecutor;
    }

    /**
     *创建自定义线程池
     */
    public static ExecutorService createExecutorService(int corePoolSize, int maxPoolSize, int queueSize) {
        return createExecutorService(corePoolSize, maxPoolSize, queueSize, DEFAULT_THREAD_FACTORY);
    }

    /**
     *创建自定义线程池（指定线程工厂）
     */
    public static ExecutorService createExecutorService(int corePoolSize, int maxPoolSize, int queueSize, ThreadFactory threadFactory) {
        return new java.util.concurrent.ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L,
            java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.LinkedBlockingQueue<>(queueSize),
            threadFactory,
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     *创建固定大小的线程池
     */
    public static ExecutorService createFixedExecutorService(int poolSize) {
        return Executors.newFixedThreadPool(poolSize, DEFAULT_THREAD_FACTORY);
    }

    /**
     *创建单线程线程池
     */
    public static ExecutorService createSingleExecutorService() {
        return Executors.newSingleThreadExecutor(DEFAULT_THREAD_FACTORY);
    }

    /**
     *创建缓存线程池
     */
    public static ExecutorService createCachedExecutorService() {
        return Executors.newCachedThreadPool(DEFAULT_THREAD_FACTORY);
    }

    /**
     *异步执行任务
     */
    public static void executeAsync(Runnable task) {
        getGlobalExecutor().execute(task);
    }

    /**
     *异步执行任务（带线程池）
     */
    public static void executeAsync(Runnable task, ExecutorService executor) {
        if (executor != null) {
            executor.execute(task);
        } else {
            executeAsync(task);
        }
    }

    /**
     *异步执行任务并返回CompletableFuture
     */
    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, getGlobalExecutor());
    }

    /**
     *异步执行任务并返回结果
     */
    public static <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, getGlobalExecutor());
    }

    /**
     *异步执行任务（指定线程池）
     */
    public static CompletableFuture<Void> runAsync(Runnable task, ExecutorService executor) {
        return CompletableFuture.runAsync(task, executor != null ? executor : getGlobalExecutor());
    }

    /**
     *异步执行任务并返回结果（指定线程池）
     */
    public static <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier, ExecutorService executor) {
        return CompletableFuture.supplyAsync(supplier, executor != null ? executor : getGlobalExecutor());
    }

    /**
     *延迟执行任务
     */
    public static void schedule(Runnable task, long delay, java.util.concurrent.TimeUnit unit) {
        java.util.concurrent.ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, DEFAULT_THREAD_FACTORY);
        scheduler.schedule(task, delay, unit);
    }

    /**
     *周期性执行任务
     */
    public static void scheduleAtFixedRate(Runnable task, long initialDelay, long period, java.util.concurrent.TimeUnit unit) {
        java.util.concurrent.ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, DEFAULT_THREAD_FACTORY);
        scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     *周期性执行任务（固定延迟）
     */
    public static void scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, java.util.concurrent.TimeUnit unit) {
        java.util.concurrent.ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, DEFAULT_THREAD_FACTORY);
        scheduler.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    /**
     *安全关闭线程池
     */
    public static void shutdown(ExecutorService executor) {
        if (executor != null && !executor.isShutdown()) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                        log.warn("线程池未能正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("线程池关闭被中断", e);
            }
        }
    }

    /**
     *立即关闭线程池
     */
    public static void shutdownNow(ExecutorService executor) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    /**
     *关闭全局线程池
     */
    public static void shutdownGlobalExecutor() {
        shutdown(globalExecutor);
        globalExecutor = null;
    }

    /**
     *获取线程池状态信息
     */
    public static String getExecutorInfo(ExecutorService executor) {
        if (executor instanceof java.util.concurrent.ThreadPoolExecutor) {
            java.util.concurrent.ThreadPoolExecutor threadPoolExecutor = (java.util.concurrent.ThreadPoolExecutor) executor;
            return String.format("核心线程数: %d, 最大线程数: %d,活跃线程数: %d,完成任务数: %d,大小: %d",
                threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getMaximumPoolSize(),
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getCompletedTaskCount(),
                threadPoolExecutor.getQueue().size());
        }
        return "未知线程池类型";
    }

    /**
     *获取全局线程池状态信息
     */
    public static String getGlobalExecutorInfo() {
        return getExecutorInfo(getGlobalExecutor());
    }

    /**
     *创建专用的IO线程池
     */
    public static ExecutorService createIOExecutorService() {
        return createExecutorService(5, 10, 50, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "bgx-io-" + threadNumber.getAndIncrement());
                t.setDaemon(false);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        });
    }

    /**
     *创建专用的计算线程池
     */
    public static ExecutorService createComputeExecutorService() {
        int processors = Runtime.getRuntime().availableProcessors();
        return createExecutorService(processors, processors * 2, 100, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "bgx-compute-" + threadNumber.getAndIncrement());
                t.setDaemon(false);
                t.setPriority(Thread.MAX_PRIORITY);
                return t;
            }
        });
    }
}