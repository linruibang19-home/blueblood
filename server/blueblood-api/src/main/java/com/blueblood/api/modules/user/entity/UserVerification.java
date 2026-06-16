package com.blueblood.api.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户认证申请
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_verification")
public class UserVerification extends BaseEntity {

    private Long userId;

    private String realName;

    /** 证件号(脱敏) */
    private String idNumber;

    /** 申请材料 URL 列表 JSON */
    private String materials;

    /** 审核状态: PENDING/APPROVED/REJECTED */
    private String status;

    private String rejectReason;

    private Long reviewerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;
}
