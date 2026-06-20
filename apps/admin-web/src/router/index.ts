import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { menuItems } from '@/config/menu'
import { TOKEN_KEY } from '@/api/request'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      // 工作台（专用视图）
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' },
      },
      // 系统管理 - 用户管理 / 认证审核 / 通知管理（专用视图）
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/UserManage.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'system/verification',
        name: 'SystemVerification',
        component: () => import('@/views/system/VerificationReview.vue'),
        meta: { title: '认证审核' },
      },
      {
        path: 'system/notification',
        name: 'SystemNotification',
        component: () => import('@/views/system/NotificationManage.vue'),
        meta: { title: '通知管理' },
      },
      // 系统管理 - 系统配置(配置/字典/任务分类/技能标签) / 操作日志（专用视图）
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/ConfigManage.vue'),
        meta: { title: '系统配置' },
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/LogManage.vue'),
        meta: { title: '操作日志' },
      },
      {
        path: 'system/permission',
        name: 'SystemPermission',
        component: () => import('@/views/system/PermissionManage.vue'),
        meta: { title: '权限管理' },
      },
      {
        path: 'system/enterprise',
        name: 'SystemEnterprise',
        component: () => import('@/views/system/EnterpriseReview.vue'),
        meta: { title: '企业认证' },
      },
      // 社区管理 - 小组管理 / 帖子管理（专用视图）
      {
        path: 'community/group',
        name: 'CommunityGroup',
        component: () => import('@/views/community/GroupManage.vue'),
        meta: { title: '小组管理' },
      },
      {
        path: 'community/post',
        name: 'CommunityPost',
        component: () => import('@/views/community/PostManage.vue'),
        meta: { title: '帖子管理' },
      },
      // 活动管理 - 黑客松管理 / 岗位管理（专用视图）
      {
        path: 'activity/hackathon',
        name: 'ActivityHackathon',
        component: () => import('@/views/activity/HackathonManage.vue'),
        meta: { title: '黑客松管理' },
      },
      {
        path: 'activity/job',
        name: 'ActivityJob',
        component: () => import('@/views/activity/JobManage.vue'),
        meta: { title: '岗位管理' },
      },
      // 教育管理 - 课程管理 / 作业管理（专用视图）
      {
        path: 'edu/course',
        name: 'EduCourse',
        component: () => import('@/views/edu/CourseManage.vue'),
        meta: { title: '课程管理' },
      },
      {
        path: 'edu/assignment',
        name: 'EduAssignment',
        component: () => import('@/views/edu/AssignmentManage.vue'),
        meta: { title: '作业管理' },
      },
      // 任务管理 - 任务管理 / 里程碑审核（专用视图）
      {
        path: 'task/task',
        name: 'TaskTask',
        component: () => import('@/views/task/TaskManage.vue'),
        meta: { title: '任务管理' },
      },
      {
        path: 'task/milestone',
        name: 'TaskMilestone',
        component: () => import('@/views/task/MilestoneReview.vue'),
        meta: { title: '里程碑审核' },
      },
      // 财务管理 - 收益结算 / 提现管理（专用视图）
      {
        path: 'finance/income',
        name: 'FinanceIncome',
        component: () => import('@/views/finance/IncomeManage.vue'),
        meta: { title: '收益结算' },
      },
      {
        path: 'finance/withdraw',
        name: 'FinanceWithdraw',
        component: () => import('@/views/finance/WithdrawManage.vue'),
        meta: { title: '提现管理' },
      },
      // 其余业务菜单 -> 通用占位页 PlaceholderView（按 title 区分）
      ...menuItems
        .filter(
          (item) =>
            item.path !== '/dashboard' &&
            item.path !== '/system/user' &&
            item.path !== '/system/verification' &&
            item.path !== '/system/notification' &&
            item.path !== '/system/config' &&
            item.path !== '/system/permission' &&
            item.path !== '/system/enterprise' &&
            item.path !== '/system/log' &&
            item.path !== '/community/group' &&
            item.path !== '/community/post' &&
            item.path !== '/activity/job' &&
            item.path !== '/activity/hackathon' &&
            item.path !== '/edu/course' &&
            item.path !== '/edu/assignment' &&
            item.path !== '/task/task' &&
            item.path !== '/task/milestone' &&
            item.path !== '/finance/income' &&
            item.path !== '/finance/withdraw'
        )
        .map((item) => {
          // 路由 name 用 path 生成唯一标识
          const name = item.path
            .split('/')
            .filter(Boolean)
            .map((s) => s.charAt(0).toUpperCase() + s.slice(1))
            .join('')
          return {
            path: item.path.slice(1), // 去掉前导 /，作为 / 的子路由
            name,
            component: () => import('@/components/PlaceholderView.vue'),
            props: { title: item.title },
            meta: { title: item.title },
          } as RouteRecordRaw
        }),
    ],
  },
  // 兜底：未匹配路由跳工作台
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局前置守卫：除 /login 外，无 token 跳 /login
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (!token && to.path !== '/login') {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  // 已登录访问 /login 则回工作台
  if (token && to.path === '/login') {
    next({ path: '/dashboard' })
    return
  }
  next()
})

export default router
