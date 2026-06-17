package com.blueblood.api.modules.user.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.user.dto.SkillRequest;
import com.blueblood.api.modules.user.dto.UpdateProfileRequest;
import com.blueblood.api.modules.user.dto.UserVO;
import com.blueblood.api.modules.user.dto.VerificationReviewRequest;
import com.blueblood.api.modules.user.dto.VerificationSubmitRequest;
import com.blueblood.api.modules.user.entity.UserCreditLog;
import com.blueblood.api.modules.user.entity.UserLevelLog;
import com.blueblood.api.modules.user.entity.UserSkill;
import com.blueblood.api.modules.user.entity.UserVerification;
import com.blueblood.api.modules.user.service.UserService;
import com.blueblood.api.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户模块：当前用户、资料、技能、认证、等级/信誉日志。
 */
@Tag(name = "用户", description = "用户资料、技能、认证、等级信誉")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "当前用户详情（含档案/技能/角色）")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.buildVO(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "查看用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.success(userService.buildVO(id));
    }

    @Operation(summary = "编辑个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        userService.updateProfile(SecurityUtils.currentUserId(), req);
        return Result.success();
    }

    // ----------------------------- 技能 -----------------------------

    @Operation(summary = "我的技能标签列表")
    @GetMapping("/skills")
    public Result<List<UserSkill>> mySkills() {
        return Result.success(userService.listSkills(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "添加技能标签")
    @PostMapping("/skills")
    public Result<Void> addSkill(@Valid @RequestBody SkillRequest req) {
        userService.addSkill(SecurityUtils.currentUserId(), req);
        return Result.success();
    }

    @Operation(summary = "删除技能标签")
    @DeleteMapping("/skills/{id}")
    public Result<Void> removeSkill(@PathVariable Long id) {
        userService.removeSkill(SecurityUtils.currentUserId(), id);
        return Result.success();
    }

    // ----------------------------- 认证 -----------------------------

    @Operation(summary = "提交认证申请")
    @PostMapping("/verification")
    public Result<Map<String, Object>> submitVerification(@Valid @RequestBody VerificationSubmitRequest req) {
        Long vid = userService.submitVerification(SecurityUtils.currentUserId(), req);
        return Result.success(Map.of("verificationId", vid));
    }

    @Operation(summary = "我的最新认证申请")
    @GetMapping("/verification")
    public Result<UserVerification> myVerification() {
        return Result.success(userService.myLatestVerification(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "审核认证申请（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/verification/{id}/review")
    public Result<Void> reviewVerification(@PathVariable Long id, @Valid @RequestBody VerificationReviewRequest req) {
        userService.reviewVerification(SecurityUtils.currentUserId(), id, req);
        return Result.success();
    }

    // ----------------------------- 等级 / 信誉日志 -----------------------------

    @Operation(summary = "等级变动日志（分页）")
    @GetMapping("/logs/level")
    public Result<PageResult<UserLevelLog>> levelLogs(@RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer pageSize) {
        return Result.success(userService.pageLevelLogs(SecurityUtils.currentUserId(), page, pageSize));
    }

    @Operation(summary = "信誉分变动日志（分页）")
    @GetMapping("/logs/credit")
    public Result<PageResult<UserCreditLog>> creditLogs(@RequestParam(required = false) Integer page,
                                                        @RequestParam(required = false) Integer pageSize) {
        return Result.success(userService.pageCreditLogs(SecurityUtils.currentUserId(), page, pageSize));
    }
}
