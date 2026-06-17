package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台活动查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台活动查询")
public class AdminActivityQuery extends PageQuery {

    @Schema(description = "小组ID")
    private Long groupId;

    @Schema(description = "关键字(标题/地点)")
    private String keyword;
}
