package com.blueblood.api.modules.enterprise.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业发布/编辑黑客松请求体。
 */
@Data
@Schema(description = "发布/编辑黑客松")
public class HackathonRequest {

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "封面图 URL")
    private String coverImage;

    @Schema(description = "奖金池")
    private BigDecimal prizePool;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报名截止时间")
    private LocalDateTime signupDeadline;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "最大参赛队伍数")
    private Integer maxTeams;

    @Schema(description = "当前已报名队伍数")
    private Integer currentTeams;

    @Schema(description = "状态：signup/ongoing/ended")
    private String status;
}
