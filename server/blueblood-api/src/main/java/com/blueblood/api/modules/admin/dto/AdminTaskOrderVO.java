package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台任务接单 VO，携带 username/nickname + taskTitle。
 */
@Data
@Schema(description = "后台任务接单 VO")
public class AdminTaskOrderVO {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long userId;
    private String username;
    private String nickname;
    private Integer progress;
    private String status;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
