package com.blueblood.api.modules.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 钱包账户：一个用户一个账户，user_id 唯一。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_account")
public class WalletAccount extends BaseEntity {

    /** 持有人用户ID（唯一） */
    private Long userId;

    /** 可用余额 */
    private BigDecimal balance;

    /** 待结算金额 */
    private BigDecimal pendingAmount;

    /** 已提现金额 */
    private BigDecimal withdrawnAmount;

    /** 累计收益 */
    private BigDecimal totalEarned;

    /** 状态：ACTIVE / FROZEN */
    private String status;
}
