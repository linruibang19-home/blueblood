package com.blueblood.api.modules.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 黑客松（企业用户发布）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hackathon")
public class Hackathon extends BaseEntity {

    private String title;

    private String description;

    /** 封面图 URL */
    private String coverImage;

    /** 奖金池 */
    private BigDecimal prizePool;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signupDeadline;

    private String location;

    /** 最大参赛队伍数 */
    private Integer maxTeams;

    /** 当前已报名队伍数 */
    private Integer currentTeams;

    /** 状态：signup/ongoing/ended */
    private String status;

    /** 发布企业用户 ID */
    private Long publishedBy;
}
