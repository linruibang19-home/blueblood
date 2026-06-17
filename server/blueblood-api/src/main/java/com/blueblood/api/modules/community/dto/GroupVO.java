package com.blueblood.api.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组视图（对外返回）。
 */
@Data
@Schema(description = "小组视图")
public class GroupVO {

    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private String category;

    private String tags;

    private Integer memberCount;

    private Integer postCount;

    private Integer activityCount;

    private String status;

    @Schema(description = "当前用户是否已是成员")
    private Boolean joined;

    @Schema(description = "当前用户在该小组的角色（leader/admin/member），未加入为 null")
    private String role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
