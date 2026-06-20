package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台某角色下的用户视图。
 */
@Data
@Schema(description = "后台角色用户视图")
public class AdminRoleUserVO {

    private Long userId;

    private String nickname;

    private String username;
}
