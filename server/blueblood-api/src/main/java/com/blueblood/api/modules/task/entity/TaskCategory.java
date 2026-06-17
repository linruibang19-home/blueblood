package com.blueblood.api.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_category")
public class TaskCategory extends BaseEntity {
    private String name;
    private String icon;
    private Integer taskCount;
    private Integer categoryOrder;
    private String status;
}
