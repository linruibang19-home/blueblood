package com.blueblood.api.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户信誉分变动日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_credit_log")
public class UserCreditLog extends BaseEntity {

    private Long userId;

    private BigDecimal delta;

    private BigDecimal afterScore;

    private String reason;
}
