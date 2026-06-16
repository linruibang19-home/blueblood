package com.blueblood.api.modules.community.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.community.dto.CommentRequest;
import com.blueblood.api.modules.community.dto.CommentVO;
import com.blueblood.api.modules.community.dto.CreatePostRequest;
import com.blueblood.api.modules.community.dto.PostDetailVO;
import com.blueblood.api.modules.community.dto.PostListItemVO;
import com.blueblood.api.modules.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 帖子模块：发布、详情、列表、评论、点赞、收藏。
 */
@Tag(name = "帖子", description = "帖子发布、详情、评论、点赞、收藏")
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "按小组分页查询帖子")
    @GetMapping("/group/{groupId}/posts")
    public Result<PageResult<PostListItemVO>> listByGroup(@PathVariable Long groupId,
                                                          @RequestParam(required = false) Integer page,
                                                          @RequestParam(required = false) Integer pageSize,
                                                          @RequestParam(required = false) String tag) {
        return Result.success(postService.pageByGroup(groupId, page, pageSize, tag));
    }

    @Operation(summary = "帖子详情（含作者信息、点赞/收藏状态）")
    @GetMapping("/post/{id}")
    public Result<PostDetailVO> detail(@PathVariable Long id) {
        return Result.success(postService.detail(id));
    }

    @Operation(summary = "发布帖子")
    @PostMapping("/post")
    public Result<Map<String, Object>> create(@Valid @RequestBody CreatePostRequest req) {
        Long id = postService.create(req);
        return Result.success(Map.of("id", id));
    }

    @Operation(summary = "评论列表（一级评论 + 子回复）")
    @GetMapping("/post/{id}/comments")
    public Result<List<CommentVO>> listComments(@PathVariable Long id) {
        return Result.success(postService.listComments(id));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/post/{id}/comments")
    public Result<Map<String, Object>> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest req) {
        Long commentId = postService.addComment(id, req);
        return Result.success(Map.of("id", commentId));
    }

    @Operation(summary = "点赞 / 取消点赞")
    @PostMapping("/post/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        return Result.success(postService.toggleLike(id));
    }

    @Operation(summary = "收藏 / 取消收藏")
    @PostMapping("/post/{id}/favorite")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Long id) {
        return Result.success(postService.toggleFavorite(id));
    }
}
