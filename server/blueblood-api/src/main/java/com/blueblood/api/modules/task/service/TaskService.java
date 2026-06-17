package com.blueblood.api.modules.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.task.dto.MilestoneVO;
import com.blueblood.api.modules.task.dto.TaskCategoryVO;
import com.blueblood.api.modules.task.dto.TaskListItemVO;
import com.blueblood.api.modules.task.dto.TaskOrderVO;
import com.blueblood.api.modules.task.dto.TaskVO;
import com.blueblood.api.modules.task.entity.Task;
import com.blueblood.api.modules.task.entity.TaskCategory;
import com.blueblood.api.modules.task.entity.TaskMilestone;
import com.blueblood.api.modules.task.entity.TaskOrder;
import com.blueblood.api.modules.task.entity.TaskSkill;
import com.blueblood.api.modules.task.mapper.TaskCategoryMapper;
import com.blueblood.api.modules.task.mapper.TaskMapper;
import com.blueblood.api.modules.task.mapper.TaskMilestoneMapper;
import com.blueblood.api.modules.task.mapper.TaskOrderMapper;
import com.blueblood.api.modules.task.mapper.TaskSkillMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务服务：分类、列表筛选、详情、接单、我的订单、执行详情。
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final TaskCategoryMapper categoryMapper;
    private final TaskSkillMapper skillMapper;
    private final TaskOrderMapper orderMapper;
    private final TaskMilestoneMapper milestoneMapper;
    private final UserMapper userMapper;
    private final MilestoneService milestoneService;

    /** 任务大厅默认可见状态 */
    private static final List<String> HALL_STATUSES = Arrays.asList("APPROVED", "RECRUITING", "IN_PROGRESS");

    // ============================== 分类 ==============================

    public List<TaskCategoryVO> categories() {
        List<TaskCategory> list = categoryMapper.selectList(new LambdaQueryWrapper<TaskCategory>()
                .eq(TaskCategory::getStatus, "ACTIVE")
                .isNull(TaskCategory::getDeletedAt)
                .orderByAsc(TaskCategory::getCategoryOrder));
        return list.stream().map(c -> {
            TaskCategoryVO vo = new TaskCategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setIcon(c.getIcon());
            vo.setTaskCount(c.getTaskCount());
            vo.setCategoryOrder(c.getCategoryOrder());
            return vo;
        }).toList();
    }

    // ============================== 列表 ==============================

    public PageResult<TaskListItemVO> pageTasks(String category, String keyword, String status,
                                                Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .isNull(Task::getDeletedAt)
                .orderByDesc(Task::getCreatedAt);

        if (StringUtils.hasText(status)) {
            wrapper.eq(Task::getStatus, status);
        } else {
            wrapper.in(Task::getStatus, HALL_STATUSES);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Task::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Task::getTitle, keyword);
        }

        Page<Task> result = taskMapper.selectPage(new Page<>(p, size), wrapper);
        return PageResult.of(result.convert(this::toListItem));
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

    // ============================== 详情 ==============================

    public TaskVO detail(Long id) {
        Task t = getTask(id);
        // 浏览数 +1
        Task patch = new Task();
        patch.setId(id);
        patch.setViewCount((t.getViewCount() == null ? 0 : t.getViewCount()) + 1);
        taskMapper.updateById(patch);
        t.setViewCount(patch.getViewCount());

        TaskVO vo = new TaskVO();
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
        vo.setSkills(loadSkillNames(t.getId()));
        vo.setAccepted(hasOrder(t.getId(), SecurityUtils.currentUserId()));
        return vo;
    }

    // ============================== 接单 ==============================

    @Transactional
    public Map<String, Long> accept(Long taskId) {
        Task t = getTask(taskId);

        if (!HALL_STATUSES.contains(t.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "任务暂不可接单");
        }
        if (t.getSlotsLeft() == null || t.getSlotsLeft() <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "名额已满");
        }
        Long userId = SecurityUtils.currentUserId();
        if (hasOrder(taskId, userId)) {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "你已接取该任务");
        }
        User user = userMapper.selectById(userId);
        int userLevel = user == null ? 1 : (user.getLevel() == null ? 1 : user.getLevel());
        if (userLevel < (t.getLevelRequired() == null ? 1 : t.getLevelRequired())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED,
                    "等级不足，需 LV" + t.getLevelRequired());
        }

        // 创建订单
        TaskOrder order = new TaskOrder();
        order.setTaskId(taskId);
        order.setUserId(userId);
        order.setProgress(0);
        order.setStatus("in_progress");
        orderMapper.insert(order);

        // 创建默认里程碑（任务交付，订单维度）
        TaskMilestone m = new TaskMilestone();
        m.setOrderId(order.getId());
        m.setTaskId(taskId);
        m.setTitle("任务交付");
        m.setDescription("完成全部任务要求并提交成果");
        m.setMilestoneOrder(1);
        m.setStatus("NOT_STARTED");
        milestoneMapper.insert(m);

        // 名额 -1，名额满则任务进入进行中
        t.setSlotsLeft(t.getSlotsLeft() - 1);
        if (t.getSlotsLeft() <= 0) {
            t.setStatus("IN_PROGRESS");
        }
        taskMapper.updateById(t);

        return Map.of("orderId", order.getId());
    }

    // ============================== 我的任务 / 执行详情 ==============================

    public PageResult<TaskOrderVO> myOrders(String status, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        Long userId = SecurityUtils.currentUserId();

        LambdaQueryWrapper<TaskOrder> wrapper = new LambdaQueryWrapper<TaskOrder>()
                .eq(TaskOrder::getUserId, userId)
                .isNull(TaskOrder::getDeletedAt)
                .orderByDesc(TaskOrder::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(TaskOrder::getStatus, status);
        }

        Page<TaskOrder> result = orderMapper.selectPage(new Page<>(p, size), wrapper);
        return PageResult.of(result.convert(this::toOrderVO));
    }

    public TaskOrderVO orderDetail(Long orderId) {
        TaskOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        // 仅订单所有者或管理员可看（管理员由 @PreAuthorize 控制；这里校验所有者）
        if (!order.getUserId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看该订单");
        }
        return toOrderVO(order);
    }

    private TaskOrderVO toOrderVO(TaskOrder order) {
        Task t = taskMapper.selectById(order.getTaskId());
        TaskOrderVO vo = new TaskOrderVO();
        vo.setId(order.getId());
        vo.setTaskId(order.getTaskId());
        vo.setTaskTitle(t == null ? "" : t.getTitle());
        vo.setCategory(t == null ? "" : t.getCategory());
        vo.setEmployerName(t == null ? "" : t.getEmployerName());
        vo.setReward(t == null ? null : t.getReward());
        vo.setProgress(order.getProgress());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());

        List<TaskMilestone> milestones = milestoneMapper.selectList(new LambdaQueryWrapper<TaskMilestone>()
                .eq(TaskMilestone::getOrderId, order.getId())
                .isNull(TaskMilestone::getDeletedAt)
                .orderByAsc(TaskMilestone::getMilestoneOrder));
        List<MilestoneVO> mvo = milestones.stream()
                .map(milestoneService::buildVO)
                .collect(Collectors.toList());
        vo.setMilestones(mvo);
        return vo;
    }

    // ============================== 工具 ==============================

    private Task getTask(Long id) {
        Task t = taskMapper.selectById(id);
        if (t == null || t.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "任务不存在");
        }
        return t;
    }

    private List<String> loadSkillNames(Long taskId) {
        return skillMapper.selectList(new LambdaQueryWrapper<TaskSkill>()
                        .eq(TaskSkill::getTaskId, taskId)
                        .isNull(TaskSkill::getDeletedAt))
                .stream().map(TaskSkill::getName).toList();
    }

    private boolean hasOrder(Long taskId, Long userId) {
        Long c = orderMapper.selectCount(new LambdaQueryWrapper<TaskOrder>()
                .eq(TaskOrder::getTaskId, taskId)
                .eq(TaskOrder::getUserId, userId)
                .isNull(TaskOrder::getDeletedAt));
        return c != null && c > 0;
    }
}
