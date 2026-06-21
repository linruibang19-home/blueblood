package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.*;
import com.blueblood.api.modules.task.dto.ReviewMilestoneRequest;
import com.blueblood.api.modules.task.dto.TaskCategoryVO;
import com.blueblood.api.modules.task.entity.*;
import com.blueblood.api.modules.task.mapper.*;
import com.blueblood.api.modules.task.service.MilestoneService;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 后台任务与里程碑审核管理：
 * 任务 CRUD / 状态变更、任务分类、接单记录、里程碑提交列表、里程碑审核。
 * 删除均为软删(set deletedAt=now)；查询均 .isNull(Entity::getDeletedAt)。
 */
@Service
@RequiredArgsConstructor
public class AdminTaskService {

    private final TaskMapper taskMapper;
    private final TaskCategoryMapper categoryMapper;
    private final TaskOrderMapper orderMapper;
    private final TaskMilestoneMapper milestoneMapper;
    private final MilestoneSubmissionMapper submissionMapper;
    private final UserMapper userMapper;
    private final MilestoneService milestoneService;

    private static final Set<String> TASK_STATUS = Set.of(
            "DRAFT", "PENDING_REVIEW", "APPROVED", "RECRUITING",
            "IN_PROGRESS", "COMPLETED", "CLOSED");

    // ==================== 任务 ====================

