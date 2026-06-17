package com.blueblood.api.modules.wallet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水项。
 */
@Data
@Schema(description = "钱包流水项")
public class WalletRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "类型：income / expense / withdraw")
    private String type;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "状态：pending / available / withdrawing / withdrawn / failed")
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
    private LocalDateTime createdAt;
}
