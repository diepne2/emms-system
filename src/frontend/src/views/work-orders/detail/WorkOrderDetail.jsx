import React, { useState } from 'react'
import {
  CCard,
  CCardBody,
  CBadge,
  CNav,
  CNavItem,
  CNavLink,
  CButton,
  CFormSelect
} from '@coreui/react'

export default function WorkOrderDetail() {
  const [tab, setTab] = useState('info')

  return (
    <div style={styles.page}>
      {/* HEADER */}
      <div style={styles.header}>
        <div>
          <h5>WO-001 – Fix air compressor</h5>
          <CBadge color="info">Open</CBadge>
        </div>
        <CButton color="primary">Edit</CButton>
      </div>

      {/* TABS */}
      <CNav variant="tabs">
        {['info', 'asset', 'assign', 'activity'].map((t) => (
          <CNavItem key={t}>
            <CNavLink
              active={tab === t}
              onClick={() => setTab(t)}
            >
              {t.toUpperCase()}
            </CNavLink>
          </CNavItem>
        ))}
      </CNav>

      <CCard style={styles.card}>
        <CCardBody>
          {tab === 'info' && (
            <>
              <p><b>Description:</b> Air pressure unstable</p>
              <p><b>Priority:</b> High</p>
            </>
          )}

          {tab === 'asset' && (
            <p><b>Asset:</b> Compressor A1 – Factory A</p>
          )}

          {tab === 'assign' && (
            <>
              <p>Assign Technician</p>
              <CFormSelect>
                <option>Nguyễn Văn A</option>
                <option>Trần Thị B</option>
              </CFormSelect>
            </>
          )}

          {tab === 'activity' && (
            <>
              <p>🕒 20/01 – Created</p>
              <p>🕒 21/01 – Assigned technician</p>
            </>
          )}
        </CCardBody>
      </CCard>
    </div>
  )
}

const styles = {
  page: { padding: 24 },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    marginBottom: 16
  },
  card: {
    marginTop: 12,
    borderRadius: 10,
    boxShadow: '0 6px 20px rgba(0,0,0,.05)'
  }
}
