import React from 'react'

const Dashboard = React.lazy(() => import('./views/dashboard'))
const WorkOrders = React.lazy(() => import('./views/work-orders'))
const Devices = React.lazy(() => import('./views/devices'))
const MaintenancePlan = React.lazy(() => import('./views/maintenance-plan'))
const Warehouse = React.lazy(() => import('./views/warehouse'))
const Locations = React.lazy(() => import('./views/locations'))
const Requests = React.lazy(() => import('./views/requests'))
const HR = React.lazy(() => import('./views/hr'))
const Reports = React.lazy(() => import('./views/reports'))
const Profile = React.lazy(() => import('./views/profile'))
const Chat = React.lazy(() => import('./views/chat'))
const Logout = React.lazy(() => import('./views/logout/Logout'))

const routes = [
  { path: '/', exact: true, name: 'Home', element: Dashboard },
  { path: '/dashboard', name: 'Dashboard', element: Dashboard },

  { path: '/work-orders/*', name: 'Work Orders', element: WorkOrders },
  { path: '/devices/*', name: 'Devices', element: Devices },
  { path: '/maintenance-plan/*', name: 'Maintenance Plan', element: MaintenancePlan },

  { path: '/warehouse/*', name: 'Warehouse', element: Warehouse },
  { path: '/locations/*', name: 'Locations', element: Locations },
  { path: '/requests/*', name: 'Requests', element: Requests },

  { path: '/hr/*', name: 'HR', element: HR },
  { path: '/reports/*', name: 'Reports', element: Reports },

  { path: '/profile', name: 'Profile', element: Profile },
  { path: '/chat', name: 'Chat', element: Chat },
  { path: '/logout', name: 'Logout', element: Logout },
]

export default routes