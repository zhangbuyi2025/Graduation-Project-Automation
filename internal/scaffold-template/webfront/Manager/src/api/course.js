import request from '../api.js'

/** 课程列表 */
export function getCourses(params) {
  return request.get('/admin/course', { params })
}

/** 新增课程 */
export function createCourse(data) {
  return request.post('/admin/course', data)
}

/** 编辑课程 */
export function updateCourse(id, data) {
  return request.put(`/admin/course/${id}`, data)
}

/** 删除（停用） */
export function deleteCourse(id) {
  return request.delete(`/admin/course/${id}`)
}
