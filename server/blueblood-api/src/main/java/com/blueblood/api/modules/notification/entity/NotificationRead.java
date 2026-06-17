package com.blueblood.api.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知已读表（UNIQUE notification_id + user_id）。
 * 一条 notification_read 记录存在即表示该用户已读对应通知。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_read")
public class NotificationRead extends BaseEntity {

    private Long notificationId;

    private Long userId;

    /** 阅读时间 */
    private LocalDateTime readAt;
}
