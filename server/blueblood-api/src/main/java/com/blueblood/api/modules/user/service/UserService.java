package com.blueblood.api.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.auth.dto.RegisterRequest;
import com.blueblood.api.modules.user.dto.SkillRequest;
import com.blueblood.api.modules.user.dto.UpdateProfileRequest;
import com.blueblood.api.modules.user.dto.UserVO;
import com.blueblood.api.modules.user.dto.VerificationReviewRequest;
import com.blueblood.api.modules.user.dto.VerificationSubmitRequest;
import com.blueblood.api.modules.user.entity.*;
import com.blueblood.api.modules.user.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserSkillMapper userSkillMapper;
    private final UserLevelLogMapper userLevelLogMapper;
    private final UserCreditLogMapper userCreditLogMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UserVerificationMapper userVerificationMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // ============================== 注册 / 登录 ==============================

    @Transactional
    public Long register(RegisterRequest req) {
        // 用户名唯一性
        Long exist = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername())
                .isNull(User::getDeletedAt));
        if (exist != null && exist > 0) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname((req.getNickname() == null || req.getNickname().isBlank())
                ? req.getUsername() : req.getNickname());
        user.setAvatar("");
        user.setPhone(req.getPhone() == null ? "" : req.getPhone());
        user.setEmail(req.getEmail() == null ? "" : req.getEmail());
        user.setGender(0);
        user.setLevel(1);
        user.setLevelName("新手");
        user.setPoints(0);
        user.setCreditScore(new BigDecimal("5.00"));
        user.setCompletedTasks(0);
        user.setVerified(0);
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        // 创建空档案
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setConnections(0);
        profile.setFollowers(0);
        profile.setFollowing(0);
        userProfileMapper.insert(profile);

        // 分配 USER 角色
        SysRole userRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "USER"));
        if (userRole != null) {
            SysUserRole rel = new SysUserRole();
            rel.setUserId(user.getId());
            rel.setRoleId(userRole.getId());
            sysUserRoleMapper.insert(rel);
        }
        return user.getId();
    }

    /**
     * 校验账号密码，返回用户。失败抛业务异常。
     */
    public User loginCheck(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .isNull(User::getDeletedAt));
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if ("BANNED".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED, "账号已被封禁");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (user.getPassword() == null || user.getPassword().isBlank()
                || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        // 更新最后登录时间
        User patch = new User();
        patch.setId(user.getId());
        patch.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(patch);
        return user;
    }

    /** 取用户最高角色编码（ADMIN > USER），用于签发 JWT */
    public String primaryRoleCode(Long userId) {
        List<String> codes = sysRoleMapper.selectRoleCodesByUserId(userId);
        if (codes == null || codes.isEmpty()) {
            return "USER";
        }
        return codes.contains("ADMIN") ? "ADMIN" : codes.get(0);
    }

    // ============================== 查询 ==============================

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        return user;
    }

    public UserVO buildVO(Long userId) {
        User user = getById(userId);
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .isNull(UserProfile::getDeletedAt)
                .last("LIMIT 1"));
        List<UserSkill> skills = userSkillMapper.selectList(new LambdaQueryWrapper<UserSkill>()
                .eq(UserSkill::getUserId, userId)
                .isNull(UserSkill::getDeletedAt));
        List<String> roles = sysRoleMapper.selectRoleCodesByUserId(userId);

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setLevel(user.getLevel());
        vo.setLevelName(user.getLevelName());
        vo.setPoints(user.getPoints());
        vo.setCreditScore(user.getCreditScore());
        vo.setCompletedTasks(user.getCompletedTasks());
        vo.setVerified(user.getVerified());
        vo.setRoles(roles);
        vo.setLastLoginAt(user.getLastLoginAt());
        if (profile != null) {
            vo.setSchool(profile.getSchool());
            vo.setMajor(profile.getMajor());
            vo.setBio(profile.getBio());
            vo.setGithub(profile.getGithub());
            vo.setConnections(profile.getConnections());
            vo.setFollowers(profile.getFollowers());
            vo.setFollowing(profile.getFollowing());
            vo.setBadges(profile.getBadges());
        }
        vo.setSkills(skills);
        return vo;
    }

    // ============================== 资料 ==============================

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest req) {
        User user = getById(userId);
        boolean userDirty = false;
        if (req.getNickname() != null && !req.getNickname().isBlank()) {
            user.setNickname(req.getNickname());
            userDirty = true;
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
            userDirty = true;
        }
        if (req.getGender() != null) {
            user.setGender(req.getGender());
            userDirty = true;
        }
        if (userDirty) {
            userMapper.updateById(user);
        }

        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .isNull(UserProfile::getDeletedAt)
                .last("LIMIT 1"));
        boolean createProfile;
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setConnections(0);
            profile.setFollowers(0);
            profile.setFollowing(0);
            createProfile = true;
        } else {
            createProfile = false;
        }
        boolean profileDirty = false;
        if (req.getSchool() != null) { profile.setSchool(req.getSchool()); profileDirty = true; }
        if (req.getMajor() != null) { profile.setMajor(req.getMajor()); profileDirty = true; }
        if (req.getBio() != null) { profile.setBio(req.getBio()); profileDirty = true; }
        if (req.getGithub() != null) { profile.setGithub(req.getGithub()); profileDirty = true; }
        if (profileDirty) {
            if (createProfile) {
                userProfileMapper.insert(profile);
            } else {
                userProfileMapper.updateById(profile);
            }
        }
    }

    // ============================== 技能 ==============================

    public List<UserSkill> listSkills(Long userId) {
        return userSkillMapper.selectList(new LambdaQueryWrapper<UserSkill>()
                .eq(UserSkill::getUserId, userId)
                .isNull(UserSkill::getDeletedAt));
    }

    public void addSkill(Long userId, SkillRequest req) {
        Long dup = userSkillMapper.selectCount(new LambdaQueryWrapper<UserSkill>()
                .eq(UserSkill::getUserId, userId)
                .eq(UserSkill::getName, req.getName())
                .isNull(UserSkill::getDeletedAt));
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "该技能已添加");
        }
        UserSkill skill = new UserSkill();
        skill.setUserId(userId);
        skill.setName(req.getName());
        skill.setCategory(req.getCategory() == null ? "" : req.getCategory());
        skill.setProficiency(req.getProficiency() == null ? 0 : req.getProficiency());
        userSkillMapper.insert(skill);
    }

    public void removeSkill(Long userId, Long skillId) {
        UserSkill skill = userSkillMapper.selectById(skillId);
        if (skill == null || skill.getDeletedAt() != null || !userId.equals(skill.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "技能不存在");
        }
        // 软删
        UserSkill patch = new UserSkill();
        patch.setId(skillId);
        patch.setDeletedAt(LocalDateTime.now());
        userSkillMapper.updateById(patch);
    }

    // ============================== 认证 ==============================

    @Transactional
    public Long submitVerification(Long userId, VerificationSubmitRequest req) {
        getById(userId);
        // 已通过或待审核则不允许重复提交
        Long pending = userVerificationMapper.selectCount(new LambdaQueryWrapper<UserVerification>()
                .eq(UserVerification::getUserId, userId)
                .in(UserVerification::getStatus, "PENDING", "APPROVED")
                .isNull(UserVerification::getDeletedAt));
        if (pending != null && pending > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "已有待审核或已通过的认证申请");
        }
        UserVerification v = new UserVerification();
        v.setUserId(userId);
        v.setRealName(req.getRealName());
        v.setIdNumber(req.getIdNumber() == null ? "" : req.getIdNumber());
        v.setStatus("PENDING");
        try {
            if (req.getMaterials() != null) {
                v.setMaterials(objectMapper.writeValueAsString(req.getMaterials()));
            }
        } catch (Exception e) {
            log.warn("序列化认证材料失败", e);
        }
        userVerificationMapper.insert(v);
        return v.getId();
    }

    public UserVerification myLatestVerification(Long userId) {
        return userVerificationMapper.selectOne(new LambdaQueryWrapper<UserVerification>()
                .eq(UserVerification::getUserId, userId)
                .isNull(UserVerification::getDeletedAt)
                .orderByDesc(UserVerification::getCreatedAt)
                .last("LIMIT 1"));
    }

    @Transactional
    public void reviewVerification(Long reviewerId, Long verificationId, VerificationReviewRequest req) {
        UserVerification v = userVerificationMapper.selectById(verificationId);
        if (v == null || v.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "认证申请不存在");
        }
        if (!"PENDING".equalsIgnoreCase(v.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "该申请已审核");
        }
        v.setStatus(req.getStatus());
        v.setReviewerId(reviewerId);
        v.setReviewedAt(LocalDateTime.now());
        if ("REJECTED".equalsIgnoreCase(req.getStatus())) {
            v.setRejectReason(req.getRejectReason() == null ? "" : req.getRejectReason());
        }
        userVerificationMapper.updateById(v);

        // 通过则置用户 verified=1
        if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
            User patch = new User();
            patch.setId(v.getUserId());
            patch.setVerified(1);
            userMapper.updateById(patch);
        }
    }

    // ============================== 等级 / 信誉日志 ==============================

    public PageResult<UserLevelLog> pageLevelLogs(Long userId, Integer page, Integer pageSize) {
        Page<UserLevelLog> p = new Page<>(page == null ? 1 : page, pageSize == null ? 10 : Math.min(pageSize, 100));
        Page<UserLevelLog> result = userLevelLogMapper.selectPage(p, new LambdaQueryWrapper<UserLevelLog>()
                .eq(UserLevelLog::getUserId, userId)
                .isNull(UserLevelLog::getDeletedAt)
                .orderByDesc(UserLevelLog::getCreatedAt));
        return PageResult.of(result);
    }

    public PageResult<UserCreditLog> pageCreditLogs(Long userId, Integer page, Integer pageSize) {
        Page<UserCreditLog> p = new Page<>(page == null ? 1 : page, pageSize == null ? 10 : Math.min(pageSize, 100));
        Page<UserCreditLog> result = userCreditLogMapper.selectPage(p, new LambdaQueryWrapper<UserCreditLog>()
                .eq(UserCreditLog::getUserId, userId)
                .isNull(UserCreditLog::getDeletedAt)
                .orderByDesc(UserCreditLog::getCreatedAt));
        return PageResult.of(result);
    }
}
