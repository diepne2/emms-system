import React, { useState } from "react"
import CIcon from "@coreui/icons-react"
import { cilInfo, cilPencil, cilTrash } from "@coreui/icons"
import { useNavigate } from "react-router-dom"

import {
  CButton,
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
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
  CForm,
  CFormLabel,
  CBadge
} from "@coreui/react"

const PartList = () => {

  const navigate = useNavigate()

  const [parts, setParts] = useState([
    {
      id: 1,
      code: "VT-001",
      name: "Bạc đạn",
      unit: "Cái",
      quantity: 12,
      description: "Bạc đạn motor"
    },
    {
      id: 2,
      code: "VT-002",
      name: "Dây curoa",
      unit: "Sợi",
      quantity: 5,
      description: "Dây truyền động"
    }
  ])

  const [search, setSearch] = useState("")
  const [visible, setVisible] = useState(false)
  const [editPart, setEditPart] = useState(null)

  const [form, setForm] = useState({
    code: "",
    name: "",
    unit: "",
    quantity: "",
    description: ""
  })

  const [currentPage, setCurrentPage] = useState(1)
  const pageSize = 5

  const getStockColor = (qty) => {
    if (qty === 0) return "danger"
    if (qty < 10) return "warning"
    return "success"
  }

  const filteredParts = parts.filter(
    (p) =>
      p.code.toLowerCase().includes(search.toLowerCase()) ||
      p.name.toLowerCase().includes(search.toLowerCase())
  )

  const start = (currentPage - 1) * pageSize
  const paginated = filteredParts.slice(start, start + pageSize)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const openAdd = () => {
    setEditPart(null)
    setForm({
      code: "",
      name: "",
      unit: "",
      quantity: "",
      description: ""
    })
    setVisible(true)
  }

  const openEdit = (part) => {
    setEditPart(part)
    setForm(part)
    setVisible(true)
  }

  const savePart = () => {

    if (editPart) {
      setParts(
        parts.map((p) =>
          p.id === editPart.id ? { ...form, id: editPart.id } : p
        )
      )
    } else {
      setParts([
        ...parts,
        { ...form, id: Date.now() }
      ])
    }

    setVisible(false)
  }

  const deletePart = (id) => {

    if (window.confirm("Bạn có chắc muốn xóa vật tư này?")) {
      setParts(parts.filter((p) => p.id !== id))
    }

  }

  return (
    <CCard>

      <CCardHeader
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center"
        }}
      >
        <h4 style={{ fontWeight: 600 }}>Danh mục vật tư</h4>

        <CButton color="primary" onClick={openAdd}>
          + Thêm vật tư
        </CButton>

      </CCardHeader>

      <CCardBody>

        <CFormInput
          placeholder="Tìm mã hoặc tên vật tư..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{
            maxWidth: "300px",
            marginBottom: "20px"
          }}
        />

        <CTable hover responsive>

          <CTableHead style={{ background: "#f5f6fa" }}>
            <CTableRow>
              <CTableHeaderCell>Mã</CTableHeaderCell>
              <CTableHeaderCell>Tên vật tư</CTableHeaderCell>
              <CTableHeaderCell>Đơn vị</CTableHeaderCell>
              <CTableHeaderCell>Tồn kho</CTableHeaderCell>
              <CTableHeaderCell>Mô tả</CTableHeaderCell>
              <CTableHeaderCell style={{ textAlign: "center" }}>
                Thao tác
              </CTableHeaderCell>
            </CTableRow>
          </CTableHead>

          <CTableBody>

            {paginated.map((part) => (

              <CTableRow key={part.id} style={{ verticalAlign: "middle" }}>

                <CTableDataCell>{part.code}</CTableDataCell>

                <CTableDataCell>{part.name}</CTableDataCell>

                <CTableDataCell>{part.unit}</CTableDataCell>

                <CTableDataCell>
                  <CBadge color={getStockColor(part.quantity)}>
                    {part.quantity}
                  </CBadge>
                </CTableDataCell>

                <CTableDataCell>{part.description}</CTableDataCell>

                <CTableDataCell>

                  <div
                    style={{
                      display: "flex",
                      gap: "12px",
                      justifyContent: "center"
                    }}
                  >

                    <CIcon
                      icon={cilInfo}
                      size="lg"
                      style={{ cursor: "pointer" }}
                      onClick={() => navigate(`/warehouse/parts/${part.id}`)}
                    />

                    <CIcon
                      icon={cilPencil}
                      size="lg"
                      style={{ color: "#0d6efd", cursor: "pointer" }}
                      onClick={() => openEdit(part)}
                    />

                    <CIcon
                      icon={cilTrash}
                      size="lg"
                      style={{ color: "#e55353", cursor: "pointer" }}
                      onClick={() => deletePart(part.id)}
                    />

                  </div>

                </CTableDataCell>

              </CTableRow>

            ))}

          </CTableBody>

        </CTable>

        {/* Pagination */}

        <div style={{ marginTop: 20 }}>

          <CButton
            size="sm"
            disabled={currentPage === 1}
            onClick={() => setCurrentPage(currentPage - 1)}
            style={{ marginRight: 10 }}
          >
            Prev
          </CButton>

          <CButton
            size="sm"
            disabled={start + pageSize >= filteredParts.length}
            onClick={() => setCurrentPage(currentPage + 1)}
          >
            Next
          </CButton>

        </div>

      </CCardBody>

      {/* Modal Form */}

      <CModal visible={visible} onClose={() => setVisible(false)}>

        <CModalHeader>
          <CModalTitle>
            {editPart ? "Cập nhật vật tư" : "Thêm vật tư"}
          </CModalTitle>
        </CModalHeader>

        <CModalBody>

          <CForm>

            <div style={{ marginBottom: 10 }}>
              <CFormLabel>Mã vật tư</CFormLabel>
              <CFormInput
                name="code"
                value={form.code}
                onChange={handleChange}
              />
            </div>

            <div style={{ marginBottom: 10 }}>
              <CFormLabel>Tên vật tư *</CFormLabel>
              <CFormInput
                name="name"
                value={form.name}
                onChange={handleChange}
              />
            </div>

            <div style={{ marginBottom: 10 }}>
              <CFormLabel>Đơn vị</CFormLabel>
              <CFormInput
                name="unit"
                value={form.unit}
                onChange={handleChange}
              />
            </div>

            <div style={{ marginBottom: 10 }}>
              <CFormLabel>Số lượng</CFormLabel>
              <CFormInput
                type="number"
                name="quantity"
                value={form.quantity}
                onChange={handleChange}
              />
            </div>

            <div style={{ marginBottom: 10 }}>
              <CFormLabel>Mô tả</CFormLabel>
              <CFormInput
                name="description"
                value={form.description}
                onChange={handleChange}
              />
            </div>

          </CForm>

        </CModalBody>

        <CModalFooter>

          <CButton color="secondary" onClick={() => setVisible(false)}>
            Hủy
          </CButton>

          <CButton color="primary" onClick={savePart}>
            Lưu
          </CButton>

        </CModalFooter>

      </CModal>

    </CCard>
  )
}

export default PartList