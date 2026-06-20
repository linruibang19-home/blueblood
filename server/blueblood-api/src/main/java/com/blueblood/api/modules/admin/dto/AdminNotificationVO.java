package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台通知列表/详情视图。
 */
@Data
@Schema(description = "后台通知视图")
public class AdminNotificationVO {

    private Long id;

    @Schema(description = "接收用户ID")
    private Long userId;

    @Schema(description = "接收人昵称")
    private String recipientNickname;

    @Schema(description = "通知类型")
    private String type;

    private String title;

    private String content;

    private String link;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
