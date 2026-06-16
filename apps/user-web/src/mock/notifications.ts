// Mock 通知数据
import type { Notification } from '@/types/notification'

export const mockNotifications: Notification[] = [
  {
    id: 'n001',
    type: 'milestone',
    title: '里程碑审核通过',
    content: '恭喜！您的里程碑「需求分析与技术方案」已审核通过，下一阶段任务已解锁。',
    read: false,
    link: '/tasks/execution/o001',
    createdAt: '2024-06-15 14:30',
  },
  {
    id: 'n002',
    type: 'task_review',
    title: '任务审核通知',
    content: '您申请参与的「市场调研报告生成」任务已发布方确认，请尽快开始执行。',
    read: false,
    link: '/tasks/execution/o002',
    createdAt: '2024-06-14 10:15',
  },
  {
    id: 'n003',
    type: 'income',
    title: '收益到账通知',
    content: '您的收益 ¥1,500.00 已到账，来源于「AI 客服 Agent 配置与优化」任务第一期结算。',
    read: true,
    link: '/mine/wallet',
    createdAt: '2024-06-12 09:00',
  },
  {
    id: 'n004',
    type: 'system',
    title: '等级提升恭喜',
    content: '恭喜！您的等级已从 LV4 提升到 LV5，解锁新技能标签和更高价值任务接单资格。',
    read: true,
    link: '/mine/profile',
    createdAt: '2024-06-10 08:00',
  },
  {
    id: 'n005',
    type: 'group',
    title: '新帖子通知',
    content: '您加入的小组「AI 前沿技术」有新帖子发布：张明分享了《LLM Agent 架构设计与实践》',
    read: false,
    link: '/discover/group/g001/post/p001',
    createdAt: '2024-06-10 14:35',
  },
  {
    id: 'n006',
    type: 'course',
    title: '作业批改完成',
    content: '您的作业「并发爬虫实战」已被批改，得分 92 分，批改意见：代码结构清晰，并发控制得当',
    read: true,
    link: '/grow/assignment/a002/result',
    createdAt: '2024-06-15 20:45',
  },
]