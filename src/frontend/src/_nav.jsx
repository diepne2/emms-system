import React from 'react'
import { CNavGroup, CNavItem, CNavTitle } from '@coreui/react'
import CIcon from '@coreui/icons-react'
import {
  cilSpeedometer,
  cilDevices,
  cilCalendar,
  cilList,
  cilStorage,
  cilChartLine,
  cilUser,
  cilChatBubble,
  cilAccountLogout,
  cilLocationPin,
  cilClipboard,
} from '@coreui/icons'

const _nav = [
  {
    component: CNavItem,
    name: 'Dashboard',
    to: '/dashboard',
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
  },

  { component: CNavTitle, name: 'QUẢN LÝ' },

  {
    component: CNavGroup,
    name: 'Thiết bị',
    icon: <CIcon icon={cilDevices} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách thiết bị', to: '/devices/list' },
      { component: CNavItem, name: 'Nhóm thiết bị', to: '/devices/group' },
      { component: CNavItem, name: 'Tài liệu thiết bị', to: '/devices/documents' },
    ],
  },

  {
    component: CNavGroup,
    name: 'Work Orders',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách', to: '/work-orders/list' },
      { component: CNavItem, name: 'Kanban', to: '/work-orders/kanban' },
      { component: CNavItem, name: 'Lịch', to: '/work-orders/calendar' },
      { component: CNavItem, name: 'Tạo mới', to: '/work-orders/new' },
    ],
  },

  {
    component: CNavGroup,
    name: 'Bảo trì định kỳ',
    icon: <CIcon icon={cilCalendar} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách kế hoạch', to: '/maintenance-plan/list' },
      { component: CNavItem, name: 'Lịch bảo trì', to: '/maintenance-plan/calendar' },
      { component: CNavItem, name: 'Lịch sử sửa chữa', to: '/maintenance-plan/history' },
    ],
  },

  {
    component: CNavGroup,
    name: 'Kho vật tư',
    icon: <CIcon icon={cilStorage} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh mục vật tư', to: '/warehouse/parts' },
    ],
  },

  {
    component: CNavGroup,
    name: 'Vị trí',
    icon: <CIcon icon={cilLocationPin} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách vị trí', to: '/locations' },
      { component: CNavItem, name: 'Bản đồ', to: '/locations/map' },
    ],
  },

  {
    component: CNavItem,
    name: 'Yêu cầu sửa chữa',
    to: '/requests',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
  },

  {
    component: CNavGroup,
    name: 'Nhân sự',
    icon: <CIcon icon={cilUser} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Nhân viên', to: '/hr/users' },
      { component: CNavItem, name: 'Tổ / đội', to: '/hr/teams' },
    ],
  },

  {
    component: CNavGroup,
    name: 'Phân tích & Báo cáo',
    icon: <CIcon icon={cilChartLine} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'KPI Dashboard', to: '/reports/kpi' },
    ],
  },

  { component: CNavTitle, name: 'Cá nhân' },

  {
    component: CNavItem,
    name: 'Hồ sơ cá nhân',
    to: '/profile',
    icon: <CIcon icon={cilUser} customClassName="nav-icon" />,
  },
  {
    component: CNavItem,
    name: 'Chat',
    to: '/chat',
    icon: <CIcon icon={cilChatBubble} customClassName="nav-icon" />,
  },
  {
    component: CNavItem,
    name: 'Đăng xuất',
    to: '/logout',
    icon: <CIcon icon={cilAccountLogout} customClassName="nav-icon" />,
  },
]

export default _nav