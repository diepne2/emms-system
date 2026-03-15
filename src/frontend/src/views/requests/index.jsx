import { Routes, Route, Navigate } from "react-router-dom"

import RequestList from "./RequestList"
import RequestForm from "./RequestForm"
import RequestDetail from "./RequestDetail"

export default function Requests() {

  return (
    <Routes>

      {/* default */}
      <Route index element={<Navigate to="list" replace />} />

      {/* list */}
      <Route path="list" element={<RequestList />} />

      {/* create request */}
      <Route path="create" element={<RequestForm />} />

      {/* request detail */}
      <Route path=":id" element={<RequestDetail />} />

      {/* fallback */}
      <Route path="*" element={<Navigate to="list" replace />} />

    </Routes>
  )
}