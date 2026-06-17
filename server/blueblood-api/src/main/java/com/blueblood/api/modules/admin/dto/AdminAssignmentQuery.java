package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台作业查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台作业查询")
public class AdminAssignmentQuery extends PageQuery {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "关键字(标题/描述)")
    private String keyword;

    @Schema(description = "状态：not_submitted / submitted / graded")
    private String status;
}
