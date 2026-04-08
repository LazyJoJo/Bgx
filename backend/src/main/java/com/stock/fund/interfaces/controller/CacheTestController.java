package com.stock.fund.interfaces.controller;

import com.stock.fund.application.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Tag(name = "缓存测试", description = "基于 Caffeine 的本地缓存操作测试接口")
public class CacheTestController {

    private final CacheService cacheService;

    @PostMapping("/set")
    @Operation(summary = "设置缓存", description = "存储键值对到本地缓存，无过期时间")
    public Map<String, Object> setValue(
            @Parameter(description = "缓存键", example = "test:key:1") @RequestParam String key,
            @Parameter(description = "缓存值", example = "testValue") @RequestParam String value) {
        Map<String, Object> response = new HashMap<>();
        try {
            cacheService.setValue(key, value);
            response.put("success", true);
            response.put("message", "缓存设置成功");
            response.put("data", Map.of("key", key, "value", value));
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/set-expire")
    @Operation(summary = "设置带过期时间的缓存", description = "存储带过期时间的键值对（近似过期）")
    public Map<String, Object> setValueWithExpire(
            @Parameter(description = "缓存键", example = "test:expire:1") @RequestParam String key,
            @Parameter(description = "缓存值", example = "tempValue") @RequestParam String value,
            @Parameter(description = "过期时间数值", example = "300") @RequestParam long time,
            @Parameter(description = "时间单位", example = "SECONDS") @RequestParam(defaultValue = "SECONDS") String timeUnit) {

        Map<String, Object> response = new HashMap<>();
        try {
            TimeUnit unit = TimeUnit.valueOf(timeUnit.toUpperCase());
            cacheService.setValueWithExpire(key, value, time, unit);
            response.put("success", true);
            response.put("message", "带过期时间的缓存设置成功");
            response.put("data", Map.of("key", key, "value", value, "expire", time + " " + timeUnit));
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/get")
    @Operation(summary = "获取缓存", description = "从本地缓存中获取指定键的缓存值")
    public Map<String, Object> getValue(@Parameter(description = "缓存键", example = "test:key:1") @RequestParam String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            Object value = cacheService.getValue(key);
            response.put("success", true);
            response.put("message", "获取缓存成功");
            response.put("data", Map.of("key", key, "value", value, "exists", value != null));
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除缓存", description = "从本地缓存中删除指定键的缓存")
    public Map<String, Object> deleteValue(@Parameter(description = "缓存键", example = "test:key:1") @RequestParam String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            cacheService.deleteValue(key);
            response.put("success", true);
            response.put("message", "删除缓存成功");
            response.put("data", Map.of("key", key, "deleted", true));
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/exists")
    @Operation(summary = "检查缓存是否存在", description = "检查指定键的缓存是否存在于本地缓存中")
    public Map<String, Object> exists(@Parameter(description = "缓存键", example = "test:key:1") @RequestParam String key) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean cacheExists = cacheService.exists(key);
            response.put("success", true);
            response.put("message", "检查缓存存在性成功");
            response.put("data", Map.of("key", key, "exists", cacheExists));
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/stats")
    @Operation(summary = "获取缓存统计", description = "获取各缓存的统计信息（命中率、条目数等）")
    public Map<String, Object> getStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            CacheService.CacheStats stats = cacheService.getCacheStats();
            response.put("success", true);
            response.put("message", "获取缓存统计成功");
            response.put("data", stats);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/clear")
    @Operation(summary = "清空所有缓存", description = "清空所有本地缓存")
    public Map<String, Object> clearAll() {
        Map<String, Object> response = new HashMap<>();
        try {
            cacheService.clearAll();
            response.put("success", true);
            response.put("message", "已清空所有缓存");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/clear/stock")
    @Operation(summary = "清空股票缓存", description = "清空股票数据缓存")
    public Map<String, Object> clearStockCache() {
        Map<String, Object> response = new HashMap<>();
        try {
            cacheService.clearStockCache();
            response.put("success", true);
            response.put("message", "已清空股票缓存");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/clear/fund")
    @Operation(summary = "清空基金缓存", description = "清空基金数据缓存")
    public Map<String, Object> clearFundCache() {
        Map<String, Object> response = new HashMap<>();
        try {
            cacheService.clearFundCache();
            response.put("success", true);
            response.put("message", "已清空基金缓存");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
