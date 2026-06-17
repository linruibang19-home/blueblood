package com.blueblood.api.modules.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "提交里程碑")
public class SubmitMilestoneRequest {

    @Schema(description = "GitHub 仓库地址")
    private String githubUrl;

    @Schema(description = "提交说明")
    private String description;

    @Schema(description = "附件URL列表")
    private List<String> attachments;
}
