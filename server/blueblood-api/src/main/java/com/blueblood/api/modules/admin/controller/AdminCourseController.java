package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminCourseQuery;
import com.blueblood.api.modules.admin.dto.AdminCourseVO;
import com.blueblood.api.modules.admin.dto.CourseRequest;
import com.blueblood.api.modules.admin.service.AdminEduService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 课程管理。
 */
@Tag(name = "后台-课程管理", description = "课程列表/详情/增改/上下架/删除")
@RestController
@RequestMapping("/admin/course")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminEduService adminEduService;

    @Operation(summary = "课程列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminCourseVO>> list(AdminCourseQuery query) {
        return Result.success(adminEduService.pageCourse(query));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/{id}")
    public Result<AdminCourseVO> get(@PathVariable Long id) {
        return Result.success(adminEduService.getCourse(id));
    }

    @Operation(summary = "新增课程")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CourseRequest req) {
        return Result.success(adminEduService.createCourse(req));
    }

    @Operation(summary = "编辑课程")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CourseRequest req) {
        adminEduService.updateCourse(id, req);
        return Result.success();
    }

    @Operation(summary = "上下架/改状态(PUBLISHED/DRAFT/OFFLINE)")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminEduService.updateCourseStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除课程(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminEduService.deleteCourse(id);
        return Result.success();
    }
}
