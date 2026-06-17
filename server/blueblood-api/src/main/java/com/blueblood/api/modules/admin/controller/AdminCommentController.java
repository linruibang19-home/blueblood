package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminCommentVO;
import com.blueblood.api.modules.admin.service.AdminCommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 评论管理。
 */
@Tag(name = "后台-评论管理", description = "评论列表/显示隐藏/删除")
@RestController
@RequestMapping("/admin/comment")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminCommunityService adminCommunityService;

    @Operation(summary = "评论列表(可按帖子筛选分页)")
    @GetMapping
    public Result<PageResult<AdminCommentVO>> list(PageQuery query,
                                                   @RequestParam(required = false) Long postId) {
        return Result.success(adminCommunityService.pageComment(postId, query));
    }

    @Operation(summary = "显示/隐藏评论(NORMAL/DELETED)")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminCommunityService.updateCommentStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除评论(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCommunityService.deleteComment(id);
        return Result.success();
    }
}
