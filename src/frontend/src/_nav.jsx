import React from 'react'
import { CNavGroup, CNavItem, CNavTitle } from '@coreui/react'
import CIcon from '@coreui/icons-react'
import {
  cilSpeedometer,
  cilDevices,
  cilCalendar,
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
    to: '/dashboard1',
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
  },

  { component: CNavTitle, name: 'QUẢN LÝ' },

  {
    component: CNavGroup,
    name: 'Thiết bị',
    icon: <CIcon icon={cilDevices} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách thiết bị', to: '/assets/list' },
      { component: CNavItem, name: 'Nhật ký dừng máy', to: '/assets/downtimes' },
    ],
  },

  {
    component: CNavGroup,
    name: 'Checklist',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Tra cứu / quản lý checklist', to: '/checklists/view' },
    ],
  },
  {
    component: CNavGroup,
    name: 'Bảo trì định kỳ',
    icon: <CIcon icon={cilCalendar} customClassName="nav-icon" />,
    items: [
      {
        component: CNavItem,
        name: 'Danh sách PM',
        to: '/preventive-maintenance',
      },
      {
        component: CNavItem,
        name: 'Work Orders của tôi',
        to: '/preventive-maintenance/work-orders/my',
      },
    ],
  },

  {
    component: CNavGroup,
    name: 'Work Orders',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Dashboard', to: '/work-orders/dashboard' },
      { component: CNavItem, name: 'Danh sách', to: '/work-orders/list' },
      { component: CNavItem, name: 'Lịch sử Work Order', to: '/work-order-histories' },
      { component: CNavItem, name: 'Work Log', to: '/labors' },
    ],
  },
  {
  component: CNavGroup,
  to: '/meter',
  name: 'Meter',
  icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
},


  {
    component: CNavItem,
    name: 'Kho vật tư',
    to: '/part',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
  },

  {
    component: CNavGroup,
    name: 'Vị trí',
    icon: <CIcon icon={cilLocationPin} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách vị trí', to: '/location' },
      { component: CNavItem, name: 'Bản đồ', to: '/location/map' },
    ],
  },

  {
    component: CNavItem,
    name: 'Yêu cầu sửa chữa',
    to: '/request',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
  },

  {
    component: CNavItem,
    name: 'Nhân sự',
    to: '/hr',
    icon: <CIcon icon={cilUser} customClassName="nav-icon" />,
  },

  {
    component: CNavGroup,
    name: 'Phân tích & Báo cáo',
    icon: <CIcon icon={cilChartLine} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Tổng quan thiết bị', to: '/dashboard/asset' },
      { component: CNavItem, name: 'Yêu cầu sửa chữa', to: '/dashboard/request' },
      { component: CNavItem, name: 'Nhân sự kỹ thuật', to: '/dashboard/user' },
      { component: CNavItem, name: 'Work Orders', to: '/dashboard/wo' },
    ],
  },


  {
    component: CNavItem,
    name: 'Thông tin người dùng',
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