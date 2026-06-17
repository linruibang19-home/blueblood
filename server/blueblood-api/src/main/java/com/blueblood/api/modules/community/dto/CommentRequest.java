package com.blueblood.api.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发表评论请求
 */
@Data
@Schema(description = "发表评论请求")
public class CommentRequest {

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "评论内容不能为空")
    private String content;

    @Schema(description = "父评论ID，一级评论不传")
    private Long parentId;
}
