package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminNotificationQuery;
import com.blueblood.api.modules.admin.dto.AdminNotificationVO;
import com.blueblood.api.modules.admin.dto.NotificationRequest;
import com.blueblood.api.modules.admin.service.AdminNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 通知管理。
 */
@Tag(name = "后台-通知管理", description = "全站通知分页/发送/删除")
@RestController
@RequestMapping("/admin/notification")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @Operation(summary = "通知列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminNotificationVO>> list(AdminNotificationQuery query) {
        return Result.success(adminNotificationService.page(query));
    }

    @Operation(summary = "发送通知")
    @PostMapping
    public Result<Void> send(@Valid @RequestBody NotificationRequest req) {
        adminNotificationService.send(req);
        return Result.success();
    }

    @Operation(summary = "删除通知(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminNotificationService.delete(id);
        return Result.success();
    }
}
