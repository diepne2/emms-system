import React from 'react'
import { Routes, Route } from 'react-router-dom'

import DeviceList from './DeviceList'
import DeviceCreate from './DeviceCreate'
import DeviceDetail from './DeviceDetail'
import DeviceDocuments from "./DeviceDocuments"


const Devices = () => {
  return (
    <Routes>
      <Route path="list" element={<DeviceList />} />
      <Route path="create" element={<DeviceCreate />} />
      <Route path=":id" element={<DeviceDetail />} />
      <Route path="documents" element={<DeviceDocuments />} />
    </Routes>
  )
}

export default Devices