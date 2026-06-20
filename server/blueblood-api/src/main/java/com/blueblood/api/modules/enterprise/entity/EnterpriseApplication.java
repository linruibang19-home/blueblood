package com.blueblood.api.modules.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 企业认证申请。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enterprise_application")
public class EnterpriseApplication extends BaseEntity {

    private Long userId;

    private String companyName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 营业执照 URL */
    private String licenseUrl;

    private String contactName;

    private String contactPhone;

    /** 审核状态: PENDING/APPROVED/REJECTED */
    private String status;

    private String rejectReason;

    private Long reviewerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;
}
