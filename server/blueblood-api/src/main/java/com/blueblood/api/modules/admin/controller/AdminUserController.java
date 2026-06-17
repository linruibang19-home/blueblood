package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminUserQuery;
import com.blueblood.api.modules.admin.dto.AdminUserVO;
import com.blueblood.api.modules.admin.dto.AdjustUserRequest;
import com.blueblood.api.modules.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 用户管理。
 */
@Tag(name = "后台-用户管理", description = "用户列表/详情/禁用启用/调整等级积分")
@RestController
@RequestMapping("/admin/user")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "用户列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminUserVO>> list(AdminUserQuery query) {
        return Result.success(adminUserService.page(query));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<AdminUserVO> detail(@PathVariable Long id) {
        return Result.success(adminUserService.detail(id));
    }

    @Operation(summary = "禁用/启用/封禁")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminUserService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "调整等级/积分(写日志)")
    @PutMapping("/{id}/adjust")
    public Result<Void> adjust(@PathVariable Long id, @Valid @RequestBody AdjustUserRequest req) {
        adminUserService.adjust(id, req);
        return Result.success();
    }
}
