package com.blueblood.api.modules.course.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.course.dto.ChapterVO;
import com.blueblood.api.modules.course.dto.CourseListItemVO;
import com.blueblood.api.modules.course.dto.CourseVO;
import com.blueblood.api.modules.course.dto.ProgressVO;
import com.blueblood.api.modules.course.dto.UpdateProgressRequest;
import com.blueblood.api.modules.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程模块：列表、详情、章节、学习进度推进、当前学习章节。
 */
@Tag(name = "课程", description = "课程列表、详情、章节、学习进度")
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "课程分页（每项含当前用户 progress/status）")
    @GetMapping
    public Result<PageResult<CourseListItemVO>> page(@RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer pageSize) {
        return Result.success(courseService.page(page, pageSize));
    }

    @Operation(summary = "课程详情（全字段 + 当前用户进度 + 章节列表）")
    @GetMapping("/{id}")
    public Result<CourseVO> detail(@PathVariable Long id) {
        return Result.success(courseService.detail(id));
    }

    @Operation(summary = "章节列表（chapter_order asc，每项带 completed）")
    @GetMapping("/{id}/chapters")
    public Result<List<ChapterVO>> chapters(@PathVariable Long id) {
        return Result.success(courseService.listChapters(id));
    }

    @Operation(summary = "更新学习进度（body: chapterId 必填, completed）")
    @PostMapping("/{id}/progress")
    public Result<ProgressVO> updateProgress(@PathVariable Long id, @Valid @RequestBody UpdateProgressRequest req) {
        return Result.success(courseService.updateProgress(id, req));
    }

    @Operation(summary = "当前学习章节（第一个未完成章节；全部完成则返回最后一章）")
    @GetMapping("/{id}/current-chapter")
    public Result<ChapterVO> currentChapter(@PathVariable Long id) {
        return Result.success(courseService.currentChapter(id));
    }
}
