package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminTaskOrderQuery;
import com.blueblood.api.modules.admin.dto.AdminTaskOrderVO;
import com.blueblood.api.modules.admin.service.AdminTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 - 任务接单管理:接单记录列表 + 强制关闭。
 */
@Tag(name = "后台-接单管理", description = "任务接单记录列表、强制关闭")
@RestController
@RequestMapping("/admin/order")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTaskOrderController {

    private final AdminTaskService adminTaskService;

    @Operation(summary = "接单记录列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminTaskOrderVO>> list(AdminTaskOrderQuery query) {
        return Result.success(adminTaskService.pageOrder(query));
    }

    @Operation(summary = "强制关闭订单(平台介入终止,置 rejected)")
    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        adminTaskService.closeOrder(id);
        return Result.success();
    }
}
