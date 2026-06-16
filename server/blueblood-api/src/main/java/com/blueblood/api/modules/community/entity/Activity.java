package com.blueblood.api.modules.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 小组活动
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class Activity extends BaseEntity {

    private Long groupId;

    private String title;

    private String description;

    /** 封面图 URL */
    private String coverImage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String location;

    /** 已报名人数 */
    private Integer signupCount;

    /** 人数上限，0 表示不限 */
    private Integer maxCount;

    /** 状态：upcoming / ongoing / ended */
    private String status;
}
