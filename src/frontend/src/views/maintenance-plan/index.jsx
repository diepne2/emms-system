import { Routes, Route, Navigate } from 'react-router-dom'

import MaintenancePlanList from './list/MaintenancePlanList'
import MaintenanceCalendar from './calendar/MaintenanceCalendar'
import MaintenanceHistory from './history/MaintenanceHistory'

export default function MaintenancePlan() {
  return (
    <Routes>

      {/* default redirect */}
      <Route index element={<Navigate to="list" replace />} />

      {/* pages */}
      <Route path="list" element={<MaintenancePlanList />} />
      <Route path="calendar" element={<MaintenanceCalendar />} />
      <Route path="history" element={<MaintenanceHistory />} />

    </Routes>
  )
}