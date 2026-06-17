package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户技能标签(带用户名)列表展示对象。
 */
@Data
@Schema(description = "用户技能标签")
public class AdminSkillVO {

    @Schema(description = "技能记录 ID")
    private Long id;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "技能名")
    private String name;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "熟练度: 0-了解 1-掌握 2-精通")
    private Integer proficiency;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
