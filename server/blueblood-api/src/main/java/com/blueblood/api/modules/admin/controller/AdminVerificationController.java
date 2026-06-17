package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminVerificationVO;
import com.blueblood.api.modules.admin.service.AdminVerificationService;
import com.blueblood.api.modules.user.dto.VerificationReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 认证审核。
 */
@Tag(name = "后台-认证审核", description = "认证申请列表/详情/审核")
@RestController
@RequestMapping("/admin/verification")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminVerificationController {

    private final AdminVerificationService adminVerificationService;

    @Operation(summary = "认证申请列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminVerificationVO>> list(PageQuery query,
                                                        @RequestParam(required = false) String status) {
        return Result.success(adminVerificationService.page(query, status));
    }

    @Operation(summary = "认证申请详情")
    @GetMapping("/{id}")
    public Result<AdminVerificationVO> detail(@PathVariable Long id) {
        AdminVerificationVO vo = adminVerificationService.detail(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "认证申请不存在");
        }
        return Result.success(vo);
    }

    @Operation(summary = "审核通过/驳回")
    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody VerificationReviewRequest req) {
        adminVerificationService.review(id, req);
        return Result.success();
    }
}
