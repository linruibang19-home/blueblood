package com.blueblood.api.controller;

import com.blueblood.api.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查接口（公开）：基础、DB 连通、Redis 连通。
 */
@Tag(name = "系统", description = "健康检查")
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Operation(summary = "基础健康检查")
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("service", "blueblood-api");
        info.put("time", LocalDateTime.now().toString());
        return Result.success(info);
    }

    @Operation(summary = "MySQL 连通性检查")
    @GetMapping("/db")
    public Result<Map<String, Object>> db() {
        Map<String, Object> info = new LinkedHashMap<>();
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            info.put("status", "UP");
            info.put("probe", "SELECT 1 -> " + one);
        } catch (Exception e) {
            info.put("status", "DOWN");
            info.put("error", e.getMessage());
        }
        return Result.success(info);
    }

    @Operation(summary = "Redis 连通性检查")
    @GetMapping("/redis")
    public Result<Map<String, Object>> redis() {
        Map<String, Object> info = new LinkedHashMap<>();
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            info.put("status", "UP");
            info.put("probe", pong);
        } catch (Exception e) {
            info.put("status", "DOWN");
            info.put("error", e.getMessage());
        }
        return Result.success(info);
    }
}
