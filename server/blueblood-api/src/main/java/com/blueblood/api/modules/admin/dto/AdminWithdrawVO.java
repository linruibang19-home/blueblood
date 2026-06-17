package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台 - 提现申请 VO。
 */
@Data
@Schema(description = "后台提现申请")
public class AdminWithdrawVO {

    @Schema(description = "提现记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "状态: pending/processing/completed/failed")
    private String status;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "银行卡号(脱敏)")
    private String bankAccount;

    @Schema(description = "失败原因")
    private String failureReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "处理时间")
    private LocalDateTime processedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "申请时间")
    private LocalDateTime createdAt;
}
