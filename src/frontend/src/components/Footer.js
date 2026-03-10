import React from 'react'
import { CFooter } from '@coreui/react'

const AppFooter = () => {
  return (
    <CFooter className="px-4">
      <div>
        <strong>EMMS</strong>
        <span className="ms-1">© 2026 – Hệ thống Quản lý Thiết bị &amp; Bảo trì</span>
      </div>
      <div className="ms-auto">
        <span className="me-1">Developed by</span>
        <span>EMMS Team</span>
      </div>
    </CFooter>
  )
}

export default React.memo(AppFooter)
