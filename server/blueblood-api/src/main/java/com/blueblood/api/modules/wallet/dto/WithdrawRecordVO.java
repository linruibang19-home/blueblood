package com.blueblood.api.modules.wallet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现记录项。
 */
@Data
@Schema(description = "提现记录项")
public class WithdrawRecordVO {

    @Schema(description = "提现记录ID")
    private Long id;

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "状态：pending / processing / completed / failed")
    private String status;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "银行卡号（脱敏）")
    private String bankAccount;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
