package com.blueblood.api.modules.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现申请记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("withdraw_record")
public class WithdrawRecord extends BaseEntity {

    private Long userId;

    private BigDecimal amount;

    /** 状态：pending / processing / completed / failed */
    private String status;

    private String bankName;

    /** 银行卡号（脱敏） */
    private String bankAccount;

    /** 失败原因 */
    private String failureReason;

    /** 处理时间（可空） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;
}
