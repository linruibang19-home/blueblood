package com.blueblood.api.modules.enterprise.controller;

import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.enterprise.dto.EnterpriseApplicationRequest;
import com.blueblood.api.modules.enterprise.dto.HackathonRequest;
import com.blueblood.api.modules.enterprise.dto.JobRequest;
import com.blueblood.api.modules.enterprise.entity.EnterpriseApplication;
import com.blueblood.api.modules.enterprise.entity.Hackathon;
import com.blueblood.api.modules.enterprise.entity.Job;
import com.blueblood.api.modules.enterprise.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业用户端：企业认证申请 + 岗位/黑客松发布管理。
 * 接口均要求登录（由 SecurityUtils.currentUserId() 保证），发布类接口在 service 内校验 user_type=enterprise。
 */
@Tag(name = "企业用户端", description = "企业认证申请 / 岗位 / 黑客松发布")
@RestController
@RequestMapping("/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    // ============================== 企业认证申请 ==============================

    @Operation(summary = "提交企业认证申请")
    @PostMapping("/application")
    public Result<Long> submitApplication(@Valid @RequestBody EnterpriseApplicationRequest req) {
        return Result.success(enterpriseService.submitApplication(req));
    }

    @Operation(summary = "我的企业申请状态")
    @GetMapping("/my-application")
    public Result<EnterpriseApplication> myApplication() {
        return Result.success(enterpriseService.myApplication());
    }

    // ============================== 岗位 ==============================

    @Operation(summary = "企业用户发布岗位")
    @PostMapping("/jobs")
    public Result<Long> publishJob(@Valid @RequestBody JobRequest req) {
        return Result.success(enterpriseService.publishJob(req));
    }

    @Operation(summary = "我发布的岗位列表")
    @GetMapping("/jobs")
    public Result<List<Job>> myJobs() {
        return Result.success(enterpriseService.myJobs());
    }

    @Operation(summary = "编辑我发布的岗位")
    @PutMapping("/jobs/{id}")
    public Result<Void> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest req) {
        enterpriseService.updateJob(id, req);
        return Result.success();
    }

    @Operation(summary = "删除我发布的岗位")
    @DeleteMapping("/jobs/{id}")
    public Result<Void> deleteJob(@PathVariable Long id) {
        enterpriseService.deleteJob(id);
        return Result.success();
    }

    // ============================== 黑客松 ==============================

    @Operation(summary = "企业用户发布黑客松")
    @PostMapping("/hackathons")
    public Result<Long> publishHackathon(@Valid @RequestBody HackathonRequest req) {
        return Result.success(enterpriseService.publishHackathon(req));
    }

    @Operation(summary = "我发布的黑客松列表")
    @GetMapping("/hackathons")
    public Result<List<Hackathon>> myHackathons() {
        return Result.success(enterpriseService.myHackathons());
    }

    @Operation(summary = "编辑我发布的黑客松")
    @PutMapping("/hackathons/{id}")
    public Result<Void> updateHackathon(@PathVariable Long id, @Valid @RequestBody HackathonRequest req) {
        enterpriseService.updateHackathon(id, req);
        return Result.success();
    }

    @Operation(summary = "删除我发布的黑客松")
    @DeleteMapping("/hackathons/{id}")
    public Result<Void> deleteHackathon(@PathVariable Long id) {
        enterpriseService.deleteHackathon(id);
        return Result.success();
    }
}
