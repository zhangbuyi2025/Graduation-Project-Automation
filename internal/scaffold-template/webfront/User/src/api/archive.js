import request from '../api.js'

/** 教学任务（可按学期） */
export function getTasks(semesterId) {
  return request.get('/work/task', { params: { semesterId } })
}

/** 我的档案列表 */
export function getMyArchives() {
  return request.get('/work/archive/mine')
}

/** 档案详情 */
export function getArchiveDetail(id) {
  return request.get(`/work/archive/${id}`)
}

/** 逻辑删除档案 */
export function deleteArchive(id) {
  return request.delete(`/work/archive/${id}`)
}

/** 归档/档案列表 */
export function getArchivedList(params) {
  return request.get('/work/archive/archived', { params })
}

/** 提交 / 重提档案（multipart） */
export function submitArchive(formData) {
  return request.post('/work/archive/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  })
}

/** 附件预览 / 下载地址（需带 token，用 blob 方式见 preview 工具） */
export function attachmentUrl(id, inline = true) {
  return `/api/work/attachment/${id}/download?inline=${inline}`
}

/** 导出归档清单 Excel（主任/院长，按角色收口） */
export async function exportArchived(params) {
  const blob = await request.get('/work/archive/export', {
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
