package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调整等级/积分请求。
 */
@Data
@Schema(description = "调整等级积分")
public class AdjustUserRequest {

    @Schema(description = "新等级")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "积分变化量(正负)")
    private Integer pointsDelta;

    @Schema(description = "变动原因(写入等级/积分日志)")
    @NotNull(message = "原因不能为空")
    private String reason;
}
