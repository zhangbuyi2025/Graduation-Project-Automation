<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const ic = {
  overview:
    '<rect x="3" y="3" width="7.5" height="7.5" rx="1.5"></rect><rect x="13.5" y="3" width="7.5" height="7.5" rx="1.5"></rect><rect x="3" y="13.5" width="7.5" height="7.5" rx="1.5"></rect><rect x="13.5" y="13.5" width="7.5" height="7.5" rx="1.5"></rect>',
  review:
    '<path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"></path><rect x="9" y="3" width="6" height="4" rx="1"></rect><path d="M9 14l2 2 4-4"></path>',
  teacher:
    '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path>',
  course:
    '<path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>',
}

const menuGroups = [
  {
    title: '全校档案管理',
    items: [
      { path: '/overview', name: '归档总览', svg: ic.overview },
      { path: '/middle-review', name: '中间审核', svg: ic.review },
    ],
  },
  {
    title: '基础数据管理',
    items: [
      { path: '/teacher', name: '教师管理', svg: ic.teacher },
      { path: '/course', name: '课程管理', svg: ic.course },
    ],
  },
]

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app">
    <div class="topbar">
      <div class="brand">
        <span class="logo-mark">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            <path d="M9 7h7M9 11h5"></path>
          </svg>
        </span>
        <span class="sys-name">
          <b>教学档案数字化系统</b>
          <span>教务处 · 管理端</span>
        </span>
      </div>
      <div class="user">
        <span class="avatar">{{ (userStore.realName || '管').charAt(0) }}</span>
        <span class="who">
          <b>{{ userStore.realName }}</b>
          <span>教务处 · 管理员</span>
        </span>
        <a class="logout" @click="handleLogout">退出登录</a>
      </div>
    </div>

    <div class="body">
      <div class="side">
        <template v-for="(group, gi) in menuGroups" :key="gi">
          <div class="group">{{ group.title }}</div>
          <a
            v-for="item in group.items"
            :key="item.path"
            class="nav"
            :class="{ active: route.path === item.path }"
            @click="router.push(item.path)"
          >
            <span class="nav-ic">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" v-html="item.svg"></svg>
            </span>
            {{ item.name }}
          </a>
        </template>
        <div class="side-foot">教学档案数字化系统 · v1.0</div>
      </div>

      <div class="main">
        <router-view />
      </div>
    </div>
  </div>
</template>