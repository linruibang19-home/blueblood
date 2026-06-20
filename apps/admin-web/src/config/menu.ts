/**
 * 侧边栏菜单静态配置。
 * icon 使用 Element Plus 图标组件名（已在 main.ts 全局注册）。
 * group 用于分组展示（同一 group 的菜单聚合在同一分组下）。
 */
export interface MenuItem {
  /** 路由 path（相对 / 的绝对路径） */
  path: string
  /** 菜单标题 / 页签标题 */
  title: string
  /** Element Plus 图标组件名 */
  icon: string
  /** 分组标识（用于侧边栏分组展示） */
  group: string
}

export interface MenuGroup {
  title: string
  children: MenuItem[]
}

/** 扁平菜单（路由生成用） */
export const menuItems: MenuItem[] = [
  // 工作台
  { path: '/dashboard', title: '工作台', icon: 'Odometer', group: '工作台' },

  // 系统管理
  { path: '/system/user', title: '用户管理', icon: 'User', group: '系统管理' },
  { path: '/system/verification', title: '认证审核', icon: 'CircleCheck', group: '系统管理' },
  { path: '/system/notification', title: '通知管理', icon: 'Bell', group: '系统管理' },
  { path: '/system/config', title: '系统配置', icon: 'Setting', group: '系统管理' },
  { path: '/system/permission', title: '权限管理', icon: 'Lock', group: '系统管理' },
  { path: '/system/enterprise', title: '企业认证', icon: 'OfficeBuilding', group: '系统管理' },
  { path: '/system/log', title: '操作日志', icon: 'Document', group: '系统管理' },

  // 社区管理
  { path: '/community/group', title: '小组管理', icon: 'UserFilled', group: '社区管理' },
  { path: '/community/post', title: '帖子管理', icon: 'ChatDotRound', group: '社区管理' },

  // 教育管理
  { path: '/edu/course', title: '课程管理', icon: 'Reading', group: '教育管理' },
  { path: '/edu/assignment', title: '作业管理', icon: 'EditPen', group: '教育管理' },

  // 任务管理
  { path: '/task/task', title: '任务管理', icon: 'List', group: '任务管理' },
  { path: '/task/order', title: '接单管理', icon: 'Tickets', group: '任务管理' },
  { path: '/task/milestone', title: '里程碑审核', icon: 'Flag', group: '任务管理' },

  // 财务管理
  { path: '/finance/income', title: '收益结算', icon: 'Money', group: '财务管理' },
  { path: '/finance/withdraw', title: '提现管理', icon: 'Wallet', group: '财务管理' },

  // 活动管理
  { path: '/activity/hackathon', title: '黑客松管理', icon: 'Trophy', group: '活动管理' },
  { path: '/activity/job', title: '岗位管理', icon: 'Briefcase', group: '活动管理' },
]

/**
 * 按 group 聚合成分组结构（侧边栏渲染用）。
 * 保持 menuItems 中的首次出现顺序作为分组顺序。
 */
export const menuGroups: MenuGroup[] = (() => {
  const order: string[] = []
  const map = new Map<string, MenuItem[]>()
  for (const item of menuItems) {
    if (!map.has(item.group)) {
      map.set(item.group, [])
      order.push(item.group)
    }
    map.get(item.group)!.push(item)
  }
  return order.map((title) => ({ title, children: map.get(title)! }))
})()
