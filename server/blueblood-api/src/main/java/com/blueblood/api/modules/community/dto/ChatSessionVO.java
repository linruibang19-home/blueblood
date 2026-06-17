package com.blueblood.api.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表项视图（当前用户视角）。
 */
@Data
@Schema(description = "会话列表项")
public class ChatSessionVO {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "对方用户ID")
    private Long peerId;

    @Schema(description = "对方昵称")
    private String peerName;

    @Schema(description = "对方头像")
    private String peerAvatar;

    @Schema(description = "最近一条消息摘要")
    private String lastMessage;

    @Schema(description = "最近一条消息时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数")
    private Integer unreadCount;
}
