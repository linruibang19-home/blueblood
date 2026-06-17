package com.blueblood.api.modules.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动报名记录 (activity_id + user_id 唯一)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_signup")
public class ActivitySignup extends BaseEntity {

    private Long activityId;

    private Long userId;

    /** 报名状态：SIGNED / CANCELLED / ATTENDED */
    private String status;
}
