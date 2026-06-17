package com.blueblood.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信小程序登录(wx.login 的 code)")
public class WxLoginRequest {

    @Schema(description = "wx.login 返回的临时登录凭证 code")
    @NotBlank(message = "code 不能为空")
    private String code;

    @Schema(description = "可选：用户昵称(授权后传入)")
    private String nickname;

    @Schema(description = "可选：头像URL")
    private String avatar;
}
