import React from "react"
import {
  CCard,
  CCardBody,
  CCardHeader,
  CRow,
  CCol,
  CBadge,
  CButton
} from "@coreui/react"

const DeviceDetail = () => {

  const headerStyle = {
    color: "#1b3c66",
    fontWeight: "600"
  }

  const labelStyle = {
    fontWeight: "600",
    color: "#495057"
  }

  const valueStyle = {
    color: "#212529"
  }

  const buttonStyle = {
    backgroundColor: "#1b3c66",
    borderColor: "#1b3c66",
    color: "white"
  }

  return (
    <div style={{ padding: "20px" }}>

      <CCard className="shadow-sm border-0">

        <CCardHeader className="d-flex justify-content-between align-items-center">

          <h5 style={headerStyle}>
            Chi tiết thiết bị
          </h5>

          <CButton style={buttonStyle}>
            Chỉnh sửa
          </CButton>

        </CCardHeader>

        <CCardBody>

          <CRow className="mb-3">

            <CCol md={6}>
              <p>
                <span style={labelStyle}>Mã thiết bị:</span>{" "}
                <span style={valueStyle}>DV-001</span>
              </p>
            </CCol>

            <CCol md={6}>
              <p>
                <span style={labelStyle}>Tên thiết bị:</span>{" "}
                <span style={valueStyle}>Máy phát điện Cummins 500kVA</span>
              </p>
            </CCol>

          </CRow>

          <CRow className="mb-3">

            <CCol md={6}>
              <p>
                <span style={labelStyle}>Phân loại:</span>{" "}
                <span style={valueStyle}>Điện</span>
              </p>
            </CCol>

            <CCol md={6}>
              <p>
                <span style={labelStyle}>Vị trí:</span>{" "}
                <span style={valueStyle}>Tòa nhà A</span>
              </p>
            </CCol>

          </CRow>

          <CRow className="mb-3">

            <CCol md={6}>
              <p>
                <span style={labelStyle}>Nhà sản xuất:</span>{" "}
                <span style={valueStyle}>Cummins</span>
              </p>
            </CCol>

            <CCol md={6}>
              <p>
                <span style={labelStyle}>Model:</span>{" "}
                <span style={valueStyle}>C500</span>
              </p>
            </CCol>

          </CRow>

          <CRow>

            <CCol md={6}>
              <p>
                <span style={labelStyle}>Trạng thái:</span>{" "}
                <CBadge color="success">
                  Active
                </CBadge>
              </p>
            </CCol>

          </CRow>

        </CCardBody>

      </CCard>

    </div>
  )
}

export default DeviceDetail