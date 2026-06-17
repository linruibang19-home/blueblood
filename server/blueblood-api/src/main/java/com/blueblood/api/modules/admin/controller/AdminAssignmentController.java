package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminAssignmentQuery;
import com.blueblood.api.modules.admin.dto.AdminAssignmentVO;
import com.blueblood.api.modules.admin.dto.AssignmentRequest;
import com.blueblood.api.modules.admin.service.AdminEduService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 作业管理。
 */
@Tag(name = "后台-作业管理", description = "作业列表/详情/增改/删除")
@RestController
@RequestMapping("/admin/assignment")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAssignmentController {

    private final AdminEduService adminEduService;

    @Operation(summary = "作业列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminAssignmentVO>> list(AdminAssignmentQuery query) {
        return Result.success(adminEduService.pageAssignment(query));
    }

    @Operation(summary = "作业详情")
    @GetMapping("/{id}")
    public Result<AdminAssignmentVO> get(@PathVariable Long id) {
        return Result.success(adminEduService.getAssignment(id));
    }

    @Operation(summary = "新增作业")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody AssignmentRequest req) {
        return Result.success(adminEduService.createAssignment(req));
    }

    @Operation(summary = "编辑作业(含参考答案)")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AssignmentRequest req) {
        adminEduService.updateAssignment(id, req);
        return Result.success();
    }

    @Operation(summary = "删除作业(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminEduService.deleteAssignment(id);
        return Result.success();
    }
}
