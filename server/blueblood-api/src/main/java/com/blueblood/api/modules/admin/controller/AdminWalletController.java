package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminWalletAccountVO;
import com.blueblood.api.modules.admin.dto.AdminWalletRecordVO;
import com.blueblood.api.modules.admin.dto.AdminWithdrawVO;
import com.blueblood.api.modules.admin.dto.WithdrawReviewRequest;
import com.blueblood.api.modules.admin.service.AdminWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 收益结算管理。
 */
@Tag(name = "后台-收益结算", description = "收益流水/待结算/用户钱包/提现申请/提现审核")
@RestController
@RequestMapping("/admin/wallet")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWalletController {

    private final AdminWalletService adminWalletService;

    @Operation(summary = "收益流水(筛选分页)")
    @GetMapping("/record")
    public Result<PageResult<AdminWalletRecordVO>> record(PageQuery query,
                                                          @RequestParam(required = false) Long userId,
                                                          @RequestParam(required = false) String type,
                                                          @RequestParam(required = false) String status) {
        return Result.success(adminWalletService.pageRecord(userId, type, status, query));
    }

    @Operation(summary = "待结算流水(pending 分页)")
    @GetMapping("/pending")
    public Result<PageResult<AdminWalletRecordVO>> pending(PageQuery query) {
        return Result.success(adminWalletService.pagePendingRecord(query));
    }

    @Operation(summary = "用户钱包账户")
    @GetMapping("/account")
    public Result<AdminWalletAccountVO> account(@RequestParam Long userId) {
        return Result.success(adminWalletService.getWalletByUser(userId));
    }

    @Operation(summary = "提现申请(筛选分页)")
    @GetMapping("/withdraw")
    public Result<PageResult<AdminWithdrawVO>> withdraw(PageQuery query,
                                                        @RequestParam(required = false) String status) {
        return Result.success(adminWalletService.pageWithdraw(status, query));
    }

    @Operation(summary = "提现审核(通过/驳回)")
    @PutMapping("/withdraw/{id}/review")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody WithdrawReviewRequest req) {
        adminWalletService.reviewWithdraw(id, req);
        return Result.success();
    }
}