    public PageResult<AdminTaskVO> pageTask(AdminTaskQuery query) {
        Page<Task> page = query.toPage();
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .isNull(Task::getDeletedAt)
                .orderByDesc(Task::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Task::getTitle, kw).or().like(Task::getEmployerName, kw));
        }
        if (query.getCategory() != null) {
            wrapper.eq(Task::getCategoryId, query.getCategory());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Task::getStatus, query.getStatus().toUpperCase());
        }
        Page<Task> result = taskMapper.selectPage(page, wrapper);
        return PageResult.of(result.convert(this::toTaskVO));
    }

    public AdminTaskVO getTask(Long id) {
        return toTaskVO(getTaskEntity(id));
    }

    @Transactional
    public Long createTask(TaskRequest req) {
        Task t = new Task();
        applyTaskFields(t, req);
        // 管理员发布：默认 APPROVED；若未填则按 RECRUITING 放出
        if (!StringUtils.hasText(t.getStatus())) {
            t.setStatus("APPROVED");
        }
        if (t.getSlotsLeft() == null) {
            t.setSlotsLeft(t.getTotalSlots() == null ? 0 : t.getTotalSlots());
        }
        if (t.getViewCount() == null) {
            t.setViewCount(0);
        }
        // 发布者记为当前管理员
        Long adminId = SecurityUtils.currentUserId();
        if (t.getEmployerId() == null) {
            t.setEmployerId(adminId);
        }
        // 分类名称冗余回填
        resolveCategoryName(t);
        taskMapper.insert(t);
        return t.getId();
    }

    @Transactional
    public void updateTask(Long id, TaskRequest req) {
        Task exists = getTaskEntity(id);
        applyTaskFields(exists, req);
        resolveCategoryName(exists);
        taskMapper.updateById(exists);
    }

    @Transactional
    public void updateTaskStatus(Long id, String status) {
        if (!StringUtils.hasText(status) || !TASK_STATUS.contains(status.toUpperCase())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法任务状态");
        }
        Task patch = new Task();
        patch.setId(id);
        patch.setStatus(status.toUpperCase());
        taskMapper.updateById(patch);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task c = getTaskEntity(id);
        Task patch = new Task();
        patch.setId(c.getId());
        patch.setDeletedAt(LocalDateTime.now());
        taskMapper.updateById(patch);
    }

    // ==================== 任务分类 ====================

    public List<TaskCategoryVO> listCategories() {
        List<TaskCategory> list = categoryMapper.selectList(new LambdaQueryWrapper<TaskCategory>()
                .isNull(TaskCategory::getDeletedAt)
                .orderByAsc(TaskCategory::getCategoryOrder));
        List<TaskCategoryVO> vos = new ArrayList<>(list.size());
        for (TaskCategory c : list) {
            TaskCategoryVO vo = new TaskCategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setIcon(c.getIcon());
            vo.setTaskCount(c.getTaskCount());
            vo.setCategoryOrder(c.getCategoryOrder());
            vos.add(vo);
        }
        return vos;
    }

    // ==================== 接单记录 ====================

    public PageResult<AdminTaskOrderVO> pageOrder(AdminTaskOrderQuery query) {
        Page<TaskOrder> page = query.toPage();
        LambdaQueryWrapper<TaskOrder> wrapper = new LambdaQueryWrapper<TaskOrder>()
                .isNull(TaskOrder::getDeletedAt)
                .orderByDesc(TaskOrder::getCreatedAt);
        if (query.getTaskId() != null) {
            wrapper.eq(TaskOrder::getTaskId, query.getTaskId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(TaskOrder::getStatus, query.getStatus());
        }
        Page<TaskOrder> result = orderMapper.selectPage(page, wrapper);

        // 批量补全 taskTitle / username / nickname
        Map<Long, String> taskTitleMap = loadTaskTitles(result.getRecords());
        Map<Long, User> userMap = loadUsers(result.getRecords());

        return PageResult.of(result.convert(o -> toOrderVO(o, taskTitleMap, userMap)));
    }

    /** 强制关闭订单(平台介入终止,置 rejected)。 */
    @Transactional
    public void closeOrder(Long orderId) {
        TaskOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        TaskOrder patch = new TaskOrder();
        patch.setId(orderId);
        patch.setStatus("rejected");
        patch.setRemark("平台强制关闭");
        orderMapper.updateById(patch);
    }

    // ==================== 里程碑提交 ====================

    public PageResult<AdminMilestoneSubmissionVO> pageMilestoneSubmission(AdminMilestoneSubmissionQuery query) {
        String status = StringUtils.hasText(query.getStatus()) ? query.getStatus().toUpperCase() : "SUBMITTED";

        // 1) 取符合状态的里程碑 id（提交以 SUBMITTED 状态挂在里程碑上）
        List<TaskMilestone> milestones = milestoneMapper.selectList(new LambdaQueryWrapper<TaskMilestone>()
                .eq(TaskMilestone::getStatus, status)
                .isNull(TaskMilestone::getDeletedAt));
        if (milestones.isEmpty()) {
            return emptyPage(query);
        }
        Set<Long> milestoneIds = new HashSet<>();
        Map<Long, TaskMilestone> milestoneMap = new HashMap<>();
        for (TaskMilestone m : milestones) {
            milestoneIds.add(m.getId());
            milestoneMap.put(m.getId(), m);
        }

        // 2) 按里程碑 id 分页取提交记录
        Page<MilestoneSubmission> page = query.toPage();
        LambdaQueryWrapper<MilestoneSubmission> wrapper = new LambdaQueryWrapper<MilestoneSubmission>()
                .in(MilestoneSubmission::getMilestoneId, milestoneIds)
                .isNull(MilestoneSubmission::getDeletedAt)
                .orderByDesc(MilestoneSubmission::getSubmittedAt);
        Page<MilestoneSubmission> result = submissionMapper.selectPage(page, wrapper);

        Map<Long, String> taskTitleMap = loadTaskTitlesForMilestones(milestones);
        Map<Long, User> userMap = loadUsersForSubmissions(result.getRecords());

        return PageResult.of(result.convert(s -> toSubmissionVO(s, milestoneMap, taskTitleMap, userMap)));
    }

    // ==================== 里程碑审核（复用 MilestoneService.review） ====================

    @Transactional
    public Map<String, Long> reviewMilestone(Long milestoneId, ReviewMilestoneRequest req) {
        return milestoneService.review(milestoneId, req);
    }

    // ==================== VO 构建 ====================

    private AdminTaskVO toTaskVO(Task t) {
        AdminTaskVO vo = new AdminTaskVO();
        vo.setId(t.getId());
        vo.setTitle(t.getTitle());
        vo.setCategoryId(t.getCategoryId());
        vo.setCategory(t.getCategory());
        vo.setEmployerId(t.getEmployerId());
        vo.setEmployerName(t.getEmployerName());
        vo.setDescription(t.getDescription());
        vo.setReward(t.getReward());
        vo.setLevelRequired(t.getLevelRequired());
        vo.setTotalSlots(t.getTotalSlots());
        vo.setSlotsLeft(t.getSlotsLeft());
        vo.setDeadline(t.getDeadline());
        vo.setStatus(t.getStatus());
        vo.setViewCount(t.getViewCount());
        vo.setCreatedAt(t.getCreatedAt());
        return vo;
    }

    private AdminTaskOrderVO toOrderVO(TaskOrder o, Map<Long, String> taskTitleMap, Map<Long, User> userMap) {
        AdminTaskOrderVO vo = new AdminTaskOrderVO();
        vo.setId(o.getId());
        vo.setTaskId(o.getTaskId());
        vo.setTaskTitle(taskTitleMap.get(o.getTaskId()));
        vo.setUserId(o.getUserId());
        User u = userMap.get(o.getUserId());
        if (u != null) {
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
        }
        vo.setProgress(o.getProgress());
        vo.setStatus(o.getStatus());
        vo.setRemark(o.getRemark());
        vo.setCreatedAt(o.getCreatedAt());
        return vo;
    }

    private AdminMilestoneSubmissionVO toSubmissionVO(MilestoneSubmission s,
                                                      Map<Long, TaskMilestone> milestoneMap,
                                                      Map<Long, String> taskTitleMap,
                                                      Map<Long, User> userMap) {
        AdminMilestoneSubmissionVO vo = new AdminMilestoneSubmissionVO();
        vo.setId(s.getId());
        vo.setMilestoneId(s.getMilestoneId());
        vo.setOrderId(s.getOrderId());
        vo.setUserId(s.getUserId());
        vo.setGithubUrl(s.getGithubUrl());
        vo.setDescription(s.getDescription());
        vo.setAttachments(s.getAttachments());
        vo.setSubmittedAt(s.getSubmittedAt());

        TaskMilestone m = milestoneMap.get(s.getMilestoneId());
        if (m != null) {
            vo.setTaskId(m.getTaskId());
            vo.setMilestoneTitle(m.getTitle());
            vo.setTaskTitle(taskTitleMap.get(m.getTaskId()));
            vo.setStatus(m.getStatus());
        }
        User u = userMap.get(s.getUserId());
        if (u != null) {
            vo.setUserNickname(u.getNickname());
        }
        return vo;
    }

    // ==================== 工具 ====================

    private Task getTaskEntity(Long id) {
        Task t = taskMapper.selectById(id);
        if (t == null || t.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        return t;
    }

    private void applyTaskFields(Task t, TaskRequest req) {
        t.setTitle(req.getTitle());
        t.setCategoryId(req.getCategoryId());
        if (req.getCategory() != null) {
            t.setCategory(req.getCategory());
        }
        t.setEmployerName(req.getEmployerName());
        t.setDescription(req.getDescription());
        t.setReward(req.getReward());
        t.setLevelRequired(req.getLevelRequired());
        // 编辑时若调整总名额，剩余名额跟随差量；新增时由 createTask 统一初始化
        if (req.getTotalSlots() != null) {
            Integer prevTotal = t.getTotalSlots();
            Integer prevLeft = t.getSlotsLeft() == null ? 0 : t.getSlotsLeft();
            if (prevTotal == null) {
                t.setSlotsLeft(req.getTotalSlots());
            } else {
                int delta = req.getTotalSlots() - prevTotal;
                t.setSlotsLeft(Math.max(0, prevLeft + delta));
            }
            t.setTotalSlots(req.getTotalSlots());
        }
        t.setDeadline(req.getDeadline());
        if (StringUtils.hasText(req.getStatus())) {
            t.setStatus(req.getStatus().toUpperCase());
        }
    }

    /** 依据 categoryId 回填分类名称冗余字段。 */
    private void resolveCategoryName(Task t) {
        if (t.getCategoryId() == null) {
            return;
        }
        TaskCategory c = categoryMapper.selectById(t.getCategoryId());
        if (c != null && c.getDeletedAt() == null) {
            t.setCategory(c.getName());
        }
    }

    private Map<Long, String> loadTaskTitles(List<TaskOrder> orders) {
        Set<Long> ids = new HashSet<>();
        for (TaskOrder o : orders) {
            if (o.getTaskId() != null) {
                ids.add(o.getTaskId());
            }
        }
        return loadTaskTitlesByIds(ids);
    }

    private Map<Long, String> loadTaskTitlesForMilestones(List<TaskMilestone> milestones) {
        Set<Long> ids = new HashSet<>();
        for (TaskMilestone m : milestones) {
            if (m.getTaskId() != null) {
                ids.add(m.getTaskId());
            }
        }
        return loadTaskTitlesByIds(ids);
    }

    private Map<Long, String> loadTaskTitlesByIds(Set<Long> ids) {
        Map<Long, String> map = new HashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .in(Task::getId, ids)
                .isNull(Task::getDeletedAt));
        for (Task t : tasks) {
            map.put(t.getId(), t.getTitle());
        }
        return map;
    }

    private Map<Long, User> loadUsers(List<TaskOrder> orders) {
        Set<Long> ids = new HashSet<>();
        for (TaskOrder o : orders) {
            if (o.getUserId() != null) {
                ids.add(o.getUserId());
            }
        }
        return loadUsersByIds(ids);
    }

    private Map<Long, User> loadUsersForSubmissions(List<MilestoneSubmission> subs) {
        Set<Long> ids = new HashSet<>();
        for (MilestoneSubmission s : subs) {
            if (s.getUserId() != null) {
                ids.add(s.getUserId());
            }
        }
        return loadUsersByIds(ids);
    }

    private Map<Long, User> loadUsersByIds(Set<Long> ids) {
        Map<Long, User> map = new HashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getId, ids)
                .isNull(User::getDeletedAt));
        for (User u : users) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private PageResult<AdminMilestoneSubmissionVO> emptyPage(PageQuery query) {
        Page<AdminMilestoneSubmissionVO> empty = new Page<>(query.getPage() == null ? 1 : query.getPage(),
                query.getPageSize() == null ? 10 : query.getPageSize());
        empty.setTotal(0);
        return PageResult.of(empty);
    }
}
