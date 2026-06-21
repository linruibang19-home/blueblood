package com.blueblood.api.modules.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.enterprise.entity.EnterpriseApplication;
import com.blueblood.api.modules.enterprise.mapper.EnterpriseApplicationMapper;
import com.blueblood.api.modules.task.dto.MilestoneReviewVO;
import com.blueblood.api.modules.task.dto.MilestoneTemplateDTO;
import com.blueblood.api.modules.task.dto.PublishTaskRequest;
import com.blueblood.api.modules.task.dto.TaskListItemVO;
import com.blueblood.api.modules.task.entity.*;
import com.blueblood.api.modules.task.mapper.*;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户端任务发布(企业/个人)与雇主工作台。
 * Q1 企业+个人均可发布;Q2 发布即上架(status=APPROVED);
 * Q3 雇主自助验收(里程碑审核权在 MilestoneService);Q4 里程碑分阶段结算。
 */
@Service
@RequiredArgsConstructor
public class TaskPublishService {

    private final TaskMapper taskMapper;
    private final TaskCategoryMapper categoryMapper;
    private final TaskSkillMapper skillMapper;
    private final TaskMilestoneMapper milestoneMapper;
    private final TaskOrderMapper orderMapper;
    private final MilestoneSubmissionMapper submissionMapper;
    private final UserMapper userMapper;
    private final EnterpriseApplicationMapper enterpriseAppMapper;

    /** 发布任务可编辑的状态(已有人接单进入 IN_PROGRESS 后禁止编辑) */
    private static final Set<String> EDITABLE_STATUS = Set.of("APPROVED", "RECRUITING");

    // ============================== 发布 / 编辑 / 下架 ==============================

    @Transactional
    public Long publish(PublishTaskRequest req) {
        validateRewardSum(req.getReward(), req.getMilestones());

        Long employerId = SecurityUtils.currentUserId();
        Task t = new Task();
        t.setTitle(req.getTitle());
        t.setCategoryId(req.getCategoryId());
        t.setDescription(req.getDescription());
        t.setReward(req.getReward());
        t.setLevelRequired(req.getLevelRequired() == null ? 1 : req.getLevelRequired());
        t.setTotalSlots(req.getTotalSlots() == null ? 1 : req.getTotalSlots());
        t.setSlotsLeft(t.getTotalSlots());
        t.setDeadline(req.getDeadline());
        t.setStatus("APPROVED"); // Q2 直接上架
        t.setViewCount(0);
        t.setEmployerId(employerId);
        t.setEmployerName(resolveEmployerName(employerId));
        resolveCategoryName(t);
        taskMapper.insert(t);

        Long taskId = t.getId();
        saveSkills(taskId, req.getSkills());
        saveMilestoneTemplates(taskId, req.getMilestones());
        return taskId;
    }

    @Transactional
    public void update(Long taskId, PublishTaskRequest req) {
        Task t = assertEmployerOwned(taskId);
        if (!EDITABLE_STATUS.contains(t.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "当前状态不可编辑(已有人接单)");
        }
        validateRewardSum(req.getReward(), req.getMilestones());

        t.setTitle(req.getTitle());
        t.setCategoryId(req.getCategoryId());
        t.setDescription(req.getDescription());
        t.setReward(req.getReward());
        t.setLevelRequired(req.getLevelRequired() == null ? 1 : req.getLevelRequired());
        // 名额调整:仅当尚无接单(slotsLeft==totalSlots)时允许改
        if (req.getTotalSlots() != null
                && (t.getSlotsLeft() != null && t.getSlotsLeft().equals(t.getTotalSlots()))) {
            t.setTotalSlots(req.getTotalSlots());
            t.setSlotsLeft(req.getTotalSlots());
        }
        t.setDeadline(req.getDeadline());
        resolveCategoryName(t);
        taskMapper.updateById(t);

        // 软删旧 skills + 旧模板,重建
        softDeleteSkills(taskId);
        softDeleteMilestoneTemplates(taskId);
        saveSkills(taskId, req.getSkills());
        saveMilestoneTemplates(taskId, req.getMilestones());
    }

    @Transactional
    public void close(Long taskId) {
        assertEmployerOwned(taskId);
        Task patch = new Task();
        patch.setId(taskId);
        patch.setStatus("CLOSED");
        taskMapper.updateById(patch);
    }

    // ============================== 我发布的任务 ==============================

