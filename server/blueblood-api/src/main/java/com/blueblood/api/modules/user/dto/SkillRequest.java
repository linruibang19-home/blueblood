package com.blueblood.api.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "技能标签请求")
public class SkillRequest {

    @Schema(description = "技能名称", example = "Vue3")
    @NotBlank(message = "技能名称不能为空")
    private String name;

    @Schema(description = "技能分类", example = "前端")
    private String category;

    @Schema(description = "熟练度: 0了解 1掌握 2精通")
    private Integer proficiency;
}
