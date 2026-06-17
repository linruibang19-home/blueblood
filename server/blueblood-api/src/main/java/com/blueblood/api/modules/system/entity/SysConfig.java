package com.blueblood.api.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    private String configKey;
    private String configValue;
    /** string/number/boolean/json */
    private String configType;
    private String label;
    private String remark;
}
