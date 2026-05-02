import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import WorkOrdersDashboard from './WorkOrdersDashboard'
import WorkOrdersPage from './WorkOrders'
import WorkOrderHistories from "./WorkOrderHistories";

export default function WorkOrdersModule() {
  return (
    <Routes>
      <Route index element={<Navigate to="/work-orders/dashboard" replace />} />
      <Route path="dashboard" element={<WorkOrdersDashboard />} />
      <Route path="list" element={<WorkOrdersPage />} />
      <Route path="*" element={<Navigate to="/work-orders/dashboard" replace />} />
      <Route path="work-order-histories" element={<WorkOrderHistories />} />
    </Routes>
  )
}