package com.blueblood.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "注册请求")
public class RegisterRequest {

    @Schema(description = "用户名(username 注册必填; email/phone 注册可不传,由后端自动设置)")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需 6-32 位")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "注册方式: username(默认) / email / phone")
    private String registerType;

    @Schema(description = "邮箱(email 注册时必填)")
    private String email;

    @Schema(description = "验证码(email/phone 注册时必填)")
    private String code;
}
