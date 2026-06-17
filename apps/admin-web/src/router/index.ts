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
      // 系统管理 - 用户管理 / 认证审核（专用视图）
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
      // 其余业务菜单 -> 通用占位页 PlaceholderView（按 title 区分）
      ...menuItems
        .filter(
          (item) =>
            item.path !== '/dashboard' &&
            item.path !== '/system/user' &&
            item.path !== '/system/verification'
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
