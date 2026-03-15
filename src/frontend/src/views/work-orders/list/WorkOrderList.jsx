import React, { useState } from "react"
import {
  CCard,
  CCardBody,
  CCardHeader,
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
  CFormInput,
  CFormSelect,
  CButton,
  CBadge,
} from "@coreui/react"

const WorkOrderList = () => {
  const [search, setSearch] = useState("")
  const [status, setStatus] = useState("all")

  const data = [
    {
      id: "WO-001",
      title: "Air conditioner not cooling",
      device: "AC - Floor 3",
      priority: "High",
      status: "Open",
      assignee: "Nguyen A",
    },
    {
      id: "WO-002",
      title: "Generator maintenance",
      device: "Generator 01",
      priority: "Medium",
      status: "In Progress",
      assignee: "Tran B",
    },
    {
      id: "WO-003",
      title: "Replace light bulb",
      device: "Office 201",
      priority: "Low",
      status: "Completed",
      assignee: "Le C",
    },
  ]

  const getStatusColor = (status) => {
    if (status === "Open") return "danger"
    if (status === "In Progress") return "warning"
    if (status === "Completed") return "success"
    return "secondary"
  }

  const getPriorityColor = (priority) => {
    if (priority === "High") return "danger"
    if (priority === "Medium") return "warning"
    if (priority === "Low") return "success"
  }

  const filtered = data.filter((item) => {
    return (
      item.title.toLowerCase().includes(search.toLowerCase()) &&
      (status === "all" || item.status === status)
    )
  })

  return (
    <div className="wo-container">
      <CCard className="wo-card">
        <CCardHeader className="wo-header">Work Orders</CCardHeader>

        <CCardBody>

          {/* TOOLBAR */}
          <div className="wo-toolbar">

            <div className="wo-search">
              <CFormInput
                placeholder="Search work order..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>

            <div className="wo-filter">
              <CFormSelect
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="all">All Status</option>
                <option value="Open">Open</option>
                <option value="In Progress">In Progress</option>
                <option value="Completed">Completed</option>
              </CFormSelect>
            </div>

            <CButton color="primary" className="wo-button">
              Tìm kiếm
            </CButton>

          </div>

          {/* TABLE */}

          <CTable hover responsive className="wo-table">

            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>ID</CTableHeaderCell>
                <CTableHeaderCell>Title</CTableHeaderCell>
                <CTableHeaderCell>Device</CTableHeaderCell>
                <CTableHeaderCell>Priority</CTableHeaderCell>
                <CTableHeaderCell>Status</CTableHeaderCell>
                <CTableHeaderCell>Assignee</CTableHeaderCell>
                <CTableHeaderCell></CTableHeaderCell>
              </CTableRow>
            </CTableHead>

            <CTableBody>

              {filtered.map((wo) => (
                <CTableRow key={wo.id}>

                  <CTableDataCell className="wo-id">
                    {wo.id}
                  </CTableDataCell>

                  <CTableDataCell>{wo.title}</CTableDataCell>

                  <CTableDataCell>{wo.device}</CTableDataCell>

                  <CTableDataCell>
                    <CBadge color={getPriorityColor(wo.priority)}>
                      {wo.priority}
                    </CBadge>
                  </CTableDataCell>

                  <CTableDataCell>
                    <CBadge color={getStatusColor(wo.status)}>
                      {wo.status}
                    </CBadge>
                  </CTableDataCell>

                  <CTableDataCell>
                    👤 {wo.assignee}
                  </CTableDataCell>

                  <CTableDataCell>

                    <CButton size="sm" color="light">
                      View
                    </CButton>

                    <CButton size="sm" color="warning" className="ms-2">
                      Edit
                    </CButton>

                  </CTableDataCell>

                </CTableRow>
              ))}

            </CTableBody>

          </CTable>

        </CCardBody>
      </CCard>

      {/* CSS */}

      <style jsx>{`

        .wo-container{
          padding:20px;
        }

        .wo-card{
          border-radius:10px;
          box-shadow:0 4px 10px rgba(0,0,0,0.08);
        }

        .wo-header{
          font-size:18px;
          font-weight:600;
        }

        .wo-toolbar{
          display:flex;
          align-items:center;
          gap:16px;
          margin-bottom:20px;
        }

        .wo-search{
          flex:2;
        }

        .wo-filter{
          flex:1;
        }

        .wo-button{
          height:42px;
          min-width:110px;
          font-weight:500;
        }

        .wo-search input{
          height:42px;
          border-radius:8px;
        }

        .wo-filter select{
          height:42px;
          border-radius:8px;
        }

        .wo-table tr:hover{
          background:#f6f8fa;
          cursor:pointer;
        }

        .wo-id{
          font-weight:600;
          color:#2f6fed;
        }

      `}</style>

    </div>
  )
}

export default WorkOrderList