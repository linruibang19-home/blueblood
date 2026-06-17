package com.blueblood.api.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    private String dictType;
    private String dictKey;
    private String dictValue;
    private String label;
    private Integer sort;
    private String remark;
    private String status;
}
