import React from "react"
import {
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
  CButton,
  CFormInput,
  CFormLabel
} from "@coreui/react"

const LocationForm = ({ visible, onClose, onSave, form, setForm, editing }) => {

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  return (
    <CModal alignment="center" visible={visible} onClose={onClose}>

      <CModalHeader>
        <CModalTitle>
          {editing ? "Chỉnh sửa vị trí" : "Thêm vị trí"}
        </CModalTitle>
      </CModalHeader>

      <CModalBody>

        <div className="mb-3">
          <CFormLabel>Tên vị trí</CFormLabel>
          <CFormInput
            name="name"
            value={form.name}
            onChange={handleChange}
          />
        </div>

        <div className="mb-3">
          <CFormLabel>Khu vực</CFormLabel>
          <CFormInput
            name="area"
            value={form.area}
            onChange={handleChange}
          />
        </div>

        <div>
          <CFormLabel>Mô tả</CFormLabel>
          <CFormInput
            name="description"
            value={form.description || ""}
            onChange={handleChange}
          />
        </div>

      </CModalBody>

      <CModalFooter>

        <CButton color="secondary" onClick={onClose}>
          Huỷ
        </CButton>

        <CButton color="primary" onClick={onSave}>
          Lưu
        </CButton>

      </CModalFooter>

    </CModal>
  )
}

export default LocationForm