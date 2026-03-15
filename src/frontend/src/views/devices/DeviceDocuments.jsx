import React, { useState } from "react"
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CFormInput,
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
} from "@coreui/react"

const DeviceDocuments = () => {
  const [documents, setDocuments] = useState([])
  const [file, setFile] = useState(null)

  const handleUpload = () => {
    if (!file) return

    const newDoc = {
      id: Date.now(),
      name: file.name,
      url: URL.createObjectURL(file),
      type: file.type,
      size: file.size,
      date: new Date().toLocaleDateString(),
    }

    setDocuments([...documents, newDoc])
    setFile(null)
  }

  const handleDelete = (id) => {
    setDocuments(documents.filter((doc) => doc.id !== id))
  }

  return (
    <div className="device-documents">

      <CCard className="doc-card">
        <CCardHeader className="doc-header">
          📄 Tài liệu thiết bị
        </CCardHeader>

        <CCardBody>

          <div className="upload-box">
            <CFormInput
              type="file"
              onChange={(e) => setFile(e.target.files[0])}
            />

            <CButton color="primary" onClick={handleUpload}>
              Upload
            </CButton>
          </div>

          <CTable hover className="doc-table">
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>File</CTableHeaderCell>
                <CTableHeaderCell>Kích thước</CTableHeaderCell>
                <CTableHeaderCell>Ngày</CTableHeaderCell>
                <CTableHeaderCell>Preview</CTableHeaderCell>
                <CTableHeaderCell>Download</CTableHeaderCell>
                <CTableHeaderCell>Xóa</CTableHeaderCell>
              </CTableRow>
            </CTableHead>

            <CTableBody>
              {documents.map((doc) => (
                <CTableRow key={doc.id}>
                  <CTableDataCell>{doc.name}</CTableDataCell>

                  <CTableDataCell>
                    {(doc.size / 1024).toFixed(1)} KB
                  </CTableDataCell>

                  <CTableDataCell>{doc.date}</CTableDataCell>

                  <CTableDataCell>
                    {doc.type.includes("image") ? (
                      <img
                        src={doc.url}
                        alt=""
                        className="preview-img"
                      />
                    ) : (
                      <a href={doc.url} target="_blank">
                        View
                      </a>
                    )}
                  </CTableDataCell>

                  <CTableDataCell>
                    <a href={doc.url} download>
                      <CButton color="success" size="sm">
                        Download
                      </CButton>
                    </a>
                  </CTableDataCell>

                  <CTableDataCell>
                    <CButton
                      color="danger"
                      size="sm"
                      onClick={() => handleDelete(doc.id)}
                    >
                      Delete
                    </CButton>
                  </CTableDataCell>

                </CTableRow>
              ))}
            </CTableBody>
          </CTable>

        </CCardBody>
      </CCard>

      <style jsx>{`
        .device-documents {
          padding: 20px;
        }

        .doc-card {
          border-radius: 10px;
          box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        }

        .doc-header {
          font-size: 18px;
          font-weight: 600;
        }

        .upload-box {
          display: flex;
          gap: 10px;
          margin-bottom: 20px;
        }

        .doc-table {
          margin-top: 10px;
        }

        .preview-img {
          width: 60px;
          height: 60px;
          object-fit: cover;
          border-radius: 6px;
          border: 1px solid #ddd;
        }
      `}</style>

    </div>
  )
}

export default DeviceDocuments