package com.blueblood.api.modules.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户端(企业/个人)发布任务请求。
 * Q1 企业+个人均可发布;Q2 发布即上架(status 默认 APPROVED)。
 * 约束:reward 必须等于各里程碑 reward 之和(分阶段结算金额守恒)。
 */
@Data
@Schema(description = "用户端发布任务请求")
public class PublishTaskRequest {

    @Schema(description = "任务标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "任务描述")
    @NotBlank(message = "描述不能为空")
    private String description;

    @Schema(description = "任务总酬金(须等于各里程碑 reward 之和)")
    @NotNull(message = "酬金不能为空")
    @DecimalMin(value = "0.01", message = "酬金须大于0")
    private BigDecimal reward;

    @Schema(description = "接单所需等级(默认1)")
    private Integer levelRequired = 1;

    @Schema(description = "总名额(默认1)")
    @Min(value = 1, message = "名额至少1")
    private Integer totalSlots = 1;

    @Schema(description = "截止日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    @Schema(description = "技能要求(名称列表)")
    private List<String> skills;

    @Schema(description = "里程碑列表(至少1个)")
    @NotEmpty(message = "至少配置一个里程碑")
    @Valid
    private List<MilestoneTemplateDTO> milestones;
}
