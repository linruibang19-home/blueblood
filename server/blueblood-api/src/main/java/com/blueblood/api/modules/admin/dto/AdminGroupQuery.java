package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台小组查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台小组查询")
public class AdminGroupQuery extends PageQuery {

    @Schema(description = "关键字(小组名/简介)")
    private String keyword;

    @Schema(description = "状态：ACTIVE / INACTIVE")
    private String status;
}
