package com.blueblood.api.modules.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "任务详情")
public class TaskVO {
    private Long id;
    private String title;
    private Long categoryId;
    private String category;
    private Long employerId;
    private String employerName;
    private String description;
    private BigDecimal reward;
    private Integer levelRequired;
    private Integer totalSlots;
    private Integer slotsLeft;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    private String status;
    private Integer viewCount;

    @Schema(description = "技能要求")
    private List<String> skills;

    @Schema(description = "当前用户是否已接单")
    private Boolean accepted;
}
