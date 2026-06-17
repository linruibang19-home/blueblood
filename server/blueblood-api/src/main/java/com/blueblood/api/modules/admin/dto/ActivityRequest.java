package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新增/编辑活动请求体。
 */
@Data
@Schema(description = "新增/编辑活动")
public class ActivityRequest {

    @Schema(description = "所属小组ID")
    private Long groupId;

    @Schema(description = "标题")
    @NotBlank(message = "活动标题不能为空")
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

    @Schema(description = "人数上限，0 表示不限")
    private Integer maxCount;

    @Schema(description = "状态：upcoming / ongoing / ended")
    private String status;
}
