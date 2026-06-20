package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminRoleUserVO;
import com.blueblood.api.modules.admin.dto.AdminRoleVO;
import com.blueblood.api.modules.admin.dto.RoleAssignRequest;
import com.blueblood.api.modules.admin.service.AdminRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台 - 权限管理。
 */
@Tag(name = "后台-权限管理", description = "角色概览/角色用户列表/分配撤销角色")
@RestController
@RequestMapping("/admin/role")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @Operation(summary = "角色列表(含用户数)")
    @GetMapping
    public Result<List<AdminRoleVO>> list() {
        return Result.success(adminRoleService.listRoles());
    }

    @Operation(summary = "某角色下的用户列表")
    @GetMapping("/users")
    public Result<List<AdminRoleUserVO>> users(@RequestParam String roleCode) {
        return Result.success(adminRoleService.listUsersByRole(roleCode));
    }

    @Operation(summary = "给用户分配角色")
    @PostMapping("/assign")
    public Result<Void> assign(@Valid @RequestBody RoleAssignRequest req) {
        adminRoleService.assign(req);
        return Result.success();
    }

    @Operation(summary = "撤销用户角色")
    @PostMapping("/revoke")
    public Result<Void> revoke(@Valid @RequestBody RoleAssignRequest req) {
        adminRoleService.revoke(req);
        return Result.success();
    }
}
