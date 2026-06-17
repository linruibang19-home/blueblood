package com.blueblood.api.modules.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程列表项（轻量，带当前用户进度）
 */
@Data
@Schema(description = "课程列表项")
public class CourseListItemVO {

    private Long id;
    private String title;
    private String subtitle;
    private String coverImage;
    private String instructor;
    private Integer totalChapters;
    private Integer rewardPoints;
    private Integer students;

    @Schema(description = "评分 decimal(3,2)")
    private BigDecimal rating;

    @Schema(description = "当前用户学习进度百分比 0-100，无进度记录为 0")
    private Integer progress;

    @Schema(description = "当前用户学习状态：not_started / in_progress / completed，无记录为 not_started")
    private String status;
}
