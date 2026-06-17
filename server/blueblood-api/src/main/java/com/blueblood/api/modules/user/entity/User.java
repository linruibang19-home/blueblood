package com.blueblood.api.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;

    @JsonIgnore
    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;

    private Integer level;

    private String levelName;

    private Integer points;

    private BigDecimal creditScore;

    private Integer completedTasks;

    /** 认证状态: 0-未认证 1-已认证 */
    private Integer verified;

    /** 账号状态: ACTIVE/INACTIVE/BANNED */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
}
