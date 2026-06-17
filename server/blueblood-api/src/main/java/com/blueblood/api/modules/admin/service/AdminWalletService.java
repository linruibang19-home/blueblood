package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminWalletAccountVO;
import com.blueblood.api.modules.admin.dto.AdminWalletRecordVO;
import com.blueblood.api.modules.admin.dto.AdminWithdrawVO;
import com.blueblood.api.modules.admin.dto.WithdrawReviewRequest;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.modules.wallet.entity.WalletAccount;
import com.blueblood.api.modules.wallet.entity.WalletRecord;
import com.blueblood.api.modules.wallet.entity.WithdrawRecord;
import com.blueblood.api.modules.wallet.mapper.WalletAccountMapper;
import com.blueblood.api.modules.wallet.mapper.WalletRecordMapper;
import com.blueblood.api.modules.wallet.mapper.WithdrawRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台 - 收益结算管理。
 * 收益流水/待结算/用户钱包/提现申请/提现审核。
 */
@Service
@RequiredArgsConstructor
public class AdminWalletService {

    private final WalletRecordMapper walletRecordMapper;
    private final WalletAccountMapper walletAccountMapper;
    private final WithdrawRecordMapper withdrawRecordMapper;
    private final UserMapper userMapper;

    /** 收益流水：按 userId/type/status 筛选分页，VO 带用户名/昵称 */
    public PageResult<AdminWalletRecordVO> pageRecord(Long userId, String type, String status, PageQuery query) {
        Page<WalletRecord> page = query.toPage();
        LambdaQueryWrapper<WalletRecord> wrapper = new LambdaQueryWrapper<WalletRecord>()
                .isNull(WalletRecord::getDeletedAt)
                .orderByDesc(WalletRecord::getCreatedAt);
        if (userId != null) {
            wrapper.eq(WalletRecord::getUserId, userId);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(WalletRecord::getType, type);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(WalletRecord::getStatus, status);
        }
        Page<WalletRecord> result = walletRecordMapper.selectPage(page, wrapper);
        Map<Long, User> userMap = loadUsers(result.getRecords().stream().map(WalletRecord::getUserId).toList());
        return PageResult.of(result.convert(r -> toRecordVO(r, userMap.get(r.getUserId()))));
    }

    /** 待结算流水：WalletRecord status=pending 分页 */
    public PageResult<AdminWalletRecordVO> pagePendingRecord(PageQuery query) {
        return pageRecord(null, null, "pending", query);
    }

    /** 用户钱包：返回 WalletAccount + 昵称 */
    public AdminWalletAccountVO getWalletByUser(Long userId) {
        LambdaQueryWrapper<WalletAccount> wrapper = new LambdaQueryWrapper<WalletAccount>()
                .isNull(WalletAccount::getDeletedAt)
                .eq(WalletAccount::getUserId, userId)
                .last("LIMIT 1");
        WalletAccount account = walletAccountMapper.selectOne(wrapper);
        if (account == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户钱包账户不存在");
        }
        User u = userMapper.selectById(userId);
        return toAccountVO(account, u == null ? null : u.getNickname());
    }

    /** 提现申请：按 status 筛选分页，VO 带用户名/昵称 */
    public PageResult<AdminWithdrawVO> pageWithdraw(String status, PageQuery query) {
        Page<WithdrawRecord> page = query.toPage();
        LambdaQueryWrapper<WithdrawRecord> wrapper = new LambdaQueryWrapper<WithdrawRecord>()
                .isNull(WithdrawRecord::getDeletedAt)
                .orderByDesc(WithdrawRecord::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(WithdrawRecord::getStatus, status);
        }
        Page<WithdrawRecord> result = withdrawRecordMapper.selectPage(page, wrapper);
        Map<Long, User> userMap = loadUsers(result.getRecords().stream().map(WithdrawRecord::getUserId).toList());
        return PageResult.of(result.convert(w -> toWithdrawVO(w, userMap.get(w.getUserId()))));
    }

    /** 提现审核 */
    @Transactional
    public void reviewWithdraw(Long withdrawId, WithdrawReviewRequest req) {
        WithdrawRecord withdraw = withdrawRecordMapper.selectById(withdrawId);
        if (withdraw == null || withdraw.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "提现记录不存在");
        }
        if (!"pending".equalsIgnoreCase(withdraw.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "该提现申请当前状态不可审核");
        }

        if ("APPROVED".equalsIgnoreCase(req.getAction())) {
            // 通过：置 completed，记处理时间；累计已提现金额
            withdraw.setStatus("completed");
            withdraw.setProcessedAt(LocalDateTime.now());
            withdrawRecordMapper.updateById(withdraw);

            WalletAccount account = findAccountByUser(withdraw.getUserId());
            BigDecimal cur = account.getWithdrawnAmount() == null ? BigDecimal.ZERO : account.getWithdrawnAmount();
            account.setWithdrawnAmount(cur.add(withdraw.getAmount() == null ? BigDecimal.ZERO : withdraw.getAmount()));
            walletAccountMapper.updateById(account);
        } else {
            // 驳回：置 failed，记录失败原因
            withdraw.setStatus("failed");
            withdraw.setFailureReason(req.getFailureReason());
            withdraw.setProcessedAt(LocalDateTime.now());
            withdrawRecordMapper.updateById(withdraw);
        }
    }

    private WalletAccount findAccountByUser(Long userId) {
        LambdaQueryWrapper<WalletAccount> wrapper = new LambdaQueryWrapper<WalletAccount>()
                .isNull(WalletAccount::getDeletedAt)
                .eq(WalletAccount::getUserId, userId)
                .last("LIMIT 1");
        WalletAccount account = walletAccountMapper.selectOne(wrapper);
        if (account == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户钱包账户不存在");
        }
        return account;
    }

    private Map<Long, User> loadUsers(List<Long> userIds) {
        Map<Long, User> map = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }
        List<Long> ids = userIds.stream().distinct().toList();
        for (Long uid : ids) {
            User u = userMapper.selectById(uid);
            if (u != null) {
                map.put(uid, u);
            }
        }
        return map;
    }

    private AdminWalletRecordVO toRecordVO(WalletRecord r, User u) {
        AdminWalletRecordVO vo = new AdminWalletRecordVO();
        vo.setId(r.getId());
        vo.setUserId(r.getUserId());
        vo.setUsername(u == null ? null : u.getUsername());
        vo.setNickname(u == null ? null : u.getNickname());
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

    private AdminWithdrawVO toWithdrawVO(WithdrawRecord w, User u) {
        AdminWithdrawVO vo = new AdminWithdrawVO();
        vo.setId(w.getId());
        vo.setUserId(w.getUserId());
        vo.setUsername(u == null ? null : u.getUsername());
        vo.setNickname(u == null ? null : u.getNickname());
        vo.setAmount(w.getAmount());
        vo.setStatus(w.getStatus());
        vo.setBankName(w.getBankName());
        vo.setBankAccount(w.getBankAccount());
        vo.setFailureReason(w.getFailureReason());
        vo.setProcessedAt(w.getProcessedAt());
        vo.setCreatedAt(w.getCreatedAt());
        return vo;
    }

    private AdminWalletAccountVO toAccountVO(WalletAccount a, String nickname) {
        AdminWalletAccountVO vo = new AdminWalletAccountVO();
        vo.setId(a.getId());
        vo.setUserId(a.getUserId());
        vo.setNickname(nickname);
        vo.setBalance(a.getBalance());
        vo.setPendingAmount(a.getPendingAmount());
        vo.setWithdrawnAmount(a.getWithdrawnAmount());
        vo.setTotalEarned(a.getTotalEarned());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }
}
