import React from "react"
import { useParams, useNavigate } from "react-router-dom"

import {
  CCard,
  CCardHeader,
  CCardBody,
  CRow,
  CCol,
  CBadge,
  CButton
} from "@coreui/react"

const styles = {

  card:{
    borderRadius:"10px",
    boxShadow:"0 2px 12px rgba(0,0,0,0.08)"
  },

  header:{
    fontSize:"20px",
    fontWeight:"600"
  },

  avatar:{
    width:"80px",
    height:"80px",
    borderRadius:"50%",
    background:"#e9ecef",
    display:"flex",
    alignItems:"center",
    justifyContent:"center",
    fontSize:"30px",
    fontWeight:"600"
  },

  top:{
    display:"flex",
    alignItems:"center",
    gap:"20px",
    marginBottom:"25px"
  },

  label:{
    fontWeight:"600",
    color:"#6c757d"
  },

  row:{
    padding:"10px 0",
    borderBottom:"1px solid #eee"
  }

}

const UserDetail = () => {

  const { id } = useParams()
  const navigate = useNavigate()

  const users = [

    {
      id:"1",
      code:"NV001",
      name:"Nguyễn Văn A",
      birthday:"1995-05-12",
      email:"vana@company.com",
      phone:"0901234567",
      role:"TPKT",
      department:"Bảo trì",
      device:"Máy nén khí",
      location:"Xưởng 1",
      status:"Active"
    },

    {
      id:"2",
      code:"NV002",
      name:"Trần Thị B",
      birthday:"1997-03-20",
      email:"thib@company.com",
      phone:"0912345678",
      role:"NVKT",
      department:"Bảo trì",
      device:"Máy CNC",
      location:"Xưởng 2",
      status:"Active"
    }

  ]

  const user = users.find(u => u.id === id)

  if(!user){
    return <div>Không tìm thấy nhân viên</div>
  }

  return (

    <CCard style={styles.card}>

      <CCardHeader style={styles.header}>
        👤 Chi tiết nhân viên
      </CCardHeader>

      <CCardBody>

        {/* Avatar + tên */}

        <div style={styles.top}>

          <div style={styles.avatar}>
            {user.name.charAt(0)}
          </div>

          <div>

            <h4 style={{margin:0}}>
              {user.name}
            </h4>

            <CBadge color="success">
              {user.status}
            </CBadge>

          </div>

        </div>

        {/* Thông tin */}

        <CRow style={styles.row}>
          <CCol md={3} style={styles.label}>
            Mã nhân viên
          </CCol>
          <CCol md={9}>{user.code}</CCol>
        </CRow>

        <CRow style={styles.row}>
          <CCol md={3} style={styles.label}>
            Ngày sinh
          </CCol>
          <CCol md={9}>{user.birthday}</CCol>
        </CRow>

        <CRow style={styles.row}>
          <CCol md={3} style={styles.label}>
            Email
          </CCol>
          <CCol md={9}>{user.email}</CCol>
        </CRow>

        <CRow style={styles.row}>
          <CCol md={3} style={styles.label}>
            Số điện thoại
          </CCol>
          <CCol md={9}>{user.phone}</CCol>
        </CRow>

        <CRow style={styles.row}>
          <CCol md={3} style={styles.label}>
            Vai trò
          </CCol>
          <CCol md={9}>{user.role}</CCol>
        </CRow>

        <CRow style={styles.row}>
          <CCol md={3} style={styles.label}>
            Phòng ban
          </CCol>
          <CCol md={9}>{user.department}</CCol>
        </CRow>

        <CRow style={styles.row}>
          <CCol md={3} style={styles.label}>
            Thiết bị
          </CCol>
          <CCol md={9}>
            <CBadge color="info">
              {user.device}
            </CBadge>
          </CCol>
        </CRow>

        <CRow style={styles.row}>
          <CCol md={3} style={styles.label}>
            Vị trí thiết bị
          </CCol>
          <CCol md={9}>{user.location}</CCol>
        </CRow>

        {/* Button */}

        <div style={{marginTop:"20px"}}>

          <CButton
            color="secondary"
            onClick={()=>navigate("/hr/users")}
          >
            Quay lại
          </CButton>

        </div>

      </CCardBody>

    </CCard>

  )

}

export default UserDetail