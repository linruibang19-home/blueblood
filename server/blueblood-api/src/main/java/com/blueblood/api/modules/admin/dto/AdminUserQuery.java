package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台用户查询(分页 + 筛选)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台用户查询")
public class AdminUserQuery extends PageQuery {

    @Schema(description = "关键字(用户名/昵称/手机号)")
    private String keyword;

    @Schema(description = "账号状态: ACTIVE/INACTIVE/BANNED")
    private String status;

    @Schema(description = "认证状态: 0-未认证 1-已认证")
    private Integer verified;
}
