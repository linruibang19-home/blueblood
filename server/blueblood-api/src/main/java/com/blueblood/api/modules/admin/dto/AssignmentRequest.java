package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新增/编辑作业请求。
 */
@Data
@Schema(description = "新增/编辑作业请求")
public class AssignmentRequest {

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Schema(description = "所属章节ID，可空")
    private Long chapterId;

    @Schema(description = "作业标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "作业标题不能为空")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "参考答案")
    private String answer;

    @Schema(description = "状态：not_submitted / submitted / graded")
    private String status;
}
