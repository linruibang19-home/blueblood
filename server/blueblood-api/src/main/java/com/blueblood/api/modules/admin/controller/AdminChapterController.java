package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminChapterVO;
import com.blueblood.api.modules.admin.dto.ChapterRequest;
import com.blueblood.api.modules.admin.service.AdminEduService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台 - 课程章节管理。
 */
@Tag(name = "后台-章节管理", description = "章节列表/增改/删除")
@RestController
@RequestMapping("/admin/chapter")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminChapterController {

    private final AdminEduService adminEduService;

    @Operation(summary = "章节列表(按课程)")
    @GetMapping
    public Result<List<AdminChapterVO>> list(@RequestParam Long courseId) {
        return Result.success(adminEduService.listChapters(courseId));
    }

    @Operation(summary = "新增章节")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ChapterRequest req) {
        return Result.success(adminEduService.createChapter(req));
    }

    @Operation(summary = "编辑章节")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ChapterRequest req) {
        adminEduService.updateChapter(id, req);
        return Result.success();
    }

    @Operation(summary = "删除章节(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminEduService.deleteChapter(id);
        return Result.success();
    }
}
