package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminEnterpriseApplicationQuery;
import com.blueblood.api.modules.admin.dto.AdminEnterpriseApplicationVO;
import com.blueblood.api.modules.admin.dto.EnterpriseReviewRequest;
import com.blueblood.api.modules.enterprise.entity.EnterpriseApplication;
import com.blueblood.api.modules.enterprise.mapper.EnterpriseApplicationMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台企业认证审核：申请列表(筛选分页)、详情、审核(APPROVED → 置 user.user_type=enterprise)。
 */
@Service
@RequiredArgsConstructor
public class AdminEnterpriseService {

    private final EnterpriseApplicationMapper applicationMapper;
    private final UserMapper userMapper;

    public PageResult<AdminEnterpriseApplicationVO> page(AdminEnterpriseApplicationQuery query) {
        Page<EnterpriseApplication> page = query.toPage();
        LambdaQueryWrapper<EnterpriseApplication> wrapper = new LambdaQueryWrapper<EnterpriseApplication>()
                .isNull(EnterpriseApplication::getDeletedAt)
                .orderByDesc(EnterpriseApplication::getCreatedAt);
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(EnterpriseApplication::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            // 按公司名过滤；昵称匹配需先反查 user，简化为公司名 like
            wrapper.like(EnterpriseApplication::getCompanyName, query.getKeyword());
        }
        Page<EnterpriseApplication> result = applicationMapper.selectPage(page, wrapper);
        Map<Long, String> nicknameMap = loadNicknames(result.getRecords());
        return PageResult.of(result.convert(v -> toVO(v, nicknameMap.get(v.getUserId()))));
    }

    public AdminEnterpriseApplicationVO detail(Long id) {
        EnterpriseApplication app = applicationMapper.selectById(id);
        if (app == null || app.getDeletedAt() != null) {
            return null;
        }
        User u = userMapper.selectById(app.getUserId());
        return toVO(app, u == null ? null : u.getNickname());
    }

    @Transactional
    public void review(Long applicationId, EnterpriseReviewRequest req) {
        EnterpriseApplication app = applicationMapper.selectById(applicationId);
        if (app == null || app.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "企业申请不存在");
        }
        if (!"PENDING".equalsIgnoreCase(app.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "该申请已审核");
        }
        app.setStatus(req.getStatus());
        app.setReviewerId(SecurityUtils.currentUserId());
        app.setReviewedAt(LocalDateTime.now());
        if ("REJECTED".equalsIgnoreCase(req.getStatus())) {
            app.setRejectReason(req.getRejectReason() == null ? "" : req.getRejectReason());
        }
        applicationMapper.updateById(app);

        // 通过则将该用户置为企业用户
        if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
            User patch = new User();
            patch.setId(app.getUserId());
            patch.setUserType("enterprise");
            userMapper.updateById(patch);
        }
    }

    private Map<Long, String> loadNicknames(List<EnterpriseApplication> records) {
        Map<Long, String> map = new HashMap<>();
        if (records == null || records.isEmpty()) {
            return map;
        }
        List<Long> userIds = records.stream().map(EnterpriseApplication::getUserId).distinct().toList();
        for (Long uid : userIds) {
            User u = userMapper.selectById(uid);
            if (u != null) {
                map.put(uid, u.getNickname());
            }
        }
        return map;
    }

    private AdminEnterpriseApplicationVO toVO(EnterpriseApplication a, String nickname) {
        AdminEnterpriseApplicationVO vo = new AdminEnterpriseApplicationVO();
        vo.setId(a.getId());
        vo.setUserId(a.getUserId());
        vo.setNickname(nickname);
        vo.setCompanyName(a.getCompanyName());
        vo.setCreditCode(a.getCreditCode());
        vo.setLicenseUrl(a.getLicenseUrl());
        vo.setContactName(a.getContactName());
        vo.setContactPhone(a.getContactPhone());
        vo.setStatus(a.getStatus());
        vo.setRejectReason(a.getRejectReason());
        vo.setReviewerId(a.getReviewerId());
        vo.setReviewedAt(a.getReviewedAt());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }
}
