package com.blueblood.api.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户技能标签
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_skill")
public class UserSkill extends BaseEntity {

    private Long userId;

    private String name;

    private String category;

    /** 熟练度: 0-了解 1-掌握 2-精通 */
    private Integer proficiency;
}
