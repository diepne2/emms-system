import { Routes, Route } from 'react-router-dom'
import Profile from './Profile'

export default function ProfileRoutes() {
  return (
    <Routes>
      <Route index element={<Profile />} />
    </Routes>
  )
}
