package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminGroupQuery;
import com.blueblood.api.modules.admin.dto.AdminGroupVO;
import com.blueblood.api.modules.admin.dto.GroupRequest;
import com.blueblood.api.modules.admin.service.AdminCommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 小组管理。
 */
@Tag(name = "后台-小组管理", description = "小组列表/详情/新增/编辑/上下架/删除")
@RestController
@RequestMapping("/admin/group")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminGroupController {

    private final AdminCommunityService adminCommunityService;

    @Operation(summary = "小组列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminGroupVO>> list(AdminGroupQuery query) {
        return Result.success(adminCommunityService.pageGroup(query));
    }

    @Operation(summary = "小组详情")
    @GetMapping("/{id}")
    public Result<AdminGroupVO> detail(@PathVariable Long id) {
        return Result.success(adminCommunityService.getGroup(id));
    }

    @Operation(summary = "新增小组")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody GroupRequest req) {
        return Result.success(adminCommunityService.createGroup(req));
    }

    @Operation(summary = "编辑小组")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody GroupRequest req) {
        adminCommunityService.updateGroup(id, req);
        return Result.success();
    }

    @Operation(summary = "小组上架/下架(ACTIVE/INACTIVE)")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminCommunityService.updateGroupStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除小组(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCommunityService.deleteGroup(id);
        return Result.success();
    }
}
