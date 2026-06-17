package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台 - 用户钱包账户 VO。
 */
@Data
@Schema(description = "后台用户钱包账户")
public class AdminWalletAccountVO {

    @Schema(description = "账户ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "可用余额")
    private BigDecimal balance;

    @Schema(description = "待结算金额")
    private BigDecimal pendingAmount;

    @Schema(description = "已提现金额")
    private BigDecimal withdrawnAmount;

    @Schema(description = "累计收益")
    private BigDecimal totalEarned;

    @Schema(description = "状态: ACTIVE/FROZEN")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
