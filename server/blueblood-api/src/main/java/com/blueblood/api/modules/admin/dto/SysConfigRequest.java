package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增/编辑系统配置。
 */
@Data
@Schema(description = "系统配置请求")
public class SysConfigRequest {

    @Schema(description = "配置键(新增必填，编辑可不传)")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "类型: string/number/boolean/json")
    private String configType;

    @Schema(description = "名称")
    private String label;

    @Schema(description = "备注")
    private String remark;
}
