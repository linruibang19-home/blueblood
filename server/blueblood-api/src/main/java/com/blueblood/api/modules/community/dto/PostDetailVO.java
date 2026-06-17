package com.blueblood.api.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情
 */
@Data
@Schema(description = "帖子详情")
public class PostDetailVO {

    private Long id;
    private Long groupId;

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private String title;
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> images;

    private String tag;

    private Integer likes;
    private Integer comments;
    private Integer views;

    @Schema(description = "当前用户是否已点赞")
    private Boolean liked;

    @Schema(description = "当前用户是否已收藏")
    private Boolean favorited;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
