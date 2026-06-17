package com.blueblood.api.modules.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blueblood.api.modules.wallet.entity.WalletAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletAccountMapper extends BaseMapper<WalletAccount> {
}
