package com.blueblood.api.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动视图对象（列表项 / 详情通用）
 */
@Data
@Schema(description = "活动视图对象")
public class ActivityVO implements Serializable {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "所属小组ID")
    private Long groupId;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "活动地点")
    private String location;

    @Schema(description = "已报名人数")
    private Integer signupCount;

    @Schema(description = "人数上限，0 表示不限")
    private Integer maxCount;

    @Schema(description = "状态：upcoming/ongoing/ended")
    private String status;

    @Schema(description = "当前用户是否已报名（仅详情返回）")
    private Boolean signedUp;
}
