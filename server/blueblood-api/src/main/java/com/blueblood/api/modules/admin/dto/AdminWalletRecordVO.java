package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台 - 钱包流水 VO。
 */
@Data
@Schema(description = "后台钱包流水")
public class AdminWalletRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "类型: income/expense/withdraw")
    private String type;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "状态: pending/available/withdrawing/withdrawn/failed")
    private String status;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "关联业务ID")
    private Long bizId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
