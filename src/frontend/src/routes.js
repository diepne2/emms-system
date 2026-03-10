import React from 'react'

const Dashboard = React.lazy(() => import('./views/dashboard'))


const routes = [
  { path: '/dashboard', name: 'Dashboard', element: Dashboard },
]

export default routes
