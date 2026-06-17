package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台按 submissionId 批改作业请求。
 */
@Data
@Schema(description = "批改提交请求")
public class GradeSubmissionRequest {

    @Schema(description = "分数，0-100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "score 不能为空")
    @DecimalMin(value = "0", message = "score 不能小于 0")
    @DecimalMax(value = "100", message = "score 不能大于 100")
    private BigDecimal score;

    @Schema(description = "批改反馈")
    private String feedback;
}
