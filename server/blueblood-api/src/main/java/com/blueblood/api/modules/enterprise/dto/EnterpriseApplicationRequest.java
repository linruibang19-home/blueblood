package com.blueblood.api.modules.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交企业认证申请。
 */
@Data
@Schema(description = "提交企业认证申请")
public class EnterpriseApplicationRequest {

    @Schema(description = "公司名称")
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @Schema(description = "统一社会信用代码")
    @NotBlank(message = "信用代码不能为空")
    private String creditCode;

    @Schema(description = "营业执照 URL")
    private String licenseUrl;

    @Schema(description = "联系人姓名")
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @Schema(description = "联系电话")
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
}
