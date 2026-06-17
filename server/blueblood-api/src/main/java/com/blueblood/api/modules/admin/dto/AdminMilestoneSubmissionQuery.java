package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台里程碑提交查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台里程碑提交查询")
public class AdminMilestoneSubmissionQuery extends PageQuery {

    @Schema(description = "里程碑状态(默认 SUBMITTED)")
    private String status;
}
