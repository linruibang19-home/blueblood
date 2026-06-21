package com.blueblood.api.modules.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "里程碑（含提交/审核摘要）")
public class MilestoneVO {
    private Long id;
    private Long orderId;
    private Long taskId;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private Integer milestoneOrder;
    /** NOT_STARTED/IN_PROGRESS/SUBMITTED/APPROVED/REJECTED/OVERDUE */
    private String status;

    /** 里程碑酬金 */
    private BigDecimal reward;

    private SubmissionSummary submission;
    private ReviewSummary review;

    @Data
    @Schema(description = "提交摘要")
    public static class SubmissionSummary {
        private Long id;
        private String githubUrl;
        private String description;
        private List<String> attachments;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime submittedAt;
    }

    @Data
    @Schema(description = "审核摘要")
    public static class ReviewSummary {
        /** PENDING/APPROVED/REJECTED */
        private String result;
        private String feedback;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reviewedAt;
    }
}
