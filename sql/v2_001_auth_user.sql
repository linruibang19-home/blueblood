-- =============================================================================
-- v2_001 模块03 扩展表：角色(多对多) + 认证申请
-- 依据：用户决策——角色用 sys_role/sys_user_role 多对多；认证用 user_verification 申请表
-- 应用到实库：mysql -u root -p < v2_001_auth_user.sql
-- =============================================================================
USE blueblood_v2;
SET NAMES utf8mb4;

-- 角色字典表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(32) NOT NULL COMMENT '角色编码: USER/ADMIN',
  `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '角色描述',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_ur_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 用户认证申请表
CREATE TABLE IF NOT EXISTS `user_verification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '申请人用户ID',
  `real_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '真实姓名',
  `id_number` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '证件号(脱敏存储)',
  `materials` JSON DEFAULT NULL COMMENT '申请材料URL列表(JSON数组)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态: PENDING/APPROVED/REJECTED',
  `reject_reason` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '驳回原因',
  `reviewer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID',
  `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_verify_user` (`user_id`),
  KEY `idx_verify_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证申请表';

-- ---------- 种子数据 ----------
-- 角色
INSERT INTO `sys_role` (`id`, `code`, `name`, `description`) VALUES
  (1, 'USER',  '普通用户', '平台普通用户'),
  (2, 'ADMIN', '管理员',   '系统管理员/审核员')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `description` = VALUES(`description`);

-- 现有演示用户(1-4) 绑定 USER 角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1,1),(2,1),(3,1),(4,1);

-- 管理员账号(id=5) —— 密码为 BCrypt("123456")
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `phone`, `email`,
                    `gender`, `level`, `level_name`, `points`, `credit_score`, `completed_tasks`, `verified`, `status`)
VALUES (5, 'admin', '$2a$10$506fC5TTEZT3S2pq6XmPouG5d/wsX18qCsP5N7clT2xpTyL6b9P4G', '平台管理员',
        'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '13800000000', 'admin@blueblood.cn',
        1, 99, '管理员', 99999, 5.00, 0, 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE `username` = `username`;
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (5,2);

-- 张明(2) 已是精英用户，预置一条已通过认证申请(演示)
INSERT INTO `user_verification` (`user_id`, `real_name`, `id_number`, `status`, `reviewer_id`, `reviewed_at`)
SELECT 2, '张明', '4401**********1234', 'APPROVED', 5, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `user_verification` WHERE `user_id` = 2);
