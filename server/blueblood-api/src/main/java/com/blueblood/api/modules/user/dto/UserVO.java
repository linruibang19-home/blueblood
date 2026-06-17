package com.blueblood.api.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.blueblood.api.modules.user.entity.UserSkill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图（对外返回，不含密码）。
 */
@Data
@Schema(description = "用户视图")
public class UserVO {

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

    @Schema(description = "角色编码列表")
    private List<String> roles;

    // 档案扩展
    private String school;
    private String major;
    private String bio;
    private String github;
    private Integer connections;
    private Integer followers;
    private Integer following;
    private String badges;

    @Schema(description = "技能标签")
    private List<UserSkill> skills;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
}
