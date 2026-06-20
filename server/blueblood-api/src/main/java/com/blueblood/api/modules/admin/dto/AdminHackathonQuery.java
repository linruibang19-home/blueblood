package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台黑客松查询(分页 + 筛选)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台黑客松查询")
public class AdminHackathonQuery extends PageQuery {

    @Schema(description = "关键字(标题/地点)")
    private String keyword;

    @Schema(description = "状态: signup/ongoing/ended")
    private String status;
}
