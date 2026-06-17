package com.blueblood.api.modules.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 提交作业请求。
 */
@Data
@Schema(description = "提交作业请求")
public class SubmitAssignmentRequest {

    @Schema(description = "提交正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "content 不能为空")
    private String content;

    @Schema(description = "附件列表")
    private List<String> attachments;
}
