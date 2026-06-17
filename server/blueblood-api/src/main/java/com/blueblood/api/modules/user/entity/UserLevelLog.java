package com.blueblood.api.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户等级变动日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_level_log")
public class UserLevelLog extends BaseEntity {

    private Long userId;

    private Integer fromLevel;

    private Integer toLevel;

    private String reason;
}
