package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台黑客松视图(含发布人昵称)。
 */
@Data
@Schema(description = "后台黑客松视图")
public class AdminHackathonVO {

    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private BigDecimal prizePool;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signupDeadline;

    private String location;
    private Integer maxTeams;
    private Integer currentTeams;

    @Schema(description = "状态: signup/ongoing/ended")
    private String status;

    private Long publishedBy;

    @Schema(description = "发布人昵称")
    private String publisherNickname;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
