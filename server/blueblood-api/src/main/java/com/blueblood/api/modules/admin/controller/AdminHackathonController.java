package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.admin.dto.AdminHackathonQuery;
import com.blueblood.api.modules.admin.dto.AdminHackathonVO;
import com.blueblood.api.modules.admin.service.AdminHackathonService;
import com.blueblood.api.modules.enterprise.dto.HackathonRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 黑客松管理。
 */
@Tag(name = "后台-黑客松管理", description = "黑客松列表/详情/增删改")
@RestController
@RequestMapping("/admin/hackathon")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminHackathonController {

    private final AdminHackathonService adminHackathonService;

    @Operation(summary = "黑客松列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminHackathonVO>> list(AdminHackathonQuery query) {
        return Result.success(adminHackathonService.page(query));
    }

    @Operation(summary = "黑客松详情")
    @GetMapping("/{id}")
    public Result<AdminHackathonVO> detail(@PathVariable Long id) {
        return Result.success(adminHackathonService.detail(id));
    }

    @Operation(summary = "新增黑客松")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody HackathonRequest req) {
        return Result.success(adminHackathonService.create(req));
    }

    @Operation(summary = "编辑黑客松")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody HackathonRequest req) {
        adminHackathonService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除黑客松")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminHackathonService.delete(id);
        return Result.success();
    }
}
