/**
 * 统一 Axios 实例
 * 开发环境走 Vite 代理 /api → http://localhost:8080
 * 生产环境 baseURL 由 VITE_API_BASE 环境变量注入
 *
 * 后端统一响应格式（common.Result）：
 *   { code: 200, message: "操作成功", data: {...}, timestamp: 1700000000 }
 */
import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? '/api',
  timeout: 15000,
})

// ============ 请求拦截器：自动带 JWT ============
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ============ 响应拦截器：解包 Result + 统一错误处理 ============
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 后端未按 Result 包装（如直接返回字符串），原样返回
    if (!res || typeof res.code === 'undefined') {
      return res
    }

    if (res.code === 200) {
      return res
    }

    // 401 → 清除 token、跳转登录
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
      return Promise.reject(new Error(res.message || '未授权'))
    }

    // 业务失败
    console.error('[API Error]', res.message, res)
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    console.error('[Axios Error]', error.message)
    return Promise.reject(error)
  },
)

export default request
