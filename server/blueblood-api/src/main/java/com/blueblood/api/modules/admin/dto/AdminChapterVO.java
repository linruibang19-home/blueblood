package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台章节 VO，携带 courseTitle。
 */
@Data
@Schema(description = "后台章节 VO")
public class AdminChapterVO {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String duration;
    private String videoUrl;
    private Integer chapterOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
