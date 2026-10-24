import request from '../api.js'

/**
 * 在线预览附件（带 JWT，通过 blob 打开）
 */
export async function previewAttachment(id) {
  const blob = await request.get(`/work/attachment/${id}/download`, {
    responseType: 'blob',
    params: { inline: true },
  })
  const url = URL.createObjectURL(blob)
  window.open(url, '_blank')
}

/**
 * 下载附件
 */
export async function downloadAttachment(id) {
  const blob = await request.get(`/work/attachment/${id}/download`, {
    responseType: 'blob',
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `attachment_${id}.pdf`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
