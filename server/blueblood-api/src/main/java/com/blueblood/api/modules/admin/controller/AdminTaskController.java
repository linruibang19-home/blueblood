package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminTaskQuery;
import com.blueblood.api.modules.admin.dto.AdminTaskVO;
import com.blueblood.api.modules.admin.dto.TaskRequest;
import com.blueblood.api.modules.admin.service.AdminTaskService;
import com.blueblood.api.modules.task.dto.TaskCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台 - 任务管理。
 */
@Tag(name = "后台-任务管理", description = "任务列表/详情/增改/状态/删除 + 任务分类")
@RestController
@RequestMapping("/admin/task")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTaskController {

    private final AdminTaskService adminTaskService;

    @Operation(summary = "任务列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminTaskVO>> list(AdminTaskQuery query) {
        return Result.success(adminTaskService.pageTask(query));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public Result<AdminTaskVO> get(@PathVariable Long id) {
        return Result.success(adminTaskService.getTask(id));
    }

    @Operation(summary = "发布任务(新增)")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TaskRequest req) {
        return Result.success(adminTaskService.createTask(req));
    }

    @Operation(summary = "编辑任务")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TaskRequest req) {
        adminTaskService.updateTask(id, req);
        return Result.success();
    }

    @Operation(summary = "改任务状态(CLOSED/COMPLETED 等)")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminTaskService.updateTaskStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除任务(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminTaskService.deleteTask(id);
        return Result.success();
    }

    @Operation(summary = "任务分类列表")
    @GetMapping("/categories")
    public Result<List<TaskCategoryVO>> categories() {
        return Result.success(adminTaskService.listCategories());
    }
}
