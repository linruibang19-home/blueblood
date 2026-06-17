package com.blueblood.api.modules.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 钱包流水：收入/支出/提现记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_record")
public class WalletRecord extends BaseEntity {

    private Long userId;

    /** 类型：income / expense / withdraw */
    private String type;

    private BigDecimal amount;

    /** 状态：pending / available / withdrawing / withdrawn / failed */
    private String status;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 业务类型 */
    private String bizType;

    /** 关联业务ID（可空） */
    private Long bizId;
}
