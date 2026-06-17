package com.blueblood.api.modules.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "任务列表项")
public class TaskListItemVO {
    private Long id;
    private String title;
    private String category;
    private String employerName;
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
}
