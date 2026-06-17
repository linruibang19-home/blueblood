package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.*;
import com.blueblood.api.modules.admin.service.AdminSystemService;
import com.blueblood.api.modules.system.entity.SysConfig;
import com.blueblood.api.modules.system.entity.SysDict;
import com.blueblood.api.modules.system.entity.SysOperationLog;
import com.blueblood.api.modules.task.entity.TaskCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台 - 系统配置与权限。
 * 路径分组：config / dict / category / skill / log
 */
@Tag(name = "后台-系统配置", description = "系统配置/字典/任务分类/技能标签/操作日志")
@RestController
@RequestMapping("/admin/system")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSystemController {

    private final AdminSystemService adminSystemService;

    // ==================== 系统配置 ====================

    @Operation(summary = "系统配置列表(筛选分页)")
    @GetMapping("/config")
    public Result<PageResult<SysConfig>> pageConfig(AdminSystemQuery query) {
        return Result.success(adminSystemService.pageConfig(query));
    }

    @Operation(summary = "新增系统配置")
    @PostMapping("/config")
    public Result<Long> createConfig(@Valid @RequestBody SysConfigRequest req) {
        return Result.success(adminSystemService.createConfig(req));
    }

    @Operation(summary = "编辑系统配置")
    @PutMapping("/config/{id}")
    public Result<Void> updateConfig(@PathVariable Long id, @Valid @RequestBody SysConfigRequest req) {
        adminSystemService.updateConfig(id, req);
        return Result.success();
    }

    @Operation(summary = "删除系统配置(软删)")
    @DeleteMapping("/config/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        adminSystemService.deleteConfig(id);
        return Result.success();
    }

    // ==================== 字典 ====================

    @Operation(summary = "字典列表(筛选分页)")
    @GetMapping("/dict")
    public Result<PageResult<SysDict>> pageDict(AdminSystemQuery query) {
        return Result.success(adminSystemService.pageDict(query));
    }

    @Operation(summary = "字典类型列表(去重)")
    @GetMapping("/dict/types")
    public Result<List<String>> dictTypes() {
        return Result.success(adminSystemService.dictTypes());
    }

    @Operation(summary = "新增字典项")
    @PostMapping("/dict")
    public Result<Long> createDict(@Valid @RequestBody SysDictRequest req) {
        return Result.success(adminSystemService.createDict(req));
    }

    @Operation(summary = "编辑字典项")
    @PutMapping("/dict/{id}")
    public Result<Void> updateDict(@PathVariable Long id, @Valid @RequestBody SysDictRequest req) {
        adminSystemService.updateDict(id, req);
        return Result.success();
    }

    @Operation(summary = "删除字典项(软删)")
    @DeleteMapping("/dict/{id}")
    public Result<Void> deleteDict(@PathVariable Long id) {
        adminSystemService.deleteDict(id);
        return Result.success();
    }

    // ==================== 任务分类 ====================

    @Operation(summary = "任务分类列表")
    @GetMapping("/category")
    public Result<List<TaskCategory>> listCategory() {
        return Result.success(adminSystemService.listCategory());
    }

    @Operation(summary = "新增任务分类")
    @PostMapping("/category")
    public Result<Long> createCategory(@Valid @RequestBody TaskCategoryRequest req) {
        return Result.success(adminSystemService.createCategory(req));
    }

    @Operation(summary = "编辑任务分类")
    @PutMapping("/category/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody TaskCategoryRequest req) {
        adminSystemService.updateCategory(id, req);
        return Result.success();
    }

    @Operation(summary = "删除任务分类(软删)")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminSystemService.deleteCategory(id);
        return Result.success();
    }

    // ==================== 用户技能标签 ====================

    @Operation(summary = "技能标签列表(带用户名,筛选分页)")
    @GetMapping("/skill")
    public Result<PageResult<AdminSkillVO>> pageSkill(AdminSystemQuery query) {
        return Result.success(adminSystemService.pageSkill(query));
    }

    @Operation(summary = "删除技能标签(软删)")
    @DeleteMapping("/skill/{id}")
    public Result<Void> deleteSkill(@PathVariable Long id) {
        adminSystemService.deleteSkill(id);
        return Result.success();
    }

    // ==================== 操作日志 ====================

    @Operation(summary = "操作日志列表(筛选分页)")
    @GetMapping("/log")
    public Result<PageResult<SysOperationLog>> pageLog(AdminSystemQuery query) {
        return Result.success(adminSystemService.pageLog(query));
    }
}
