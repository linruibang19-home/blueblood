package com.blueblood.api.modules.notification.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.notification.dto.CreateNotificationRequest;
import com.blueblood.api.modules.notification.dto.NotificationVO;
import com.blueblood.api.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知模块：我的通知分页、未读数、详情（自动已读）、标记已读、批量已读、管理员创建。
 */
@Tag(name = "通知", description = "通知分页、未读数、已读、批量已读、管理员创建")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "我的通知分页（可按 type 筛选，每项含 read 布尔）")
    @GetMapping
    public Result<PageResult<NotificationVO>> myPage(@RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer pageSize,
                                                     @RequestParam(required = false) String type) {
        return Result.success(notificationService.pageMy(page, pageSize, type));
    }

    @Operation(summary = "未读通知数")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.success(Map.of("count", notificationService.unreadCount()));
    }

    @Operation(summary = "通知详情（校验归属，自动标记已读）")
    @GetMapping("/{id}")
    public Result<NotificationVO> detail(@PathVariable Long id) {
        return Result.success(notificationService.detail(id));
    }

    @Operation(summary = "标记单条通知已读（校验归属）")
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    @Operation(summary = "批量已读（当前用户全部未读；可选 type 仅已读某类型）")
    @PostMapping("/read-all")
    public Result<Map<String, Integer>> markAllRead(@RequestParam(required = false) String type) {
        return Result.success(Map.of("count", notificationService.markAllRead(type)));
    }

    @Operation(summary = "创建系统通知（管理员预留）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody CreateNotificationRequest req) {
        Long id = notificationService.create(req);
        return Result.success(Map.of("notificationId", id));
    }
}
