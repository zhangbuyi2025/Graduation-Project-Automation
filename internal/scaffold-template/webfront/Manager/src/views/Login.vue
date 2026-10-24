<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const errorMsg = ref('')
const loading = ref(false)

async function handleLogin() {
  errorMsg.value = ''
  if (!form.value.username.trim() || !form.value.password) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const res = await login({
      username: form.value.username.trim(),
      password: form.value.password,
    })
    const data = res.data
    userStore.setLogin(data.token, data.userInfo)

    if (data.userInfo.role !== 'admin') {
      errorMsg.value = '教师 / 主任 / 院长账号请在用户端（User，端口 5174）登录'
      userStore.logout()
      return
    }
    router.push('/overview')
  } catch (e) {
    errorMsg.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <div class="login-stage">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="hero-logo">
          <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            <path d="M9 7h7M9 11h5"></path>
          </svg>
        </div>
        <h1 class="hero-title">教学档案数字化系统</h1>
        <div class="hero-slogan">校内教务处 · 管理端</div>
        <ul class="hero-points">
          <li>
            <span class="hp-ic">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
              </svg>
            </span>
            全校档案 · 集中归档管理
          </li>
          <li>
            <span class="hp-ic">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 11l3 3L22 4"></path>
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>
              </svg>
            </span>
            多级审核 · 流程全程留痕
          </li>
          <li>
            <span class="hp-ic">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </span>
            安全可靠 · 权限精细管控
          </li>
        </ul>
        <div class="hero-foot">教学档案数字化软件实训项目 · 第 2 组</div>
      </div>

      <!-- 右侧玻璃登录卡 -->
      <div class="login-panel">
        <div class="panel-head">
          <h2>管理员登录</h2>
          <div class="sub">欢迎回来，请登录管理控制台</div>
        </div>

        <div v-if="errorMsg" class="error-tip">{{ errorMsg }}</div>

        <div class="field">
          <span class="f-ic">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </span>
          <input
            v-model="form.username"
            type="text"
            placeholder="管理员用户名"
            @keyup.enter="handleLogin"
          >
        </div>
        <div class="field">
          <span class="f-ic">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
          </span>
          <input
            v-model="form.password"
            type="password"
            placeholder="密码"
            @keyup.enter="handleLogin"
          >
        </div>
        <button class="btn btn-primary" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中…' : '登 录' }}
        </button>

        <div class="login-tip">教师 / 主任 / 院长请前往用户端登录</div>
      </div>
    </div>
  </div>
</template>