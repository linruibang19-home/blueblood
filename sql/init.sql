-- =============================================================================
-- 文件名称: init.sql
-- 用途: 蓝血菁英平台（BlueBlood）第二阶段后端 MySQL 数据库初始化脚本
--       包含全部业务表结构 + 阶段一演示场景种子数据
-- 数据库:   blueblood_v2 (MySQL 8, utf8mb4 / utf8mb4_unicode_ci, InnoDB)
-- 说明:     新库名 blueblood_v2，与遗留的 blueblood(Laravel/PHP 旧后端) 库共存，互不影响
-- 生成依据:
--   1) apps/user-web/src/types/*.ts         —— 阶段一前端类型定义
--   2) apps/user-web/src/mock/*.ts          —— 阶段一 Mock 演示数据
--   3) docs/phase-2-springboot-api/02-数据库模型设计.md
--   4) docs/phase-2-springboot-api/06-任务与里程碑模块任务.md
-- 导入方式:
--   mysql -u root -p < init.sql
--   或进入 mysql 客户端后: source init.sql;
-- 说明: 种子用户(含 admin)密码均为 BCrypt("123456")，便于本地演示登录；生产环境务必改用强密码并重新哈希。
-- =============================================================================

CREATE DATABASE IF NOT EXISTS blueblood_v2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blueblood_v2;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 一、用户组
-- =============================================================================

-- 表说明：用户主表（账号、认证、等级、信誉分）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名(登录账号)',
  `password` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '密码(BCrypt哈希)',
  `nickname` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '头像URL',
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '手机号',
  `email` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '邮箱',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
  `level` INT NOT NULL DEFAULT 1 COMMENT '用户等级',
  `level_name` VARCHAR(32) NOT NULL DEFAULT '新手' COMMENT '等级名称',
  `points` INT NOT NULL DEFAULT 0 COMMENT '成长积分',
  `credit_score` DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '信誉分(0.00-5.00)',
  `completed_tasks` INT NOT NULL DEFAULT 0 COMMENT '已完成任务数',
  `verified` TINYINT NOT NULL DEFAULT 0 COMMENT '认证状态: 0-未认证 1-已认证',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态: ACTIVE-正常 INACTIVE-停用 BANNED-封禁',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  KEY `idx_user_phone` (`phone`),
  KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

-- 表说明：用户档案（扩展信息：学校、专业、简介、社交、徽章、雷达能力）
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `school` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '学校',
  `major` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '专业',
  `bio` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '个人简介',
  `github` VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'GitHub地址',
  `connections` INT NOT NULL DEFAULT 0 COMMENT '人脉数',
  `followers` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `following` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `badges` JSON DEFAULT NULL COMMENT '徽章列表(JSON)',
  `radar_data` JSON DEFAULT NULL COMMENT '能力雷达数据(JSON: labels+values)',
  `joined_at` DATE DEFAULT NULL COMMENT '加入平台日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_profile_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户档案表';

-- 表说明：用户技能标签
DROP TABLE IF EXISTS `user_skill`;
CREATE TABLE `user_skill` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `name` VARCHAR(64) NOT NULL COMMENT '技能名称',
  `category` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '技能分类',
  `proficiency` TINYINT NOT NULL DEFAULT 0 COMMENT '熟练度: 0-了解 1-掌握 2-精通',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_user_skill_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户技能标签表';

-- 表说明：用户等级变动日志
DROP TABLE IF EXISTS `user_level_log`;
CREATE TABLE `user_level_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `from_level` INT NOT NULL DEFAULT 0 COMMENT '变动前等级',
  `to_level` INT NOT NULL DEFAULT 0 COMMENT '变动后等级',
  `reason` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '变动原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_level_log_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户等级变动日志';

-- 表说明：用户信誉分变动日志
DROP TABLE IF EXISTS `user_credit_log`;
CREATE TABLE `user_credit_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `delta` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '信誉分变化量',
  `after_score` DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '变动后信誉分',
  `reason` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '变动原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_credit_log_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信誉分变动日志';

-- 表说明：用户人脉关系（关注 / 互关）
DROP TABLE IF EXISTS `user_connection`;
CREATE TABLE `user_connection` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发起用户ID',
  `target_user_id` BIGINT UNSIGNED NOT NULL COMMENT '目标用户ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'FOLLOWING' COMMENT '关系状态: FOLLOWING-关注 MUTUAL-互关',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conn_pair` (`user_id`, `target_user_id`),
  KEY `idx_conn_target` (`target_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户人脉关系表';


-- =============================================================================
-- 二、社区组
-- =============================================================================

-- 表说明：兴趣小组
DROP TABLE IF EXISTS `interest_group`;
CREATE TABLE `interest_group` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(128) NOT NULL COMMENT '小组名称',
  `description` TEXT COMMENT '小组简介',
  `cover_image` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '封面图URL',
  `leader_id` BIGINT UNSIGNED NOT NULL COMMENT '组长用户ID',
  `category` VARCHAR(32) NOT NULL DEFAULT 'AI' COMMENT '小组分类: AI/Dev/Design/Product/Study',
  `tags` JSON DEFAULT NULL COMMENT '标签列表(JSON数组)',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数',
  `post_count` INT NOT NULL DEFAULT 0 COMMENT '帖子数',
  `activity_count` INT NOT NULL DEFAULT 0 COMMENT '活动数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-正常 INACTIVE-停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_group_leader` (`leader_id`),
  KEY `idx_group_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兴趣小组表';

-- 表说明：小组成员
DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '小组ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role` VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '成员角色: leader-组长 admin-管理员 member-成员',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gm_pair` (`group_id`, `user_id`),
  KEY `idx_gm_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组成员表';

-- 表说明：帖子
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '所属小组ID',
  `author_id` BIGINT UNSIGNED NOT NULL COMMENT '作者用户ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '正文内容',
  `images` JSON DEFAULT NULL COMMENT '图片URL列表(JSON数组)',
  `tag` VARCHAR(32) NOT NULL DEFAULT '话题' COMMENT '帖子标签: 话题/任务/经验分享/活动',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comments` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `views` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED-已发布 DRAFT-草稿 HIDDEN-隐藏',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_post_group` (`group_id`),
  KEY `idx_post_author` (`author_id`),
  KEY `idx_post_tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 表说明：帖子评论（支持二级回复）
DROP TABLE IF EXISTS `post_comment`;
CREATE TABLE `post_comment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `author_id` BIGINT UNSIGNED NOT NULL COMMENT '评论人ID',
  `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID(为空表示一级评论)',
  `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL-正常 DELETED-已删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_comment_post` (`post_id`),
  KEY `idx_comment_author` (`author_id`),
  KEY `idx_comment_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子评论表';

-- 表说明：帖子点赞记录
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like_pair` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞记录表';

-- 表说明：帖子收藏记录
DROP TABLE IF EXISTS `post_favorite`;
CREATE TABLE `post_favorite` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '收藏用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fav_pair` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏记录表';

-- 表说明：小组活动
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '所属小组ID',
  `title` VARCHAR(255) NOT NULL COMMENT '活动标题',
  `description` TEXT COMMENT '活动描述',
  `cover_image` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '封面图URL',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `location` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '活动地点',
  `signup_count` INT NOT NULL DEFAULT 0 COMMENT '已报名人数',
  `max_count` INT NOT NULL DEFAULT 0 COMMENT '人数上限',
  `status` VARCHAR(20) NOT NULL DEFAULT 'upcoming' COMMENT '状态: upcoming-未开始 ongoing-进行中 ended-已结束',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_activity_group` (`group_id`),
  KEY `idx_activity_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组活动表';

-- 表说明：活动报名记录
DROP TABLE IF EXISTS `activity_signup`;
CREATE TABLE `activity_signup` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_id` BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '报名用户ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SIGNED' COMMENT '状态: SIGNED-已报名 CANCELLED-已取消 ATTENDED-已到场',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_signup_pair` (`activity_id`, `user_id`),
  KEY `idx_signup_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名记录表';

-- 表说明：聊天会话
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '当前用户ID',
  `peer_id` BIGINT UNSIGNED NOT NULL COMMENT '对方用户ID',
  `last_message` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '最后一条消息',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-正常 MUTED-免打扰',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_pair` (`user_id`, `peer_id`),
  KEY `idx_session_peer` (`peer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 表说明：聊天消息
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `content` TEXT COMMENT '消息内容',
  `type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '消息类型: text-文本 image-图片 system-系统',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_message_session` (`session_id`),
  KEY `idx_message_sender` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';


-- =============================================================================
-- 三、成长组
-- =============================================================================

-- 表说明：课程
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '课程标题',
  `subtitle` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '副标题',
  `cover_image` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '封面图URL',
  `instructor` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '讲师名称',
  `instructor_avatar` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '讲师头像',
  `total_chapters` INT NOT NULL DEFAULT 0 COMMENT '总章节数',
  `reward_points` INT NOT NULL DEFAULT 0 COMMENT '奖励积分',
  `students` INT NOT NULL DEFAULT 0 COMMENT '学习人数',
  `rating` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '评分(0.00-5.00)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED-已发布 DRAFT-草稿 OFFLINE-下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_course_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 表说明：课程章节
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
  `title` VARCHAR(255) NOT NULL COMMENT '章节标题',
  `duration` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '时长(如 45分钟)',
  `video_url` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '视频URL',
  `chapter_order` INT NOT NULL DEFAULT 0 COMMENT '章节顺序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_chapter_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程章节表';

-- 表说明：课程学习进度
DROP TABLE IF EXISTS `course_progress`;
CREATE TABLE `course_progress` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `completed_chapters` INT NOT NULL DEFAULT 0 COMMENT '已完成章节数',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '进度百分比(0-100)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'not_started' COMMENT '学习状态: not_started-未开始 in_progress-学习中 completed-已完成',
  `last_study_at` DATETIME DEFAULT NULL COMMENT '最后学习时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_progress_pair` (`course_id`, `user_id`),
  KEY `idx_progress_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程学习进度表';

-- 表说明：课程作业
DROP TABLE IF EXISTS `assignment`;
CREATE TABLE `assignment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
  `chapter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联章节ID',
  `title` VARCHAR(255) NOT NULL COMMENT '作业标题',
  `description` TEXT COMMENT '作业描述',
  `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
  `answer` TEXT COMMENT '参考答案',
  `status` VARCHAR(20) NOT NULL DEFAULT 'not_submitted' COMMENT '状态: not_submitted-未提交 submitted-已提交 graded-已评分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_assignment_course` (`course_id`),
  KEY `idx_assignment_chapter` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程作业表';

-- 表说明：作业提交记录
DROP TABLE IF EXISTS `assignment_submission`;
CREATE TABLE `assignment_submission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `assignment_id` BIGINT UNSIGNED NOT NULL COMMENT '作业ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '提交用户ID',
  `content` TEXT COMMENT '提交内容/答案',
  `attachments` JSON DEFAULT NULL COMMENT '附件列表(JSON数组)',
  `submitted_at` DATETIME DEFAULT NULL COMMENT '提交时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'submitted' COMMENT '状态: submitted-已提交 graded-已评分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_pair` (`assignment_id`, `user_id`),
  KEY `idx_sub_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交记录表';

-- 表说明：作业评分
DROP TABLE IF EXISTS `assignment_grade`;
CREATE TABLE `assignment_grade` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `submission_id` BIGINT UNSIGNED NOT NULL COMMENT '提交记录ID',
  `assignment_id` BIGINT UNSIGNED NOT NULL COMMENT '作业ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '被评学生ID',
  `grader_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '评分人ID',
  `score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '得分',
  `feedback` TEXT COMMENT '批改反馈',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grade_sub` (`submission_id`),
  KEY `idx_grade_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业评分表';


-- =============================================================================
-- 四、任务组
-- =============================================================================

-- 表说明：任务分类
DROP TABLE IF EXISTS `task_category`;
CREATE TABLE `task_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '图标(图标名)',
  `task_count` INT NOT NULL DEFAULT 0 COMMENT '该分类下任务数',
  `category_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用 INACTIVE-停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tc_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务分类表';

-- 表说明：任务主表
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '任务标题',
  `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '任务分类ID',
  `category` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '分类名称(冗余)',
  `employer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发布方(雇主)用户ID',
  `employer_name` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '发布方名称',
  `description` TEXT COMMENT '任务描述',
  `reward` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '任务酬金',
  `level_required` INT NOT NULL DEFAULT 1 COMMENT '接单所需等级',
  `total_slots` INT NOT NULL DEFAULT 1 COMMENT '总名额',
  `slots_left` INT NOT NULL DEFAULT 1 COMMENT '剩余名额',
  `deadline` DATE DEFAULT NULL COMMENT '截止日期',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿 PENDING_REVIEW-待审核 APPROVED-审核通过 RECRUITING-招募中 IN_PROGRESS-进行中 COMPLETED-已完成 CLOSED-已关闭',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_task_category` (`category_id`),
  KEY `idx_task_status` (`status`),
  KEY `idx_task_employer` (`employer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务主表';

-- 表说明：任务技能要求
DROP TABLE IF EXISTS `task_skill`;
CREATE TABLE `task_skill` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  `name` VARCHAR(64) NOT NULL COMMENT '技能名称',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_task_skill_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务技能要求表';

-- 表说明：任务接单（订单）
DROP TABLE IF EXISTS `task_order`;
CREATE TABLE `task_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接单用户ID',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '完成进度(0-100)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'applied' COMMENT '状态: applied-已申请 accepted-已接单 in_progress-进行中 wait_acceptance-待验收 passed-验收通过 rejected-已驳回 settling-结算中 settled-已结算',
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_pair` (`task_id`, `user_id`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务接单表';

-- 表说明：任务里程碑
DROP TABLE IF EXISTS `task_milestone`;
CREATE TABLE `task_milestone` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '订单ID(NULL=任务级模板行;接单时复制为实例)',
  `task_id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  `title` VARCHAR(255) NOT NULL COMMENT '里程碑标题',
  `description` TEXT COMMENT '里程碑描述',
  `due_date` DATE DEFAULT NULL COMMENT '截止日期',
  `milestone_order` INT NOT NULL DEFAULT 0 COMMENT '里程碑顺序',
  `reward` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '里程碑酬金(分阶段结算:APPROVED 即按此金额入账)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态: NOT_STARTED-未开始 IN_PROGRESS-进行中 SUBMITTED-已提交 APPROVED-已通过 REJECTED-已驳回 OVERDUE-已逾期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_milestone_order` (`order_id`),
  KEY `idx_milestone_task` (`task_id`),
  KEY `idx_milestone_status` (`status`),
  KEY `idx_milestone_template` (`task_id`, `order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务里程碑表';

-- 表说明：里程碑提交记录
DROP TABLE IF EXISTS `milestone_submission`;
CREATE TABLE `milestone_submission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `milestone_id` BIGINT UNSIGNED NOT NULL COMMENT '里程碑ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '提交用户ID',
  `github_url` VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'GitHub仓库地址',
  `description` TEXT COMMENT '提交说明',
  `attachments` JSON DEFAULT NULL COMMENT '附件列表(JSON数组)',
  `submitted_at` DATETIME DEFAULT NULL COMMENT '提交时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ms_milestone` (`milestone_id`),
  KEY `idx_ms_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='里程碑提交记录表';

-- 表说明：里程碑审核记录
DROP TABLE IF EXISTS `milestone_review`;
CREATE TABLE `milestone_review` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `milestone_id` BIGINT UNSIGNED NOT NULL COMMENT '里程碑ID',
  `submission_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '提交记录ID',
  `reviewer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID',
  `result` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核结果: PENDING-待审核 APPROVED-通过 REJECTED-驳回',
  `feedback` TEXT COMMENT '审核反馈',
  `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_review_milestone` (`milestone_id`),
  KEY `idx_review_result` (`result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='里程碑审核记录表';


-- =============================================================================
-- 五、财务与通知组
-- =============================================================================

-- 表说明：钱包账户
DROP TABLE IF EXISTS `wallet_account`;
CREATE TABLE `wallet_account` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
  `pending_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '待入账金额',
  `withdrawn_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已提现金额',
  `total_earned` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计收益',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-正常 FROZEN-冻结',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wallet_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包账户表';

-- 表说明：钱包流水
DROP TABLE IF EXISTS `wallet_record`;
CREATE TABLE `wallet_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `type` VARCHAR(20) NOT NULL DEFAULT 'income' COMMENT '类型: income-收入 expense-支出 withdraw-提现',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待入账 available-已入账 withdrawing-提现中 withdrawn-已提现 failed-失败',
  `title` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '流水标题',
  `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '流水描述',
  `biz_type` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '业务类型: task/course/withdraw等',
  `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务关联ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_wallet_rec_user` (`user_id`),
  KEY `idx_wallet_rec_type` (`type`),
  KEY `idx_wallet_rec_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包流水表';

-- 表说明：提现记录
DROP TABLE IF EXISTS `withdraw_record`;
CREATE TABLE `withdraw_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '提现金额',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待处理 processing-处理中 completed-已完成 failed-失败',
  `bank_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '银行名称',
  `bank_account` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '银行账号(脱敏)',
  `failure_reason` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '失败原因',
  `processed_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_withdraw_user` (`user_id`),
  KEY `idx_withdraw_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现记录表';

-- 表说明：通知
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
  `type` VARCHAR(32) NOT NULL DEFAULT 'system' COMMENT '通知类型: milestone-里程碑 task_review-任务审核 income-收益 system-系统 group-社区 course-课程',
  `title` VARCHAR(255) NOT NULL COMMENT '通知标题',
  `content` TEXT COMMENT '通知内容',
  `link` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '跳转链接',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_notif_user` (`user_id`),
  KEY `idx_notif_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 表说明：通知已读记录
DROP TABLE IF EXISTS `notification_read`;
CREATE TABLE `notification_read` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notification_id` BIGINT UNSIGNED NOT NULL COMMENT '通知ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `read_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_read_pair` (`notification_id`, `user_id`),
  KEY `idx_read_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知已读记录表';


-- =============================================================================
-- 六、活动与岗位组
-- =============================================================================

-- 表说明：黑客松（Hackathon）
DROP TABLE IF EXISTS `hackathon`;
CREATE TABLE `hackathon` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '比赛标题',
  `description` TEXT COMMENT '比赛描述',
  `cover_image` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '封面图URL',
  `prize_pool` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '奖金池金额',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `signup_deadline` DATETIME DEFAULT NULL COMMENT '报名截止时间',
  `location` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '举办地点',
  `max_teams` INT NOT NULL DEFAULT 0 COMMENT '最大队伍数',
  `current_teams` INT NOT NULL DEFAULT 0 COMMENT '当前队伍数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'signup' COMMENT '状态: signup-报名中 ongoing-进行中 ended-已结束',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_hackathon_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑客松比赛表';

-- 表说明：黑客松参赛队伍
DROP TABLE IF EXISTS `hackathon_team`;
CREATE TABLE `hackathon_team` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hackathon_id` BIGINT UNSIGNED NOT NULL COMMENT '黑客松ID',
  `name` VARCHAR(128) NOT NULL COMMENT '队伍名称',
  `leader_id` BIGINT UNSIGNED NOT NULL COMMENT '队长用户ID',
  `project_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '项目名称',
  `project_desc` TEXT COMMENT '项目描述',
  `submitted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已提交项目: 0-否 1-是',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-正常 DISBANDED-已解散',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_hteam_hackathon` (`hackathon_id`),
  KEY `idx_hteam_leader` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑客松参赛队伍表';

-- 表说明：黑客松队伍成员
DROP TABLE IF EXISTS `hackathon_team_member`;
CREATE TABLE `hackathon_team_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_id` BIGINT UNSIGNED NOT NULL COMMENT '队伍ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role` VARCHAR(32) NOT NULL DEFAULT 'member' COMMENT '成员角色: leader-队长 member-队员',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_htm_pair` (`team_id`, `user_id`),
  KEY `idx_htm_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑客松队伍成员表';

-- 表说明：黑客松项目
DROP TABLE IF EXISTS `hackathon_project`;
CREATE TABLE `hackathon_project` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hackathon_id` BIGINT UNSIGNED NOT NULL COMMENT '黑客松ID',
  `team_id` BIGINT UNSIGNED NOT NULL COMMENT '队伍ID',
  `name` VARCHAR(255) NOT NULL COMMENT '项目名称',
  `description` TEXT COMMENT '项目描述',
  `repo_url` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '仓库地址',
  `demo_url` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '演示地址',
  `attachments` JSON DEFAULT NULL COMMENT '附件列表(JSON数组)',
  `score` DECIMAL(5,2) DEFAULT NULL COMMENT '得分',
  `rank` INT DEFAULT NULL COMMENT '排名',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态: DRAFT-草稿 SUBMITTED-已提交 SCORED-已评分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_hproject_team` (`team_id`),
  KEY `idx_hproject_hackathon` (`hackathon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑客松项目表';

-- 表说明：招聘岗位
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '岗位名称',
  `company` VARCHAR(128) NOT NULL COMMENT '公司名称',
  `company_logo` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '公司Logo',
  `location` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '工作地点',
  `salary` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '薪资描述',
  `type` VARCHAR(32) NOT NULL DEFAULT '全职' COMMENT '岗位类型: 实习/兼职/全职/外包',
  `tags` JSON DEFAULT NULL COMMENT '标签列表(JSON数组)',
  `description` TEXT COMMENT '岗位描述',
  `requirements` JSON DEFAULT NULL COMMENT '任职要求(JSON数组)',
  `contact` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '联系方式',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-招聘中 CLOSED-已关闭',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_job_type` (`type`),
  KEY `idx_job_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招聘岗位表';

-- 表说明：岗位投递记录
DROP TABLE IF EXISTS `job_application`;
CREATE TABLE `job_application` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_id` BIGINT UNSIGNED NOT NULL COMMENT '岗位ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '投递用户ID',
  `resume_url` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '简历地址',
  `cover_letter` TEXT COMMENT '求职信',
  `status` VARCHAR(20) NOT NULL DEFAULT 'submitted' COMMENT '状态: submitted-已投递 viewed-已查看 interview-面试中 offered-已发offer rejected-已拒绝',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_pair` (`job_id`, `user_id`),
  KEY `idx_app_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位投递记录表';


-- =============================================================================
-- 七、系统组
-- =============================================================================

-- 表说明：系统文件
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `original_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原始文件名',
  `stored_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '存储文件名',
  `url` VARCHAR(500) NOT NULL COMMENT '访问URL',
  `mime_type` VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'MIME类型',
  `size` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `storage_type` VARCHAR(32) NOT NULL DEFAULT 'local' COMMENT '存储类型: local-本地 oss-对象存储 cos-腾讯云',
  `biz_type` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '业务类型',
  `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务ID',
  `uploaded_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_file_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统文件表';

-- 表说明：数据字典
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型',
  `dict_key` VARCHAR(64) NOT NULL COMMENT '字典键',
  `dict_value` VARCHAR(255) NOT NULL COMMENT '字典值',
  `label` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '展示文本',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '备注',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用 INACTIVE-停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict` (`dict_type`, `dict_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 表说明：系统配置
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `config_type` VARCHAR(32) NOT NULL DEFAULT 'string' COMMENT '值类型: string/number/boolean/json',
  `label` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '配置名称',
  `remark` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 表说明：系统操作日志
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作用户名',
  `module` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '业务模块',
  `action` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作动作',
  `method` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '请求方法: GET/POST/PUT/DELETE',
  `url` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '请求URL',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'IP地址',
  `cost_ms` INT NOT NULL DEFAULT 0 COMMENT '耗时(毫秒)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '状态: SUCCESS-成功 FAIL-失败',
  `error_msg` TEXT COMMENT '错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删)',
  PRIMARY KEY (`id`),
  KEY `idx_oplog_user` (`user_id`),
  KEY `idx_oplog_module` (`module`),
  KEY `idx_oplog_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';


-- =============================================================================
-- 种子数据 (Seed Data)
-- 注意: 密码字段为 BCrypt 占位哈希，生产环境务必重新生成。
-- =============================================================================

-- ---------- 用户组种子 ----------
-- id=1 演示用户「林同学」(普通用户)，id=2~4 菁英用户
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `phone`, `email`, `gender`, `level`, `level_name`, `points`, `credit_score`, `completed_tasks`, `verified`, `status`, `last_login_at`) VALUES
(1, 'lin',    '$2a$10$506fC5TTEZT3S2pq6XmPouG5d/wsX18qCsP5N7clT2xpTyL6b9P4G', '林同学', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '13800000001', 'lin@blueblood.cn',   1, 5, '精英', 12800, 4.80, 23, 1, 'ACTIVE', '2024-06-15 10:00:00'),
(2, 'zhangming','$2a$10$506fC5TTEZT3S2pq6XmPouG5d/wsX18qCsP5N7clT2xpTyL6b9P4G', '张明',   'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '13800000002', 'zm@blueblood.cn',    1, 8, '大师', 45000, 4.95, 156, 1, 'ACTIVE', '2024-06-14 18:00:00'),
(3, 'lina',   '$2a$10$506fC5TTEZT3S2pq6XmPouG5d/wsX18qCsP5N7clT2xpTyL6b9P4G', '李娜',   'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '13800000003', 'lina@blueblood.cn',  2, 6, '资深', 28000, 4.90, 78,  1, 'ACTIVE', '2024-06-13 09:30:00'),
(4, 'wangqiang','$2a$10$506fC5TTEZT3S2pq6XmPouG5d/wsX18qCsP5N7clT2xpTyL6b9P4G','王强',   'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '13800000004', 'wq@blueblood.cn',    1, 7, '专家', 36000, 4.85, 112, 1, 'ACTIVE', '2024-06-12 14:00:00');

INSERT INTO `user_profile` (`user_id`, `school`, `major`, `bio`, `github`, `connections`, `followers`, `following`, `badges`, `radar_data`, `joined_at`) VALUES
(1, '浙江大学',   '计算机科学与技术', '热爱 AI 技术，专注于前端工程化与机器学习应用', 'https://github.com/lin同学',  45,  120, 60,  JSON_ARRAY(),                                            JSON_OBJECT('labels', JSON_ARRAY('代码能力','学习能力','协作能力','创新力','业务理解'), 'values', JSON_ARRAY(80,85,75,70,72)), '2024-01-15'),
(2, '清华大学',   '人工智能',        'AI 研究者，专注于大语言模型与应用落地',         'https://github.com/zhangming', 328, 1205, 89, JSON_ARRAY(JSON_OBJECT('id','b001','name','年度最佳','icon','🏆','description','2024年度最佳开发者'), JSON_OBJECT('id','b002','name','代码狂人','icon','💻','description','提交代码超过10000行')), JSON_OBJECT('labels', JSON_ARRAY('代码能力','学习能力','协作能力','创新力','业务理解'), 'values', JSON_ARRAY(95,88,82,90,85)), '2023-06-01'),
(3, '上海交通大学','软件工程',       '全栈工程师，专注云原生与 DevOps',               'https://github.com/lina',     156, 456,  234, JSON_ARRAY(JSON_OBJECT('id','b003','name','云原生专家','icon','☁️','description','K8s 认证专家')), JSON_OBJECT('labels', JSON_ARRAY('代码能力','学习能力','协作能力','创新力','业务理解'), 'values', JSON_ARRAY(88,85,90,78,88)), '2023-09-20'),
(4, '北京大学',   '数据科学',        '数据工程师，擅长大规模数据处理与 BI',           'https://github.com/wangqiang',245, 678,  123, JSON_ARRAY(JSON_OBJECT('id','b004','name','数据达人','icon','📊','description','处理数据量超过1TB')), JSON_OBJECT('labels', JSON_ARRAY('代码能力','学习能力','协作能力','创新力','业务理解'), 'values', JSON_ARRAY(82,92,80,85,90)), '2023-08-15');

INSERT INTO `user_skill` (`user_id`, `name`, `category`, `proficiency`) VALUES
(1,'Vue','前端',2),(1,'TypeScript','前端',2),(1,'Python','后端',1),(1,'TensorFlow','AI',1),
(2,'PyTorch','AI',2),(2,'深度学习','AI',2),(2,'计算机视觉','AI',2),(2,'LLM','AI',2),
(3,'React','前端',2),(3,'Node.js','后端',2),(3,'Go','后端',2),(3,'K8s','运维',2),
(4,'Python','后端',2),(4,'数据分析','数据',2),(4,'Spark','数据',2),(4,'机器学习','AI',2);

INSERT INTO `user_level_log` (`user_id`, `from_level`, `to_level`, `reason`) VALUES
(1, 4, 5, '完成任务积累成长积分，自动升级'),
(2, 7, 8, '年度最佳开发者奖励升级');

INSERT INTO `user_credit_log` (`user_id`, `delta`, `after_score`, `reason`) VALUES
(1, 0.10, 4.80, '任务验收通过，信誉分提升'),
(2, 0.05, 4.95, '高质量交付里程碑奖励');

INSERT INTO `user_connection` (`user_id`, `target_user_id`, `status`) VALUES
(1, 2, 'MUTUAL'),
(1, 4, 'MUTUAL'),
(1, 3, 'FOLLOWING'),
(3, 2, 'FOLLOWING');

-- ---------- 社区组种子 ----------
INSERT INTO `interest_group` (`id`, `name`, `description`, `cover_image`, `leader_id`, `category`, `tags`, `member_count`, `post_count`, `activity_count`, `status`) VALUES
(1, 'AI 前沿技术', '聚焦大语言模型、Agent、多模态等 AI 前沿话题', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 2, 'AI',      JSON_ARRAY('LLM','Agent','大模型'), 528, 36, 4, 'ACTIVE'),
(2, '前端工程化',   '讨论 Vue/React、构建工具、性能优化等前端工程实践','https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 3, 'Dev',     JSON_ARRAY('Vue','Vite','性能'), 312, 22, 2, 'ACTIVE'),
(3, '数据科学社',   '数据分析、可视化、数据工程经验交流',             'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 4, 'Study',   JSON_ARRAY('Python','SQL','BI'), 198, 14, 1, 'ACTIVE');

INSERT INTO `group_member` (`group_id`, `user_id`, `role`, `joined_at`) VALUES
(1, 2, 'leader', '2023-06-05 10:00:00'),
(1, 1, 'member', '2024-01-20 10:00:00'),
(2, 3, 'leader', '2023-09-22 10:00:00'),
(2, 1, 'member', '2024-02-01 10:00:00'),
(3, 4, 'leader', '2023-08-16 10:00:00');

INSERT INTO `post` (`id`, `group_id`, `author_id`, `title`, `content`, `images`, `tag`, `likes`, `comments`, `views`, `status`) VALUES
(1, 1, 2, 'LLM Agent 架构设计与实践', '本文分享如何基于 LangChain 设计一个可扩展的多 Agent 架构，涵盖规划、记忆、工具调用三大模块。', JSON_ARRAY('https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'), '经验分享', 128, 24, 890, 'PUBLISHED'),
(2, 1, 4, '数据预处理在 AI 训练中的重要性', '高质量的数据是模型效果的根本。本文总结了数据清洗、增强、采样的最佳实践。', JSON_ARRAY(), '话题', 56, 8, 432, 'PUBLISHED'),
(3, 2, 3, 'Vue3 + Vite 性能优化实战', '从构建配置到运行时优化，全面梳理 Vue3 项目的性能提升方案。', JSON_ARRAY('https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'), '经验分享', 89, 15, 678, 'PUBLISHED'),
(4, 2, 1, 'TypeScript 类型体操入门', '分享几个实用的 TypeScript 高级类型技巧，提升代码类型安全性。', JSON_ARRAY(), '话题', 34, 6, 256, 'PUBLISHED'),
(5, 3, 4, '一个数据分析师的成长路径', '从 SQL 到 Python 再到机器学习，分享我的学习路线与踩坑经验。', JSON_ARRAY(), '经验分享', 72, 12, 521, 'PUBLISHED');

INSERT INTO `post_comment` (`post_id`, `author_id`, `parent_id`, `content`, `likes`, `status`) VALUES
(1, 1, NULL, '非常系统的总结，受教了！', 5, 'NORMAL'),
(1, 4, NULL, '关于记忆模块能否再展开讲讲？', 3, 'NORMAL'),
(3, 2, NULL, '构建配置那部分很实用，已收藏。', 2, 'NORMAL');

INSERT INTO `post_like` (`post_id`, `user_id`) VALUES
(1,1),(1,4),(3,1),(3,2),(5,1);

INSERT INTO `post_favorite` (`post_id`, `user_id`) VALUES
(1,1),(3,1),(5,2);

INSERT INTO `activity` (`group_id`, `title`, `description`, `cover_image`, `start_time`, `end_time`, `location`, `signup_count`, `max_count`, `status`) VALUES
(1, 'LLM Hackathon 线下沙龙', '邀请业内专家分享大模型最新进展与 Agent 落地实践', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '2024-06-25 14:00:00', '2024-06-25 18:00:00', '杭州·西湖区会议室A', 35, 50, 'upcoming'),
(2, '前端性能优化 Workshop', '动手实操 Vue3 项目性能调优', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '2024-06-20 19:00:00', '2024-06-20 21:00:00', '线上·腾讯会议', 28, 100, 'upcoming');

INSERT INTO `activity_signup` (`activity_id`, `user_id`, `status`) VALUES
(1, 1, 'SIGNED'), (1, 4, 'SIGNED'), (2, 1, 'SIGNED'), (2, 3, 'SIGNED');

INSERT INTO `chat_session` (`user_id`, `peer_id`, `last_message`, `last_message_time`, `unread_count`, `status`) VALUES
(1, 2, '林同学，里程碑审核已通过，辛苦了！', '2024-06-15 14:30:00', 1, 'ACTIVE'),
(1, 3, '前端那篇帖子可以转发吗？',           '2024-06-14 10:00:00', 0, 'ACTIVE');

INSERT INTO `chat_message` (`session_id`, `sender_id`, `receiver_id`, `content`, `type`, `is_read`) VALUES
(1, 2, 1, '你好，任务进展如何？', 'text', 1),
(1, 1, 2, '第一阶段已完成，正在推进第二阶段。', 'text', 1),
(1, 2, 1, '林同学，里程碑审核已通过，辛苦了！', 'text', 0),
(2, 3, 1, '前端那篇帖子可以转发吗？', 'text', 1);

-- ---------- 成长组种子 ----------
INSERT INTO `course` (`id`, `title`, `subtitle`, `cover_image`, `instructor`, `instructor_avatar`, `total_chapters`, `reward_points`, `students`, `rating`, `status`) VALUES
(1, 'Vue3 企业级实战：从前端到全栈', '从 0 到 1 构建企业级后台管理系统', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '李娜', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 5, 500, 1234, 4.90, 'PUBLISHED'),
(2, 'Python 异步编程精进',           '掌握 asyncio 与并发编程核心',     'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '张明', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 3, 300, 876,  4.80, 'PUBLISHED'),
(3, '机器学习实战：从理论到部署',     'Scikit-Learn 与 MLflow 全流程覆盖','https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '王强', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 2, 400, 2105, 4.70, 'PUBLISHED'),
(4, 'AI Agent 开发实战', '从 0 到 1 构建能自主规划与执行的智能体', 'https://picsum.photos/seed/agent/400/300', '陈昊', 'https://picsum.photos/seed/avatar1/100/100', 4, 600, 1856, 4.90, 'PUBLISHED'),
(5, '提示词工程精通', '掌握 Prompt 核心技巧,让大模型输出质变', 'https://picsum.photos/seed/prompt/400/300', '林若', 'https://picsum.photos/seed/avatar2/100/100', 4, 400, 3204, 4.85, 'PUBLISHED'),
(6, 'RAG 检索增强系统', '向量库 + 检索 + 生成的企业知识库实战', 'https://picsum.photos/seed/rag/400/300', '王哲', 'https://picsum.photos/seed/avatar3/100/100', 5, 700, 980, 4.80, 'PUBLISHED'),
(7, 'LangChain 应用开发', '用 LangChain 快速搭建复杂 LLM 应用', 'https://picsum.photos/seed/langchain/400/300', '赵敏', 'https://picsum.photos/seed/avatar4/100/100', 4, 500, 1340, 4.75, 'PUBLISHED'),
(8, '大模型微调实战', 'LoRA/QLoRA 高效微调开源大模型', 'https://picsum.photos/seed/finetune/400/300', '刘洋', 'https://picsum.photos/seed/avatar5/100/100', 4, 800, 670, 4.70, 'PUBLISHED'),
(9, 'AI 数据处理与标注', '高质量数据集构建与清洗全流程', 'https://picsum.photos/seed/data/400/300', '孙琳', 'https://picsum.photos/seed/avatar6/100/100', 3, 350, 540, 4.60, 'PUBLISHED'),
(10, '计算机视觉入门', 'PyTorch 图像分类与目标检测实战', 'https://picsum.photos/seed/cv/400/300', '周磊', 'https://picsum.photos/seed/avatar7/100/100', 4, 550, 1120, 4.65, 'PUBLISHED'),
(11, 'AI 产品设计思维', '从需求到落地,设计有价值的 AI 产品', 'https://picsum.photos/seed/product/400/300', '吴婷', 'https://picsum.photos/seed/avatar8/100/100', 3, 300, 2100, 4.88, 'PUBLISHED');

INSERT INTO `course_chapter` (`id`, `course_id`, `title`, `duration`, `video_url`, `chapter_order`) VALUES
(1, 1, 'Vue3 核心概念回顾',        '45分钟', 'https://cdn.blueblood.cn/v1.mp4', 1),
(2, 1, 'TypeScript 在 Vue 中的应用','60分钟', 'https://cdn.blueblood.cn/v2.mp4', 2),
(3, 1, 'Pinia 状态管理详解',       '55分钟', 'https://cdn.blueblood.cn/v3.mp4', 3),
(4, 1, 'Vue Router 高级用法',      '50分钟', 'https://cdn.blueblood.cn/v4.mp4', 4),
(5, 1, 'Vant 组件库实战',          '65分钟', 'https://cdn.blueblood.cn/v5.mp4', 5),
(6, 2, '异步编程基础概念',         '40分钟', 'https://cdn.blueblood.cn/p1.mp4', 1),
(7, 2, 'asyncio 核心 API',         '70分钟', 'https://cdn.blueblood.cn/p2.mp4', 2),
(8, 2, '异步 HTTP 请求实战',       '55分钟', 'https://cdn.blueblood.cn/p3.mp4', 3),
(9, 3, '机器学习概述',             '35分钟', 'https://cdn.blueblood.cn/m1.mp4', 1),
(10,3, '监督学习算法',             '80分钟', 'https://cdn.blueblood.cn/m2.mp4', 2),
(11,4,'Agent 概念与核心架构','38分钟','',1),
(12,4,'工具调用 Function Calling','52分钟','',2),
(13,4,'规划与记忆机制','46分钟','',3),
(14,4,'多 Agent 协作实战','60分钟','',4),
(15,5,'Prompt 基础原理','30分钟','',1),
(16,5,'Few-shot 与思维链','45分钟','',2),
(17,5,'结构化输出控制','40分钟','',3),
(18,5,'复杂任务 Prompt 编排','55分钟','',4),
(19,6,'RAG 原理与整体架构','35分钟','',1),
(20,6,'文档切分与向量化','48分钟','',2),
(21,6,'向量数据库选型','42分钟','',3),
(22,6,'混合检索与重排序','50分钟','',4),
(23,6,'生产级 RAG 优化','65分钟','',5),
(24,7,'LangChain 核心组件','36分钟','',1),
(25,7,'Chain 与 Memory','44分钟','',2),
(26,7,'Agent 与工具集成','52分钟','',3),
(27,7,'实战:智能问答机器人','58分钟','',4),
(28,8,'微调原理与数据准备','40分钟','',1),
(29,8,'LoRA 低秩适配','55分钟','',2),
(30,8,'QLoRA 显存优化','50分钟','',3),
(31,8,'微调效果评估','45分钟','',4),
(32,9,'数据采集与清洗','38分钟','',1),
(33,9,'标注规范设计','42分钟','',2),
(34,9,'质量校验与迭代','36分钟','',3),
(35,10,'PyTorch 基础','34分钟','',1),
(36,10,'图像分类实战','48分钟','',2),
(37,10,'目标检测 YOLO','56分钟','',3),
(38,10,'模型部署','44分钟','',4),
(39,11,'AI 产品需求洞察','32分钟','',1),
(40,11,'场景与价值设计','40分钟','',2),
(41,11,'从 MVP 到增长','46分钟','',3);

INSERT INTO `course_progress` (`course_id`, `user_id`, `completed_chapters`, `progress`, `status`, `last_study_at`) VALUES
(1, 1, 2, 40, 'in_progress', '2024-06-15 20:00:00'),
(2, 1, 2, 67, 'in_progress', '2024-06-14 21:00:00'),
(3, 1, 0, 0,  'not_started', NULL);

INSERT INTO `assignment` (`id`, `course_id`, `chapter_id`, `title`, `description`, `deadline`, `answer`, `status`) VALUES
(1, 1, 3, 'Pinia 状态管理课后练习', '基于给定的电商场景，使用 Pinia 实现购物车模块，包含添加商品、修改数量、删除商品、计算总价、持久化到 localStorage。', '2024-06-20 23:59:00', '参考实现已上传至课程资料', 'not_submitted'),
(2, 2, 8, '并发爬虫实战',           '使用 aiohttp 实现一个并发爬虫程序，要求使用信号量控制并发、实现错误重试、保存结果到 SQLite。',          '2024-06-18 23:59:00', '参考实现已上传至课程资料', 'graded');

INSERT INTO `assignment_submission` (`id`, `assignment_id`, `user_id`, `content`, `attachments`, `submitted_at`, `status`) VALUES
(1, 2, 1, '已完成并发爬虫，使用信号量控制并发为 10，包含三次重试机制，结果存入 SQLite。', JSON_ARRAY('https://cdn.blueblood.cn/sub_a002.zip'), '2024-06-15 20:30:00', 'graded');

INSERT INTO `assignment_grade` (`submission_id`, `assignment_id`, `user_id`, `grader_id`, `score`, `feedback`) VALUES
(1, 2, 1, 2, 92.00, '代码结构清晰，并发控制得当，建议增加代理池提升稳定性');

-- ---------- 任务组种子 ----------
INSERT INTO `task_category` (`id`, `name`, `icon`, `task_count`, `category_order`, `status`) VALUES
(1, '全部',       'apps-o',             156, 1, 'ACTIVE'),
(2, 'Agent配置',  'chat-o',              34, 2, 'ACTIVE'),
(3, '自动化脚本', 'play-circle-o',       45, 3, 'ACTIVE'),
(4, '流程梳理',   'orders-o',            28, 4, 'ACTIVE'),
(5, '报告生成',   'description',         23, 5, 'ACTIVE'),
(6, '数据处理',   'chart-trending-o',    26, 6, 'ACTIVE');

INSERT INTO `task` (`id`, `title`, `category_id`, `category`, `employer_id`, `employer_name`, `description`, `reward`, `level_required`, `total_slots`, `slots_left`, `deadline`, `status`, `view_count`) VALUES
(1, 'AI 客服 Agent 配置与优化',    2, 'Agent配置',  NULL, '星辰科技', '配置一个基于 LangChain 的 AI 客服 Agent，支持多轮对话、意图识别和知识库检索，接入企业微信，响应时间<2s。', 5000.00, 5, 3, 2, '2024-07-15', 'RECRUITING',  320),
(2, '数据采集自动化脚本开发',      3, '自动化脚本',NULL, '明远数据', '开发数据采集脚本，支持定时抓取多个电商平台商品价格与库存，存储到 MySQL 并导出 Excel。',           3000.00, 3, 2, 1, '2024-06-30', 'IN_PROGRESS', 256),
(3, '业务流程梳理与文档编写',      4, '流程梳理',  NULL, '云尚电商', '梳理电商平台退换货业务流程，输出流程图、状态机文档和接口需求说明。',                              2000.00, 2, 3, 3, '2024-06-25', 'IN_PROGRESS', 180),
(4, '市场调研报告生成',            5, '报告生成',  NULL, '创投资本', '生成某细分领域 AI 市场调研报告，含市场规模、竞争格局、主要玩家分析和趋势预测，5000字以上。',       2500.00, 4, 1, 0, '2024-06-20', 'COMPLETED',   150),
(5, '用户行为数据分析',            6, '数据处理',  NULL, '蓝鲸数据', '对 App 用户行为日志进行数据分析，输出留存率、转化率、用户路径等指标并给出优化建议。',             4000.00, 4, 2, 2, '2024-07-01', 'RECRUITING',  210),
(6, '大模型微调与评测',            2, 'Agent配置',  NULL, '智源科技', '基于开源大模型进行领域数据微调，并完成评测与对比报告。',                                          8000.00, 6, 1, 1, '2024-07-10', 'APPROVED',    95);

INSERT INTO `task_skill` (`task_id`, `name`) VALUES
(1,'LangChain'),(1,'Python'),(1,'Agent'),(1,'知识库'),
(2,'Python'),(2,'Selenium'),(2,'MySQL'),(2,'爬虫'),
(3,'流程设计'),(3,'文档编写'),(3,'UML'),
(4,'市场分析'),(4,'报告撰写'),(4,'AI行业'),
(5,'Python'),(5,'数据分析'),(5,'SQL'),(5,'可视化'),
(6,'PyTorch'),(6,'大模型'),(6,'微调');

-- 进行中接单样例: 用户1 接了任务2
INSERT INTO `task_order` (`id`, `task_id`, `user_id`, `progress`, `status`, `remark`) VALUES
(1, 2, 1, 60, 'in_progress', '林同学接单进行中，里程碑推进至第二阶段');

INSERT INTO `task_milestone` (`id`, `order_id`, `task_id`, `title`, `description`, `due_date`, `milestone_order`, `status`) VALUES
(1, 1, 2, '需求分析与技术方案', '完成技术方案设计',       '2024-06-15', 1, 'APPROVED'),
(2, 1, 2, '核心脚本开发',       '完成数据采集核心逻辑',   '2024-06-22', 2, 'IN_PROGRESS'),
(3, 1, 2, '数据存储与导出',     'MySQL 存储与 Excel 导出','2024-06-28', 3, 'NOT_STARTED');

INSERT INTO `milestone_submission` (`id`, `milestone_id`, `order_id`, `user_id`, `github_url`, `description`, `attachments`, `submitted_at`) VALUES
(1, 1, 1, 1, 'https://github.com/lin同学/data-collector', '已完成需求分析和技术方案设计，详见文档。', JSON_ARRAY('https://cdn.blueblood.cn/m1_doc.pdf'), '2024-06-14 18:00:00');

INSERT INTO `milestone_review` (`milestone_id`, `submission_id`, `reviewer_id`, `result`, `feedback`, `reviewed_at`) VALUES
(1, 1, NULL, 'APPROVED', '技术方案清晰可行，准予进入下一阶段。', '2024-06-15 14:30:00');

-- ---------- 财务与通知组种子 ----------
INSERT INTO `wallet_account` (`user_id`, `balance`, `pending_amount`, `withdrawn_amount`, `total_earned`, `status`) VALUES
(1, 3850.00, 1500.00, 12500.00, 17850.00, 'ACTIVE'),
(2, 12000.00, 0.00, 30000.00, 42000.00, 'ACTIVE');

INSERT INTO `wallet_record` (`id`, `user_id`, `type`, `amount`, `status`, `title`, `description`, `biz_type`, `biz_id`) VALUES
(1, 1, 'income',   1500.00, 'available', '任务结算收入', 'AI 客服 Agent 配置与优化 - 第一期里程碑', 'task', NULL),
(2, 1, 'income',   2000.00, 'pending',   '任务结算收入', '数据采集自动化脚本开发 - 定金',           'task', 2),
(3, 1, 'withdraw', 3000.00, 'withdrawn', '提现',         '提现至 招商银行 **** 4589',                'withdraw', NULL),
(4, 1, 'income',   500.00,  'available', '课程学习奖励', '完成 Vue3 企业级实战 课程第二章',          'course', 1),
(5, 1, 'withdraw', 5000.00, 'withdrawn', '提现',         '提现至 招商银行 **** 4589',                'withdraw', NULL);

INSERT INTO `withdraw_record` (`id`, `user_id`, `amount`, `status`, `bank_name`, `bank_account`, `failure_reason`, `processed_at`) VALUES
(1, 1, 3000.00, 'completed', '招商银行', '**** 4589', '', '2024-06-05 12:00:00'),
(2, 1, 5000.00, 'completed', '招商银行', '**** 4589', '', '2024-05-28 14:00:00');

INSERT INTO `notification` (`id`, `user_id`, `type`, `title`, `content`, `link`) VALUES
(1, 1, 'milestone',   '里程碑审核通过', '恭喜！您的里程碑「需求分析与技术方案」已审核通过，下一阶段任务已解锁。', '/tasks/execution/1'),
(2, 1, 'task_review', '任务审核通知',   '您申请参与的「市场调研报告生成」任务已发布方确认，请尽快开始执行。',     '/tasks/execution/2'),
(3, 1, 'income',      '收益到账通知',   '您的收益 ¥1,500.00 已到账，来源于「AI 客服 Agent 配置与优化」任务第一期结算。', '/mine/wallet'),
(4, 1, 'system',      '等级提升恭喜',   '恭喜！您的等级已从 LV4 提升到 LV5，解锁新技能标签和更高价值任务接单资格。', '/mine/profile'),
(5, 1, 'group',       '新帖子通知',     '您加入的小组「AI 前沿技术」有新帖子发布：张明分享了《LLM Agent 架构设计与实践》', '/discover/group/1/post/1'),
(6, 1, 'course',      '作业批改完成',   '您的作业「并发爬虫实战」已被批改，得分 92 分。',                         '/grow/assignment/2/result');

INSERT INTO `notification_read` (`notification_id`, `user_id`, `read_at`) VALUES
(3, 1, '2024-06-12 09:05:00'),
(4, 1, '2024-06-10 08:10:00'),
(6, 1, '2024-06-15 21:00:00');

-- ---------- 活动与岗位组种子 ----------
INSERT INTO `hackathon` (`id`, `title`, `description`, `cover_image`, `prize_pool`, `start_time`, `end_time`, `signup_deadline`, `location`, `max_teams`, `current_teams`, `status`) VALUES
(1, '2024 AI Agent 创新马拉松', '48 小时极速开发，打造下一代 AI Agent 应用', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 100000.00, '2024-07-05 09:00:00', '2024-07-07 18:00:00', '2024-07-01 23:59:00', '杭州·滨江创业园', 50, 32, 'signup'),
(2, '数据可视化挑战赛',         '用数据讲述故事，赢取万元奖金',               'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', 50000.00,  '2024-06-10 09:00:00', '2024-06-12 18:00:00', '2024-06-08 23:59:00', '线上',           30, 30, 'ended');

INSERT INTO `hackathon_team` (`id`, `hackathon_id`, `name`, `leader_id`, `project_name`, `project_desc`, `submitted`, `status`) VALUES
(1, 1, '代码先锋队', 1, 'AI 编程助手', '基于大模型的智能编程辅助 Agent，支持代码生成、审查、重构。', 1, 'ACTIVE'),
(2, 1, '数据极客',   4, '数据叙事者',   '自动从数据生成可视化故事的工具。',                          1, 'ACTIVE');

INSERT INTO `hackathon_team_member` (`team_id`, `user_id`, `role`) VALUES
(1, 1, 'leader'), (1, 2, 'member'),
(2, 4, 'leader'), (2, 3, 'member');

INSERT INTO `hackathon_project` (`hackathon_id`, `team_id`, `name`, `description`, `repo_url`, `demo_url`, `attachments`, `score`, `rank`, `status`) VALUES
(2, 1, '数据叙事者', '自动从数据生成可视化故事的工具，支持自然语言交互。', 'https://github.com/lin同学/data-story', 'https://demo.blueblood.cn/data-story', JSON_ARRAY('https://cdn.blueblood.cn/proj_demo.pdf'), 88.50, 3, 'SCORED');

INSERT INTO `job` (`id`, `title`, `company`, `company_logo`, `location`, `salary`, `type`, `tags`, `description`, `requirements`, `contact`, `status`) VALUES
(1, 'AI 算法实习生',   '星辰科技', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '杭州', '300-500/天', '实习', JSON_ARRAY('LLM','Python','PyTorch'), '参与大模型应用研发，负责 Agent 与 RAG 系统开发。', JSON_ARRAY('熟悉 Python 与深度学习','了解 LLM/Agent 相关技术','每周实习 4 天以上'), 'hr@xingchen.cn', 'ACTIVE'),
(2, '前端开发工程师', '云尚电商', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '上海', '20-35K·14薪', '全职', JSON_ARRAY('Vue','TypeScript'), '负责电商平台前端开发与性能优化。', JSON_ARRAY('3 年以上 Vue 开发经验','熟悉工程化与性能优化','本科及以上学历'), 'hr@yunshang.cn', 'ACTIVE'),
(3, '数据分析师(兼职)','蓝鲸数据','https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '远程', '8-12K/月',    '兼职', JSON_ARRAY('SQL','Python','BI'), '负责业务数据分析与可视化报告输出。', JSON_ARRAY('熟练 SQL 与 Python','有电商数据分析经验优先'), 'hr@lanjing.cn', 'ACTIVE');

INSERT INTO `job_application` (`job_id`, `user_id`, `resume_url`, `cover_letter`, `status`) VALUES
(1, 1, 'https://cdn.blueblood.cn/resume_lin.pdf', '对大模型应用充满热情，希望加入团队积累实战经验。', 'submitted'),
(2, 3, 'https://cdn.blueblood.cn/resume_lina.pdf','5 年 Vue 开发经验，希望加入贵司前端团队。',         'interview');

-- ---------- 系统组种子 ----------
INSERT INTO `sys_dict` (`dict_type`, `dict_key`, `dict_value`, `label`, `sort`, `remark`) VALUES
('task_status', 'DRAFT',          'DRAFT',          '草稿',     1, '任务状态'),
('task_status', 'PENDING_REVIEW', 'PENDING_REVIEW', '待审核',   2, '任务状态'),
('task_status', 'APPROVED',       'APPROVED',       '审核通过', 3, '任务状态'),
('task_status', 'RECRUITING',     'RECRUITING',     '招募中',   4, '任务状态'),
('task_status', 'IN_PROGRESS',    'IN_PROGRESS',    '进行中',   5, '任务状态'),
('task_status', 'COMPLETED',      'COMPLETED',      '已完成',   6, '任务状态'),
('task_status', 'CLOSED',         'CLOSED',         '已关闭',   7, '任务状态'),
('milestone_status', 'NOT_STARTED','NOT_STARTED','未开始', 1, '里程碑状态'),
('milestone_status', 'IN_PROGRESS','IN_PROGRESS','进行中', 2, '里程碑状态'),
('milestone_status', 'SUBMITTED',  'SUBMITTED',  '已提交', 3, '里程碑状态'),
('milestone_status', 'APPROVED',   'APPROVED',   '已通过', 4, '里程碑状态'),
('milestone_status', 'REJECTED',   'REJECTED',   '已驳回', 5, '里程碑状态'),
('milestone_status', 'OVERDUE',    'OVERDUE',    '已逾期', 6, '里程碑状态'),
('notification_type','milestone',  'milestone',  '里程碑',  1, '通知类型'),
('notification_type','task_review','task_review','任务审核',2, '通知类型'),
('notification_type','income',     'income',     '收益',    3, '通知类型'),
('notification_type','system',     'system',     '系统',    4, '通知类型'),
('notification_type','group',      'group',      '社区',    5, '通知类型'),
('notification_type','course',     'course',     '课程',    6, '通知类型');

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `label`, `remark`) VALUES
('site.name',            '蓝血菁英平台',           'string', '站点名称',   '平台名称'),
('user.init_level',      '1',                     'number', '初始等级',   '新用户默认等级'),
('user.init_credit',     '5.00',                  'string', '初始信誉分', '新用户默认信誉分'),
('task.min_level',       '1',                     'number', '接单最低等级','接单所需最低等级'),
('withdraw.min_amount',  '100.00',                'string', '最小提现金额','单笔最小提现金额'),
('withdraw.fee_rate',    '0.00',                  'string', '提现手续费率','提现手续费比例'),
('upload.max_size_mb',   '20',                    'number', '上传大小上限','单文件上传大小上限(MB)');

-- =============================================================================
-- 八、模块03扩展（角色多对多 + 认证申请）
-- =============================================================================

-- 表说明：系统角色（USER/ADMIN）
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
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

-- 表说明：用户-角色关联（多对多）
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
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

-- 表说明：用户认证申请（状态流转 PENDING/APPROVED/REJECTED）
DROP TABLE IF EXISTS `user_verification`;
CREATE TABLE `user_verification` (
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

INSERT INTO `sys_role` (`id`, `code`, `name`, `description`) VALUES
(1, 'USER',  '普通用户', '平台普通用户'),
(2, 'ADMIN', '管理员',   '系统管理员/审核员');

-- 管理员账号(id=5)；密码为 BCrypt 占位，生产/联调前需用真实哈希覆盖
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `phone`, `email`,
                    `gender`, `level`, `level_name`, `points`, `credit_score`, `completed_tasks`, `verified`, `status`)
VALUES (5, 'admin', '$2a$10$506fC5TTEZT3S2pq6XmPouG5d/wsX18qCsP5N7clT2xpTyL6b9P4G', '平台管理员',
        'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '13800000000', 'admin@blueblood.cn',
        1, 99, '管理员', 99999, 5.00, 0, 1, 'ACTIVE');

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1,1),(2,1),(3,1),(4,1),(5,2);

INSERT INTO `user_verification` (`user_id`, `real_name`, `id_number`, `status`, `reviewer_id`, `reviewed_at`) VALUES
(2, '张明', '4401**********1234', 'APPROVED', 5, '2024-06-15 12:00:00');

-- =============================================================================
-- 九、企业用户 + 活动发布扩展(v3_001)
-- =============================================================================
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `user_type` VARCHAR(20) NOT NULL DEFAULT 'personal' COMMENT '用户类型: personal-个人 enterprise-企业';
ALTER TABLE `job` ADD COLUMN IF NOT EXISTS `published_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '发布企业用户ID';
ALTER TABLE `hackathon` ADD COLUMN IF NOT EXISTS `published_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '发布企业用户ID';

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

-- 演示企业账号(id=6, ACME科技, 密码 123456, 已是企业用户)
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `phone`, `email`,
                    `gender`, `level`, `level_name`, `points`, `credit_score`, `completed_tasks`, `verified`, `status`, `user_type`)
VALUES (6, 'acme', '$2a$10$506fC5TTEZT3S2pq6XmPouG5d/wsX18qCsP5N7clT2xpTyL6b9P4G', 'ACME科技',
        'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '13800000006', 'hr@acme.cn',
        0, 1, '企业', 0, 5.00, 0, 1, 'ACTIVE', 'enterprise');
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (6, 1);
INSERT INTO `enterprise_application` (`user_id`, `company_name`, `credit_code`, `license_url`, `contact_name`, `contact_phone`, `status`, `reviewer_id`, `reviewed_at`)
VALUES (6, 'ACME科技', '91110100MA01ABCDEF', 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', '李HR', '13800000006', 'APPROVED', 5, '2024-06-10 10:00:00');

SET FOREIGN_KEY_CHECKS = 1;
