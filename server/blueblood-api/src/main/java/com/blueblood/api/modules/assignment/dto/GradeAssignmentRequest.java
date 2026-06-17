package com.blueblood.api.modules.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批改作业请求（管理员）。
 */
@Data
@Schema(description = "批改作业请求")
public class GradeAssignmentRequest {

    @Schema(description = "被批改用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "userId 不能为空")
    private Long userId;

    @Schema(description = "分数，0-100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "score 不能为空")
    @DecimalMin(value = "0", message = "score 不能小于 0")
    @DecimalMax(value = "100", message = "score 不能大于 100")
    private BigDecimal score;

    @Schema(description = "批改反馈")
    private String feedback;
}
