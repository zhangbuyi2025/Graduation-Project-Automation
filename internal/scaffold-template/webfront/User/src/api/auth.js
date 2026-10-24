import request from '../api.js'

/**
 * 登录
 */
export function login(data) {
  return request.post('/public/login', data)
}
