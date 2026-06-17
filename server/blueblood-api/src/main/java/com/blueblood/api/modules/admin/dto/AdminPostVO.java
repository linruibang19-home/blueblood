package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台帖子视图。
 */
@Data
@Schema(description = "后台帖子视图")
public class AdminPostVO {

    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "所属小组ID")
    private Long groupId;

    @Schema(description = "所属小组名称")
    private String groupName;

    @Schema(description = "作者用户ID")
    private Long authorId;

    @Schema(description = "作者昵称")
    private String authorNickname;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "正文")
    private String content;

    @Schema(description = "图片列表，JSON 字符串")
    private String images;

    @Schema(description = "话题标签：话题/任务/经验分享/活动")
    private String tag;

    @Schema(description = "点赞数")
    private Integer likes;

    @Schema(description = "评论数")
    private Integer comments;

    @Schema(description = "浏览数")
    private Integer views;

    @Schema(description = "状态：PUBLISHED / DRAFT / HIDDEN")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
