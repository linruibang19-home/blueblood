package com.blueblood.api.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程学习进度表（course_id + user_id 唯一）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_progress")
public class CourseProgress extends BaseEntity {

    private Long courseId;

    private Long userId;

    private Integer completedChapters;

    /** 进度百分比 0-100 */
    private Integer progress;

    /** 状态：not_started / in_progress / completed */
    private String status;

    private LocalDateTime lastStudyAt;
}
