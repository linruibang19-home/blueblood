package com.blueblood.api.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论视图（含子回复）
 */
@Data
@Schema(description = "评论视图")
public class CommentVO {

    private Long id;
    private Long postId;
    private Long parentId;

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private String content;
    private Integer likes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "子回复列表（仅一级评论携带）")
    private List<CommentVO> replies;
}
