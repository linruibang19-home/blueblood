package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置 / 字典 / 技能 / 日志 通用分页查询。
 * 各字段按需使用，未填即忽略。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统管理通用查询")
public class AdminSystemQuery extends PageQuery {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "字典类型(dict 用)")
    private String dictType;

    @Schema(description = "模块(log 用)")
    private String module;

    @Schema(description = "状态(log 用): SUCCESS/FAIL")
    private String status;
}
