package com.blueblood.api.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新系统配置（管理员）")
public class UpdateConfigRequest {

    @Schema(description = "配置值")
    @NotBlank(message = "配置值不能为空")
    private String configValue;
}
