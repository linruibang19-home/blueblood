package com.blueblood.api.modules.admin.dto;

import com.blueblood.api.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台企业申请查询(分页 + 筛选)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台企业申请查询")
public class AdminEnterpriseApplicationQuery extends PageQuery {

    @Schema(description = "审核状态: PENDING/APPROVED/REJECTED")
    private String status;

    @Schema(description = "公司名称/申请人昵称关键字")
    private String keyword;
}
