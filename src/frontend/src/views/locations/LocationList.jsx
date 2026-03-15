import React, { useState } from "react"
import CIcon from "@coreui/icons-react"
import { cilInfo, cilPencil, cilTrash } from "@coreui/icons"

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
  CFormInput,
  CBadge,
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter
} from "@coreui/react"

import LocationForm from "./LocationForm"
import LocationDetail from "./LocationDetail"

const styles = {
  card: {
    borderRadius: "12px",
    boxShadow: "0 4px 12px rgba(0,0,0,0.08)"
  },

  header: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center"
  },

  search: {
    maxWidth: "320px",
    marginBottom: "20px"
  },

  actionBox: {
    display: "flex",
    justifyContent: "center",
    gap: "16px"
  },

  icon: {
    cursor: "pointer",
    transition: "0.2s"
  },

  iconEdit: {
    color: "#0d6efd",
    cursor: "pointer"
  },

  iconDelete: {
    color: "#e55353",
    cursor: "pointer"
  }
}

const LocationList = () => {

  const [locations, setLocations] = useState([
    { id: 1, name: "Kho A", area: "Nhà máy 1", description: "Kho vật tư chính" },
    { id: 2, name: "Kho B", area: "Nhà máy 2", description: "Kho dự phòng" }
  ])

  const [search, setSearch] = useState("")

  const [formVisible, setFormVisible] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [deleteVisible, setDeleteVisible] = useState(false)

  const [editing, setEditing] = useState(false)

  const [selected, setSelected] = useState(null)

  const [form, setForm] = useState({
    name: "",
    area: "",
    description: ""
  })

  const openAdd = () => {
    setForm({ name: "", area: "", description: "" })
    setEditing(false)
    setFormVisible(true)
  }

  const openEdit = (loc) => {
    setForm(loc)
    setEditing(true)
    setFormVisible(true)
  }

  const openDetail = (loc) => {
    setSelected(loc)
    setDetailVisible(true)
  }

  const openDelete = (loc) => {
    setSelected(loc)
    setDeleteVisible(true)
  }

  const saveLocation = () => {

    if (editing) {

      setLocations(
        locations.map((l) =>
          l.id === form.id ? form : l
        )
      )

    } else {

      setLocations([
        ...locations,
        { id: Date.now(), ...form }
      ])

    }

    setFormVisible(false)
  }

  const confirmDelete = () => {

    setLocations(
      locations.filter((l) => l.id !== selected.id)
    )

    setDeleteVisible(false)
  }

  const filtered = locations.filter(
    (l) =>
      l.name.toLowerCase().includes(search.toLowerCase()) ||
      l.area.toLowerCase().includes(search.toLowerCase())
  )

  return (

    <CCard style={styles.card}>

      <CCardHeader style={styles.header}>

        <h4>Danh sách vị trí</h4>

        <CButton color="primary" onClick={openAdd}>
          + Thêm vị trí
        </CButton>

      </CCardHeader>

      <CCardBody>

        {/* Search */}

        <CFormInput
          placeholder="Tìm tên vị trí hoặc khu vực..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={styles.search}
        />

        {/* Table */}

        <CTable hover responsive>

          <CTableHead>

            <CTableRow>

              <CTableHeaderCell>Tên</CTableHeaderCell>
              <CTableHeaderCell>Khu vực</CTableHeaderCell>
              <CTableHeaderCell>Mô tả</CTableHeaderCell>

              <CTableHeaderCell className="text-center">
                Thao tác
              </CTableHeaderCell>

            </CTableRow>

          </CTableHead>

          <CTableBody>

            {filtered.map((loc) => (

              <CTableRow key={loc.id}>

                <CTableDataCell>
                  <strong>{loc.name}</strong>
                </CTableDataCell>

                <CTableDataCell>
                  <CBadge color="info">
                    {loc.area}
                  </CBadge>
                </CTableDataCell>

                <CTableDataCell>
                  {loc.description}
                </CTableDataCell>

                <CTableDataCell>

                  <div style={styles.actionBox}>

                    {/* Detail */}

                    <CIcon
                      icon={cilInfo}
                      size="lg"
                      style={styles.icon}
                      onClick={() => openDetail(loc)}
                    />

                    {/* Edit */}

                    <CIcon
                      icon={cilPencil}
                      size="lg"
                      style={styles.iconEdit}
                      onClick={() => openEdit(loc)}
                    />

                    {/* Delete */}

                    <CIcon
                      icon={cilTrash}
                      size="lg"
                      style={styles.iconDelete}
                      onClick={() => openDelete(loc)}
                    />

                  </div>

                </CTableDataCell>

              </CTableRow>

            ))}

          </CTableBody>

        </CTable>

      </CCardBody>

      {/* Form Modal */}

      <LocationForm
        visible={formVisible}
        onClose={() => setFormVisible(false)}
        onSave={saveLocation}
        form={form}
        setForm={setForm}
        editing={editing}
      />

      {/* Detail Modal */}

      <LocationDetail
        visible={detailVisible}
        onClose={() => setDetailVisible(false)}
        location={selected}
      />

      {/* Delete Modal */}

      <CModal alignment="center" visible={deleteVisible} onClose={() => setDeleteVisible(false)}>

        <CModalHeader>
          <CModalTitle>Xác nhận xoá</CModalTitle>
        </CModalHeader>

        <CModalBody>
          Bạn có chắc muốn xoá vị trí này không?
        </CModalBody>

        <CModalFooter>

          <CButton color="secondary" onClick={() => setDeleteVisible(false)}>
            Huỷ
          </CButton>

          <CButton color="danger" onClick={confirmDelete}>
            Xoá
          </CButton>

        </CModalFooter>

      </CModal>

    </CCard>

  )

}

export default LocationList