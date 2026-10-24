import request from '../api.js'

/** 全校档案列表 */
export function getArchiveList(params) {
  return request.get('/admin/archive', { params })
}

/** 档案详情（只读） */
export function getArchiveDetail(id) {
  return request.get(`/work/archive/${id}`)
}

/** 导出归档清单 */
export async function exportArchive(params) {
  const blob = await request.get('/admin/archive/export', {
    responseType: 'blob',
    params,
    timeout: 60000,
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '教学档案归档清单.xlsx'
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

/** 待中间审核列表 */
export function getMiddlePending(params) {
  return request.get('/admin/middle-review/pending', { params })
}

/** 执行中间审核 */
export function middleReview(id, data) {
  return request.post(`/admin/middle-review/${id}`, data)
}
