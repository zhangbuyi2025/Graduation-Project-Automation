export const STATUS_TAG = {
  NOT_SUBMITTED: 'tag-gray',
  PENDING: 'tag-amber',
  FIRST_REVIEW: 'tag-orange',
  MIDDLE_REVIEW: 'tag-blue',
  SECOND_REVIEW: 'tag-blue',
  ARCHIVED: 'tag-green',
  REJECTED: 'tag-red',
}

export const STATUS_LABEL = {
  NOT_SUBMITTED: '未提交',
  PENDING: '待初审',
  FIRST_REVIEW: '待中间审核',
  MIDDLE_REVIEW: '待终审',
  SECOND_REVIEW: '终审中',
  ARCHIVED: '已归档',
  REJECTED: '已驳回',
}

export const TYPE_LABEL = { PRE: '课前', POST: '课后' }

export const LEVEL_LABEL = { 1: '初审', 2: '中间审核', 3: '终审' }

export function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

export function formatDateTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').substring(0, 16)
}
