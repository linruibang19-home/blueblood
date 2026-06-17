package com.blueblood.api.modules.assignment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 作业提交记录 (assignment_id + user_id 唯一)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment_submission")
public class AssignmentSubmission extends BaseEntity {

    private Long assignmentId;

    private Long userId;

    private String content;

    /** 附件列表，JSON 列，按字符串映射 */
    private String attachments;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    /** 状态：submitted / graded */
    private String status;
}
