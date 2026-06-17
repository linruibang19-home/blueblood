package com.blueblood.api.modules.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包概要。
 */
@Data
@Schema(description = "钱包概要")
public class WalletVO {

    @Schema(description = "账户ID")
    private Long id;

    @Schema(description = "可用余额")
    private BigDecimal balance;

    @Schema(description = "待结算金额")
    private BigDecimal pendingAmount;

    @Schema(description = "已提现金额")
    private BigDecimal withdrawnAmount;

    @Schema(description = "累计收益")
    private BigDecimal totalEarned;

    @Schema(description = "状态：ACTIVE / FROZEN")
    private String status;
}
