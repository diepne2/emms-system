import React, { useState } from "react"
import {
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
  CButton,
  CForm,
  CFormInput,
  CFormTextarea
} from "@coreui/react"

const PartForm = ({ visible, onClose }) => {

  const [form, setForm] = useState({
    code: "",
    name: "",
    unit: "",
    quantity: "",
    description: ""
  })

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value
    })
  }

  const handleSubmit = () => {

    if (!form.name || !form.unit) {
      alert("Vui lòng nhập thông tin bắt buộc")
      return
    }

    console.log("SAVE PART:", form)

    alert("Lưu vật tư thành công")

    onClose()
  }

  return (

    <CModal visible={visible} onClose={onClose}>

      <CModalHeader>
        <CModalTitle>Thêm vật tư</CModalTitle>
      </CModalHeader>

      <CModalBody>

        <CForm>

          <CFormInput
            label="Mã vật tư"
            name="code"
            value={form.code}
            onChange={handleChange}
            style={{ marginBottom: "15px" }}
          />

          <CFormInput
            label="Tên vật tư *"
            name="name"
            value={form.name}
            onChange={handleChange}
            style={{ marginBottom: "15px" }}
          />

          <CFormInput
            label="Đơn vị tính *"
            name="unit"
            value={form.unit}
            onChange={handleChange}
            style={{ marginBottom: "15px" }}
          />

          <CFormInput
            type="number"
            label="Số lượng tồn"
            name="quantity"
            value={form.quantity}
            onChange={handleChange}
            style={{ marginBottom: "15px" }}
          />

          <CFormTextarea
            label="Mô tả"
            name="description"
            value={form.description}
            onChange={handleChange}
          />

        </CForm>

      </CModalBody>

      <CModalFooter>

        <CButton color="secondary" onClick={onClose}>
          Hủy
        </CButton>

        <CButton color="primary" onClick={handleSubmit}>
          Lưu
        </CButton>

      </CModalFooter>

    </CModal>
  )
}

export default PartForm