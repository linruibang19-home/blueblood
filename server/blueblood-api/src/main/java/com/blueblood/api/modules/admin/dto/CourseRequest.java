package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增/编辑课程请求。
 */
@Data
@Schema(description = "新增/编辑课程请求")
public class CourseRequest {

    @Schema(description = "课程标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "封面图 URL")
    private String coverImage;

    @Schema(description = "讲师")
    private String instructor;

    @Schema(description = "讲师头像 URL")
    private String instructorAvatar;

    @Schema(description = "总章节数")
    private Integer totalChapters;

    @Schema(description = "奖励积分")
    private Integer rewardPoints;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "状态：PUBLISHED / DRAFT / OFFLINE")
    private String status;
}
