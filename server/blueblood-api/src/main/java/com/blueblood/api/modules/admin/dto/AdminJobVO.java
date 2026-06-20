package com.blueblood.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台岗位视图(含发布人昵称)。
 */
@Data
@Schema(description = "后台岗位视图")
public class AdminJobVO {

    private Long id;
    private String title;
    private String company;
    private String companyLogo;
    private String location;
    private String salary;
    private String type;
    /** 标签 JSON */
    private String tags;
    private String description;
    /** 要求 JSON */
    private String requirements;
    private String contact;

    @Schema(description = "状态: ACTIVE/CLOSED")
    private String status;

    private Long publishedBy;

    @Schema(description = "发布人昵称")
    private String publisherNickname;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
