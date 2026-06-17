package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminSubmissionVO;
import com.blueblood.api.modules.admin.dto.GradeSubmissionRequest;
import com.blueblood.api.modules.admin.service.AdminEduService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 作业提交与批改管理。
 */
@Tag(name = "后台-作业提交管理", description = "提交列表/批改")
@RestController
@RequestMapping("/admin/submission")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSubmissionController {

    private final AdminEduService adminEduService;

    @Operation(summary = "提交列表(按作业,筛选分页)")
    @GetMapping
    public Result<PageResult<AdminSubmissionVO>> list(
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) String status,
            PageQuery query) {
        return Result.success(adminEduService.pageSubmission(assignmentId, status, query));
    }

    @Operation(summary = "批改提交(按 submissionId)")
    @PutMapping("/{submissionId}/grade")
    public Result<Long> grade(@PathVariable Long submissionId,
                              @Valid @RequestBody GradeSubmissionRequest req) {
        return Result.success(adminEduService.gradeSubmission(submissionId, req));
    }
}
