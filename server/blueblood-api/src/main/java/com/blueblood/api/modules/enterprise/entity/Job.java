package com.blueblood.api.modules.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位（企业用户发布）。
 * tags/requirements 为 JSON 列，统一以 String 存储。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job")
public class Job extends BaseEntity {

    private String title;

    private String company;

    /** 公司 Logo URL */
    private String companyLogo;

    private String location;

    /** 薪资描述，如 "25-40K·14薪" */
    private String salary;

    /** 类型：实习/兼职/全职/外包 */
    private String type;

    /** 标签 JSON 数组 */
    private String tags;

    private String description;

    /** 要求 JSON 数组 */
    private String requirements;

    /** 联系方式 */
    private String contact;

    /** 状态：ACTIVE/CLOSED */
    private String status;

    /** 发布企业用户 ID */
    private Long publishedBy;
}
