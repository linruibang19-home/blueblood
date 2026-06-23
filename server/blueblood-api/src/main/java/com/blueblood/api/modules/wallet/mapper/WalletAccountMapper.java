package com.blueblood.api.modules.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blueblood.api.modules.wallet.entity.WalletAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface WalletAccountMapper extends BaseMapper<WalletAccount> {

    /** 原子加余额(入账):balance += amount, total_earned += amount。行锁保证无丢失更新。 */
    @Update("UPDATE wallet_account SET balance = balance + #{amount}, total_earned = total_earned + #{amount} "
            + "WHERE user_id = #{userId}")
    int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /** 原子扣余额(提现冻结):仅当 balance >= amount 才扣,返回影响行数(0=余额不足)。 */
    @Update("UPDATE wallet_account SET balance = balance - #{amount} "
            + "WHERE user_id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
