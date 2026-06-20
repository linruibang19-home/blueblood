package com.blueblood.api.modules.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 企业发布/编辑岗位请求体。
 * tags、requirements 为 JSON 列，前端传数组，后端序列化为 String 落库。
 */
@Data
@Schema(description = "发布/编辑岗位")
public class JobRequest {

    @Schema(description = "岗位标题")
    @NotBlank(message = "岗位标题不能为空")
    private String title;

    @Schema(description = "公司名称")
    @NotBlank(message = "公司名称不能为空")
    private String company;

    @Schema(description = "公司 Logo URL")
    private String companyLogo;

    @Schema(description = "工作地点")
    private String location;

    @Schema(description = "薪资描述")
    private String salary;

    @Schema(description = "类型：实习/兼职/全职/外包")
    private String type;

    @Schema(description = "标签数组")
    private List<String> tags;

    @Schema(description = "岗位描述")
    private String description;

    @Schema(description = "要求数组")
    private List<String> requirements;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "状态：ACTIVE/CLOSED")
    private String status;
}
