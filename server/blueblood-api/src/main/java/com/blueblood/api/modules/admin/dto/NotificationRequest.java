package com.blueblood.api.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台发送通知请求。
 */
@Data
@Schema(description = "发送通知请求")
public class NotificationRequest {

    @Schema(description = "接收用户ID(必填)")
    @NotNull(message = "接收用户不能为空")
    private Long userId;

    @Schema(description = "通知类型: milestone/task_review/income/system/group/course")
    @NotBlank(message = "通知类型不能为空")
    private String type;

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "跳转链接")
    private String link;
}
