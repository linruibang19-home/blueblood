package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台角色视图(含用户数)。
 */
@Data
@Schema(description = "后台角色视图")
public class AdminRoleVO {

    private Long id;

    @Schema(description = "角色编码: USER/ADMIN")
    private String code;

    private String name;

    private String description;

    @Schema(description = "状态: ACTIVE/INACTIVE")
    private String status;

    @Schema(description = "该角色下的用户数")
    private Long userCount;
}
