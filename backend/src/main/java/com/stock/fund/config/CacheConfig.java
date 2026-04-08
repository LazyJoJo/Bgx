package com.stock.fund.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置
 * Caffeine 是目前 Java 生态中性能最优的本地缓存库
 *
 * 优势：
 * - 基于 ConcurrentHashMap，使用 JDK 8 优化
 * - 支持多种过期策略（访问后过期、写入后过期）
 * - 支持容量淘汰策略（LRU、LFU、WeakReference 等）
 * - 提供缓存统计和监听器
 * - Spring Cache 完美集成
 */
@Configuration
public class CacheConfig {

    @Value("${cache.default-ttl:3600}")
    private long defaultTtlSeconds;

    @Value("${cache.max-size:10000}")
    private long maxSize;

    /**
     * 通用缓存配置
     */
    public static final String DEFAULT_CACHE = "defaultCache";

    /**
     * 股票数据缓存
     */
    public static final String STOCK_CACHE = "stockCache";

    /**
     * 基金数据缓存
     */
    public static final String FUND_CACHE = "fundCache";

    /**
     * API 响应缓存
     */
    public static final String API_CACHE = "apiCache";

    /**
     * 配置 Caffeine 缓存构建器
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                // 基于容量淘汰
                .maximumSize(maxSize)
                // 写入后过期时间
                .expireAfterWrite(defaultTtlSeconds, TimeUnit.SECONDS)
                // 访问后过期时间（30 分钟无访问则过期）
                .expireAfterAccess(30, TimeUnit.MINUTES)
                // 记录统计信息
                .recordStats();
    }

    /**
     * 配置 Spring Cache 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        cacheManager.setCacheNames(java.util.List.of(
                DEFAULT_CACHE,
                STOCK_CACHE,
                FUND_CACHE,
                API_CACHE
        ));
        return cacheManager;
    }

    /**
     * 通用缓存 Bean（可直接注入使用）
     */
    @Bean
    public Cache<String, Object> generalCache(Caffeine<Object, Object> caffeine) {
        return caffeine.build();
    }

    /**
     * 股票数据缓存（短过期时间，适合实时行情）
     */
    @Bean
    public Cache<String, Object> stockCache() {
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(60, TimeUnit.SECONDS) // 股票行情 60 秒过期
                .expireAfterAccess(10, TimeUnit.SECONDS) // 10 秒无访问则过期
                .recordStats()
                .build();
    }

    /**
     * 基金数据缓存
     */
    @Bean
    public Cache<String, Object> fundCache() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(300, TimeUnit.SECONDS) // 基金净值 5 分钟过期
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }

    /**
     * API 响应缓存
     */
    @Bean
    public Cache<String, Object> apiCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(300, TimeUnit.SECONDS) // API 响应 5 分钟过期
                .recordStats()
                .build();
    }
}
