package com.blueblood.api.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发布帖子请求
 */
@Data
@Schema(description = "发布帖子请求")
public class CreatePostRequest {

    @Schema(description = "所属小组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "小组ID不能为空")
    private Long groupId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> images;

    @Schema(description = "话题标签：话题/任务/经验分享/活动")
    private String tag;
}
