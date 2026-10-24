import request from '../api.js'

/** 教师账号列表 */
export function getTeachers(params) {
  return request.get('/admin/teacher', { params })
}

/** 新增教师 */
export function createTeacher(data) {
  return request.post('/admin/teacher', data)
}

/** 编辑教师 */
export function updateTeacher(id, data) {
  return request.put(`/admin/teacher/${id}`, data)
}

/** 删除（停用） */
export function deleteTeacher(id) {
  return request.delete(`/admin/teacher/${id}`)
}

/** 启用 / 停用 */
export function updateTeacherStatus(id, status) {
  return request.put(`/admin/teacher/${id}/status`, null, { params: { status } })
}

/** 重置密码 */
export function resetPassword(id, newPassword) {
  return request.post(`/admin/teacher/${id}/reset-password`, { newPassword })
}
