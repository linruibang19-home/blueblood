-- =============================================================================
-- v3_001 企业用户 + 活动发布
-- 依据：用户审核通过方案 —— user_type 列区分个人/企业；企业申请表；job/hackathon 加 published_by
-- 应用：mysql -u root -p < v3_001_enterprise.sql
-- =============================================================================
USE blueblood_v2;
SET NAMES utf8mb4;

-- 1. 用户表加 user_type
ALTER TABLE `user` ADD COLUMN `user_type` VARCHAR(20) NOT NULL DEFAULT 'personal' COMMENT '用户类型: personal-个人 enterprise-企业';

-- 2. 岗位/黑客松加 published_by(发布企业用户ID)
ALTER TABLE `job` ADD COLUMN `published_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '发布企业用户ID';
ALTER TABLE `hackathon` ADD COLUMN `published_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '发布企业用户ID';

-- 3. 企业认证申请表
CREATE TABLE IF NOT EXISTS `enterprise_application` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '申请用户ID',
  `company_name` VARCHAR(128) NOT NULL COMMENT '公司名称',
  `credit_code` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '统一社会信用代码',
  `license_url` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '营业执照图片URL',
  `contact_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '联系电话',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态: PENDING/APPROVED/REJECTED',
  `reject_reason` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '驳回原因',
  `reviewer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID',
  `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_ent_user` (`user_id`),
  KEY `idx_ent_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业认证申请表';

-- 4. 演示企业账号(id=6, ACME科技, 密码 123456, 已是企业用户)
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `phone`, `email`,
                    `gender`, `level`, `level_name`, `points`, `credit_score`, `completed_tasks`, `verified`, `status`, `user_type`)
VALUES (6, 'acme', '$2a$10$506fC5TTEZT3S2pq6XmPouG5d/wsX18qCsP5N7clT2xpTyL6b9P4G', 'ACME科技',
        'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '13800000006', 'hr@acme.cn',
        0, 1, '企业', 0, 5.00, 0, 1, 'ACTIVE', 'enterprise');
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (6, 1);  -- 给 USER 基础角色
INSERT INTO `enterprise_application` (`user_id`, `company_name`, `credit_code`, `license_url`, `contact_name`, `contact_phone`, `status`, `reviewer_id`, `reviewed_at`)
VALUES (6, 'ACME科技', '91110100MA01ABCDEF', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '李HR', '13800000006', 'APPROVED', 5, '2024-06-10 10:00:00');
