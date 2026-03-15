import React, { useState } from "react"
import { useNavigate } from "react-router-dom"

import {
  CRow,
  CCol,
  CCard,
  CCardHeader,
  CCardBody,
  CButton,
  CFormInput,
  CFormSelect,
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
  CBadge
} from "@coreui/react"

const INIT_DATA = [
  {
    id: "REQ-001",
    device: "Máy nén khí",
    priority: "High",
    status: "Pending",
    created: "2026-03-10"
  },
  {
    id: "REQ-002",
    device: "Bơm nước",
    priority: "Medium",
    status: "Approved",
    created: "2026-03-11"
  }
]

export default function RequestList() {

  const navigate = useNavigate()

  const [data] = useState(INIT_DATA)
  const [search, setSearch] = useState("")
  const [deviceFilter, setDeviceFilter] = useState("All")
  const [priorityFilter, setPriorityFilter] = useState("All")

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

  const priorityColor = (p) => {
    switch (p) {
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

  // FILTER DATA

  const filtered = data.filter((r) => {

    const matchSearch =
      r.id.toLowerCase().includes(search.toLowerCase()) ||
      r.device.toLowerCase().includes(search.toLowerCase())

    const matchDevice =
      deviceFilter === "All" || r.device === deviceFilter

    const matchPriority =
      priorityFilter === "All" || r.priority === priorityFilter

    return matchSearch && matchDevice && matchPriority
  })

  return (

    <CRow>

      <CCol xs={12}>

        {/* HEADER */}

        <div style={{ marginBottom: "20px" }}>

          <h4 style={{ fontWeight: "600", color: "#2f353a" }}>
            Maintenance Requests
          </h4>

          <p style={{ color: "#6c757d", fontSize: "14px" }}>
            Submit and manage equipment maintenance requests
          </p>

        </div>


        {/* FILTER */}

        <CCard
          style={{
            borderRadius: "10px",
            border: "none",
            boxShadow: "0 3px 10px rgba(0,0,0,0.05)",
            marginBottom: "20px"
          }}
        >

          <CCardBody>

            <CRow>

              <CCol md={3}>

                <CFormInput
                  placeholder="Search request..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  style={{ borderRadius: "6px" }}
                />

              </CCol>

              <CCol md={3}>

                <CFormSelect
                  value={deviceFilter}
                  onChange={(e) => setDeviceFilter(e.target.value)}
                  style={{ borderRadius: "6px" }}
                >
                  <option value="All">All Devices</option>
                  <option value="Máy nén khí">Máy nén khí</option>
                  <option value="Bơm nước">Bơm nước</option>
                </CFormSelect>

              </CCol>

              <CCol md={3}>

                <CFormSelect
                  value={priorityFilter}
                  onChange={(e) => setPriorityFilter(e.target.value)}
                  style={{ borderRadius: "6px" }}
                >
                  <option value="All">Priority</option>
                  <option value="Low">Low</option>
                  <option value="Medium">Medium</option>
                  <option value="High">High</option>
                </CFormSelect>

              </CCol>

              <CCol md={3} style={{ textAlign: "right" }}>

                <CButton
                  color="primary"
                  style={{
                    minWidth: "150px",
                    fontWeight: "500"
                  }}
                  onClick={() => navigate("/requests/create")}
                >
                  + Create Request
                </CButton>

              </CCol>

            </CRow>

          </CCardBody>

        </CCard>


        {/* TABLE */}

        <CCard
          style={{
            borderRadius: "10px",
            border: "none",
            boxShadow: "0 6px 20px rgba(0,0,0,0.06)"
          }}
        >

          <CCardHeader
            style={{
              background: "#f8f9fa",
              fontWeight: "600",
              fontSize: "15px"
            }}
          >
            Request List
          </CCardHeader>

          <CCardBody>

            <CTable hover responsive>

              <CTableHead style={{ background: "#f5f7fa" }}>

                <CTableRow>

                  <CTableHeaderCell>ID</CTableHeaderCell>
                  <CTableHeaderCell>Device</CTableHeaderCell>
                  <CTableHeaderCell>Priority</CTableHeaderCell>
                  <CTableHeaderCell>Status</CTableHeaderCell>
                  <CTableHeaderCell>Created</CTableHeaderCell>
                  <CTableHeaderCell>Action</CTableHeaderCell>

                </CTableRow>

              </CTableHead>

              <CTableBody>

                {filtered.map((r) => (

                  <CTableRow key={r.id}>

                    <CTableDataCell>{r.id}</CTableDataCell>

                    <CTableDataCell>{r.device}</CTableDataCell>

                    <CTableDataCell>

                      <CBadge color={priorityColor(r.priority)}>
                        {r.priority}
                      </CBadge>

                    </CTableDataCell>

                    <CTableDataCell>

                      <CBadge color={statusColor(r.status)}>
                        {r.status}
                      </CBadge>

                    </CTableDataCell>

                    <CTableDataCell>{r.created}</CTableDataCell>

                    <CTableDataCell>

                      <CButton
                        size="sm"
                        color="info"
                        style={{ minWidth: "70px" }}
                        onClick={() => navigate(`/requests/${r.id}`)}
                      >
                        View
                      </CButton>

                    </CTableDataCell>

                  </CTableRow>

                ))}

              </CTableBody>

            </CTable>

          </CCardBody>

        </CCard>

      </CCol>

    </CRow>

  )
}