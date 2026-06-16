package com.blueblood.api.modules.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子评论表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_comment")
public class PostComment extends BaseEntity {

    private Long postId;

    private Long authorId;

    /** 父评论ID，一级评论为 null */
    private Long parentId;

    private String content;

    private Integer likes;

    /** 状态：NORMAL / ... */
    private String status;
}
