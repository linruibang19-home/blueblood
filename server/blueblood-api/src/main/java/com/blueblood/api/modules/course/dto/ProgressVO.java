package com.blueblood.api.modules.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 进度推进结果
 */
@Data
@Schema(description = "进度推进结果")
public class ProgressVO {

    @Schema(description = "已完成章节数")
    private Integer completedChapters;

    @Schema(description = "学习进度百分比 0-100")
    private Integer progress;

    @Schema(description = "学习状态：not_started / in_progress / completed")
    private String status;
}
