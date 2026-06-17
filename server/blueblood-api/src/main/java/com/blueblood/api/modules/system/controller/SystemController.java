package com.blueblood.api.modules.system.controller;

import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.system.dto.UpdateConfigRequest;
import com.blueblood.api.modules.system.entity.SysDict;
import com.blueblood.api.modules.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置与数据字典。
 */
@Tag(name = "系统", description = "数据字典、系统配置")
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    @Operation(summary = "字典类型列表")
    @GetMapping("/dict/types")
    public Result<List<String>> dictTypes() {
        return Result.success(systemService.dictTypes());
    }

    @Operation(summary = "某类型下的字典项")
    @GetMapping("/dict/items")
    public Result<List<SysDict>> dictItems(@RequestParam String type) {
        return Result.success(systemService.dictItems(type));
    }

    @Operation(summary = "读取系统配置值")
    @GetMapping("/config/{key}")
    public Result<Map<String, String>> config(@PathVariable String key) {
        return Result.success(Map.of("key", key, "value", String.valueOf(systemService.configValue(key))));
    }

    @Operation(summary = "更新系统配置（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/config/{key}")
    public Result<Void> updateConfig(@PathVariable String key, @Valid @RequestBody UpdateConfigRequest req) {
        systemService.updateConfig(key, req.getConfigValue());
        return Result.success();
    }
}
