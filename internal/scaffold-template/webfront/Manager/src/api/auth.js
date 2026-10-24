import request from '../api.js'

export function login(data) {
  return request.post('/public/login', data)
}
