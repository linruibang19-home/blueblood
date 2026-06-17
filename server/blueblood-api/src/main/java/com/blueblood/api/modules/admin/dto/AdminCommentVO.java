package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台评论视图。
 */
@Data
@Schema(description = "后台评论视图")
public class AdminCommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "所属帖子ID")
    private Long postId;

    @Schema(description = "作者用户ID")
    private Long authorId;

    @Schema(description = "作者昵称")
    private String authorNickname;

    @Schema(description = "父评论ID，一级评论为 null")
    private Long parentId;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likes;

    @Schema(description = "状态：NORMAL / DELETED")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
