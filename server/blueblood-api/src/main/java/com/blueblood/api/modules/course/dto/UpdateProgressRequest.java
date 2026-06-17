package com.blueblood.api.modules.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新学习进度请求
 */
@Data
@Schema(description = "更新学习进度请求")
public class UpdateProgressRequest {

    @Schema(description = "章节ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @Schema(description = "是否完成该章节。为 true 时推进 completed_chapters，false 时默认忽略")
    private Boolean completed;
}
