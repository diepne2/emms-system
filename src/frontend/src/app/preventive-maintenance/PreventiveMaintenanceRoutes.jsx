import { Route, Routes } from 'react-router-dom'
import PreventiveMaintenanceList from './PreventiveMaintenanceList'
import PreventiveMaintenanceDetail from './PreventiveMaintenanceDetail'
import WorkOrderList from './WorkOrderList'
import './PreventiveMaintenance.css'

export default function PreventiveMaintenanceRoutes() {
  return (
    <Routes>
      <Route index element={<PreventiveMaintenanceList />} />
      <Route path="new" element={<PreventiveMaintenanceList autoOpenCreate />} />
      <Route path="work-orders/my" element={<WorkOrderList mode="my" />} />
      <Route path="work-orders" element={<WorkOrderList mode="all" />} />
      <Route path=":id" element={<PreventiveMaintenanceDetail />} />
    </Routes>
  )
}