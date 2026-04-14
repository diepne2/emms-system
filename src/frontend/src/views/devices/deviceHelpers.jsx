export const DEVICE_STATUS_OPTIONS = [
  'NEW',
  'ACTIVE',
  'MAINTENANCE',
  'BROKEN',
  'RETIRED',
]

export const DEVICE_PRIORITY_OPTIONS = ['HIGH', 'MEDIUM', 'LOW']

export function getStatusLabel(status) {
  switch (status) {
    case 'NEW':
      return 'Mới'
    case 'ACTIVE':
      return 'Hoạt động'
    case 'MAINTENANCE':
      return 'Bảo trì'
    case 'BROKEN':
      return 'Hỏng'
    case 'RETIRED':
      return 'Ngừng dùng'
    default:
      return status || '-'
  }
}

export function getPriorityLabel(priority) {
  switch (priority) {
    case 'HIGH':
      return 'Cao'
    case 'MEDIUM':
      return 'Trung bình'
    case 'LOW':
      return 'Thấp'
    default:
      return priority || '-'
  }
}

export function getStatusClass(status) {
  switch (status) {
    case 'ACTIVE':
      return 'cmms-badge success'
    case 'MAINTENANCE':
      return 'cmms-badge warning'
    case 'BROKEN':
      return 'cmms-badge danger'
    case 'NEW':
      return 'cmms-badge info'
    case 'RETIRED':
      return 'cmms-badge neutral'
    default:
      return 'cmms-badge'
  }
}

export function getPriorityClass(priority) {
  switch (priority) {
    case 'HIGH':
      return 'cmms-badge danger'
    case 'MEDIUM':
      return 'cmms-badge warning'
    case 'LOW':
      return 'cmms-badge success'
    default:
      return 'cmms-badge'
  }
}

export function formatDate(value) {
  if (!value) return '-'
  try {
    return new Date(value).toLocaleDateString('vi-VN')
  } catch {
    return value
  }
}

export function formatDateTime(value) {
  if (!value) return '-'
  try {
    return new Date(value).toLocaleString('vi-VN')
  } catch {
    return value
  }
}

export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '-'
  return Number(value).toLocaleString('vi-VN') + ' đ'
}