package com.blueblood.api.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 任务主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task")
public class Task extends BaseEntity {

    private String title;

    private Long categoryId;

    /** 分类名称(冗余) */
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

    /** DRAFT/PENDING_REVIEW/APPROVED/RECRUITING/IN_PROGRESS/COMPLETED/CLOSED */
    private String status;

    private Integer viewCount;
}
