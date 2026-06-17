package com.blueblood.api.modules.assignment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 作业批改成绩 (submission_id 唯一)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment_grade")
public class AssignmentGrade extends BaseEntity {

    private Long submissionId;

    private Long assignmentId;

    private Long userId;

    /** 批改人 ID，可为空 */
    private Long graderId;

    /** 分数，0-100 */
    private BigDecimal score;

    /** 批改反馈 */
    private String feedback;
}