    public PageResult<TaskListItemVO> pagePublished(String status, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        Long employerId = SecurityUtils.currentUserId();

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getEmployerId, employerId)
                .isNull(Task::getDeletedAt)
                .orderByDesc(Task::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Task::getStatus, status.toUpperCase());
        }
        Page<Task> result = taskMapper.selectPage(new Page<>(p, size), wrapper);
        return PageResult.of(result.convert(this::toListItem));
    }

    // ============================== 待审核里程碑(雇主工作台) ==============================

    public PageResult<MilestoneReviewVO> pageMilestonesForReview(String status, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        Long employerId = SecurityUtils.currentUserId();
        String st = StringUtils.hasText(status) ? status.toUpperCase() : "SUBMITTED";

        // 1. 雇主的任务集合
        List<Task> myTasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getEmployerId, employerId)
                .isNull(Task::getDeletedAt));
        if (myTasks.isEmpty()) {
            return emptyPage(p, size);
        }
        Map<Long, String> taskTitleMap = myTasks.stream()
                .collect(Collectors.toMap(Task::getId, Task::getTitle, (a, b) -> a));

        // 2. 这些任务下的里程碑实例(order_id 非空)且状态匹配
        Page<TaskMilestone> result = milestoneMapper.selectPage(new Page<>(p, size),
                new LambdaQueryWrapper<TaskMilestone>()
                        .in(TaskMilestone::getTaskId, taskTitleMap.keySet())
                        .isNotNull(TaskMilestone::getOrderId)
                        .eq(TaskMilestone::getStatus, st)
                        .isNull(TaskMilestone::getDeletedAt)
                        .orderByDesc(TaskMilestone::getCreatedAt));

        // 3. 批量补 submission + order + worker
        Set<Long> milestoneIds = result.getRecords().stream().map(TaskMilestone::getId).collect(Collectors.toSet());
        Set<Long> orderIds = result.getRecords().stream().map(TaskMilestone::getOrderId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, MilestoneSubmission> subMap = loadSubmissions(milestoneIds);
        Map<Long, TaskOrder> orderMap = loadOrders(orderIds);
        Set<Long> workerIds = orderMap.values().stream().map(TaskOrder::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = loadUsers(workerIds);

        List<MilestoneReviewVO> list = result.getRecords().stream()
                .map(m -> toReviewVO(m, taskTitleMap, subMap, orderMap, userMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    // ============================== 内部 ==============================

    private void validateRewardSum(BigDecimal total, List<MilestoneTemplateDTO> ms) {
        if (total == null || ms == null || ms.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "酬金与里程碑均不能为空");
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (MilestoneTemplateDTO m : ms) {
            if (m.getReward() == null || m.getReward().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "每个里程碑金额须大于0");
            }
            sum = sum.add(m.getReward());
        }
        if (total.compareTo(sum) != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "任务酬金(" + total + ")须等于各里程碑金额之和(" + sum + ")");
        }
    }

    /** employerName:企业取已审核通过的公司名,个人取昵称。 */
    private String resolveEmployerName(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            return "匿名雇主";
        }
        if ("enterprise".equalsIgnoreCase(u.getUserType())) {
            EnterpriseApplication app = enterpriseAppMapper.selectOne(new LambdaQueryWrapper<EnterpriseApplication>()
                    .eq(EnterpriseApplication::getUserId, userId)
                    .eq(EnterpriseApplication::getStatus, "APPROVED")
                    .isNull(EnterpriseApplication::getDeletedAt)
                    .last("LIMIT 1"));
            if (app != null && StringUtils.hasText(app.getCompanyName())) {
                return app.getCompanyName();
            }
        }
        return StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername();
    }

    private void resolveCategoryName(Task t) {
        if (t.getCategoryId() == null) {
            return;
        }
        TaskCategory c = categoryMapper.selectById(t.getCategoryId());
        if (c != null && c.getDeletedAt() == null) {
            t.setCategory(c.getName());
        }
    }

    private void saveSkills(Long taskId, List<String> skills) {
        if (skills == null) {
            return;
        }
        for (String name : skills) {
            if (StringUtils.hasText(name)) {
                TaskSkill s = new TaskSkill();
                s.setTaskId(taskId);
                s.setName(name.trim());
                skillMapper.insert(s);
            }
        }
    }

    private void saveMilestoneTemplates(Long taskId, List<MilestoneTemplateDTO> templates) {
        int seq = 0;
        for (MilestoneTemplateDTO tpl : templates) {
            seq++;
            TaskMilestone m = new TaskMilestone();
            m.setTaskId(taskId);
            m.setOrderId(null); // 模板行
            m.setTitle(tpl.getTitle());
            m.setDescription(tpl.getDescription());
            m.setDueDate(tpl.getDueDate());
            m.setMilestoneOrder(tpl.getMilestoneOrder() == null ? seq : tpl.getMilestoneOrder());
            m.setReward(tpl.getReward());
            m.setStatus("NOT_STARTED");
            milestoneMapper.insert(m);
        }
    }

    private void softDeleteSkills(Long taskId) {
        List<TaskSkill> olds = skillMapper.selectList(new LambdaQueryWrapper<TaskSkill>()
                .eq(TaskSkill::getTaskId, taskId)
                .isNull(TaskSkill::getDeletedAt));
        LocalDateTime now = LocalDateTime.now();
        for (TaskSkill s : olds) {
            TaskSkill patch = new TaskSkill();
            patch.setId(s.getId());
            patch.setDeletedAt(now);
            skillMapper.updateById(patch);
        }
    }

    private void softDeleteMilestoneTemplates(Long taskId) {
        List<TaskMilestone> olds = milestoneMapper.selectList(new LambdaQueryWrapper<TaskMilestone>()
                .eq(TaskMilestone::getTaskId, taskId)
                .isNull(TaskMilestone::getOrderId)
                .isNull(TaskMilestone::getDeletedAt));
        LocalDateTime now = LocalDateTime.now();
        for (TaskMilestone m : olds) {
            TaskMilestone patch = new TaskMilestone();
            patch.setId(m.getId());
            patch.setDeletedAt(now);
            milestoneMapper.updateById(patch);
        }
    }

    private Task assertEmployerOwned(Long taskId) {
        Task t = taskMapper.selectOne(new LambdaQueryWrapper<Task>()
                .eq(Task::getId, taskId)
                .isNull(Task::getDeletedAt)
                .last("LIMIT 1"));
        if (t == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        if (!SecurityUtils.currentUserId().equals(t.getEmployerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅任务发布者可操作");
        }
        return t;
    }

    private TaskListItemVO toListItem(Task t) {
        TaskListItemVO vo = new TaskListItemVO();
        vo.setId(t.getId());
        vo.setTitle(t.getTitle());
        vo.setCategory(t.getCategory());
        vo.setEmployerName(t.getEmployerName());
        vo.setReward(t.getReward());
        vo.setLevelRequired(t.getLevelRequired());
        vo.setTotalSlots(t.getTotalSlots());
        vo.setSlotsLeft(t.getSlotsLeft());
        vo.setDeadline(t.getDeadline());
        vo.setStatus(t.getStatus());
        vo.setViewCount(t.getViewCount());
        vo.setSkills(loadSkillNames(t.getId()));
        return vo;
    }

    private List<String> loadSkillNames(Long taskId) {
        return skillMapper.selectList(new LambdaQueryWrapper<TaskSkill>()
                        .eq(TaskSkill::getTaskId, taskId)
                        .isNull(TaskSkill::getDeletedAt))
                .stream().map(TaskSkill::getName).toList();
    }

    private MilestoneReviewVO toReviewVO(TaskMilestone m, Map<Long, String> taskTitleMap,
                                         Map<Long, MilestoneSubmission> subMap,
                                         Map<Long, TaskOrder> orderMap,
                                         Map<Long, User> userMap) {
        MilestoneReviewVO vo = new MilestoneReviewVO();
        vo.setMilestoneId(m.getId());
        vo.setOrderId(m.getOrderId());
        vo.setTaskId(m.getTaskId());
        vo.setTaskTitle(taskTitleMap.get(m.getTaskId()));
        vo.setMilestoneTitle(m.getTitle());
        vo.setDescription(m.getDescription());
        vo.setMilestoneOrder(m.getMilestoneOrder());
        vo.setReward(m.getReward());
        vo.setStatus(m.getStatus());

        TaskOrder order = m.getOrderId() == null ? null : orderMap.get(m.getOrderId());
        if (order != null) {
            vo.setWorkerId(order.getUserId());
            User u = userMap.get(order.getUserId());
            if (u != null) {
                vo.setWorkerName(StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername());
            }
        }
        MilestoneSubmission sub = subMap.get(m.getId());
        if (sub != null) {
            vo.setGithubUrl(sub.getGithubUrl());
            vo.setSubmissionDesc(sub.getDescription());
            vo.setAttachments(sub.getAttachments());
            vo.setSubmittedAt(sub.getSubmittedAt());
        }
        return vo;
    }

    private Map<Long, MilestoneSubmission> loadSubmissions(Set<Long> milestoneIds) {
        if (milestoneIds.isEmpty()) return Map.of();
        return submissionMapper.selectList(new LambdaQueryWrapper<MilestoneSubmission>()
                        .in(MilestoneSubmission::getMilestoneId, milestoneIds)
                        .isNull(MilestoneSubmission::getDeletedAt))
                .stream().collect(Collectors.toMap(MilestoneSubmission::getMilestoneId, s -> s, (a, b) -> a));
    }

    private Map<Long, TaskOrder> loadOrders(Set<Long> orderIds) {
        if (orderIds.isEmpty()) return Map.of();
        return orderMapper.selectList(new LambdaQueryWrapper<TaskOrder>()
                        .in(TaskOrder::getId, orderIds)
                        .isNull(TaskOrder::getDeletedAt))
                .stream().collect(Collectors.toMap(TaskOrder::getId, o -> o, (a, b) -> a));
    }

    private Map<Long, User> loadUsers(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds)
                        .isNull(User::getDeletedAt))
                .stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    private PageResult<MilestoneReviewVO> emptyPage(int page, int size) {
        return new PageResult<>(Collections.emptyList(), 0L, (long) page, (long) size);
    }
}
