package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台任务接单查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台任务接单查询")
public class AdminTaskOrderQuery extends PageQuery {

    @Schema(description = "任务 ID")
    private Long taskId;

    @Schema(description = "订单状态")
    private String status;
}
