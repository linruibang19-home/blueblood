package com.blueblood.api.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务接单(订单)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_order")
public class TaskOrder extends BaseEntity {

    private Long taskId;

    private Long userId;

    /** 完成进度 0-100 */
    private Integer progress;

    /** applied/accepted/in_progress/wait_acceptance/passed/rejected/settling/settled */
    private String status;

    private String remark;
}
