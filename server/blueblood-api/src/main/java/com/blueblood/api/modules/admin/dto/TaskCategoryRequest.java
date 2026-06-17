package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增/编辑任务分类。
 */
@Data
@Schema(description = "任务分类请求")
public class TaskCategoryRequest {

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer categoryOrder;

    @Schema(description = "状态: ACTIVE/INACTIVE")
    private String status;
}
