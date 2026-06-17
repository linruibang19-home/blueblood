package com.blueblood.api.modules.wallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.wallet.dto.WalletRecordVO;
import com.blueblood.api.modules.wallet.dto.WalletVO;
import com.blueblood.api.modules.wallet.dto.WithdrawRecordVO;
import com.blueblood.api.modules.wallet.dto.WithdrawRequest;
import com.blueblood.api.modules.wallet.entity.WalletAccount;
import com.blueblood.api.modules.wallet.entity.WalletRecord;
import com.blueblood.api.modules.wallet.entity.WithdrawRecord;
import com.blueblood.api.modules.wallet.mapper.WalletAccountMapper;
import com.blueblood.api.modules.wallet.mapper.WalletRecordMapper;
import com.blueblood.api.modules.wallet.mapper.WithdrawRecordMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 钱包服务：账户概要、流水查询、待结算列表、提现记录、提现申请。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletAccountMapper walletAccountMapper;
    private final WalletRecordMapper walletRecordMapper;
    private final WithdrawRecordMapper withdrawRecordMapper;

    // ============================== 概要 ==============================

    /**
     * 钱包概要。当前用户无账户时自动创建空账户（balance=0）。
     */
    public WalletVO summary() {
        Long userId = SecurityUtils.currentUserId();
        WalletAccount account = getOrCreateAccount(userId);
        return toWalletVO(account);
    }

    // ============================== 流水分页 ==============================

    /**
     * 钱包流水分页，可按 type / status 过滤，createdAt desc。
     */
    public PageResult<WalletRecordVO> pageRecords(Integer page, Integer pageSize, String type, String status) {
        Long userId = SecurityUtils.currentUserId();
        Page<WalletRecord> p = toPage(page, pageSize);
        Page<WalletRecord> result = walletRecordMapper.selectPage(p, new LambdaQueryWrapper<WalletRecord>()
                .eq(WalletRecord::getUserId, userId)
                .eq(type != null && !type.isBlank(), WalletRecord::getType, type)
                .eq(status != null && !status.isBlank(), WalletRecord::getStatus, status)
                .isNull(WalletRecord::getDeletedAt)
                .orderByDesc(WalletRecord::getCreatedAt));

        return toPageResult(result, this::toRecordVO);
    }

    // ============================== 待结算列表 ==============================

    /**
     * 待结算列表（status='pending'），createdAt desc。
     */
    public PageResult<WalletRecordVO> pagePending(Integer page, Integer pageSize) {
        Long userId = SecurityUtils.currentUserId();
        Page<WalletRecord> p = toPage(page, pageSize);
        Page<WalletRecord> result = walletRecordMapper.selectPage(p, new LambdaQueryWrapper<WalletRecord>()
                .eq(WalletRecord::getUserId, userId)
                .eq(WalletRecord::getStatus, "pending")
                .isNull(WalletRecord::getDeletedAt)
                .orderByDesc(WalletRecord::getCreatedAt));

        return toPageResult(result, this::toRecordVO);
    }

    // ============================== 提现记录分页 ==============================

    /**
     * 提现记录分页，createdAt desc。
     */
    public PageResult<WithdrawRecordVO> pageWithdraws(Integer page, Integer pageSize) {
        Long userId = SecurityUtils.currentUserId();
        Page<WithdrawRecord> p = toPageWithdraw(page, pageSize);
        Page<WithdrawRecord> result = withdrawRecordMapper.selectPage(p, new LambdaQueryWrapper<WithdrawRecord>()
                .eq(WithdrawRecord::getUserId, userId)
                .isNull(WithdrawRecord::getDeletedAt)
                .orderByDesc(WithdrawRecord::getCreatedAt));

        return toPageResult(result, this::toWithdrawVO);
    }

    // ============================== 创建提现申请 ==============================

    /**
     * 创建提现申请：
     * 1) 校验 amount > 0 且 balance >= amount；
     * 2) balance -= amount（冻结）；
     * 3) 建 withdraw_record(status=pending)；
     * 4) 建 wallet_record(type=withdraw, status=withdrawing)。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createWithdraw(WithdrawRequest req) {
        Long userId = SecurityUtils.currentUserId();
        BigDecimal amount = req.getAmount();

        // 校验：amount > 0（@DecimalMin 已校验非空且 >= 0.01，这里做二次保护）
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "提现金额须大于0");
        }

        WalletAccount account = getOrCreateAccount(userId);

        // 账户状态校验
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "账户已被冻结，无法提现");
        }

        // 余额校验
        BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        if (balance.compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "余额不足");
        }

        // 1. 扣减余额（冻结）
        BigDecimal newBalance = balance.subtract(amount);
        account.setBalance(newBalance);
        walletAccountMapper.updateById(account);

        // 2. 创建提现记录
        WithdrawRecord withdraw = new WithdrawRecord();
        withdraw.setUserId(userId);
        withdraw.setAmount(amount);
        withdraw.setStatus("pending");
        withdraw.setBankName(req.getBankName());
        withdraw.setBankAccount(req.getBankAccount());
        withdrawRecordMapper.insert(withdraw);

        // 3. 创建钱包流水
        WalletRecord record = new WalletRecord();
        record.setUserId(userId);
        record.setType("withdraw");
        record.setAmount(amount);
        record.setStatus("withdrawing");
        record.setTitle("提现申请");
        record.setDescription("提现至 " + nullSafe(req.getBankName()));
        record.setBizType("withdraw");
        record.setBizId(withdraw.getId());
        walletRecordMapper.insert(record);

        return withdraw.getId();
    }

    // ============================== 内部 ==============================

    /**
     * 获取当前用户账户，不存在则创建空账户（balance=0, ACTIVE）。
     */
    private WalletAccount getOrCreateAccount(Long userId) {
        // 注意：user_id 唯一，且 deletedAt 在 BaseEntity 中 select=false，
        // 这里用 LambdaQueryWrapper 显式 isNull 以确保拿到未软删的账户。
        WalletAccount account = walletAccountMapper.selectOne(new LambdaQueryWrapper<WalletAccount>()
                .eq(WalletAccount::getUserId, userId)
                .isNull(WalletAccount::getDeletedAt)
                .last("LIMIT 1"));

        if (account == null) {
            account = new WalletAccount();
            account.setUserId(userId);
            account.setBalance(BigDecimal.ZERO);
            account.setPendingAmount(BigDecimal.ZERO);
            account.setWithdrawnAmount(BigDecimal.ZERO);
            account.setTotalEarned(BigDecimal.ZERO);
            account.setStatus("ACTIVE");
            walletAccountMapper.insert(account);
        }
        return account;
    }

    private WalletVO toWalletVO(WalletAccount account) {
        WalletVO vo = new WalletVO();
        vo.setId(account.getId());
        vo.setBalance(account.getBalance());
        vo.setPendingAmount(account.getPendingAmount());
        vo.setWithdrawnAmount(account.getWithdrawnAmount());
        vo.setTotalEarned(account.getTotalEarned());
        vo.setStatus(account.getStatus());
        return vo;
    }

    private WalletRecordVO toRecordVO(WalletRecord r) {
        WalletRecordVO vo = new WalletRecordVO();
        vo.setId(r.getId());
        vo.setType(r.getType());
        vo.setAmount(r.getAmount());
        vo.setStatus(r.getStatus());
        vo.setTitle(r.getTitle());
        vo.setDescription(r.getDescription());
        vo.setBizType(r.getBizType());
        vo.setBizId(r.getBizId());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }

    private WithdrawRecordVO toWithdrawVO(WithdrawRecord w) {
        WithdrawRecordVO vo = new WithdrawRecordVO();
        vo.setId(w.getId());
        vo.setAmount(w.getAmount());
        vo.setStatus(w.getStatus());
        vo.setBankName(w.getBankName());
        vo.setBankAccount(w.getBankAccount());
        vo.setFailureReason(w.getFailureReason());
        vo.setProcessedAt(w.getProcessedAt());
        vo.setCreatedAt(w.getCreatedAt());
        return vo;
    }

    private <S, T> PageResult<T> toPageResult(Page<S> result, Function<S, T> mapper) {
        java.util.List<T> list = result.getRecords() == null
                ? java.util.Collections.emptyList()
                : result.getRecords().stream().map(mapper).collect(Collectors.toList());
        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private <S> Page<S> toPage(Integer page, Integer pageSize) {
        return new Page<>(page == null || page < 1 ? 1 : page,
                pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100));
    }

    private Page<WithdrawRecord> toPageWithdraw(Integer page, Integer pageSize) {
        return new Page<>(page == null || page < 1 ? 1 : page,
                pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100));
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
