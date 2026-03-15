import React, { useState } from "react"
import {
  CCard,
  CCardBody,
  CCardHeader,
  CRow,
  CCol,
  CForm,
  CFormLabel,
  CFormInput,
  CFormTextarea,
  CFormSelect,
  CButton
} from "@coreui/react"

export default function WorkOrderForm() {

  const [form, setForm] = useState({
    title: "",
    description: "",
    priority: "Medium",
    asset: "",
    location: "",
    startDate: "",
    endDate: "",
    technician: ""
  })

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    console.log("Work Order Data:", form)
  }

  const styles = {

    page: {
      paddingTop: "10px"
    },

    card: {
      borderRadius: "12px",
      transition: "all 0.2s ease"
    },

    header: {
      background: "#f8f9fa",
      fontWeight: "600",
      fontSize: "16px",
      borderBottom: "1px solid #eee"
    },

    sectionTitle: {
      fontSize: "14px",
      fontWeight: "600",
      color: "#6c757d",
      marginBottom: "12px",
      borderLeft: "4px solid #5856d6",
      paddingLeft: "8px"
    },

    formGroup: {
      marginBottom: "20px"
    },

    actions: {
      marginTop: "10px",
      display: "flex",
      gap: "10px"
    },

    button: {
      minWidth: "150px"
    }

  }

  return (

    <CRow style={styles.page}>
      <CCol lg={8}>

        <CCard style={styles.card} className="shadow-sm border-0">

          <CCardHeader style={styles.header}>
            Create Work Order
          </CCardHeader>

          <CCardBody>

            <CForm onSubmit={handleSubmit}>

              {/* WORK ORDER INFO */}

              <h6 style={styles.sectionTitle}>Work Order Information</h6>

              <CRow style={styles.formGroup}>
                <CCol md={12}>
                  <CFormLabel>Title</CFormLabel>
                  <CFormInput
                    name="title"
                    value={form.title}
                    onChange={handleChange}
                    placeholder="Enter work order title"
                  />
                </CCol>
              </CRow>

              <CRow style={styles.formGroup}>
                <CCol md={12}>
                  <CFormLabel>Description</CFormLabel>
                  <CFormTextarea
                    rows={3}
                    name="description"
                    value={form.description}
                    onChange={handleChange}
                    placeholder="Describe maintenance work"
                  />
                </CCol>
              </CRow>

              <CRow style={styles.formGroup}>

                <CCol md={6}>
                  <CFormLabel>Priority</CFormLabel>
                  <CFormSelect
                    name="priority"
                    value={form.priority}
                    onChange={handleChange}
                  >
                    <option>Low</option>
                    <option>Medium</option>
                    <option>High</option>
                    <option>Critical</option>
                  </CFormSelect>
                </CCol>

                <CCol md={6}>
                  <CFormLabel>Status</CFormLabel>
                  <CFormInput
                    value="Pending Approval"
                    disabled
                  />
                </CCol>

              </CRow>


              {/* ASSET INFO */}

              <h6 style={styles.sectionTitle}>Asset Information</h6>

              <CRow style={styles.formGroup}>

                <CCol md={6}>
                  <CFormLabel>Device</CFormLabel>
                  <CFormSelect
                    name="asset"
                    value={form.asset}
                    onChange={handleChange}
                  >
                    <option>Select device</option>
                    <option>Compressor A1</option>
                    <option>Chiller B2</option>
                    <option>Pump C3</option>
                  </CFormSelect>
                </CCol>

                <CCol md={6}>
                  <CFormLabel>Location</CFormLabel>
                  <CFormInput
                    name="location"
                    value={form.location}
                    onChange={handleChange}
                    placeholder="Plant / Building / Floor"
                  />
                </CCol>

              </CRow>


              {/* EXECUTION PLAN */}

              <h6 style={styles.sectionTitle}>Execution Plan</h6>

              <CRow style={styles.formGroup}>

                <CCol md={6}>
                  <CFormLabel>Planned Start</CFormLabel>
                  <CFormInput
                    type="datetime-local"
                    name="startDate"
                    value={form.startDate}
                    onChange={handleChange}
                  />
                </CCol>

                <CCol md={6}>
                  <CFormLabel>Planned End</CFormLabel>
                  <CFormInput
                    type="datetime-local"
                    name="endDate"
                    value={form.endDate}
                    onChange={handleChange}
                  />
                </CCol>

              </CRow>


              <CRow style={styles.formGroup}>
                <CCol md={6}>
                  <CFormLabel>Assign Technician</CFormLabel>
                  <CFormSelect
                    name="technician"
                    value={form.technician}
                    onChange={handleChange}
                  >
                    <option>Select technician</option>
                    <option>Nguyen Van A</option>
                    <option>Tran Van B</option>
                  </CFormSelect>
                </CCol>
              </CRow>


              {/* ACTION BUTTONS */}

              <div style={styles.actions}>

                <CButton style={styles.button} color="primary" type="submit">
                  Save Work Order
                </CButton>

                <CButton style={styles.button} color="success">
                  Submit for Approval
                </CButton>

                <CButton style={styles.button} color="secondary" variant="outline">
                  Cancel
                </CButton>

              </div>

            </CForm>

          </CCardBody>

        </CCard>

      </CCol>
    </CRow>
  )
}