package com.blueblood.api.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子列表项（轻量）
 */
@Data
@Schema(description = "帖子列表项")
public class PostListItemVO {

    private Long id;
    private Long groupId;

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private String title;

    @Schema(description = "正文摘要")
    private String content;

    private String tag;

    private Integer likes;
    private Integer comments;
    private Integer views;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
