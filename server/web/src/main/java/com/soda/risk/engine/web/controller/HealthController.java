package com.soda.risk.engine.web.controller;

import com.soda.risk.engine.api.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查Controller - 服务健康状态监控
 */
@Tag(name = "健康检查", description = "服务健康状态监控接口")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Operation(summary = "健康检查", description = "检查服务整体健康状态")
    @GetMapping
    public Response<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new LinkedHashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        healthInfo.put("version", "3.0.0");

        // Redis健康检查
        Map<String, Object> redisHealth = new LinkedHashMap<>();
        try {
            if (redisTemplate != null) {
                redisTemplate.hasKey("health_check");
                redisHealth.put("status", "UP");
            } else {
                redisHealth.put("status", "NOT_CONFIGURED");
            }
        } catch (Exception e) {
            redisHealth.put("status", "DOWN");
            redisHealth.put("error", e.getMessage());
        }
        healthInfo.put("redis", redisHealth);

        return Response.success(healthInfo);
    }

    @Operation(summary = "版本信息", description = "获取服务版本信息")
    @GetMapping("/version")
    public Response<Map<String, String>> version() {
        Map<String, String> versionInfo = new LinkedHashMap<>();
        versionInfo.put("name", "risk-engine");
        versionInfo.put("version", "3.0.0");
        versionInfo.put("java", System.getProperty("java.version"));
        versionInfo.put("os", System.getProperty("os.name"));
        return Response.success(versionInfo);
    }
}
