package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台课程查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台课程查询")
public class AdminCourseQuery extends PageQuery {

    @Schema(description = "关键字(标题/副标题)")
    private String keyword;

    @Schema(description = "状态：PUBLISHED / DRAFT / OFFLINE")
    private String status;
}
