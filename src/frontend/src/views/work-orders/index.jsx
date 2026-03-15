import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'

import {
  WorkOrderList,
  WorkOrderDetail,
  WorkOrderKanban,
  WorkOrderCalendar,
  WorkOrderForm,
} from './screens'

export default function WorkOrders() {
  return (
    <Routes>

      {/* default route */}
      <Route index element={<Navigate to="list" replace />} />

      {/* List */}
      <Route path="list" element={<WorkOrderList />} />

      {/* Kanban */}
      <Route path="kanban" element={<WorkOrderKanban />} />

      {/* Calendar */}
      <Route path="calendar" element={<WorkOrderCalendar />} />

      {/* Create */}
      <Route path="new" element={<WorkOrderForm />} />

      {/* Detail */}
      <Route path=":id" element={<WorkOrderDetail />} />

    </Routes>
  )
}