package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台兴趣小组视图。
 */
@Data
@Schema(description = "后台小组视图")
public class AdminGroupVO {

    @Schema(description = "小组ID")
    private Long id;

    @Schema(description = "小组名称")
    private String name;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "组长用户ID")
    private Long leaderId;

    @Schema(description = "组长昵称")
    private String leaderNickname;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "标签，JSON 字符串")
    private String tags;

    @Schema(description = "成员数")
    private Integer memberCount;

    @Schema(description = "帖子数")
    private Integer postCount;

    @Schema(description = "活动数")
    private Integer activityCount;

    @Schema(description = "状态：ACTIVE / INACTIVE")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
