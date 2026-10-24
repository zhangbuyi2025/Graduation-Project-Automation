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
    redirect: '/overview',
    children: [
      {
        path: 'overview',
        name: 'ArchiveOverview',
        component: () => import('@/views/ArchiveOverview.vue'),
        meta: { title: '归档总览', roles: ['admin'] },
      },
      {
        path: 'middle-review',
        name: 'MiddleReview',
        component: () => import('@/views/MiddleReview.vue'),
        meta: { title: '中间审核', roles: ['admin'] },
      },
      {
        path: 'teacher',
        name: 'TeacherManage',
        component: () => import('@/views/TeacherManage.vue'),
        meta: { title: '教师管理', roles: ['admin'] },
      },
      {
        path: 'course',
        name: 'CourseManage',
        component: () => import('@/views/CourseManage.vue'),
        meta: { title: '课程管理', roles: ['admin'] },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/overview',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

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
    return { path: '/login' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta?.title ?? '教学档案数字化系统'
})

export default router
