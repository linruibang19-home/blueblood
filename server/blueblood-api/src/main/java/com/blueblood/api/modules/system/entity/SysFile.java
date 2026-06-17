package com.blueblood.api.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {
    private String originalName;
    private String storedName;
    private String url;
    private String mimeType;
    private Long size;
    /** local/oss/cos */
    private String storageType;
    private String bizType;
    private Long bizId;
    private Long uploadedBy;
}
