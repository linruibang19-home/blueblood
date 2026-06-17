package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台作业提交 VO，携带 studentNickname / assignmentTitle / score。
 */
@Data
@Schema(description = "后台作业提交 VO")
public class AdminSubmissionVO {

    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long userId;
    private String studentNickname;
    private String content;
    private String attachments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    /** submitted / graded */
    private String status;

    /** 已评分则有值 */
    private BigDecimal score;
    private String feedback;
}
