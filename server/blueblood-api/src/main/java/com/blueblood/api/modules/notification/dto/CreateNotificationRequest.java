package com.blueblood.api.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建系统通知请求（管理员预留）。
 */
@Data
@Schema(description = "创建系统通知请求")
public class CreateNotificationRequest {

    @Schema(description = "接收用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "类型：milestone/task_review/income/system/group/course")
    private String type;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "跳转链接")
    private String link;
}
