package com.blueblood.api.modules.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "任务分类")
public class TaskCategoryVO {
    private Long id;
    private String name;
    private String icon;
    private Integer taskCount;
    private Integer categoryOrder;
}
