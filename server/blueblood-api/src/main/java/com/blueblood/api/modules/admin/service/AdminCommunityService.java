package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.*;
import com.blueblood.api.modules.community.entity.Activity;
import com.blueblood.api.modules.community.entity.InterestGroup;
import com.blueblood.api.modules.community.entity.Post;
import com.blueblood.api.modules.community.entity.PostComment;
import com.blueblood.api.modules.community.mapper.ActivityMapper;
import com.blueblood.api.modules.community.mapper.InterestGroupMapper;
import com.blueblood.api.modules.community.mapper.PostCommentMapper;
import com.blueblood.api.modules.community.mapper.PostMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 后台社区内容管理：兴趣小组 / 帖子 / 评论 / 活动的增删改查与状态管理。
 * 列表 VO 携带作者/组长昵称、所属小组名称等冗余字段。
 * 删除均为软删(set deletedAt=now)。
 */
@Service
@RequiredArgsConstructor
public class AdminCommunityService {

    private final InterestGroupMapper groupMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    // ==================== 小组 ====================

    public PageResult<AdminGroupVO> pageGroup(AdminGroupQuery query) {
        Page<InterestGroup> page = query.toPage();
        LambdaQueryWrapper<InterestGroup> wrapper = new LambdaQueryWrapper<InterestGroup>()
                .isNull(InterestGroup::getDeletedAt)
                .orderByDesc(InterestGroup::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(InterestGroup::getName, kw)
                    .or().like(InterestGroup::getDescription, kw));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(InterestGroup::getStatus, query.getStatus());
        }
        Page<InterestGroup> result = groupMapper.selectPage(page, wrapper);
        Map<Long, String> nicknameMap = loadNicknames(
                result.getRecords().stream().map(InterestGroup::getLeaderId).filter(Objects::nonNull).distinct().toList());
        return PageResult.of(result.convert(g -> toGroupVO(g, nicknameMap.get(g.getLeaderId()))));
    }

    public AdminGroupVO getGroup(Long id) {
        InterestGroup g = getGroupEntity(id);
        String nickname = g.getLeaderId() == null ? null : nicknameOf(g.getLeaderId());
        return toGroupVO(g, nickname);
    }

    @Transactional
    public Long createGroup(GroupRequest req) {
        InterestGroup g = new InterestGroup();
        applyGroupFields(g, req);
        if (!StringUtils.hasText(g.getStatus())) {
            g.setStatus("ACTIVE");
        }
        g.setMemberCount(0);
        g.setPostCount(0);
        g.setActivityCount(0);
        groupMapper.insert(g);
        return g.getId();
    }

    @Transactional
    public void updateGroup(Long id, GroupRequest req) {
        InterestGroup exists = getGroupEntity(id);
        applyGroupFields(exists, req);
        groupMapper.updateById(exists);
    }

    @Transactional
    public void updateGroupStatus(Long id, String status) {
        if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法小组状态");
        }
        InterestGroup patch = new InterestGroup();
        patch.setId(id);
        patch.setStatus(status.toUpperCase());
        groupMapper.updateById(patch);
    }

    @Transactional
    public void deleteGroup(Long id) {
        InterestGroup g = getGroupEntity(id);
        InterestGroup patch = new InterestGroup();
        patch.setId(g.getId());
        patch.setDeletedAt(LocalDateTime.now());
        groupMapper.updateById(patch);
    }

    // ==================== 帖子 ====================

    public PageResult<AdminPostVO> pagePost(AdminPostQuery query) {
        Page<Post> page = query.toPage();
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .isNull(Post::getDeletedAt)
                .orderByDesc(Post::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Post::getTitle, kw).or().like(Post::getContent, kw));
        }
        if (query.getGroupId() != null) {
            wrapper.eq(Post::getGroupId, query.getGroupId());
        }
        if (StringUtils.hasText(query.getTag())) {
            wrapper.eq(Post::getTag, query.getTag());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Post::getStatus, query.getStatus());
        }
        Page<Post> result = postMapper.selectPage(page, wrapper);
        List<Post> records = result.getRecords();
        Map<Long, String> authorMap = loadNicknames(
                records.stream().map(Post::getAuthorId).filter(Objects::nonNull).distinct().toList());
        Map<Long, String> groupMap = loadGroupNames(
                records.stream().map(Post::getGroupId).filter(Objects::nonNull).distinct().toList());
        return PageResult.of(result.convert(p ->
                toPostVO(p, authorMap.get(p.getAuthorId()), groupMap.get(p.getGroupId()))));
    }

    @Transactional
    public void updatePostStatus(Long id, String status) {
        if (!"HIDDEN".equalsIgnoreCase(status) && !"PUBLISHED".equalsIgnoreCase(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法帖子状态");
        }
        Post patch = new Post();
        patch.setId(id);
        patch.setStatus(status.toUpperCase());
        postMapper.updateById(patch);
    }

    @Transactional
    public void deletePost(Long id) {
        Post p = getPostEntity(id);
        Post patch = new Post();
        patch.setId(p.getId());
        patch.setDeletedAt(LocalDateTime.now());
        postMapper.updateById(patch);
    }

    // ==================== 评论 ====================

    public PageResult<AdminCommentVO> pageComment(Long postId, PageQuery query) {
        Page<PostComment> page = query.toPage();
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .isNull(PostComment::getDeletedAt)
                .orderByDesc(PostComment::getCreatedAt);
        if (postId != null) {
            wrapper.eq(PostComment::getPostId, postId);
        }
        Page<PostComment> result = commentMapper.selectPage(page, wrapper);
        Map<Long, String> authorMap = loadNicknames(
                result.getRecords().stream().map(PostComment::getAuthorId).filter(Objects::nonNull).distinct().toList());
        return PageResult.of(result.convert(c -> toCommentVO(c, authorMap.get(c.getAuthorId()))));
    }

    @Transactional
    public void updateCommentStatus(Long id, String status) {
        if (!"NORMAL".equalsIgnoreCase(status) && !"DELETED".equalsIgnoreCase(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法评论状态");
        }
        PostComment patch = new PostComment();
        patch.setId(id);
        patch.setStatus(status.toUpperCase());
        commentMapper.updateById(patch);
    }

    @Transactional
    public void deleteComment(Long id) {
        PostComment c = getCommentEntity(id);
        PostComment patch = new PostComment();
        patch.setId(c.getId());
        patch.setDeletedAt(LocalDateTime.now());
        commentMapper.updateById(patch);
    }

    // ==================== 活动 ====================

    public PageResult<AdminActivityVO> pageActivity(AdminActivityQuery query) {
        Page<Activity> page = query.toPage();
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .isNull(Activity::getDeletedAt)
                .orderByDesc(Activity::getCreatedAt);
        if (query.getGroupId() != null) {
            wrapper.eq(Activity::getGroupId, query.getGroupId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Activity::getTitle, kw).or().like(Activity::getLocation, kw));
        }
        Page<Activity> result = activityMapper.selectPage(page, wrapper);
        Map<Long, String> groupMap = loadGroupNames(
                result.getRecords().stream().map(Activity::getGroupId).filter(Objects::nonNull).distinct().toList());
        return PageResult.of(result.convert(a -> toActivityVO(a, groupMap.get(a.getGroupId()))));
    }

    @Transactional
    public Long createActivity(ActivityRequest req) {
        Activity a = new Activity();
        applyActivityFields(a, req);
        a.setSignupCount(0);
        if (!StringUtils.hasText(a.getStatus())) {
            a.setStatus("upcoming");
        }
        activityMapper.insert(a);
        return a.getId();
    }

    @Transactional
    public void updateActivity(Long id, ActivityRequest req) {
        Activity exists = getActivityEntity(id);
        applyActivityFields(exists, req);
        activityMapper.updateById(exists);
    }

    @Transactional
    public void deleteActivity(Long id) {
        Activity a = getActivityEntity(id);
        Activity patch = new Activity();
        patch.setId(a.getId());
        patch.setDeletedAt(LocalDateTime.now());
        activityMapper.updateById(patch);
    }

    // ==================== 实体取数 ====================

    private InterestGroup getGroupEntity(Long id) {
        InterestGroup g = groupMapper.selectById(id);
        if (g == null || g.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "小组不存在");
        }
        return g;
    }

    private Post getPostEntity(Long id) {
        Post p = postMapper.selectById(id);
        if (p == null || p.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "帖子不存在");
        }
        return p;
    }

    private PostComment getCommentEntity(Long id) {
        PostComment c = commentMapper.selectById(id);
        if (c == null || c.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "评论不存在");
        }
        return c;
    }

    private Activity getActivityEntity(Long id) {
        Activity a = activityMapper.selectById(id);
        if (a == null || a.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "活动不存在");
        }
        return a;
    }

    // ==================== 字段拷贝 ====================

    private void applyGroupFields(InterestGroup g, GroupRequest req) {
        g.setName(req.getName());
        g.setDescription(req.getDescription());
        g.setCoverImage(req.getCoverImage());
        if (req.getLeaderId() != null) {
            g.setLeaderId(req.getLeaderId());
        }
        if (StringUtils.hasText(req.getCategory())) {
            g.setCategory(req.getCategory());
        }
        if (req.getTags() != null) {
            g.setTags(req.getTags());
        }
        if (StringUtils.hasText(req.getStatus())) {
            g.setStatus(req.getStatus().toUpperCase());
        }
    }

    private void applyActivityFields(Activity a, ActivityRequest req) {
        if (req.getGroupId() != null) {
            a.setGroupId(req.getGroupId());
        }
        a.setTitle(req.getTitle());
        a.setDescription(req.getDescription());
        a.setCoverImage(req.getCoverImage());
        a.setStartTime(req.getStartTime());
        a.setEndTime(req.getEndTime());
        a.setLocation(req.getLocation());
        if (req.getMaxCount() != null) {
            a.setMaxCount(req.getMaxCount());
        }
        if (StringUtils.hasText(req.getStatus())) {
            a.setStatus(req.getStatus());
        }
    }

    // ==================== VO 映射 ====================

    private AdminGroupVO toGroupVO(InterestGroup g, String leaderNickname) {
        AdminGroupVO vo = new AdminGroupVO();
        vo.setId(g.getId());
        vo.setName(g.getName());
        vo.setDescription(g.getDescription());
        vo.setCoverImage(g.getCoverImage());
        vo.setLeaderId(g.getLeaderId());
        vo.setLeaderNickname(leaderNickname);
        vo.setCategory(g.getCategory());
        vo.setTags(g.getTags());
        vo.setMemberCount(g.getMemberCount());
        vo.setPostCount(g.getPostCount());
        vo.setActivityCount(g.getActivityCount());
        vo.setStatus(g.getStatus());
        vo.setCreatedAt(g.getCreatedAt());
        return vo;
    }

    private AdminPostVO toPostVO(Post p, String authorNickname, String groupName) {
        AdminPostVO vo = new AdminPostVO();
        vo.setId(p.getId());
        vo.setGroupId(p.getGroupId());
        vo.setGroupName(groupName);
        vo.setAuthorId(p.getAuthorId());
        vo.setAuthorNickname(authorNickname);
        vo.setTitle(p.getTitle());
        vo.setContent(p.getContent());
        vo.setImages(p.getImages());
        vo.setTag(p.getTag());
        vo.setLikes(p.getLikes());
        vo.setComments(p.getComments());
        vo.setViews(p.getViews());
        vo.setStatus(p.getStatus());
        vo.setCreatedAt(p.getCreatedAt());
        return vo;
    }

    private AdminCommentVO toCommentVO(PostComment c, String authorNickname) {
        AdminCommentVO vo = new AdminCommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setAuthorId(c.getAuthorId());
        vo.setAuthorNickname(authorNickname);
        vo.setParentId(c.getParentId());
        vo.setContent(c.getContent());
        vo.setLikes(c.getLikes());
        vo.setStatus(c.getStatus());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    private AdminActivityVO toActivityVO(Activity a, String groupName) {
        AdminActivityVO vo = new AdminActivityVO();
        vo.setId(a.getId());
        vo.setGroupId(a.getGroupId());
        vo.setGroupName(groupName);
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setCoverImage(a.getCoverImage());
        vo.setStartTime(a.getStartTime());
        vo.setEndTime(a.getEndTime());
        vo.setLocation(a.getLocation());
        vo.setSignupCount(a.getSignupCount());
        vo.setMaxCount(a.getMaxCount());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }

    // ==================== 批量关联 ====================

    private String nicknameOf(Long userId) {
        if (userId == null) {
            return null;
        }
        User u = userMapper.selectById(userId);
        return u == null || u.getDeletedAt() != null ? null : u.getNickname();
    }

    private Map<Long, String> loadNicknames(List<Long> userIds) {
        Map<Long, String> map = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        for (User u : users) {
            if (u.getDeletedAt() == null) {
                map.put(u.getId(), u.getNickname());
            }
        }
        return map;
    }

    private Map<Long, String> loadGroupNames(List<Long> groupIds) {
        Map<Long, String> map = new HashMap<>();
        if (groupIds == null || groupIds.isEmpty()) {
            return map;
        }
        List<InterestGroup> groups = groupMapper.selectBatchIds(groupIds);
        for (InterestGroup g : groups) {
            if (g.getDeletedAt() == null) {
                map.put(g.getId(), g.getName());
            }
        }
        return map;
    }
}
