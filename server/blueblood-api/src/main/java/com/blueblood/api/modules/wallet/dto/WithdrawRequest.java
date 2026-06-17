package com.blueblood.api.modules.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建提现申请请求。
 */
@Data
@Schema(description = "创建提现申请请求")
public class WithdrawRequest {

    @Schema(description = "提现金额（必须大于0）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额须大于0")
    private BigDecimal amount;

    @Schema(description = "银行名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "银行名称不能为空")
    private String bankName;

    @Schema(description = "银行卡号（脱敏）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "银行卡号不能为空")
    private String bankAccount;
}
