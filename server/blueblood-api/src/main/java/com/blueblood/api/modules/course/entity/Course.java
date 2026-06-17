package com.blueblood.api.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 课程主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    private String title;

    private String subtitle;

    private String coverImage;

    private String instructor;

    private String instructorAvatar;

    private Integer totalChapters;

    private Integer rewardPoints;

    private Integer students;

    /** 评分，decimal(3,2) */
    private BigDecimal rating;

    /** 状态：PUBLISHED / ... */
    private String status;
}
