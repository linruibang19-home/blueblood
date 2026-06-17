package com.blueblood.api.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 里程碑提交记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("milestone_submission")
public class MilestoneSubmission extends BaseEntity {

    private Long milestoneId;

    private Long orderId;

    private Long userId;

    private String githubUrl;

    private String description;

    /** 附件列表 JSON */
    private String attachments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;
}
