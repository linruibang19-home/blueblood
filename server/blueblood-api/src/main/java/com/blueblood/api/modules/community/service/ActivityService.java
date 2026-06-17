package com.blueblood.api.modules.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.community.dto.ActivityVO;
import com.blueblood.api.modules.community.entity.Activity;
import com.blueblood.api.modules.community.entity.ActivitySignup;
import com.blueblood.api.modules.community.mapper.ActivityMapper;
import com.blueblood.api.modules.community.mapper.ActivitySignupMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 活动服务：列表、详情、报名 / 取消报名。
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;

    // ============================== 查询 ==============================

    /** 小组活动分页 */
    public PageResult<ActivityVO> pageByGroup(Long groupId, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        Page<Activity> result = activityMapper.selectPage(new Page<>(p, size),
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getGroupId, groupId)
                        .isNull(Activity::getDeletedAt)
                        .orderByDesc(Activity::getStartTime)
                        .orderByDesc(Activity::getId));
        return PageResult.of(result.convert(this::toVO));
    }

    /** 活动详情（含 signedUp） */
    public ActivityVO detail(Long activityId) {
        Activity activity = getActive(activityId);
        ActivityVO vo = toVO(activity);
        vo.setSignedUp(isSignedUp(activityId, SecurityUtils.currentUserId()));
        return vo;
    }

    // ============================== 报名 ==============================

    /**
     * 报名活动：未报名则新增 SIGNED 记录并 signup_count+1；
     * 名额已满（max_count>0 且已满）抛 OPERATION_FAILED("报名已满")；
     * 已报名（有效 SIGNED）抛 DATA_DUPLICATED。
     */
    @Transactional
    public void signup(Long activityId, Long userId) {
        Activity activity = getActive(activityId);

        // 已存在有效报名记录
        ActivitySignup existing = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId)
                .isNull(ActivitySignup::getDeletedAt)
                .last("LIMIT 1"));

        if (existing != null && "SIGNED".equalsIgnoreCase(existing.getStatus())) {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "已报名该活动");
        }

        // 名额校验
        if (activity.getMaxCount() != null && activity.getMaxCount() > 0) {
            int current = activity.getSignupCount() == null ? 0 : activity.getSignupCount();
            if (current >= activity.getMaxCount()) {
                throw new BusinessException(ResultCode.OPERATION_FAILED, "报名已满");
            }
        }

        if (existing != null) {
            // 历史已取消的记录：复用并恢复为 SIGNED
            existing.setStatus("SIGNED");
            existing.setDeletedAt(null);
            activitySignupMapper.updateById(existing);
        } else {
            ActivitySignup signup = new ActivitySignup();
            signup.setActivityId(activityId);
            signup.setUserId(userId);
            signup.setStatus("SIGNED");
            activitySignupMapper.insert(signup);
        }

        // 计数 +1
        Activity patch = new Activity();
        patch.setId(activityId);
        patch.setSignupCount((activity.getSignupCount() == null ? 0 : activity.getSignupCount()) + 1);
        activityMapper.updateById(patch);
    }

    /**
     * 取消报名：将有效 SIGNED 记录置 CANCELLED（软删），signup_count-1（下限 0）；
     * 未报名抛 DATA_NOT_FOUND。
     */
    @Transactional
    public void cancelSignup(Long activityId, Long userId) {
        Activity activity = getActive(activityId);

        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId)
                .isNull(ActivitySignup::getDeletedAt)
                .last("LIMIT 1"));

        if (signup == null || !"SIGNED".equalsIgnoreCase(signup.getStatus())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "未报名该活动");
        }

        signup.setStatus("CANCELLED");
        signup.setDeletedAt(LocalDateTime.now());
        activitySignupMapper.updateById(signup);

        // 计数 -1（下限 0）
        int current = activity.getSignupCount() == null ? 0 : activity.getSignupCount();
        Activity patch = new Activity();
        patch.setId(activityId);
        patch.setSignupCount(Math.max(0, current - 1));
        activityMapper.updateById(patch);
    }

    // ============================== 工具 ==============================

    private Activity getActive(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "活动不存在");
        }
        return activity;
    }

    private boolean isSignedUp(Long activityId, Long userId) {
        Long cnt = activitySignupMapper.selectCount(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getStatus, "SIGNED")
                .isNull(ActivitySignup::getDeletedAt));
        return cnt != null && cnt > 0;
    }

    private ActivityVO toVO(Activity a) {
        ActivityVO vo = new ActivityVO();
        vo.setId(a.getId());
        vo.setGroupId(a.getGroupId());
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setCoverImage(a.getCoverImage());
        vo.setStartTime(a.getStartTime());
        vo.setEndTime(a.getEndTime());
        vo.setLocation(a.getLocation());
        vo.setSignupCount(a.getSignupCount());
        vo.setMaxCount(a.getMaxCount());
        vo.setStatus(a.getStatus());
        return vo;
    }
}
