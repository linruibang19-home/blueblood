package com.blueblood.api.modules.community.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.community.dto.ActivityVO;
import com.blueblood.api.modules.community.service.ActivityService;
import com.blueblood.api.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 活动模块：小组活动列表、详情、报名 / 取消报名。
 * 注意：列表接口挂载在 /group/{groupId}/activities，其余在 /activity。
 */
@Tag(name = "活动", description = "小组活动列表、详情、报名")
@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "小组活动分页列表")
    @GetMapping("/group/{groupId}/activities")
    public Result<PageResult<ActivityVO>> listByGroup(@PathVariable Long groupId,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer pageSize) {
        return Result.success(activityService.pageByGroup(groupId, page, pageSize));
    }

    @Operation(summary = "活动详情（含当前用户报名状态）")
    @GetMapping("/activity/{id}")
    public Result<ActivityVO> detail(@PathVariable Long id) {
        return Result.success(activityService.detail(id));
    }

    @Operation(summary = "报名活动")
    @PostMapping("/activity/{id}/signup")
    public Result<Void> signup(@PathVariable Long id) {
        activityService.signup(id, SecurityUtils.currentUserId());
        return Result.success();
    }

    @Operation(summary = "取消报名")
    @PostMapping("/activity/{id}/signup/cancel")
    public Result<Void> cancelSignup(@PathVariable Long id) {
        activityService.cancelSignup(id, SecurityUtils.currentUserId());
        return Result.success();
    }
}
