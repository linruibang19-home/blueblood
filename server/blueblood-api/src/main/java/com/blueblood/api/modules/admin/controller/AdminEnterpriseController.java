package com.blueblood.api.modules.admin.controller;

import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminEnterpriseApplicationQuery;
import com.blueblood.api.modules.admin.dto.AdminEnterpriseApplicationVO;
import com.blueblood.api.modules.admin.dto.EnterpriseReviewRequest;
import com.blueblood.api.modules.admin.service.AdminEnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 后台 - 企业认证审核。
 */
@Tag(name = "后台-企业认证审核", description = "企业申请列表/详情/审核")
@RestController
@RequestMapping("/admin/enterprise")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminEnterpriseController {

    private final AdminEnterpriseService adminEnterpriseService;

    @Operation(summary = "企业申请列表(筛选分页)")
    @GetMapping
    public Result<PageResult<AdminEnterpriseApplicationVO>> list(AdminEnterpriseApplicationQuery query) {
        return Result.success(adminEnterpriseService.page(query));
    }

    @Operation(summary = "企业申请详情")
    @GetMapping("/{id}")
    public Result<AdminEnterpriseApplicationVO> detail(@PathVariable Long id) {
        AdminEnterpriseApplicationVO vo = adminEnterpriseService.detail(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "企业申请不存在");
        }
        return Result.success(vo);
    }

    @Operation(summary = "审核通过/驳回(APPROVED → 置企业用户)")
    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody EnterpriseReviewRequest req) {
        adminEnterpriseService.review(id, req);
        return Result.success();
    }
}
