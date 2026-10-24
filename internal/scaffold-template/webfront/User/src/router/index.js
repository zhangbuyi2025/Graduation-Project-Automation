import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/submit',
    children: [
      {
        path: 'submit',
        name: 'Submit',
        component: () => import('@/views/teacher/Submit.vue'),
        meta: { title: '提交教学附件', roles: ['teacher', 'dean_chair', 'dean'] },
      },
      {
        path: 'review-status',
        name: 'ReviewStatus',
        component: () => import('@/views/teacher/ReviewStatus.vue'),
        meta: { title: '查看审核情况', roles: ['teacher', 'dean_chair', 'dean'] },
      },
      {
        path: 'first-review',
        name: 'FirstReview',
        component: () => import('@/views/reviewer/FirstReview.vue'),
        meta: { title: '初审审核', roles: ['dean_chair'] },
      },
      {
        path: 'final-review',
        name: 'FinalReview',
        component: () => import('@/views/reviewer/FinalReview.vue'),
        meta: { title: '终审审核', roles: ['dean'] },
      },
      {
        path: 'review-logs',
        name: 'ReviewLogs',
        component: () => import('@/views/shared/ReviewLogs.vue'),
        meta: { title: '审批记录', roles: ['dean_chair', 'dean'] },
      },
      {
        path: 'archive',
        name: 'Archive',
        component: () =>import('@/views/shared/Archive.vue'),
        meta: { title: '归档管理', roles: ['dean_chair', 'dean'] },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/submit',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 登录 & 角色守卫
router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.path === '/login') {
    return true
  }

  if (!userStore.isLoggedIn) {
    return { path: '/login' }
  }

  const roles = to.meta?.roles
  if (roles && !roles.includes(userStore.role)) {
    // 角色不匹配时回到其默认页
    if (userStore.role === 'teacher') return { path: '/submit' }
    if (userStore.role === 'dean_chair') return { path: '/first-review' }
    if (userStore.role === 'dean') return { path: '/final-review' }
    return { path: '/login' }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta?.title ?? '教学档案数字化系统'
})

export default router
