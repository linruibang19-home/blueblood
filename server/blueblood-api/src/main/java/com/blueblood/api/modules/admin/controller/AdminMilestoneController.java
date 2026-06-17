package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminMilestoneSubmissionQuery;
import com.blueblood.api.modules.admin.dto.AdminMilestoneSubmissionVO;
import com.blueblood.api.modules.admin.service.AdminTaskService;
import com.blueblood.api.modules.task.dto.ReviewMilestoneRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台 - 里程碑审核管理。
 */
@Tag(name = "后台-里程碑审核", description = "里程碑提交列表 + 审核(复用 MilestoneService.review)")
@RestController
@RequestMapping("/admin/milestone")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMilestoneController {

    private final AdminTaskService adminTaskService;

    @Operation(summary = "里程碑提交列表(筛选分页，默认 SUBMITTED)")
    @GetMapping("/submissions")
    public Result<PageResult<AdminMilestoneSubmissionVO>> submissions(AdminMilestoneSubmissionQuery query) {
        return Result.success(adminTaskService.pageMilestoneSubmission(query));
    }

    @Operation(summary = "审核里程碑(通过/驳回)")
    @PutMapping("/{milestoneId}/review")
    public Result<Map<String, Long>> review(@PathVariable Long milestoneId,
                                            @Valid @RequestBody ReviewMilestoneRequest req) {
        return Result.success(adminTaskService.reviewMilestone(milestoneId, req));
    }
}
