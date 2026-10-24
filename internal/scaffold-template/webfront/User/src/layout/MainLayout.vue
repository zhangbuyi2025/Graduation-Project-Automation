<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const roleLabels = {
  teacher: '教师',
  dean_chair: '教研室主任',
  dean: '教学院长',
  auditor: '审计',
}

const ic = {
  submit:
    '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><path d="M7 8l5-5 5 5"></path><path d="M12 3v12"></path>',
  status:
    '<path d="M8 6h13"></path><path d="M8 12h13"></path><path d="M8 18h13"></path><path d="M3 6h.01"></path><path d="M3 12h.01"></path><path d="M3 18h.01"></path>',
  first:
    '<path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><path d="M17 11l2 2 4-4"></path>',
  final:
    '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path><path d="M9 12l2 2 4-4"></path>',
  logs:
    '<circle cx="12" cy="12" r="10"></circle><path d="M12 6v6l4 2"></path>',
  archive:
    '<rect x="2" y="3" width="20" height="5" rx="1"></rect><path d="M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8"></path><path d="M10 12h4"></path>',
}

const menuGroups = computed(() => {
  const groups = [
    {
      title: '我的工作台',
      items: [
        { path: '/submit', name: '提交教学附件', svg: ic.submit },
        { path: '/review-status', name: '查看审核情况', svg: ic.status },
      ],
    },
  ]

  if (userStore.role === 'dean_chair') {
    groups.push({
      title: '教研室管理',
      items: [
        { path: '/first-review', name: '初审审核', svg: ic.first },
        { path: '/review-logs', name: '审批记录', svg: ic.logs },
        { path: '/archive', name: '归档', svg: ic.archive },
      ],
    })
  } else if (userStore.role === 'dean') {
    groups.push({
      title: '学院管理',
      items: [
        { path: '/final-review', name: '终审审核', svg: ic.final },
        { path: '/review-logs', name: '审批记录', svg: ic.logs },
        { path: '/archive', name: '归档', svg: ic.archive },
      ],
    })
  }
  return groups
})

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
          <span>用户端 · 教学档案</span>
        </span>
      </div>
      <div class="user">
        <span class="avatar">{{ (userStore.realName || '师').charAt(0) }}</span>
        <span class="who">
          <b>{{ userStore.realName }}</b>
          <span>{{ roleLabels[userStore.role] || '用户' }}</span>
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