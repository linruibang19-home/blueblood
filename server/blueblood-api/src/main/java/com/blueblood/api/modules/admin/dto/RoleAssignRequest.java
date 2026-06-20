package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色分配/撤销请求。
 */
@Data
@Schema(description = "角色分配/撤销请求")
public class RoleAssignRequest {

    @Schema(description = "用户ID")
    @NotNull(message = "用户不能为空")
    private Long userId;

    @Schema(description = "角色编码: USER/ADMIN")
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
}
