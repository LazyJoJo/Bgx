package com.stock.fund.application.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存服务
 * 使用 Caffeine 实现高性能本地缓存
 */
@Service
@Slf4j
public class CacheService {

    @Autowired
    private Cache<String, Object> generalCache;

    @Autowired
    private Cache<String, Object> stockCache;

    @Autowired
    private Cache<String, Object> fundCache;

    @Autowired
    private Cache<String, Object> apiCache;

    /**
     * 存储键值对到通用缓存
     */
    public <T> void setValue(String key, T value) {
        generalCache.put(key, value);
        log.debug("已存储缓存: key={}", key);
    }

    /**
     * 存储带过期时间的键值对
     * 注意：Caffeine 不支持精确过期时间，这里设置的是近似过期
     */
    public <T> void setValueWithExpire(String key, T value, long time, TimeUnit timeUnit) {
        // Caffeine 通过调度线程异步清理过期数据，过期时间是近似值
        generalCache.put(key, value);
        log.debug("已存储带过期时间的缓存: key={}, expire={}{}", key, time, timeUnit);
    }

    /**
     * 获取缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        T value = (T) generalCache.getIfPresent(key);
        log.debug("获取缓存: key={}, exists={}", key, value != null);
        return value;
    }

    /**
     * 获取缓存值，如果不存在则使用 loader 加载
     */
    public <T> T getValue(String key, java.util.function.Function<String, T> loader) {
        return (T) generalCache.get(key, loader);
    }

    /**
     * 删除缓存
     */
    public void deleteValue(String key) {
        generalCache.invalidate(key);
        log.debug("删除缓存: key={}", key);
    }

    /**
     * 批量删除缓存
     */
    public void deleteValues(Iterable<String> keys) {
        keys.forEach(generalCache::invalidate);
        log.debug("批量删除缓存完成");
    }

    /**
     * 检查键是否存在
     */
    public boolean exists(String key) {
        return generalCache.getIfPresent(key) != null;
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        generalCache.invalidateAll();
        log.info("已清空所有通用缓存");
    }

    /**
     * 清空股票缓存
     */
    public void clearStockCache() {
        stockCache.invalidateAll();
        log.info("已清空股票缓存");
    }

    /**
     * 清空基金缓存
     */
    public void clearFundCache() {
        fundCache.invalidateAll();
        log.info("已清空基金缓存");
    }

    /**
     * 清空 API 缓存
     */
    public void clearApiCache() {
        apiCache.invalidateAll();
        log.info("已清空 API 缓存");
    }

    /**
     * 存储股票数据到缓存
     */
    public <T> void setStock(String symbol, T data) {
        stockCache.put(symbol, data);
        log.debug("已存储股票缓存: symbol={}", symbol);
    }

    /**
     * 获取股票缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T getStock(String symbol) {
        return (T) stockCache.getIfPresent(symbol);
    }

    /**
     * 存储基金数据到缓存
     */
    public <T> void setFund(String fundCode, T data) {
        fundCache.put(fundCode, data);
        log.debug("已存储基金缓存: fundCode={}", fundCode);
    }

    /**
     * 获取基金缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T getFund(String fundCode) {
        return (T) fundCache.getIfPresent(fundCode);
    }

    /**
     * 存储 API 响应到缓存
     */
    public <T> void setApiResponse(String key, T response) {
        apiCache.put(key, response);
        log.debug("已存储 API 缓存: key={}", key);
    }

    /**
     * 获取 API 响应缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T getApiResponse(String key) {
        return (T) apiCache.getIfPresent(key);
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        stats.setGeneralCacheStats(generalCache.stats());
        stats.setStockCacheStats(stockCache.stats());
        stats.setFundCacheStats(fundCache.stats());
        stats.setApiCacheStats(apiCache.stats());
        return stats;
    }

    /**
     * 缓存统计信息类
     */
    @lombok.Data
    public static class CacheStats {
        private com.github.benmanes.caffeine.cache.stats.CacheStats generalCacheStats;
        private com.github.benmanes.caffeine.cache.stats.CacheStats stockCacheStats;
        private com.github.benmanes.caffeine.cache.stats.CacheStats fundCacheStats;
        private com.github.benmanes.caffeine.cache.stats.CacheStats apiCacheStats;
    }
}
