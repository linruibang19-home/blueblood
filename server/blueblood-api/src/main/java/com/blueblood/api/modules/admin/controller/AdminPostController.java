package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminPostQuery;
import com.blueblood.api.modules.admin.dto.AdminPostVO;
import com.blueblood.api.modules.admin.service.AdminCommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 帖子管理。
 */
@Tag(name = "后台-帖子管理", description = "帖子列表/显示隐藏/删除")
@RestController
@RequestMapping("/admin/post")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminCommunityService adminCommunityService;

    @Operation(summary = "帖子列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminPostVO>> list(AdminPostQuery query) {
        return Result.success(adminCommunityService.pagePost(query));
    }

    @Operation(summary = "显示/隐藏帖子(PUBLISHED/HIDDEN)")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminCommunityService.updatePostStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除帖子(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCommunityService.deletePost(id);
        return Result.success();
    }
}
