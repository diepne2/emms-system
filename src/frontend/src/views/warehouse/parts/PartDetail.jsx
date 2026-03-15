import React from "react"
import { useNavigate, useParams } from "react-router-dom"
import CIcon from "@coreui/icons-react"
import { cilArrowLeft, cilPencil } from "@coreui/icons"

import {
  CCard,
  CCardBody,
  CCardHeader,
  CRow,
  CCol,
  CButton,
  CBadge
} from "@coreui/react"

const PartDetail = () => {

  const navigate = useNavigate()
  const { id } = useParams()

  // demo data
  const part = {
    id,
    code: "VT-001",
    name: "Bạc đạn",
    unit: "Cái",
    quantity: 12,
    description: "Bạc đạn motor"
  }

  const getStockColor = (qty) => {
    if (qty === 0) return "danger"
    if (qty < 10) return "warning"
    return "success"
  }

  return (

    <CCard
      style={{
        borderRadius: "10px",
        boxShadow: "0 2px 10px rgba(0,0,0,0.05)"
      }}
    >

      {/* HEADER */}

      <CCardHeader
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          background: "#f8f9fa",
          borderBottom: "1px solid #eee"
        }}
      >

        <h4
          style={{
            fontWeight: "600",
            display: "flex",
            alignItems: "center",
            gap: "8px"
          }}
        >
          📦 Chi tiết vật tư
        </h4>

        <div style={{ display: "flex", gap: "10px" }}>

          <CButton
            color="secondary"
            onClick={() => navigate("/warehouse/parts")}
          >
            <CIcon icon={cilArrowLeft} style={{ marginRight: "6px" }} />
            Quay lại
          </CButton>

          <CButton color="primary">
            <CIcon icon={cilPencil} style={{ marginRight: "6px" }} />
            Chỉnh sửa
          </CButton>

        </div>

      </CCardHeader>

      {/* BODY */}

      <CCardBody style={{ padding: "30px" }}>

        <CRow style={{ marginBottom: "20px" }}>
          <CCol md={3} style={{ fontWeight: "600", color: "#6c757d" }}>
            Mã vật tư
          </CCol>

          <CCol md={9}>
            {part.code}
          </CCol>
        </CRow>

        <hr />

        <CRow style={{ marginBottom: "20px" }}>
          <CCol md={3} style={{ fontWeight: "600", color: "#6c757d" }}>
            Tên vật tư
          </CCol>

          <CCol md={9}>
            {part.name}
          </CCol>
        </CRow>

        <hr />

        <CRow style={{ marginBottom: "20px" }}>
          <CCol md={3} style={{ fontWeight: "600", color: "#6c757d" }}>
            Đơn vị tính
          </CCol>

          <CCol md={9}>
            {part.unit}
          </CCol>
        </CRow>

        <hr />

        <CRow style={{ marginBottom: "20px" }}>
          <CCol md={3} style={{ fontWeight: "600", color: "#6c757d" }}>
            Tồn kho
          </CCol>

          <CCol md={9}>

            <CBadge
              color={getStockColor(part.quantity)}
              style={{
                padding: "8px 12px",
                fontSize: "14px"
              }}
            >
              {part.quantity}
            </CBadge>

          </CCol>
        </CRow>

        <hr />

        <CRow>
          <CCol md={3} style={{ fontWeight: "600", color: "#6c757d" }}>
            Mô tả
          </CCol>

          <CCol md={9}>
            {part.description || "Không có mô tả"}
          </CCol>
        </CRow>

      </CCardBody>

    </CCard>

  )
}

export default PartDetail