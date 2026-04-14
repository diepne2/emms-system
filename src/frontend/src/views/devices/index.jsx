import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import DevicesPage from './Devices'

function DeviceGroupsPage() {
  return (
    <div className="p-3">
      <div className="card border-0 shadow-sm">
        <div className="card-body">
          <h4 className="mb-2">Nhóm thiết bị</h4>
          <p className="text-medium-emphasis mb-0">Chức năng đang cập nhật.</p>
        </div>
      </div>
    </div>
  )
}

function DeviceDocumentsPage() {
  return (
    <div className="p-3">
      <DevicesPage defaultOpenDocuments={true} />
    </div>
  )
}

export default function DevicesModule() {
  return (
    <Routes>
      <Route index element={<Navigate to="/devices/list" replace />} />
      <Route path="list" element={<DevicesPage />} />
      <Route path="group" element={<DeviceGroupsPage />} />
      <Route path="documents" element={<DeviceDocumentsPage />} />
      <Route path="*" element={<Navigate to="/devices/list" replace />} />
    </Routes>
  )
}