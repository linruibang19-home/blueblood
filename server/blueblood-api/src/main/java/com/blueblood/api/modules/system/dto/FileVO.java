package com.blueblood.api.modules.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文件信息")
public class FileVO {
    private Long id;
    private String originalName;
    private String url;
    private String mimeType;
    private Long size;
    private String bizType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
