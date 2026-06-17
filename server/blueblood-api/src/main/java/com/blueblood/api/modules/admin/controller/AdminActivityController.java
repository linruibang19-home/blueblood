package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.ActivityRequest;
import com.blueblood.api.modules.admin.dto.AdminActivityQuery;
import com.blueblood.api.modules.admin.dto.AdminActivityVO;
import com.blueblood.api.modules.admin.service.AdminCommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 活动管理。
 */
@Tag(name = "后台-活动管理", description = "活动列表/新增/编辑/删除")
@RestController
@RequestMapping("/admin/activity")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminActivityController {

    private final AdminCommunityService adminCommunityService;

    @Operation(summary = "活动列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminActivityVO>> list(AdminActivityQuery query) {
        return Result.success(adminCommunityService.pageActivity(query));
    }

    @Operation(summary = "新增活动")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ActivityRequest req) {
        return Result.success(adminCommunityService.createActivity(req));
    }

    @Operation(summary = "编辑活动")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ActivityRequest req) {
        adminCommunityService.updateActivity(id, req);
        return Result.success();
    }

    @Operation(summary = "删除活动(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCommunityService.deleteActivity(id);
        return Result.success();
    }
}
