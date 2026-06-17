package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台帖子查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台帖子查询")
public class AdminPostQuery extends PageQuery {

    @Schema(description = "关键字(标题/正文)")
    private String keyword;

    @Schema(description = "小组ID")
    private Long groupId;

    @Schema(description = "标签：话题/任务/经验分享/活动")
    private String tag;

    @Schema(description = "状态：PUBLISHED / DRAFT / HIDDEN")
    private String status;
}
