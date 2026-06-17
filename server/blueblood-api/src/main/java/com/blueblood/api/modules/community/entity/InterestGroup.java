package com.blueblood.api.modules.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 兴趣小组主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interest_group")
public class InterestGroup extends BaseEntity {

    private String name;

    private String description;

    private String coverImage;

    private Long leaderId;

    /** 分类，默认 AI */
    private String category;

    /** 标签，JSON 列，按字符串映射 */
    private String tags;

    private Integer memberCount;

    private Integer postCount;

    private Integer activityCount;

    /** 状态：ACTIVE / INACTIVE */
    private String status;
}
