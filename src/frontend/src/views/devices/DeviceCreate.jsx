import React, { useState } from "react"
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CRow,
  CCol,
  CFormInput,
  CFormSelect,
  CButton,
  CFormTextarea
} from "@coreui/react"

const DeviceCreate = () => {

  const [device, setDevice] = useState({
    deviceCode: "",
    deviceName: "",
    category: "",
    location: "",
    status: "Active",
    manufacturer: "",
    model: "",
    note: ""
  })

  const handleChange = (e) => {
    const { name, value } = e.target
    setDevice({
      ...device,
      [name]: value
    })
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    console.log("Device created:", device)

    alert("Thêm thiết bị thành công!")
  }

  const buttonStyle = {
    backgroundColor: "#1b3c66",
    borderColor: "#1b3c66",
    color: "#fff"
  }

  return (
    <div className="p-3">

      <CCard className="shadow-sm border-0">
        <CCardHeader>
          <h5 style={{ color: "#1b3c66", fontWeight: "600" }}>
            Thêm thiết bị mới
          </h5>
        </CCardHeader>

        <CCardBody>

          <CForm onSubmit={handleSubmit}>

            <CRow className="g-3">

              <CCol md={6}>
                <CFormInput
                  label="Mã thiết bị"
                  name="deviceCode"
                  placeholder="VD: DV-005"
                  value={device.deviceCode}
                  onChange={handleChange}
                  required
                />
              </CCol>

              <CCol md={6}>
                <CFormInput
                  label="Tên thiết bị"
                  name="deviceName"
                  placeholder="Nhập tên thiết bị"
                  value={device.deviceName}
                  onChange={handleChange}
                  required
                />
              </CCol>

              <CCol md={6}>
                <CFormSelect
                  label="Phân loại thiết bị"
                  name="category"
                  value={device.category}
                  onChange={handleChange}
                >
                  <option value="">Chọn loại thiết bị</option>
                  <option value="Electrical">Điện</option>
                  <option value="HVAC">HVAC</option>
                  <option value="Mechanical">Cơ khí</option>
                  <option value="Fire">PCCC</option>
                </CFormSelect>
              </CCol>

              <CCol md={6}>
                <CFormInput
                  label="Vị trí"
                  name="location"
                  placeholder="Ví dụ: Tòa nhà A"
                  value={device.location}
                  onChange={handleChange}
                />
              </CCol>

              <CCol md={6}>
                <CFormInput
                  label="Nhà sản xuất"
                  name="manufacturer"
                  placeholder="Cummins, Mitsubishi..."
                  value={device.manufacturer}
                  onChange={handleChange}
                />
              </CCol>

              <CCol md={6}>
                <CFormInput
                  label="Model"
                  name="model"
                  placeholder="Model thiết bị"
                  value={device.model}
                  onChange={handleChange}
                />
              </CCol>

              <CCol md={6}>
                <CFormSelect
                  label="Trạng thái"
                  name="status"
                  value={device.status}
                  onChange={handleChange}
                >
                  <option value="Active">Active</option>
                  <option value="Inactive">Inactive</option>
                  <option value="Maintenance">Maintenance</option>
                </CFormSelect>
              </CCol>

              <CCol md={12}>
                <CFormTextarea
                  label="Ghi chú"
                  rows={3}
                  name="note"
                  value={device.note}
                  onChange={handleChange}
                />
              </CCol>

            </CRow>

            <div className="mt-4 d-flex gap-2">

              <CButton type="submit" style={buttonStyle}>
                Lưu thiết bị
              </CButton>

              <CButton color="secondary">
                Hủy
              </CButton>

            </div>

          </CForm>

        </CCardBody>
      </CCard>

    </div>
  )
}

export default DeviceCreate