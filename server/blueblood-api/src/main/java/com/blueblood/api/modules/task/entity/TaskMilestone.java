package com.blueblood.api.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 任务里程碑
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_milestone")
public class TaskMilestone extends BaseEntity {

    private Long orderId;

    private Long taskId;

    private String title;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private Integer milestoneOrder;

    /** NOT_STARTED/IN_PROGRESS/SUBMITTED/APPROVED/REJECTED/OVERDUE */
    private String status;
}
