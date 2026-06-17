package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台任务查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台任务查询")
public class AdminTaskQuery extends PageQuery {

    @Schema(description = "关键字(标题/雇主)")
    private String keyword;

    @Schema(description = "分类 ID")
    private Long category;

    @Schema(description = "任务状态")
    private String status;
}
