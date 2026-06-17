package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增/编辑字典项。
 */
@Data
@Schema(description = "字典项请求")
public class SysDictRequest {

    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @Schema(description = "字典键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典键不能为空")
    private String dictKey;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "名称")
    private String label;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态: ACTIVE/INACTIVE")
    private String status;
}
