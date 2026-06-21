package com.blueblood.api.modules.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 里程碑模板(发布任务时配置;接单时复制为订单实例)。
 */
@Data
@Schema(description = "里程碑模板")
public class MilestoneTemplateDTO {

    @Schema(description = "里程碑标题")
    @NotBlank(message = "里程碑标题不能为空")
    private String title;

    @Schema(description = "里程碑描述")
    private String description;

    @Schema(description = "截止日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Schema(description = "顺序(可空,后端按列表序自动编号)")
    private Integer milestoneOrder;

    @Schema(description = "里程碑酬金(APPROVED 即按此金额结算)")
    @NotNull(message = "里程碑金额不能为空")
    @DecimalMin(value = "0.01", message = "里程碑金额须大于0")
    private BigDecimal reward;
}
