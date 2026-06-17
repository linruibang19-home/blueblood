package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "提现审核（管理员）")
public class WithdrawReviewRequest {

    @Schema(description = "审核动作: APPROVED/REJECTED")
    @NotBlank(message = "审核动作不能为空")
    @Pattern(regexp = "APPROVED|REJECTED", message = "审核动作只能为 APPROVED 或 REJECTED")
    private String action;

    @Schema(description = "驳回原因(REJECTED 时填写)")
    private String failureReason;
}
