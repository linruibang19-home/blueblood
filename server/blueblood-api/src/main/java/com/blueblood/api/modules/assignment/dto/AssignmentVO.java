package com.blueblood.api.modules.assignment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业视图对象：列表项 + 详情统一结构。
 * 列表场景仅填充 id/title/deadline/status/score；
 * 详情场景额外填充 description/submission/grade。
 */
@Data
@Schema(description = "作业视图对象")
public class AssignmentVO {

    @Schema(description = "作业ID")
    private Long id;

    @Schema(description = "作业标题")
    private String title;

    @Schema(description = "作业描述")
    private String description;

    @Schema(description = "截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    @Schema(description = "参考答案（仅批改结果接口在已评分时返回）")
    private String answer;

    /** 当前用户作业状态：not_submitted / submitted / graded */
    @Schema(description = "当前用户作业状态：not_submitted / submitted / graded")
    private String status;

    @Schema(description = "当前用户成绩（已评分时返回）")
    private BigDecimal score;

    @Schema(description = "当前用户提交记录")
    private SubmissionDTO submission;

    @Schema(description = "当前用户批改结果")
    private GradeDTO grade;

    /**
     * 列表项构造（仅核心字段 + 当前用户状态/成绩）。
     */
    @Data
    @Schema(description = "提交记录")
    public static class SubmissionDTO {
        @Schema(description = "提交ID")
        private Long id;
        @Schema(description = "提交正文")
        private String content;
        @Schema(description = "附件列表")
        private List<String> attachments;
        @Schema(description = "提交时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime submittedAt;
        @Schema(description = "提交状态：submitted / graded")
        private String status;
    }

    @Data
    @Schema(description = "批改结果")
    public static class GradeDTO {
        @Schema(description = "成绩ID")
        private Long id;
        @Schema(description = "分数 0-100")
        private BigDecimal score;
        @Schema(description = "批改反馈")
        private String feedback;
    }
}
