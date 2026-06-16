package com.blueblood.api.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送私信请求。
 */
@Data
@Schema(description = "发送私信请求")
public class SendMessageRequest {

    @Schema(description = "接收者用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "receiverId 不能为空")
    private Long receiverId;

    @Schema(description = "消息正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "content 不能为空")
    @Size(max = 2000, message = "消息内容过长")
    private String content;

    @Schema(description = "消息类型：text / image / system，默认 text")
    private String type;
}
