package com.blueblood.api.modules.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程章节表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_chapter")
public class CourseChapter extends BaseEntity {

    private Long courseId;

    private String title;

    /** 时长，如 "12:30" */
    private String duration;

    private String videoUrl;

    /** 章节序号，1 基 */
    private Integer chapterOrder;
}
