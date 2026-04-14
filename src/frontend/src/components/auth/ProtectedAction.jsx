import React from "react";
import { getCurrentUser, hasAnyRole } from "../../api/auth";

export default function ProtectedAction({ roles = [], children, fallback = null }) {
  const user = getCurrentUser();
  const allowed = hasAnyRole(user, roles);

  if (!allowed) return fallback;
  return children;
}