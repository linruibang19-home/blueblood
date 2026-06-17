package com.blueblood.api.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_skill")
public class TaskSkill extends BaseEntity {
    private Long taskId;
    private String name;
}
