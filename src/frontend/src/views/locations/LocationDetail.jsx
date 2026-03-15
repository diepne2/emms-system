import React from "react"
import {
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CButton
} from "@coreui/react"

const LocationDetail = ({ visible, onClose, location }) => {

  if (!location) return null

  return (

    <CModal alignment="center" visible={visible} onClose={onClose}>

      <CModalHeader>
        <CModalTitle>Chi tiết vị trí</CModalTitle>
      </CModalHeader>

      <CModalBody>

        <p><strong>Tên vị trí:</strong> {location.name}</p>
        <p><strong>Khu vực:</strong> {location.area}</p>
        <p><strong>Mô tả:</strong> {location.description}</p>

      </CModalBody>

      <div className="text-end p-3">
        <CButton color="secondary" onClick={onClose}>
          Đóng
        </CButton>
      </div>

    </CModal>

  )
}

export default LocationDetail