import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import AssetsPage from './Assets'
import AssetDowntimes from './AssetDowntimes'

// Tạm thời dùng lại AssetsPage cho đến khi bạn có form riêng
const AssetCreatePage = AssetsPage
const AssetEditPage = AssetsPage

export default function AssetModule() {
  return (
    <Routes>
      <Route index element={<Navigate to="/assets/list" replace />} />

      {/* MAIN */}
      <Route path="list" element={<AssetsPage />} />
      <Route path="downtimes" element={<AssetDowntimes />} /> 

      {/* OTHER */}
      <Route path="group" element={<AssetsPage />} />
      <Route path="documents" element={<AssetsPage />} />

      {/* CRUD */}
      <Route path="new" element={<AssetCreatePage />} />
      <Route path="edit/:id" element={<AssetEditPage />} />
      <Route path=":id" element={<AssetsPage />} />

      <Route path="*" element={<Navigate to="/assets/list" replace />} />
    </Routes>
  )
}