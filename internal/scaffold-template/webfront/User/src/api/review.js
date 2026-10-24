import request from '../api.js'

/** 待审核列表（按角色自动匹配） */
export function getPending(semesterId) {
  return request.get('/work/review/pending', { params: { semesterId } })
}

/** 教研室主任初审 */
export function firstReview(data) {
  return request.post('/work/review/first', data)
}

/** 教学院长终审 */
export function secondReview(data) {
  return request.post('/work/review/second', data)
}

/** 审批记录查询 */
export function getReviewLogs(params) {
  return request.get('/work/review/logs', { params })
}
