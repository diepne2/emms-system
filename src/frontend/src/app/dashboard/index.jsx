import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'

const AssetDashboard = React.lazy(() => import('./asset/Dashboard'))
const RequestDashboard = React.lazy(() => import('./request/Dashboard'))
const UserDashboard = React.lazy(() => import('./user/Dashboard'))
const WODashboard = React.lazy(() => import('./wo/Dashboard'))

const Dashboard = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="asset" replace />} />
      <Route path="asset" element={<AssetDashboard />} />
      <Route path="request" element={<RequestDashboard />} />
      <Route path="user" element={<UserDashboard />} />
      <Route path="wo" element={<WODashboard />} />
    </Routes>
  )
}

export default Dashboard