package com.blueblood.api.modules.task.controller;

import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.task.dto.MilestoneVO;
import com.blueblood.api.modules.task.dto.ReviewMilestoneRequest;
import com.blueblood.api.modules.task.dto.SubmitMilestoneRequest;
import com.blueblood.api.modules.task.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 里程碑模块：提交、查看审核结果、管理员审核。
 */
@Tag(name = "里程碑", description = "里程碑提交、审核结果、管理员审核")
@RestController
@RequestMapping("/task/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @Operation(summary = "提交里程碑")
    @PostMapping("/{milestoneId}/submit")
    public Result<Map<String, Long>> submit(@PathVariable Long milestoneId,
                                            @Valid @RequestBody SubmitMilestoneRequest req) {
        return Result.success(milestoneService.submit(milestoneId, req));
    }

    @Operation(summary = "查看里程碑审核结果")
    @GetMapping("/{milestoneId}/result")
    public Result<MilestoneVO> result(@PathVariable Long milestoneId) {
        return Result.success(milestoneService.result(milestoneId));
    }

    @Operation(summary = "审核里程碑（雇主/管理员）")
    @PostMapping("/{milestoneId}/review")
    public Result<Map<String, Long>> review(@PathVariable Long milestoneId,
                                            @Valid @RequestBody ReviewMilestoneRequest req) {
        return Result.success(milestoneService.review(milestoneId, req));
    }
}
