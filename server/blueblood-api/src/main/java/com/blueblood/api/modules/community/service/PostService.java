package com.blueblood.api.modules.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.community.dto.CommentRequest;
import com.blueblood.api.modules.community.dto.CommentVO;
import com.blueblood.api.modules.community.dto.CreatePostRequest;
import com.blueblood.api.modules.community.dto.PostDetailVO;
import com.blueblood.api.modules.community.dto.PostListItemVO;
import com.blueblood.api.modules.community.entity.Post;
import com.blueblood.api.modules.community.entity.PostComment;
import com.blueblood.api.modules.community.entity.PostFavorite;
import com.blueblood.api.modules.community.entity.PostLike;
import com.blueblood.api.modules.community.mapper.PostCommentMapper;
import com.blueblood.api.modules.community.mapper.PostFavoriteMapper;
import com.blueblood.api.modules.community.mapper.PostLikeMapper;
import com.blueblood.api.modules.community.mapper.PostMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子服务：发布、详情、列表、评论、点赞、收藏。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private static final int CONTENT_SUMMARY_LEN = 120;
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    // ============================== 列表 ==============================

    /**
     * 按小组分页查询帖子（可按 tag 过滤），轻量返回。
     */
    public PageResult<PostListItemVO> pageByGroup(Long groupId, Integer page, Integer pageSize, String tag) {
        Page<Post> p = new Page<>(page == null || page < 1 ? 1 : page,
                pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100));
        Page<Post> result = postMapper.selectPage(p, new LambdaQueryWrapper<Post>()
                .eq(Post::getGroupId, groupId)
                .eq(Post::getStatus, "PUBLISHED")
                .eq(tag != null && !tag.isBlank(), Post::getTag, tag)
                .isNull(Post::getDeletedAt)
                .orderByDesc(Post::getCreatedAt));

        List<Post> posts = result.getRecords();
        if (posts == null || posts.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), result.getTotal(),
                    result.getCurrent(), result.getSize());
        }

        // 批量查作者
        Set<Long> authorIds = posts.stream().map(Post::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> authorMap = loadUsers(authorIds);

        List<PostListItemVO> list = posts.stream().map(post -> {
            PostListItemVO vo = new PostListItemVO();
            vo.setId(post.getId());
            vo.setGroupId(post.getGroupId());
            vo.setAuthorId(post.getAuthorId());
            User author = authorMap.get(post.getAuthorId());
            vo.setAuthorName(author != null ? author.getNickname() : null);
            vo.setAuthorAvatar(author != null ? author.getAvatar() : null);
            vo.setTitle(post.getTitle());
            vo.setContent(summary(post.getContent()));
            vo.setTag(post.getTag());
            vo.setLikes(post.getLikes());
            vo.setComments(post.getComments());
            vo.setViews(post.getViews());
            vo.setCreatedAt(post.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    // ============================== 详情 ==============================

    @Transactional
    public PostDetailVO detail(Long id) {
        Post post = getPostOrThrow(id);
        User author = userMapper.selectById(post.getAuthorId());

        PostDetailVO vo = new PostDetailVO();
        vo.setId(post.getId());
        vo.setGroupId(post.getGroupId());
        vo.setAuthorId(post.getAuthorId());
        vo.setAuthorName(author != null ? author.getNickname() : null);
        vo.setAuthorAvatar(author != null ? author.getAvatar() : null);
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setImages(parseImages(post.getImages()));
        vo.setTag(post.getTag());
        vo.setLikes(post.getLikes());
        vo.setComments(post.getComments());
        vo.setViews(post.getViews());
        vo.setCreatedAt(post.getCreatedAt());

        // 当前用户的点赞 / 收藏状态
        Long cur = currentUserIdOrNull();
        vo.setLiked(cur != null && hasLiked(cur, id));
        vo.setFavorited(cur != null && hasFavorited(cur, id));

        // 浏览量 +1（仅 PUBLISHED 帖子）
        if ("PUBLISHED".equalsIgnoreCase(post.getStatus())) {
            Post patch = new Post();
            patch.setId(id);
            patch.setViews((post.getViews() == null ? 0 : post.getViews()) + 1);
            postMapper.updateById(patch);
            vo.setViews(patch.getViews());
        }
        return vo;
    }

    // ============================== 发布 ==============================

    @Transactional
    public Long create(CreatePostRequest req) {
        Post post = new Post();
        post.setGroupId(req.getGroupId());
        post.setAuthorId(currentUserIdOrNull());
        post.setTitle(req.getTitle());
        post.setContent(req.getContent() == null ? "" : req.getContent());
        post.setImages(serializeImages(req.getImages()));
        post.setTag((req.getTag() == null || req.getTag().isBlank()) ? "话题" : req.getTag());
        post.setLikes(0);
        post.setComments(0);
        post.setViews(0);
        post.setStatus("PUBLISHED");
        postMapper.insert(post);
        return post.getId();
    }

    // ============================== 评论 ==============================

    /**
     * 评论列表：一级评论 + 各自 replies。
     */
    public List<CommentVO> listComments(Long postId) {
        getPostOrThrow(postId);
        List<PostComment> all = postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .isNull(PostComment::getDeletedAt)
                .orderByAsc(PostComment::getCreatedAt));
        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> authorIds = all.stream().map(PostComment::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> authorMap = loadUsers(authorIds);

        Map<Long, CommentVO> voMap = new LinkedHashMap<>();
        for (PostComment c : all) {
            voMap.put(c.getId(), toVO(c, authorMap));
        }

        List<CommentVO> roots = new ArrayList<>();
        for (PostComment c : all) {
            CommentVO vo = voMap.get(c.getId());
            if (c.getParentId() == null) {
                roots.add(vo);
            } else {
                CommentVO parent = voMap.get(c.getParentId());
                if (parent != null) {
                    if (parent.getReplies() == null) {
                        parent.setReplies(new ArrayList<>());
                    }
                    parent.getReplies().add(vo);
                }
            }
        }
        roots.sort(Comparator.comparing(CommentVO::getCreatedAt));
        return roots;
    }

    @Transactional
    public Long addComment(Long postId, CommentRequest req) {
        Post post = getPostOrThrow(postId);
        Long cur = currentUserIdOrNull();

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setAuthorId(cur);
        comment.setContent(req.getContent());
        comment.setLikes(0);
        comment.setStatus("NORMAL");
        if (req.getParentId() != null) {
            // 校验父评论存在且属于同一帖子
            PostComment parent = postCommentMapper.selectById(req.getParentId());
            if (parent == null || parent.getDeletedAt() != null || !postId.equals(parent.getPostId())) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "父评论不存在");
            }
            // 二级回复：统一挂在一级评论下，避免无限嵌套
            comment.setParentId(parent.getParentId() == null ? parent.getId() : parent.getParentId());
        }
        postCommentMapper.insert(comment);

        // post.comments + 1
        Post patch = new Post();
        patch.setId(postId);
        patch.setComments((post.getComments() == null ? 0 : post.getComments()) + 1);
        postMapper.updateById(patch);
        return comment.getId();
    }

    // ============================== 点赞 toggle ==============================

    @Transactional
    public Map<String, Object> toggleLike(Long postId) {
        Post post = getPostOrThrow(postId);
        Long userId = currentUserIdOrNull();
        boolean liked = doToggle(postId, userId, postLikeMapper, PostLike.class, true);

        // 更新计数
        int newCount = liked
                ? (post.getLikes() == null ? 0 : post.getLikes()) + 1
                : Math.max(0, (post.getLikes() == null ? 0 : post.getLikes()) - 1);
        Post patch = new Post();
        patch.setId(postId);
        patch.setLikes(newCount);
        postMapper.updateById(patch);

        return Map.of("liked", liked, "likes", newCount);
    }

    // ============================== 收藏 toggle ==============================

    @Transactional
    public Map<String, Object> toggleFavorite(Long postId) {
        getPostOrThrow(postId); // 校验帖子存在
        Long userId = currentUserIdOrNull();
        boolean favorited = doToggle(postId, userId, postFavoriteMapper, PostFavorite.class, false);

        // 收藏不维护计数列，仅返回状态
        return Map.of("favorited", favorited);
    }

    // ============================== 内部 ==============================

    private Post getPostOrThrow(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private boolean hasLiked(Long userId, Long postId) {
        Long c = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId)
                .isNull(PostLike::getDeletedAt));
        return c != null && c > 0;
    }

    private boolean hasFavorited(Long userId, Long postId) {
        Long c = postFavoriteMapper.selectCount(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getUserId, userId)
                .isNull(PostFavorite::getDeletedAt));
        return c != null && c > 0;
    }

    /**
     * 点赞/收藏 toggle 通用逻辑（依赖 post_id+user_id 唯一键）：
     * - 已存在且未软删 → 软删（取消），返回 false
     * - 已存在但已软删 → 恢复（清空 deletedAt），返回 true
     * - 不存在 → 新增，返回 true
     */
    private <T extends com.blueblood.api.common.entity.BaseEntity> boolean doToggle(
            Long postId, Long userId, com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper,
            Class<T> clazz, boolean isLike) {

        // 查既有记录（含已软删，用于恢复）。deletedAt 在 BaseEntity 中 select=false，需自定义查询绕过。
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("post_id", postId).eq("user_id", userId).last("LIMIT 1");
        T exist = mapper.selectOne(qw);

        try {
            if (exist != null) {
                boolean active = exist.getDeletedAt() == null;
                if (active) {
                    // 取消：软删
                    exist.setDeletedAt(LocalDateTime.now());
                    mapper.updateById(exist);
                    return false;
                } else {
                    // 恢复
                    exist.setDeletedAt(null);
                    mapper.updateById(exist);
                    return true;
                }
            }
            T entity = clazz.getDeclaredConstructor().newInstance();
            if (entity instanceof PostLike pl) {
                pl.setPostId(postId);
                pl.setUserId(userId);
            } else if (entity instanceof PostFavorite pf) {
                pf.setPostId(postId);
                pf.setUserId(userId);
            }
            mapper.insert(entity);
            return true;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "操作失败");
        }
    }

    private Map<Long, User> loadUsers(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectBatchIds(ids);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    private CommentVO toVO(PostComment c, Map<Long, User> authorMap) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setParentId(c.getParentId());
        vo.setAuthorId(c.getAuthorId());
        User author = authorMap.get(c.getAuthorId());
        vo.setAuthorName(author != null ? author.getNickname() : null);
        vo.setAuthorAvatar(author != null ? author.getAvatar() : null);
        vo.setContent(c.getContent());
        vo.setLikes(c.getLikes());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    private String summary(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > CONTENT_SUMMARY_LEN ? content.substring(0, CONTENT_SUMMARY_LEN) + "..." : content;
    }

    private List<String> parseImages(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (Exception e) {
            log.warn("解析帖子图片失败: {}", json, e);
            return Collections.emptyList();
        }
    }

    private String serializeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            log.warn("序列化帖子图片失败", e);
            return null;
        }
    }

    private Long currentUserIdOrNull() {
        try {
            return com.blueblood.api.security.SecurityUtils.currentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
