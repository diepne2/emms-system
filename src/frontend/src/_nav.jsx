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
    name: 'Tổng quan hệ thống',
    to: '/dashboard1',
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
  },

  {
    component: CNavGroup,
    name: 'Quản lý Thiết bị',
    icon: <CIcon icon={cilDevices} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách thiết bị', to: '/assets/list' },
      { component: CNavItem, name: 'Nhật ký dừng máy', to: '/assets/downtimes' },
    ],
  },
  {
    component: CNavItem,
    to: '/meter',
    name: 'Chỉ số thiết bị',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
  },

  {
    component: CNavItem,
    name: 'Danh sách kiểm tra',
    to: '/Checklist',
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
  },
  {
    component: CNavGroup,
    name: 'Quản lý bảo trì',
    icon: <CIcon icon={cilCalendar} customClassName="nav-icon" />,
    items: [
      {
        component: CNavItem,
        name: 'Kế hoạch bảo trì',
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
    component: CNavItem,
    name: 'Yêu cầu sửa chữa',
    to: '/request',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
  },

  {
    component: CNavGroup,
    name: 'Quản lý Work Orders',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách Work Orders', to: '/work-orders/list' },
      { component: CNavItem, name: 'Tổng quan Work Order', to: '/work-orders/dashboard' },
      { component: CNavItem, name: 'Lịch sử Work Order', to: '/work-order-histories' },
      { component: CNavItem, name: 'Nhật ký công việc', to: '/labors' },
    ],
  },


  {
    component: CNavItem,
    name: 'Quản lý kho vật tư',
    to: '/part',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
  },

  {
    component: CNavGroup,
    name: 'Quản lý vị trí',
    icon: <CIcon icon={cilLocationPin} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Danh sách vị trí', to: '/location' },
      { component: CNavItem, name: 'Bản đồ vị trí', to: '/location/map' },
    ],
  },



  {
    component: CNavItem,
    name: 'Quản lý nhân sự',
    to: '/hr',
    icon: <CIcon icon={cilUser} customClassName="nav-icon" />,
  },
    {
    component: CNavGroup,
    name: 'Phân tích & Báo cáo',
    icon: <CIcon icon={cilChartLine} customClassName="nav-icon" />,
    items: [
      { component: CNavItem, name: 'Thống kê thiết bị', to: '/dashboard/asset' },
      { component: CNavItem, name: 'Thống kê sửa chữa', to: '/dashboard/request' },
      { component: CNavItem, name: 'Hiệu suất nhân sự', to: '/dashboard/user' },
      { component: CNavItem, name: 'Phân tích Work Order', to: '/dashboard/wo' },
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