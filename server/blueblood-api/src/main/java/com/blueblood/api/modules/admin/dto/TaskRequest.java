package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增/编辑任务请求。
 */
@Data
@Schema(description = "新增/编辑任务请求")
public class TaskRequest {

    @Schema(description = "任务标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "分类名称(冗余)")
    private String category;

    @Schema(description = "雇主名称")
    private String employerName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "酬金")
    private BigDecimal reward;

    @Schema(description = "所需等级")
    private Integer levelRequired;

    @Schema(description = "总名额")
    private Integer totalSlots;

    @Schema(description = "截止日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    @Schema(description = "状态：APPROVED/RECRUITING/IN_PROGRESS/COMPLETED/CLOSED 等")
    private String status;
}
