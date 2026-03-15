import React, { useState } from "react"
import {
  CCard,
  CCardHeader,
  CCardBody,
  CRow,
  CCol,
  CFormInput,
  CFormSelect,
  CButton,
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
  CBadge,
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody
} from "@coreui/react"

const mockHistory = [
  {
    id: "WO-001",
    asset: "Chiller A1",
    technician: "Nguyễn Văn A",
    department: "HVAC",
    date: "2026-02-15",
    task: "Thay dầu máy nén",
    status: "Completed"
  },
  {
    id: "WO-002",
    asset: "Boiler B1",
    technician: "Trần Văn B",
    department: "Mechanical",
    date: "2026-02-20",
    task: "Kiểm tra áp suất",
    status: "Completed"
  }
]

export default function MaintenanceHistory() {

  const [history] = useState(mockHistory)

  const [search, setSearch] = useState("")
  const [month, setMonth] = useState("")
  const [year, setYear] = useState("")

  const [detail, setDetail] = useState(null)

  const filteredData = history.filter(item => {

    const matchSearch =
      item.asset.toLowerCase().includes(search.toLowerCase()) ||
      item.technician.toLowerCase().includes(search.toLowerCase())

    const itemDate = new Date(item.date)

    const matchMonth = month ? itemDate.getMonth() + 1 === Number(month) : true
    const matchYear = year ? itemDate.getFullYear() === Number(year) : true

    return matchSearch && matchMonth && matchYear
  })

  return (

    <CCard style={{
      borderRadius: "12px",
      boxShadow: "0 4px 12px rgba(0,0,0,0.08)"
    }}>

      <CCardHeader style={{
        background: "#2f3e5c",
        color: "white",
        fontWeight: "600",
        fontSize: "16px"
      }}>
        Lịch sử bảo trì & sửa chữa
      </CCardHeader>

      <CCardBody>

        {/* FILTER */}

        <CRow className="mb-3">

          <CCol md={4}>

            <CFormInput
              placeholder="Tìm kiếm"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />

          </CCol>

          <CCol md={3}>

            <CFormSelect
              value={month}
              onChange={(e) => setMonth(e.target.value)}
            >
              <option value="">Tháng</option>

              {[...Array(12)].map((_, i) => (
                <option key={i} value={i + 1}>
                  Tháng {i + 1}
                </option>
              ))}

            </CFormSelect>

          </CCol>

          <CCol md={3}>

            <CFormInput
              placeholder="Năm"
              value={year}
              onChange={(e) => setYear(e.target.value)}
            />

          </CCol>

          <CCol md={2}>

            <CButton
              style={{
                width: "100%",
                background: "#321fdb",
                color: "white"
              }}
            >
              Tìm kiếm
            </CButton>

          </CCol>

        </CRow>

        {/* TABLE */}

        <CTable
          hover
          bordered
          style={{
            borderRadius: "10px",
            overflow: "hidden"
          }}
        >

          <CTableHead style={{ background: "#eef1f5" }}>

            <CTableRow>

              <CTableHeaderCell>WO</CTableHeaderCell>
              <CTableHeaderCell>Thiết bị</CTableHeaderCell>
              <CTableHeaderCell>Nhân viên</CTableHeaderCell>
              <CTableHeaderCell>Phòng ban</CTableHeaderCell>
              <CTableHeaderCell>Ngày sửa</CTableHeaderCell>
              <CTableHeaderCell>Trạng thái</CTableHeaderCell>

              <CTableHeaderCell style={{ textAlign: "center" }}>
                Chi tiết
              </CTableHeaderCell>

            </CTableRow>

          </CTableHead>

          <CTableBody>

            {filteredData.map(item => (

              <CTableRow
                key={item.id}
                style={{ verticalAlign: "middle" }}
              >

                <CTableDataCell>

                  <a
                    href={`/work-order/${item.id}`}
                    style={{
                      textDecoration: "none",
                      fontWeight: "600",
                      color: "#321fdb"
                    }}
                  >
                    {item.id}
                  </a>

                </CTableDataCell>

                <CTableDataCell>{item.asset}</CTableDataCell>

                <CTableDataCell>{item.technician}</CTableDataCell>

                <CTableDataCell>{item.department}</CTableDataCell>

                <CTableDataCell>{item.date}</CTableDataCell>

                <CTableDataCell>

                  <CBadge
                    style={{
                      background: "#2eb85c",
                      padding: "6px 10px"
                    }}
                  >
                    {item.status}
                  </CBadge>

                </CTableDataCell>

                <CTableDataCell style={{ textAlign: "center" }}>

                  <CButton
                    size="sm"
                    style={{
                      background: "#321fdb",
                      color: "white"
                    }}
                    onClick={() => setDetail(item)}
                  >
                    Xem chi tiết
                  </CButton>

                </CTableDataCell>

              </CTableRow>

            ))}

          </CTableBody>

        </CTable>

      </CCardBody>

      {/* MODAL DETAIL */}

      <CModal
        visible={detail !== null}
        onClose={() => setDetail(null)}
      >

        <CModalHeader>

          <CModalTitle>
            Chi tiết Work Order
          </CModalTitle>

        </CModalHeader>

        <CModalBody>

          {detail && (

            <div>

              <p><strong>WO:</strong> {detail.id}</p>

              <p><strong>Thiết bị:</strong> {detail.asset}</p>

              <p><strong>Nhân viên sửa:</strong> {detail.technician}</p>

              <p><strong>Phòng ban:</strong> {detail.department}</p>

              <p><strong>Công việc:</strong> {detail.task}</p>

              <p><strong>Ngày sửa:</strong> {detail.date}</p>

              <p><strong>Trạng thái:</strong> {detail.status}</p>

            </div>

          )}

        </CModalBody>

      </CModal>

    </CCard>

  )
}