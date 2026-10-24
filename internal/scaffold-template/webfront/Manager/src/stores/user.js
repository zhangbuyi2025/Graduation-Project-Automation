import { defineStore } from 'pinia'

/**
 * 用户登录状态 store（管理端）
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('manager_token') || '',
    userInfo: JSON.parse(localStorage.getItem('manager_userInfo') || 'null'),
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.userInfo?.role || '',
    realName: (state) => state.userInfo?.realName || '',
  },
  actions: {
    setLogin(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      localStorage.setItem('manager_token', token)
      localStorage.setItem('manager_userInfo', JSON.stringify(userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('manager_token')
      localStorage.removeItem('manager_userInfo')
    },
  },
})
