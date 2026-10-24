import request from '../api.js'

/** 学院列表 */
export function getColleges() {
  return request.get('/work/college')
}

/** 教研室列表（可按学院过滤） */
export function getDepartments(collegeId) {
  return request.get('/work/department', { params: { collegeId } })
}

/** 学期列表 */
export function getSemesters() {
  return request.get('/work/semester')
}
