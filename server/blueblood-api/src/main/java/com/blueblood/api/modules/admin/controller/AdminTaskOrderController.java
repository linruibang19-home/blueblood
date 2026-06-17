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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 - 任务接单记录。
 */
@Tag(name = "后台-接单管理", description = "任务接单记录列表")
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
}
