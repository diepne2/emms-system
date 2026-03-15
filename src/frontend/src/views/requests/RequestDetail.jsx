import React from "react"
import { useParams, useNavigate } from "react-router-dom"

import {
  CRow,
  CCol,
  CCard,
  CCardHeader,
  CCardBody,
  CBadge,
  CButton
} from "@coreui/react"

export default function RequestDetail() {

  const { id } = useParams()
  const navigate = useNavigate()

  const request = {
    id: id || "REQ-001",
    device: "Máy nén khí",
    priority: "High",
    status: "Pending",
    created: "2026-03-10",
    description: "Máy nén khí phát ra tiếng ồn lớn khi hoạt động."
  }

  const statusColor = (status) => {
    switch (status) {
      case "Pending":
        return "warning"
      case "Approved":
        return "success"
      case "Rejected":
        return "danger"
      default:
        return "secondary"
    }
  }

  const priorityColor = (priority) => {
    switch (priority) {
      case "High":
        return "danger"
      case "Medium":
        return "warning"
      case "Low":
        return "secondary"
      default:
        return "secondary"
    }
  }

  return (

    <CRow className="justify-content-center">

      <CCol md={8}>

        <CCard
          style={{
            borderRadius: "10px",
            border: "none",
            boxShadow: "0 6px 20px rgba(0,0,0,0.05)"
          }}
        >

          <CCardHeader
            style={{
              background: "#f8f9fa",
              fontWeight: "600",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center"
            }}
          >
            Request Detail

            <CButton
              size="sm"
              color="secondary"
              onClick={() => navigate("/requests/list")}
            >
              Back
            </CButton>

          </CCardHeader>

          <CCardBody>

            <div style={{ marginBottom: "15px" }}>
              <strong>ID:</strong> {request.id}
            </div>

            <div style={{ marginBottom: "15px" }}>
              <strong>Device:</strong> {request.device}
            </div>

            <div style={{ marginBottom: "15px" }}>
              <strong>Priority:</strong>{" "}
              <CBadge color={priorityColor(request.priority)}>
                {request.priority}
              </CBadge>
            </div>

            <div style={{ marginBottom: "15px" }}>
              <strong>Status:</strong>{" "}
              <CBadge color={statusColor(request.status)}>
                {request.status}
              </CBadge>
            </div>

            <div style={{ marginBottom: "15px" }}>
              <strong>Created:</strong> {request.created}
            </div>

            <div style={{ marginBottom: "25px" }}>
              <strong>Description:</strong>

              <p
                style={{
                  marginTop: "8px",
                  padding: "10px",
                  background: "#f8f9fa",
                  borderRadius: "6px",
                  color: "#6c757d"
                }}
              >
                {request.description}
              </p>

            </div>

            <CButton
              color="success"
              style={{ marginRight: "10px" }}
            >
              Approve
            </CButton>

            <CButton color="danger">
              Reject
            </CButton>

          </CCardBody>

        </CCard>

      </CCol>

    </CRow>

  )
}