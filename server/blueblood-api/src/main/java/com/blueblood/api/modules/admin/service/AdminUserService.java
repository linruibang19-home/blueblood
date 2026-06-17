package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminUserQuery;
import com.blueblood.api.modules.admin.dto.AdminUserVO;
import com.blueblood.api.modules.admin.dto.AdjustUserRequest;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.entity.UserLevelLog;
import com.blueblood.api.modules.user.mapper.UserLevelLogMapper;
import com.blueblood.api.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 后台用户管理：列表筛选分页、详情、禁用/启用、调整等级积分。
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final UserLevelLogMapper userLevelLogMapper;

    public PageResult<AdminUserVO> page(AdminUserQuery query) {
        Page<User> page = query.toPage();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .isNull(User::getDeletedAt)
                .orderByDesc(User::getCreatedAt);

        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(User::getUsername, kw)
                    .or().like(User::getNickname, kw)
                    .or().like(User::getPhone, kw));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        if (query.getVerified() != null) {
            wrapper.eq(User::getVerified, query.getVerified());
        }

        Page<User> result = userMapper.selectPage(page, wrapper);
        return PageResult.of(result.convert(this::toVO));
    }

    public AdminUserVO detail(Long id) {
        return toVO(getUser(id));
    }

    /** 禁用/启用 (status: ACTIVE/INACTIVE/BANNED) */
    @Transactional
    public void updateStatus(Long id, String status) {
        if (!"ACTIVE".equalsIgnoreCase(status)
                && !"INACTIVE".equalsIgnoreCase(status)
                && !"BANNED".equalsIgnoreCase(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法状态");
        }
        User patch = new User();
        patch.setId(id);
        patch.setStatus(status.toUpperCase());
        userMapper.updateById(patch);
    }

    /** 调整等级/积分（写等级日志） */
    @Transactional
    public void adjust(Long id, AdjustUserRequest req) {
        User user = getUser(id);
        Integer oldLevel = user.getLevel() == null ? 0 : user.getLevel();

        User patch = new User();
        patch.setId(id);
        boolean levelChanged = false;
        if (req.getLevel() != null) {
            patch.setLevel(req.getLevel());
            levelChanged = true;
            if (StringUtils.hasText(req.getLevelName())) {
                patch.setLevelName(req.getLevelName());
            }
        }
        if (req.getPointsDelta() != null && req.getPointsDelta() != 0) {
            int cur = user.getPoints() == null ? 0 : user.getPoints();
            patch.setPoints(Math.max(0, cur + req.getPointsDelta()));
        }
        userMapper.updateById(patch);

        // 等级变动写日志
        if (levelChanged && req.getLevel() != null && !req.getLevel().equals(oldLevel)) {
            UserLevelLog log = new UserLevelLog();
            log.setUserId(id);
            log.setFromLevel(oldLevel);
            log.setToLevel(req.getLevel());
            log.setReason(req.getReason() == null ? "管理员调整" : req.getReason());
            userLevelLogMapper.insert(log);
        }
    }

    private User getUser(Long id) {
        User u = userMapper.selectById(id);
        if (u == null || u.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        return u;
    }

    private AdminUserVO toVO(User u) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        vo.setGender(u.getGender());
        vo.setLevel(u.getLevel());
        vo.setLevelName(u.getLevelName());
        vo.setPoints(u.getPoints());
        vo.setCreditScore(u.getCreditScore());
        vo.setCompletedTasks(u.getCompletedTasks());
        vo.setVerified(u.getVerified());
        vo.setStatus(u.getStatus());
        vo.setLastLoginAt(u.getLastLoginAt());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }
}
