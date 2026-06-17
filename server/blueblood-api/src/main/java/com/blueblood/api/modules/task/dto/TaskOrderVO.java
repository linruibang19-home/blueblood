package com.blueblood.api.modules.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务执行详情 / 我的任务项。
 */
@Data
@Schema(description = "任务订单(执行详情)")
public class TaskOrderVO {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private String category;
    private String employerName;
    private BigDecimal reward;

    @Schema(description = "整体进度 0-100")
    private Integer progress;

    /** applied/accepted/in_progress/wait_acceptance/passed/rejected/settling/settled */
    private String status;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "里程碑列表")
    private List<MilestoneVO> milestones;
}
