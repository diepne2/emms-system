import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import ChecklistPage from './Checklist'

export default function ChecklistModule() {
  return (
    <Routes>
      <Route index element={<Navigate to="/checklists/view" replace />} />
      <Route path="view" element={<ChecklistPage />} />
      <Route path=":id" element={<ChecklistPage />} />
      <Route path="*" element={<Navigate to="/checklists/view" replace />} />
    </Routes>
  )
}