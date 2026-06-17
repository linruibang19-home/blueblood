package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增/编辑小组请求体。
 */
@Data
@Schema(description = "新增/编辑小组")
public class GroupRequest {

    @Schema(description = "小组名称")
    @NotBlank(message = "小组名称不能为空")
    private String name;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "组长用户ID")
    private Long leaderId;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "标签(JSON 字符串)")
    private String tags;

    @Schema(description = "状态：ACTIVE / INACTIVE")
    private String status;
}
