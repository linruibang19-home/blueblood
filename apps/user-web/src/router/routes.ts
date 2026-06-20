import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/discover',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/LoginPage.vue'),
    meta: { tab: '' },
  },
  {
    path: '/enterprise-register',
    name: 'EnterpriseRegister',
    component: () => import('@/pages/auth/EnterpriseRegisterPage.vue'),
    meta: { tab: '' },
  },
  {
    path: '/discover',
    name: 'Discover',
    component: () => import('@/pages/discover/DiscoverPage.vue'),
    meta: { tab: 'discover' },
  },
  {
    path: '/groups/:id',
    name: 'GroupDetail',
    component: () => import('@/pages/discover/GroupDetail.vue'),
    meta: { tab: '' },
  },
  {
    path: '/posts/:id',
    name: 'PostDetail',
    component: () => import('@/pages/discover/PostDetail.vue'),
    meta: { tab: '' },
  },
  {
    path: '/profiles/:id',
    name: 'EliteProfile',
    component: () => import('@/pages/discover/EliteProfile.vue'),
    meta: { tab: '' },
  },
  {
    path: '/chats/:id',
    name: 'ChatDetail',
    component: () => import('@/pages/discover/ChatDetail.vue'),
    meta: { tab: '' },
  },
  {
    path: '/grow',
    name: 'Grow',
    component: () => import('@/pages/grow/GrowPage.vue'),
    meta: { tab: 'grow' },
  },
  {
    path: '/grow/course/:id',
    name: 'CourseDetail',
    component: () => import('@/pages/grow/CourseDetail.vue'),
    meta: { tab: '' },
  },
  {
    path: '/grow/video/:courseId/:chapterId',
    name: 'VideoLearn',
    component: () => import('@/pages/grow/VideoLearn.vue'),
    meta: { tab: '' },
  },
  {
    path: '/grow/assignment/:id/submit',
    name: 'AssignmentSubmit',
    component: () => import('@/pages/grow/AssignmentSubmit.vue'),
    meta: { tab: '' },
  },
  {
    path: '/grow/assignment/:id/result',
    name: 'AssignmentResult',
    component: () => import('@/pages/grow/AssignmentResult.vue'),
    meta: { tab: '' },
  },
  {
    path: '/grow/hackathon/:id',
    name: 'HackathonDetail',
    component: () => import('@/pages/grow/HackathonDetail.vue'),
    meta: { tab: '' },
  },
  {
    path: '/grow/job/:id',
    name: 'JobDetail',
    component: () => import('@/pages/grow/JobDetail.vue'),
    meta: { tab: '' },
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: () => import('@/pages/task/TaskPage.vue'),
    meta: { tab: 'tasks' },
  },
  {
    path: '/tasks/detail/:id',
    name: 'TaskDetail',
    component: () => import('@/pages/task/TaskDetail.vue'),
    meta: { tab: '' },
  },
  {
    path: '/tasks/execution/:id',
    name: 'TaskExecution',
    component: () => import('@/pages/task/TaskExecution.vue'),
    meta: { tab: '' },
  },
  {
    path: '/tasks/milestone/:orderId/:milestoneId',
    name: 'MilestoneSubmit',
    component: () => import('@/pages/task/MilestoneSubmit.vue'),
    meta: { tab: '' },
  },
  {
    path: '/mine',
    name: 'Mine',
    component: () => import('@/pages/mine/MinePage.vue'),
    meta: { tab: 'mine' },
  },
  {
    path: '/mine/wallet',
    name: 'WalletDetail',
    component: () => import('@/pages/mine/WalletDetail.vue'),
    meta: { tab: '' },
  },
  {
    path: '/mine/notifications',
    name: 'Notifications',
    component: () => import('@/pages/mine/Notifications.vue'),
    meta: { tab: '' },
  },
  {
    path: '/mine/settings',
    name: 'Settings',
    component: () => import('@/pages/mine/Settings.vue'),
    meta: { tab: '' },
  },
  {
    path: '/mine/profile/edit',
    name: 'EditProfile',
    component: () => import('@/pages/mine/EditProfile.vue'),
    meta: { tab: '' },
  },
  {
    path: '/mine/enterprise',
    name: 'EnterpriseConsole',
    component: () => import('@/pages/mine/EnterpriseConsole.vue'),
    meta: { tab: '' },
  },
]

export default routes