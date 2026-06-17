package com.blueblood.api.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 里程碑审核记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("milestone_review")
public class MilestoneReview extends BaseEntity {

    private Long milestoneId;

    private Long submissionId;

    private Long reviewerId;

    /** PENDING/APPROVED/REJECTED */
    private String result;

    private String feedback;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;
}
