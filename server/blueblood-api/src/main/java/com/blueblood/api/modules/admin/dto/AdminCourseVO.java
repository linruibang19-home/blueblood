package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台课程列表/详情 VO。
 */
@Data
@Schema(description = "后台课程 VO")
public class AdminCourseVO {

    private Long id;
    private String title;
    private String subtitle;
    private String coverImage;
    private String instructor;
    private String instructorAvatar;
    private Integer totalChapters;
    private Integer rewardPoints;
    private Integer students;
    private BigDecimal rating;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
