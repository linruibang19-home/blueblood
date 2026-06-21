package com.blueblood.api.modules.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 雇主待审核里程碑(雇主工作台审核列表项)。
 */
@Data
@Schema(description = "雇主待审核里程碑")
public class MilestoneReviewVO {

    private Long milestoneId;
    private Long orderId;
    private Long taskId;
    private String taskTitle;

    private String milestoneTitle;
    private String description;
    private Integer milestoneOrder;
    private BigDecimal reward;
    /** NOT_STARTED/SUBMITTED/APPROVED/REJECTED */
    private String status;

    private Long workerId;
    private String workerName;

    private String githubUrl;
    private String submissionDesc;
    /** 附件 JSON 数组字符串 */
    private String attachments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;
}
