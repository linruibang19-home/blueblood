package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 后台任务列表/详情 VO。
 */
@Data
@Schema(description = "后台任务 VO")
public class AdminTaskVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
