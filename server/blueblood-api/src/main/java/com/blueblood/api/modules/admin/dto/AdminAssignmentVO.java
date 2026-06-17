package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台作业 VO，携带 courseTitle 与参考答案。
 */
@Data
@Schema(description = "后台作业 VO")
public class AdminAssignmentVO {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long chapterId;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    private String answer;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
