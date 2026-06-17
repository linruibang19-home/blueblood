package com.blueblood.api.modules.assignment.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.assignment.dto.AssignmentVO;
import com.blueblood.api.modules.assignment.dto.GradeAssignmentRequest;
import com.blueblood.api.modules.assignment.dto.SubmitAssignmentRequest;
import com.blueblood.api.modules.assignment.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 作业模块：课程作业列表、详情、提交、批改结果、管理员批改。
 * 注意：列表接口挂载在 /course/{courseId}/assignments，其余在 /assignment。
 */
@Tag(name = "作业", description = "作业列表、详情、提交、批改")
@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "课程作业分页列表（含当前用户状态/成绩）")
    @GetMapping("/course/{courseId}/assignments")
    public Result<PageResult<AssignmentVO>> listByCourse(@PathVariable Long courseId,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer pageSize) {
        return Result.success(assignmentService.pageByCourse(courseId, page, pageSize));
    }

    @Operation(summary = "作业详情（含当前用户提交/批改）")
    @GetMapping("/assignment/{id}")
    public Result<AssignmentVO> detail(@PathVariable Long id) {
        return Result.success(assignmentService.detail(id));
    }

    @Operation(summary = "提交作业")
    @PostMapping("/assignment/{id}/submit")
    public Result<Map<String, Long>> submit(@PathVariable Long id,
                                            @Valid @RequestBody SubmitAssignmentRequest request) {
        return Result.success(assignmentService.submit(id, request));
    }

    @Operation(summary = "查看批改结果（当前用户提交 + 成绩 + 参考答案）")
    @GetMapping("/assignment/{id}/result")
    public Result<AssignmentVO> result(@PathVariable Long id) {
        return Result.success(assignmentService.result(id));
    }

    @Operation(summary = "批改作业（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assignment/{id}/grade")
    public Result<Map<String, Long>> grade(@PathVariable Long id,
                                           @Valid @RequestBody GradeAssignmentRequest request) {
        return Result.success(assignmentService.grade(id, request));
    }
}
