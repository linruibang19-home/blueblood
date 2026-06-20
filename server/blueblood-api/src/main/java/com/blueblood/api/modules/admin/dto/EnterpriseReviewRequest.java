package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 审核企业认证申请。
 */
@Data
@Schema(description = "审核企业认证申请")
public class EnterpriseReviewRequest {

    @Schema(description = "审核结果: APPROVED/REJECTED")
    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "APPROVED|REJECTED", message = "审核结果只能为 APPROVED 或 REJECTED")
    private String status;

    @Schema(description = "驳回原因(REJECTED 时必填)")
    private String rejectReason;
}
