package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.modules.admin.dto.AdminVerificationVO;
import com.blueblood.api.modules.user.dto.VerificationReviewRequest;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.entity.UserVerification;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.modules.user.mapper.UserVerificationMapper;
import com.blueblood.api.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台认证审核：申请列表(筛选分页)、详情；审核动作复用 UserService.reviewVerification。
 */
@Service
@RequiredArgsConstructor
public class AdminVerificationService {

    private final UserVerificationMapper verificationMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    public PageResult<AdminVerificationVO> page(PageQuery query, String status) {
        Page<UserVerification> page = query.toPage();
        LambdaQueryWrapper<UserVerification> wrapper = new LambdaQueryWrapper<UserVerification>()
                .isNull(UserVerification::getDeletedAt)
                .orderByDesc(UserVerification::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(UserVerification::getStatus, status);
        }
        Page<UserVerification> result = verificationMapper.selectPage(page, wrapper);
        List<UserVerification> records = result.getRecords();
        Map<Long, String> nicknameMap = loadNicknames(records);

        return PageResult.of(result.convert(v -> toVO(v, nicknameMap.get(v.getUserId()))));
    }

    public AdminVerificationVO detail(Long id) {
        UserVerification v = verificationMapper.selectById(id);
        if (v == null || v.getDeletedAt() != null) {
            return null;
        }
        User u = userMapper.selectById(v.getUserId());
        return toVO(v, u == null ? null : u.getNickname());
    }

    /** 审核通过/驳回：复用阶段二 UserService(reviewerId=当前管理员,自动置 user.verified) */
    public void review(Long verificationId, VerificationReviewRequest req) {
        // 注意：UserService.reviewVerification 用 SecurityUtils.currentUserId() 作 reviewerId
        userService.reviewVerification(
                com.blueblood.api.security.SecurityUtils.currentUserId(), verificationId, req);
    }

    private Map<Long, String> loadNicknames(List<UserVerification> records) {
        Map<Long, String> map = new HashMap<>();
        if (records == null || records.isEmpty()) {
            return map;
        }
        List<Long> userIds = records.stream().map(UserVerification::getUserId).distinct().toList();
        for (Long uid : userIds) {
            User u = userMapper.selectById(uid);
            if (u != null) {
                map.put(uid, u.getNickname());
            }
        }
        return map;
    }

    private AdminVerificationVO toVO(UserVerification v, String nickname) {
        AdminVerificationVO vo = new AdminVerificationVO();
        vo.setId(v.getId());
        vo.setUserId(v.getUserId());
        vo.setNickname(nickname);
        vo.setRealName(v.getRealName());
        vo.setIdNumber(v.getIdNumber());
        vo.setMaterials(v.getMaterials());
        vo.setStatus(v.getStatus());
        vo.setRejectReason(v.getRejectReason());
        vo.setReviewerId(v.getReviewerId());
        vo.setReviewedAt(v.getReviewedAt());
        vo.setCreatedAt(v.getCreatedAt());
        return vo;
    }
}
