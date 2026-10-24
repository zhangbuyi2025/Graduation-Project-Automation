import request from '../api.js'

export async function previewAttachment(id) {
  const blob = await request.get(`/work/attachment/${id}/download`, {
    responseType: 'blob',
    params: { inline: true },
  })
  window.open(URL.createObjectURL(blob), '_blank')
}
