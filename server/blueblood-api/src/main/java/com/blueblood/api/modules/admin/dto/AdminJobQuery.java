package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台岗位查询(分页 + 筛选)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台岗位查询")
public class AdminJobQuery extends PageQuery {

    @Schema(description = "关键字(标题/公司)")
    private String keyword;

    @Schema(description = "状态: ACTIVE/CLOSED")
    private String status;

    @Schema(description = "类型: 实习/兼职/全职/外包")
    private String type;
}
