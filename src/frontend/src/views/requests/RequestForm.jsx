import React, { useState } from "react"
import {
  CRow,
  CCol,
  CCard,
  CCardHeader,
  CCardBody,
  CButton,
  CFormInput,
  CFormTextarea,
  CFormSelect
} from "@coreui/react"

export default function RequestForm() {

  const [device, setDevice] = useState("")
  const [priority, setPriority] = useState("")
  const [description, setDescription] = useState("")

  const handleSubmit = (e) => {
    e.preventDefault()

    const newRequest = {
      device,
      priority,
      description,
      date: new Date().toISOString().slice(0,10)
    }

    console.log(newRequest)
    alert("Request submitted!")
  }

  return (
    <CRow className="justify-content-center">

      <CCol md={8}>

        <CCard className="shadow-lg border-0">

          <CCardHeader
            style={{
              background: "#f8f9fa",
              fontWeight: "600",
              fontSize: "18px"
            }}
          >
            🛠 Create Maintenance Request
          </CCardHeader>

          <CCardBody>

            <form onSubmit={handleSubmit}>

              {/* Device */}
              <div style={{marginBottom:"20px"}}>
                <label className="form-label fw-semibold">
                  Device / Equipment
                </label>

                <CFormInput
                  placeholder="Enter device name (e.g. Air Compressor A1)"
                  value={device}
                  onChange={(e)=>setDevice(e.target.value)}
                  required
                />
              </div>

              {/* Priority */}
              <div style={{marginBottom:"20px"}}>
                <label className="form-label fw-semibold">
                  Priority Level
                </label>

                <CFormSelect
                  value={priority}
                  onChange={(e)=>setPriority(e.target.value)}
                  required
                >
                  <option value="">Select priority</option>
                  <option value="Low">Low</option>
                  <option value="Medium">Medium</option>
                  <option value="High">High</option>
                  <option value="Critical">Critical</option>
                </CFormSelect>
              </div>

              {/* Description */}
              <div style={{marginBottom:"25px"}}>
                <label className="form-label fw-semibold">
                  Problem Description
                </label>

                <CFormTextarea
                  rows={4}
                  placeholder="Describe the issue clearly (noise, vibration, overheating...)"
                  value={description}
                  onChange={(e)=>setDescription(e.target.value)}
                />
              </div>

              {/* Buttons */}
              <div
                style={{
                  display:"flex",
                  justifyContent:"flex-end",
                  gap:"10px"
                }}
              >

                <CButton
                  color="secondary"
                  variant="outline"
                >
                  Cancel
                </CButton>

                <CButton
                  color="primary"
                  type="submit"
                >
                  Submit Request
                </CButton>

              </div>

            </form>

          </CCardBody>
        </CCard>

      </CCol>

    </CRow>
  )
}