package com.stock.fund.interfaces.controller;

import com.stock.fund.application.service.CacheService;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "健康检查", description = "服务状态监控和健康检查接口")
public class HealthController {

    @Autowired(required = false)
    private CacheService cacheService;

    @GetMapping
    @Operation(summary = "服务健康检查", description = "检查服务运行状态，返回服务基本信息和版本号")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "Stock Fund Data Collector");
        healthInfo.put("version", "1.0.0");

        return ApiResponse.success(healthInfo);
    }

    @GetMapping("/ping")
    @Operation(summary = "简单心跳检测", description = "返回 pong，用于快速验证服务是否可访问")
    public String ping() {
        return "pong";
    }

    @GetMapping("/cache")
    @Operation(summary = "缓存状态检查", description = "检查本地缓存服务状态")
    public ApiResponse<Map<String, Object>> cacheCheck() {
        Map<String, Object> cacheInfo = new HashMap<>();

        if (cacheService != null) {
            cacheInfo.put("status", "UP");
            cacheInfo.put("timestamp", LocalDateTime.now());
            cacheInfo.put("cacheType", "Caffeine (Local Cache)");
            cacheInfo.put("available", true);
        } else {
            cacheInfo.put("status", "DOWN");
            cacheInfo.put("timestamp", LocalDateTime.now());
            cacheInfo.put("cacheType", "Caffeine (Local Cache)");
            cacheInfo.put("available", false);
        }

        return ApiResponse.success(cacheInfo);
    }
}
