import request from '../api.js'

export function getColleges() {
  return request.get('/work/college')
}

export function getDepartments(collegeId) {
  return request.get('/work/department', { params: { collegeId } })
}

export function getSemesters() {
  return request.get('/work/semester')
}
