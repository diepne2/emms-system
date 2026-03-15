import React from 'react'
import {
  CCard,
  CCardBody,
  CCol,
  CRow,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CButton,
  CFormInput,
  CFormSelect,
  CBadge,
  CInputGroup,
  CInputGroupText,
} from '@coreui/react'

import CIcon from '@coreui/icons-react'
import { cilSearch, cilPlus, cilFilter, cilOptions } from '@coreui/icons'

const DeviceList = () => {

  const assets = [
    { id: 'DV-001', name: 'Máy phát điện Cummins 500kVA', category: 'Điện', location: 'Tòa nhà A', status: 'Active' },
    { id: 'DV-002', name: 'Hệ thống điều hòa trung tâm VRV', category: 'HVAC', location: 'Tòa nhà B', status: 'Inactive' },
    { id: 'DV-003', name: 'Thang máy Mitsubishi', category: 'Cơ khí', location: 'Tòa nhà C', status: 'Maintenance' },
    { id: 'DV-004', name: 'Bơm nước cứu hỏa Pentax', category: 'PCCC', location: 'Tầng hầm B1', status: 'Active' },
  ]

  const getBadgeColor = (status) => {
    switch (status) {
      case 'Active':
        return 'success'
      case 'Inactive':
        return 'secondary'
      case 'Maintenance':
        return 'warning'
      default:
        return 'primary'
    }
  }

  return (
    <div className="p-3">

      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h4 className="mb-0 fw-bold" style={{ color: '#1b3c66' }}>
            Danh sách thiết bị
          </h4>
          <span className="text-muted small">
            Quản lý và tra cứu thông tin tài sản
          </span>
        </div>

        <CButton style={{ backgroundColor: '#1b3c66', borderColor: '#1b3c66', color: 'white' }}>
          <CIcon icon={cilPlus} className="me-2" />
          Thêm thiết bị
        </CButton>
      </div>

      {/* FILTER */}
      <CCard className="mb-4 shadow-sm border-0">
        <CCardBody>
          <CRow className="g-3 align-items-end">

            <CCol md={4}>
              <label className="form-label small text-muted fw-bold">
                Từ khóa
              </label>

              <CInputGroup>
                <CInputGroupText className="bg-white">
                  <CIcon icon={cilSearch}/>
                </CInputGroupText>

                <CFormInput placeholder="Tên thiết bị, mã thiết bị..." />
              </CInputGroup>
            </CCol>

            <CCol md={3}>
              <label className="form-label small text-muted fw-bold">
                Loại thiết bị
              </label>

              <CFormSelect>
                <option>Tất cả các loại</option>
                <option>Hệ thống Điện</option>
                <option>HVAC</option>
                <option>Cơ khí</option>
              </CFormSelect>
            </CCol>

            <CCol md={3}>
              <label className="form-label small text-muted fw-bold">
                Trạng thái
              </label>

              <CFormSelect>
                <option>Tất cả trạng thái</option>
                <option>Active</option>
                <option>Inactive</option>
                <option>Maintenance</option>
              </CFormSelect>
            </CCol>

            <CCol md={2} className="d-flex gap-2">
              <CButton
                className="w-100"
                style={{ backgroundColor: '#1b3c66', borderColor: '#1b3c66',color: 'white' }}
              >
                <CIcon icon={cilFilter} className="me-1"/>
                Lọc
              </CButton>

              <CButton color="light" variant="outline">
                <CIcon icon={cilOptions}/>
              </CButton>
            </CCol>

          </CRow>
        </CCardBody>
      </CCard>

      {/* TABLE */}
      <CCard className="shadow-sm border-0">

        <CCardBody className="p-0">

          <CTable hover responsive align="middle" className="mb-0 border">

            <CTableHead color="light">
              <CTableRow>
                <CTableHeaderCell className="text-center">Mã TB</CTableHeaderCell>
                <CTableHeaderCell>Tên thiết bị</CTableHeaderCell>
                <CTableHeaderCell>Phân loại</CTableHeaderCell>
                <CTableHeaderCell>Vị trí</CTableHeaderCell>
                <CTableHeaderCell className="text-center">Trạng thái</CTableHeaderCell>
                <CTableHeaderCell className="text-center">Hành động</CTableHeaderCell>
              </CTableRow>
            </CTableHead>

            <CTableBody>

              {assets.map((item) => (
                <CTableRow key={item.id}>

                  <CTableDataCell className="text-center text-muted font-monospace">
                    {item.id}
                  </CTableDataCell>

                  <CTableDataCell>
                    <strong>{item.name}</strong>
                  </CTableDataCell>

                  <CTableDataCell>
                    {item.category}
                  </CTableDataCell>

                  <CTableDataCell>
                    {item.location}
                  </CTableDataCell>

                  <CTableDataCell className="text-center">

                    <CBadge
                      color={getBadgeColor(item.status)}
                      shape="rounded-pill"
                    >
                      {item.status}
                    </CBadge>

                  </CTableDataCell>

                  <CTableDataCell className="text-center">

                    <CButton
                      color="link"
                      size="sm"
                      className="text-decoration-none"
                    >
                      Chi tiết
                    </CButton>

                  </CTableDataCell>

                </CTableRow>
              ))}

            </CTableBody>

          </CTable>

        </CCardBody>

        <div className="card-footer bg-white border-top d-flex justify-content-between align-items-center py-3">
          <span className="small text-muted">
          </span>
        </div>

      </CCard>

    </div>
  )
}

export default DeviceList