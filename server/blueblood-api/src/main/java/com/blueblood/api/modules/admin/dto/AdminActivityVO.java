package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台活动视图。
 */
@Data
@Schema(description = "后台活动视图")
public class AdminActivityVO {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "所属小组ID")
    private Long groupId;

    @Schema(description = "所属小组名称")
    private String groupName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "已报名人数")
    private Integer signupCount;

    @Schema(description = "人数上限，0 表示不限")
    private Integer maxCount;

    @Schema(description = "状态：upcoming / ongoing / ended")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
