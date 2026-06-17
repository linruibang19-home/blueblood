package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台认证申请视图(含申请人昵称)。
 */
@Data
@Schema(description = "后台认证申请视图")
public class AdminVerificationVO {

    private Long id;
    private Long userId;
    private String nickname;
    private String realName;
    private String idNumber;
    private String materials;

    @Schema(description = "审核状态: PENDING/APPROVED/REJECTED")
    private String status;
    private String rejectReason;
    private Long reviewerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
