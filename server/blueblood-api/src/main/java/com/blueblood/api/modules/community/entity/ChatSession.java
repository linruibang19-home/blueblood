package com.blueblood.api.modules.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 私信会话。
 * 单向维护：每条记录代表 user_id 视角下与 peer_id 的会话。
 * UNIQUE(user_id, peer_id) 保证同一方向唯一。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_session")
public class ChatSession extends BaseEntity {

    /** 会话所属用户（当前视角） */
    private Long userId;

    /** 对方用户ID */
    private Long peerId;

    /** 最近一条消息内容摘要（varchar 500） */
    private String lastMessage;

    /** 最近一条消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 会话状态：ACTIVE 等，默认 ACTIVE */
    private String status;
}
