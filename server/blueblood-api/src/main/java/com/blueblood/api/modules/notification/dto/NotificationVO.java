package com.blueblood.api.modules.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知视图（含已读状态）。
 */
@Data
@Schema(description = "通知视图")
public class NotificationVO {

    private Long id;

    @Schema(description = "类型：milestone/task_review/income/system/group/course")
    private String type;

    private String title;

    private String content;

    @Schema(description = "跳转链接")
    private String link;

    @Schema(description = "是否已读（当前用户）")
    private Boolean read;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
