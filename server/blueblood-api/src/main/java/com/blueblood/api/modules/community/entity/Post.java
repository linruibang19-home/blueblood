package com.blueblood.api.modules.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post")
public class Post extends BaseEntity {

    private Long groupId;

    private Long authorId;

    private String title;

    private String content;

    /** 图片列表，JSON 列，按字符串映射 */
    private String images;

    /** 话题标签：话题 / 任务 / 经验分享 / 活动 */
    private String tag;

    private Integer likes;

    private Integer comments;

    private Integer views;

    /** 状态：PUBLISHED / ... */
    private String status;
}
