import React from 'react'
import { useLocation } from 'react-router-dom'
import { CBreadcrumb, CBreadcrumbItem } from '@coreui/react'

const PAGE_NAME_MAP = {
  '/dashboard': 'Dashboard',
  '/dashboard1': 'Bảng điều khiển bảo trì',

  '/assets/list': 'Danh sách thiết bị',
  '/assets/downtimes': 'Nhật ký dừng máy',
  '/assets': 'Thiết bị',

  '/preventive-maintenance': 'Bảo trì định kỳ',
  '/preventive-maintenance/work-orders/my': 'Work Orders của tôi',

  '/meter': 'Meter',
  '/checklist': 'Checklist',
  '/request': 'Yêu cầu sửa chữa',
  '/requests': 'Yêu cầu sửa chữa',
  '/work-orders': 'Work Orders',
  '/inventory': 'Kho vật tư',
  '/location': 'Vị trí',
  '/hr': 'Nhân sự',
  '/analytics': 'Phân tích & Báo cáo',
  '/profile': 'Thông tin người dùng',
}

const formatFallbackName = (pathname) => {
  const lastSegment = pathname.split('/').filter(Boolean).pop()

  if (!lastSegment) return null

  return lastSegment
    .replace(/-/g, ' ')
    .replace(/\b\w/g, (char) => char.toUpperCase())
}

const AppBreadcrumb = () => {
  const { pathname } = useLocation()

  const pageName = PAGE_NAME_MAP[pathname] || formatFallbackName(pathname)

  return (
    <CBreadcrumb className="emms-breadcrumb">
      <CBreadcrumbItem href="#/dashboard">Trang chủ</CBreadcrumbItem>

      {pathname !== '/dashboard' && pageName && (
        <CBreadcrumbItem active>{pageName}</CBreadcrumbItem>
      )}
    </CBreadcrumb>
  )
}

export default React.memo(AppBreadcrumb)