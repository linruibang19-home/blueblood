package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminJobQuery;
import com.blueblood.api.modules.admin.dto.AdminJobVO;
import com.blueblood.api.modules.admin.service.AdminJobService;
import com.blueblood.api.modules.enterprise.dto.JobRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 岗位管理。
 */
@Tag(name = "后台-岗位管理", description = "岗位列表/详情/增删改")
@RestController
@RequestMapping("/admin/job")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminJobController {

    private final AdminJobService adminJobService;

    @Operation(summary = "岗位列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminJobVO>> list(AdminJobQuery query) {
        return Result.success(adminJobService.page(query));
    }

    @Operation(summary = "岗位详情")
    @GetMapping("/{id}")
    public Result<AdminJobVO> detail(@PathVariable Long id) {
        return Result.success(adminJobService.detail(id));
    }

    @Operation(summary = "新增岗位")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody JobRequest req) {
        return Result.success(adminJobService.create(req));
    }

    @Operation(summary = "编辑岗位")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody JobRequest req) {
        adminJobService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminJobService.delete(id);
        return Result.success();
    }
}
