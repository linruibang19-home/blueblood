package com.blueblood.api.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知主表（按用户维度，user_id 为接收用户）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    /** 接收用户ID */
    private Long userId;

    /** 类型：milestone/task_review/income/system/group/course */
    private String type;

    private String title;

    private String content;

    /** 跳转链接 */
    private String link;
}
