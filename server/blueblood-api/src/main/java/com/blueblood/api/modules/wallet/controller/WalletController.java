package com.blueblood.api.modules.wallet.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.wallet.dto.WalletRecordVO;
import com.blueblood.api.modules.wallet.dto.WalletVO;
import com.blueblood.api.modules.wallet.dto.WithdrawRecordVO;
import com.blueblood.api.modules.wallet.dto.WithdrawRequest;
import com.blueblood.api.modules.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 钱包模块：账户概要、流水、待结算、提现。
 */
@Tag(name = "钱包", description = "钱包概要、流水、待结算、提现")
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "钱包概要（无账户则自动创建空账户）")
    @GetMapping("/summary")
    public Result<WalletVO> summary() {
        return Result.success(walletService.summary());
    }

    @Operation(summary = "钱包流水分页（可按 type/status 筛选）")
    @GetMapping("/records")
    public Result<PageResult<WalletRecordVO>> records(@RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer pageSize) {
        return Result.success(walletService.pageRecords(page, pageSize, type, status));
    }

    @Operation(summary = "待结算列表（status=pending）")
    @GetMapping("/pending")
    public Result<PageResult<WalletRecordVO>> pending(@RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer pageSize) {
        return Result.success(walletService.pagePending(page, pageSize));
    }

    @Operation(summary = "提现记录分页")
    @GetMapping("/withdraws")
    public Result<PageResult<WithdrawRecordVO>> withdraws(@RequestParam(required = false) Integer page,
                                                          @RequestParam(required = false) Integer pageSize) {
        return Result.success(walletService.pageWithdraws(page, pageSize));
    }

    @Operation(summary = "创建提现申请")
    @PostMapping("/withdraw")
    public Result<Map<String, Object>> withdraw(@Valid @RequestBody WithdrawRequest req) {
        Long withdrawId = walletService.createWithdraw(req);
        return Result.success(Map.of("withdrawId", withdrawId));
    }
}
