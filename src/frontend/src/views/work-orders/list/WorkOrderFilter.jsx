import { CRow, CCol, CFormSelect, CButton } from '@coreui/react'

export default function WorkOrderFilter() {
  return (
    <CRow style={{ marginBottom: 16 }}>
      <CCol md={3}>
        <CFormSelect>
          <option>Status</option>
          <option>Open</option>
          <option>In Progress</option>
          <option>Completed</option>
        </CFormSelect>
      </CCol>
      <CCol md={3}>
        <CButton color="primary">Apply</CButton>
      </CCol>
    </CRow>
  )
}
