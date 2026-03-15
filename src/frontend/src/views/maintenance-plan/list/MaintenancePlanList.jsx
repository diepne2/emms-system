import React, { useState } from "react"
import {
  CCard,
  CCardHeader,
  CCardBody,
  CButton,
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
  CBadge
} from "@coreui/react"

const mockPlans = [
  {
    id: "MP-001",
    asset: "Chiller A1",
    type: "Định kỳ",
    cycle: "30 ngày",
    startDate: "2026-03-20",
    status: "Hiệu lực"
  },
  {
    id: "MP-002",
    asset: "Boiler B1",
    type: "Đột xuất",
    cycle: "-",
    startDate: "2026-03-25",
    status: "Hiệu lực"
  }
]

export default function MaintenancePlanList() {

  const [plans] = useState(mockPlans)

  return (

    <CCard style={{
      borderRadius: "12px",
      boxShadow: "0 4px 12px rgba(0,0,0,0.08)"
    }}>

      <CCardHeader style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        background: "#2f3e5c",
        color: "white",
        fontWeight: "600"
      }}>
        Danh sách kế hoạch bảo trì

        <CButton style={{
          background: "#321fdb",
          color: "white",
          borderRadius: "6px"
        }}>
          + Tạo kế hoạch
        </CButton>

      </CCardHeader>

      <CCardBody>

        <CTable hover bordered>

          <CTableHead style={{ background: "#eef1f5" }}>
            <CTableRow>

              <CTableHeaderCell>ID</CTableHeaderCell>
              <CTableHeaderCell>Thiết bị</CTableHeaderCell>
              <CTableHeaderCell>Loại</CTableHeaderCell>
              <CTableHeaderCell>Chu kỳ</CTableHeaderCell>
              <CTableHeaderCell>Ngày bắt đầu</CTableHeaderCell>
              <CTableHeaderCell>Trạng thái</CTableHeaderCell>

            </CTableRow>
          </CTableHead>

          <CTableBody>

            {plans.map(p => (

              <CTableRow key={p.id}>

                <CTableDataCell>{p.id}</CTableDataCell>
                <CTableDataCell>{p.asset}</CTableDataCell>
                <CTableDataCell>{p.type}</CTableDataCell>
                <CTableDataCell>{p.cycle}</CTableDataCell>
                <CTableDataCell>{p.startDate}</CTableDataCell>

                <CTableDataCell>

                  <CBadge style={{
                    background: "#2eb85c",
                    padding: "6px 10px"
                  }}>
                    {p.status}
                  </CBadge>

                </CTableDataCell>

              </CTableRow>

            ))}

          </CTableBody>

        </CTable>

      </CCardBody>

    </CCard>
  )
}