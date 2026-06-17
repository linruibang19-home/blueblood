package com.blueblood.api.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户档案表（扩展信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_profile")
public class UserProfile extends BaseEntity {

    private Long userId;

    private String school;

    private String major;

    private String bio;

    private String github;

    private Integer connections;

    private Integer followers;

    private Integer following;

    /** 徽章列表 JSON */
    private String badges;

    /** 能力雷达数据 JSON */
    private String radarData;

    private LocalDate joinedAt;
}
