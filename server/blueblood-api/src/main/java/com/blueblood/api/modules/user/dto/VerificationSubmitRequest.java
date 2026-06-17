package com.blueblood.api.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "提交认证申请")
public class VerificationSubmitRequest {

    @Schema(description = "真实姓名")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "证件号(脱敏)")
    private String idNumber;

    @Schema(description = "申请材料URL列表")
    private List<String> materials;
}
