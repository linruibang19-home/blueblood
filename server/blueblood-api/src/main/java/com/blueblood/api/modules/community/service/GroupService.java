package com.blueblood.api.modules.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.community.dto.GroupVO;
import com.blueblood.api.modules.community.entity.GroupMember;
import com.blueblood.api.modules.community.entity.InterestGroup;
import com.blueblood.api.modules.community.mapper.GroupMemberMapper;
import com.blueblood.api.modules.community.mapper.InterestGroupMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小组子域：小组列表 / 详情 / 我加入的 / 加入 / 退出。
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final InterestGroupMapper interestGroupMapper;
    private final GroupMemberMapper groupMemberMapper;

    // ============================== 列表 ==============================

    /**
     * 小组分页列表（可按 category 过滤）。
     */
    public PageResult<GroupVO> page(Integer page, Integer pageSize, String category) {
        Long currentUserId = SecurityUtils.currentUserId();

        Page<InterestGroup> p = new Page<>(
                page == null || page < 1 ? 1 : page,
                pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100));

        LambdaQueryWrapper<InterestGroup> qw = new LambdaQueryWrapper<InterestGroup>()
                .isNull(InterestGroup::getDeletedAt)
                .orderByDesc(InterestGroup::getCreatedAt);
        if (category != null && !category.isBlank()) {
            qw.eq(InterestGroup::getCategory, category);
        }

        Page<InterestGroup> result = interestGroupMapper.selectPage(p, qw);
        List<GroupVO> list = result.getRecords() == null ? new ArrayList<>()
                : result.getRecords().stream().map(g -> toListItemVO(g, currentUserId)).collect(Collectors.toList());

        // PageResult.of 直接用 IPage；这里手动构造以承载 VO 列表
        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    // ============================== 详情 ==============================

    public GroupVO detail(Long groupId) {
        Long currentUserId = SecurityUtils.currentUserId();
        InterestGroup group = getGroupOrThrow(groupId);
        return toDetailVO(group, currentUserId);
    }

    // ============================== 我加入的 ==============================

    public List<GroupVO> mine() {
        Long currentUserId = SecurityUtils.currentUserId();

        List<GroupMember> memberships = groupMemberMapper.selectList(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getUserId, currentUserId)
                .isNull(GroupMember::getDeletedAt));
        if (memberships.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> groupIds = memberships.stream().map(GroupMember::getGroupId).distinct().collect(Collectors.toList());
        List<InterestGroup> groups = interestGroupMapper.selectList(new LambdaQueryWrapper<InterestGroup>()
                .in(InterestGroup::getId, groupIds)
                .isNull(InterestGroup::getDeletedAt));

        return groups.stream().map(g -> toListItemVO(g, currentUserId)).collect(Collectors.toList());
    }

    // ============================== 加入 ==============================

    @Transactional
    public void join(Long groupId) {
        Long currentUserId = SecurityUtils.currentUserId();
        InterestGroup group = getGroupOrThrow(groupId);

        // 已加入（未软删的成员记录存在）则抛重复
        Long exist = groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, currentUserId)
                .isNull(GroupMember::getDeletedAt));
        if (exist != null && exist > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "已加入该小组");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(currentUserId);
        member.setRole("member");
        member.setJoinedAt(LocalDateTime.now());
        groupMemberMapper.insert(member);

        // member_count + 1
        InterestGroup patch = new InterestGroup();
        patch.setId(group.getId());
        patch.setMemberCount(safeCount(group.getMemberCount()) + 1);
        interestGroupMapper.updateById(patch);
    }

    // ============================== 退出 ==============================

    @Transactional
    public void leave(Long groupId) {
        Long currentUserId = SecurityUtils.currentUserId();
        InterestGroup group = getGroupOrThrow(groupId);

        GroupMember member = groupMemberMapper.selectOne(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, currentUserId)
                .isNull(GroupMember::getDeletedAt));
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "您不是该小组成员");
        }

        // 软删成员记录
        GroupMember patch = new GroupMember();
        patch.setId(member.getId());
        patch.setDeletedAt(LocalDateTime.now());
        groupMemberMapper.updateById(patch);

        // member_count - 1，下限 0
        InterestGroup gPatch = new InterestGroup();
        gPatch.setId(group.getId());
        gPatch.setMemberCount(Math.max(0, safeCount(group.getMemberCount()) - 1));
        interestGroupMapper.updateById(gPatch);
    }

    // ============================== 工具 ==============================

    private InterestGroup getGroupOrThrow(Long groupId) {
        InterestGroup group = interestGroupMapper.selectById(groupId);
        if (group == null || group.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "小组不存在");
        }
        return group;
    }

    private GroupVO toListItemVO(InterestGroup g, Long currentUserId) {
        GroupVO vo = new GroupVO();
        vo.setId(g.getId());
        vo.setName(g.getName());
        vo.setDescription(g.getDescription());
        vo.setCoverImage(g.getCoverImage());
        vo.setCategory(g.getCategory());
        vo.setMemberCount(g.getMemberCount());
        vo.setPostCount(g.getPostCount());
        vo.setActivityCount(g.getActivityCount());
        vo.setStatus(g.getStatus());
        vo.setCreatedAt(g.getCreatedAt());
        vo.setJoined(isMember(g.getId(), currentUserId));
        return vo;
    }

    private GroupVO toDetailVO(InterestGroup g, Long currentUserId) {
        GroupVO vo = toListItemVO(g, currentUserId);
        vo.setTags(g.getTags());
        // 填充角色
        GroupMember member = groupMemberMapper.selectOne(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, g.getId())
                .eq(GroupMember::getUserId, currentUserId)
                .isNull(GroupMember::getDeletedAt));
        if (member != null) {
            vo.setRole(member.getRole());
            vo.setJoined(true);
        } else {
            vo.setRole(null);
            vo.setJoined(false);
        }
        return vo;
    }

    private boolean isMember(Long groupId, Long userId) {
        Long c = groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .isNull(GroupMember::getDeletedAt));
        return c != null && c > 0;
    }

    private int safeCount(Integer c) {
        return c == null ? 0 : Math.max(0, c);
    }
}
