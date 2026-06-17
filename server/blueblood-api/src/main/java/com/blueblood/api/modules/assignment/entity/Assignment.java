package com.blueblood.api.modules.assignment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 作业
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment")
public class Assignment extends BaseEntity {

    private Long courseId;

    /** 所属章节 ID，可为空 */
    private Long chapterId;

    private String title;

    private String description;

    /** 截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    /** 参考答案 */
    private String answer;

    /** 状态：not_submitted / submitted / graded（仅作记录，用户维度状态以 submission+grade 为准） */
    private String status;
}
