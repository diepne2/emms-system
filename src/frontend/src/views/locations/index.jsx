import { Routes, Route, Navigate } from 'react-router-dom'
import LocationList from './LocationList'
import LocationForm from './LocationForm'
import LocationDetail from './LocationDetail'
import LocationMap from './LocationMap' 

export default function Locations() {
  return (
    <Routes>
      <Route index element={<Navigate to="list" replace />} />

      <Route path="list" element={<LocationList />} />
      <Route path="map" element={<LocationMap />} />
      <Route path="new" element={<LocationForm />} />
      <Route path=":id" element={<LocationDetail />} />

      <Route path="*" element={<Navigate to="list" replace />} />
    </Routes>
  )
}
