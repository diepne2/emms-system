import React from 'react'

const Dashboard1 = React.lazy(() => import('./app/dashboard1'))

const Dashboard = React.lazy(() => import('./app/dashboard'))
const WorkOrders = React.lazy(() => import('./app/work-orders'))
const WorkOrderHistories = React.lazy(() =>
  import('./app/work-orders/WorkOrderHistories')
)
const Assets = React.lazy(() => import('./app/asset'))
const Checklist = React.lazy(() => import('./app/Checklist'))
const Meter = React.lazy(() => import('./app/meter'))
const PreventiveMaintenance = React.lazy(() =>
  import('./app/preventive-maintenance')
)
const Part = React.lazy(() => import("./app/part"));
const Location = React.lazy(() => import('./app/location'))
const LocationMap = React.lazy(() => import('./app/location/LocationMap'))
const Request = React.lazy(() => import('./app/request'))
const HR = React.lazy(() => import('./app/hr'))
const Profile = React.lazy(() => import('./app/profile'))
const Chat = React.lazy(() => import('./app/chat'))
const Logout = React.lazy(() => import('./app/pages/logout/Logout'))
const Labor = React.lazy(() => import('./app/labor/Labor'))

const routes = [
  { path: '/dashboard1', name: 'Trang chủ', element: Dashboard1 },

  { path: '/work-orders/*', name: 'Lệnh công việc', element: WorkOrders },
  { path: '/work-order-histories', name: 'History WO', element: WorkOrderHistories },
  { path: '/assets/*', name: 'Thiết bị', element: Assets },
  { path: '/Checklist/*', name: 'Checklist', element: Checklist },

  { path: '/request', name: 'Yêu cầu sửa chữa', element: Request },
  { path: '/meter', name: 'Meters', element: Meter },


  { path: '/labors', name: 'Work Log', element: Labor },
  { path: '/work-orders/:workOrderId/labors', name: 'Work Log theo WO', element: Labor },

  { path: '/preventive-maintenance/*', name: 'Kế hoạch bảo trì', element: PreventiveMaintenance },
  

  { path: '/part', name: 'Kho vật tư', element: Part },
  { path: '/location', name: 'Vị trí', element: Location },
  { path: '/location/map', name: 'Bản đồ vị trí', element: LocationMap },

  

  { path: '/hr', name: 'Nhân sự', element: HR },
  { path: '/dashboard/*', name: 'Phân tích & Báo cáo', element: Dashboard },

  { path: '/profile', name: 'Thông tin người dùng', element: Profile },
  { path: '/chat', name: 'Chat', element: Chat },
  { path: '/logout', name: 'Đăng xuất', element: Logout },
]

export default routes