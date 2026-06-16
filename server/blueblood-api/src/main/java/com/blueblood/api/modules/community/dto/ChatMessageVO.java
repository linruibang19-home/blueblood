package com.blueblood.api.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息视图。
 */
@Data
@Schema(description = "私信消息")
public class ChatMessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "消息正文")
    private String content;

    @Schema(description = "消息类型：text / image / system")
    private String type;

    @Schema(description = "是否已读：0 未读 1 已读")
    private Integer isRead;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
