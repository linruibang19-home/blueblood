package com.blueblood.api.modules.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子收藏表（UNIQUE post_id + user_id）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_favorite")
public class PostFavorite extends BaseEntity {

    private Long postId;

    private Long userId;
}
