import { Routes, Route, Navigate } from "react-router-dom";
import { UserList, UserDetail, TeamList, TeamForm } from "./screens";

export default function HR() {
  return (
    <Routes>
      <Route index element={<Navigate to="users" replace />} />

      {/* USERS */}
      <Route path="users" element={<UserList />} />
      <Route path="users/:id" element={<UserDetail />} />

      {/* TEAMS */}
      <Route path="teams" element={<TeamList />} />
      <Route path="teams/new" element={<TeamForm />} />
    </Routes>
  );
}
