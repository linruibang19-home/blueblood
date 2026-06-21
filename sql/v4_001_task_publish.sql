-- =============================================================================
-- v4_001 任务模块:用户端发布 + 多里程碑模板 + 分阶段结算
-- 决策依据:
--   Q1 企业+个人均可发布;Q2 发布即上架;Q3 雇主自助验收;Q4 里程碑分阶段结算
-- 设计:
--   - task_milestone.order_id 改 NULLABLE(NULL=任务级模板行;接单时复制为实例)
--   - task_milestone 加 reward(每里程碑独立金额,APPROVED 即入账接单者钱包)
--   - wallet_record 加 (biz_type,biz_id) 索引(结算幂等查重)
-- 应用(已运行的库,执行一次):
--   docker exec -i blueblood-v2-mysql mysql -uroot -pblueblood blueblood_v2 < sql/v4_001_task_publish.sql
-- 注意:init.sql 已同步为最终结构,新部署的库无需再跑本文件。
-- =============================================================================
USE blueblood_v2;
SET NAMES utf8mb4;

-- 1. task_milestone: order_id 改 NULLABLE(NULL 表示任务级模板行)
ALTER TABLE `task_milestone`
    MODIFY COLUMN `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '订单ID(NULL=任务级模板行)';

-- 2. task_milestone: 加每里程碑独立金额
ALTER TABLE `task_milestone`
    ADD COLUMN `reward` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '里程碑酬金(分阶段结算)' AFTER `milestone_order`;

-- 3. task_milestone: 模板查询索引(task_id + order_id,order_id IS NULL 取模板)
ALTER TABLE `task_milestone`
    ADD KEY `idx_milestone_template` (`task_id`, `order_id`);

-- 4. wallet_record: 结算幂等查重索引(biz_type + biz_id)
ALTER TABLE `wallet_record`
    ADD KEY `idx_wallet_rec_biz` (`biz_type`, `biz_id`);

-- 5. 历史数据回填:已有订单实例里程碑(单条"任务交付")reward 设为 task.reward,保证可结算
UPDATE `task_milestone` m
JOIN `task` t ON m.task_id = t.id
SET m.reward = t.reward
WHERE m.order_id IS NOT NULL
  AND m.deleted_at IS NULL
  AND m.reward = 0.00
  AND t.reward IS NOT NULL;
