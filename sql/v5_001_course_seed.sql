-- =============================================================================
-- v5_001 课程种子数据:补充 8 门 AI 主题课程 + 章节
-- 平台定位"AI 人才成长",课程围绕 Agent/Prompt/RAG/LangChain/微调/CV/数据/产品
-- 封面用 picsum 占位(每门不同 seed → 不同图);执行一次
--   docker exec -i blueblood-v2-mysql mysql -uroot -pblueblood blueblood_v2 < sql/v5_001_course_seed.sql
-- =============================================================================
USE blueblood_v2;
SET NAMES utf8mb4;

-- 8 门课程
INSERT INTO `course` (title, subtitle, cover_image, instructor, instructor_avatar, total_chapters, reward_points, students, rating, status) VALUES
('AI Agent 开发实战', '从 0 到 1 构建能自主规划与执行的智能体', 'https://picsum.photos/seed/agent/400/300', '陈昊', 'https://picsum.photos/seed/avatar1/100/100', 4, 600, 1856, 4.90, 'PUBLISHED'),
('提示词工程精通', '掌握 Prompt 核心技巧,让大模型输出质变', 'https://picsum.photos/seed/prompt/400/300', '林若', 'https://picsum.photos/seed/avatar2/100/100', 4, 400, 3204, 4.85, 'PUBLISHED'),
('RAG 检索增强系统', '向量库 + 检索 + 生成的企业知识库实战', 'https://picsum.photos/seed/rag/400/300', '王哲', 'https://picsum.photos/seed/avatar3/100/100', 5, 700, 980, 4.80, 'PUBLISHED'),
('LangChain 应用开发', '用 LangChain 快速搭建复杂 LLM 应用', 'https://picsum.photos/seed/langchain/400/300', '赵敏', 'https://picsum.photos/seed/avatar4/100/100', 4, 500, 1340, 4.75, 'PUBLISHED'),
('大模型微调实战', 'LoRA/QLoRA 高效微调开源大模型', 'https://picsum.photos/seed/finetune/400/300', '刘洋', 'https://picsum.photos/seed/avatar5/100/100', 4, 800, 670, 4.70, 'PUBLISHED'),
('AI 数据处理与标注', '高质量数据集构建与清洗全流程', 'https://picsum.photos/seed/data/400/300', '孙琳', 'https://picsum.photos/seed/avatar6/100/100', 3, 350, 540, 4.60, 'PUBLISHED'),
('计算机视觉入门', 'PyTorch 图像分类与目标检测实战', 'https://picsum.photos/seed/cv/400/300', '周磊', 'https://picsum.photos/seed/avatar7/100/100', 4, 550, 1120, 4.65, 'PUBLISHED'),
('AI 产品设计思维', '从需求到落地,设计有价值的 AI 产品', 'https://picsum.photos/seed/product/400/300', '吴婷', 'https://picsum.photos/seed/avatar8/100/100', 3, 300, 2100, 4.88, 'PUBLISHED');

-- 章节(按 title 子查询关联 course_id;每门 3-5 章)
INSERT INTO `course_chapter` (course_id, title, duration, video_url, chapter_order) VALUES
((SELECT id FROM course WHERE title='AI Agent 开发实战'), 'Agent 概念与核心架构', '38分钟', '', 1),
((SELECT id FROM course WHERE title='AI Agent 开发实战'), '工具调用 Function Calling', '52分钟', '', 2),
((SELECT id FROM course WHERE title='AI Agent 开发实战'), '规划与记忆机制', '46分钟', '', 3),
((SELECT id FROM course WHERE title='AI Agent 开发实战'), '多 Agent 协作实战', '60分钟', '', 4),

((SELECT id FROM course WHERE title='提示词工程精通'), 'Prompt 基础原理', '30分钟', '', 1),
((SELECT id FROM course WHERE title='提示词工程精通'), 'Few-shot 与思维链', '45分钟', '', 2),
((SELECT id FROM course WHERE title='提示词工程精通'), '结构化输出控制', '40分钟', '', 3),
((SELECT id FROM course WHERE title='提示词工程精通'), '复杂任务 Prompt 编排', '55分钟', '', 4),

((SELECT id FROM course WHERE title='RAG 检索增强系统'), 'RAG 原理与整体架构', '35分钟', '', 1),
((SELECT id FROM course WHERE title='RAG 检索增强系统'), '文档切分与向量化', '48分钟', '', 2),
((SELECT id FROM course WHERE title='RAG 检索增强系统'), '向量数据库选型', '42分钟', '', 3),
((SELECT id FROM course WHERE title='RAG 检索增强系统'), '混合检索与重排序', '50分钟', '', 4),
((SELECT id FROM course WHERE title='RAG 检索增强系统'), '生产级 RAG 优化', '65分钟', '', 5),

((SELECT id FROM course WHERE title='LangChain 应用开发'), 'LangChain 核心组件', '36分钟', '', 1),
((SELECT id FROM course WHERE title='LangChain 应用开发'), 'Chain 与 Memory', '44分钟', '', 2),
((SELECT id FROM course WHERE title='LangChain 应用开发'), 'Agent 与工具集成', '52分钟', '', 3),
((SELECT id FROM course WHERE title='LangChain 应用开发'), '实战:智能问答机器人', '58分钟', '', 4),

((SELECT id FROM course WHERE title='大模型微调实战'), '微调原理与数据准备', '40分钟', '', 1),
((SELECT id FROM course WHERE title='大模型微调实战'), 'LoRA 低秩适配', '55分钟', '', 2),
((SELECT id FROM course WHERE title='大模型微调实战'), 'QLoRA 显存优化', '50分钟', '', 3),
((SELECT id FROM course WHERE title='大模型微调实战'), '微调效果评估', '45分钟', '', 4),

((SELECT id FROM course WHERE title='AI 数据处理与标注'), '数据采集与清洗', '38分钟', '', 1),
((SELECT id FROM course WHERE title='AI 数据处理与标注'), '标注规范设计', '42分钟', '', 2),
((SELECT id FROM course WHERE title='AI 数据处理与标注'), '质量校验与迭代', '36分钟', '', 3),

((SELECT id FROM course WHERE title='计算机视觉入门'), 'PyTorch 基础', '34分钟', '', 1),
((SELECT id FROM course WHERE title='计算机视觉入门'), '图像分类实战', '48分钟', '', 2),
((SELECT id FROM course WHERE title='计算机视觉入门'), '目标检测 YOLO', '56分钟', '', 3),
((SELECT id FROM course WHERE title='计算机视觉入门'), '模型部署', '44分钟', '', 4),

((SELECT id FROM course WHERE title='AI 产品设计思维'), 'AI 产品需求洞察', '32分钟', '', 1),
((SELECT id FROM course WHERE title='AI 产品设计思维'), '场景与价值设计', '40分钟', '', 2),
((SELECT id FROM course WHERE title='AI 产品设计思维'), '从 MVP 到增长', '46分钟', '', 3);
