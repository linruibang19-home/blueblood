package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台通知查询(分页 + 筛选)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台通知查询")
public class AdminNotificationQuery extends PageQuery {

    @Schema(description = "接收用户ID")
    private Long userId;

    @Schema(description = "通知类型: milestone/task_review/income/system/group/course")
    private String type;

    @Schema(description = "关键字(标题/内容)")
    private String keyword;
}
