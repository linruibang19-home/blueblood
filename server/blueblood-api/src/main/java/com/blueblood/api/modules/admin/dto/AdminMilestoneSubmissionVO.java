package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台里程碑提交 VO，携带 taskTitle/milestoneTitle/userNickname + 提交字段。
 */
@Data
@Schema(description = "后台里程碑提交 VO")
public class AdminMilestoneSubmissionVO {

    private Long id;
    private Long milestoneId;
    private Long orderId;
    private Long taskId;
    private String taskTitle;
    private String milestoneTitle;
    private Long userId;
    private String userNickname;

    private String githubUrl;
    private String description;
    private String attachments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    /** 里程碑当前状态(SUBMITTED/APPROVED/REJECTED 等) */
    private String status;
}
