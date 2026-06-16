package com.blueblood.api.modules.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 私信消息。归属某个 ChatSession。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class ChatMessage extends BaseEntity {

    /** 所属会话ID */
    private Long sessionId;

    /** 发送者ID */
    private Long senderId;

    /** 接收者ID */
    private Long receiverId;

    /** 消息正文 */
    private String content;

    /** 消息类型：text / image / system，默认 text */
    private String type;

    /** 是否已读：0 未读 1 已读 */
    private Integer isRead;
}
