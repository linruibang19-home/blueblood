package com.blueblood.api.modules.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "审核里程碑（管理员）")
public class ReviewMilestoneRequest {

    @Schema(description = "审核结果: APPROVED/REJECTED")
    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "APPROVED|REJECTED", message = "审核结果只能为 APPROVED 或 REJECTED")
    private String result;

    @Schema(description = "审核反馈")
    private String feedback;
}
