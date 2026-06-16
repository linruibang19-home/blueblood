package com.blueblood.api.modules.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 章节项（带当前用户完成标记）
 */
@Data
@Schema(description = "章节项")
public class ChapterVO {

    private Long id;
    private Long courseId;
    private String title;

    @Schema(description = "时长，如 12:30")
    private String duration;

    private String videoUrl;

    @Schema(description = "章节序号，1 基")
    private Integer chapterOrder;

    @Schema(description = "当前用户是否已完成该章节（chapter_order <= completedChapters 判定）")
    private Boolean completed;
}
