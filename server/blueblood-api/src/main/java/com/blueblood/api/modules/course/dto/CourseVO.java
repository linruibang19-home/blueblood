package com.blueblood.api.modules.course.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程详情（全字段 + 当前用户进度 + 章节列表）
 */
@Data
@Schema(description = "课程详情")
public class CourseVO {

    private Long id;
    private String title;
    private String subtitle;
    private String coverImage;
    private String instructor;
    private String instructorAvatar;
    private Integer totalChapters;
    private Integer rewardPoints;
    private Integer students;

    @Schema(description = "评分 decimal(3,2)")
    private BigDecimal rating;

    private String status;

    // ===== 当前用户进度 =====
    @Schema(description = "当前用户学习进度百分比 0-100，无记录为 0")
    private Integer progress;

    @Schema(description = "当前用户已完成章节数，无记录为 0")
    private Integer completedChapters;

    @Schema(description = "当前用户学习状态：not_started / in_progress / completed，无记录为 not_started")
    private String statusForUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最近学习时间，无记录为 null")
    private LocalDateTime lastStudyAt;

    // ===== 章节列表 =====
    @Schema(description = "章节列表（按 chapter_order 升序，带 completed 标记）")
    private List<ChapterVO> chapters;
}
