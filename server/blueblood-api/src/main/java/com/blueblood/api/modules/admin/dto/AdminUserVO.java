package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台用户列表/详情视图。
 */
@Data
@Schema(description = "后台用户视图")
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private Integer level;
    private String levelName;
    private Integer points;
    private BigDecimal creditScore;
    private Integer completedTasks;
    private Integer verified;
    private String status;

    @Schema(description = "用户类型: personal/enterprise")
    private String userType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
