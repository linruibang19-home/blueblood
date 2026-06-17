package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增/编辑课程章节请求。
 */
@Data
@Schema(description = "新增/编辑章节请求")
public class ChapterRequest {

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Schema(description = "章节标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "章节标题不能为空")
    private String title;

    @Schema(description = "时长，如 12:30")
    private String duration;

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "章节序号(1基)")
    private Integer chapterOrder;
}
