import { Routes, Route, Navigate } from "react-router-dom";
import {
  PartList,
  PartForm,
  PartDetail,
} from "./screens";

export default function Warehouse() {
  return (
    <Routes>
      <Route index element={<Navigate to="parts" replace />} />

      {/* Parts */}
      <Route path="parts" element={<PartList />} />
      <Route path="parts/new" element={<PartForm />} />
      <Route path="parts/:id" element={<PartDetail />} />

      <Route path="*" element={<Navigate to="parts" replace />} />
    </Routes>
  );
}
