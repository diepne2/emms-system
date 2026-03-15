import React, { useState } from "react"
import CIcon from "@coreui/icons-react"
import {
  cilPeople,
  cilUser,
  cilLocationPin,
  cilCalendar,
  cilPencil,
  cilTrash
} from "@coreui/icons"

import {
  CCard,
  CCardBody,
  CButton,
  CBadge,
  CRow,
  CCol
} from "@coreui/react"

export default function TeamList() {

const [teams] = useState([

{
id:1,
name:"Bảo trì",
manager:"Nguyễn Văn A",
location:"Xưởng sản xuất",
employees:6,
created:"2023-01-10",
status:"Active"
},

{
id:2,
name:"Kho vật tư",
manager:"Trần Thị B",
location:"Kho A",
employees:3,
created:"2023-03-15",
status:"Active"
}

])

const styles={

header:{
display:"flex",
justifyContent:"space-between",
alignItems:"center",
marginBottom:"30px"
},

title:{
fontSize:"26px",
fontWeight:"600",
color:"#2f353a"
},

card:{
borderRadius:"14px",
boxShadow:"0 4px 14px rgba(0,0,0,0.08)",
border:"none",
transition:"all 0.25s ease"
},

cardHover:{
transform:"translateY(-4px)",
boxShadow:"0 8px 20px rgba(0,0,0,0.12)"
},

name:{
fontSize:"20px",
fontWeight:"600",
marginBottom:"12px",
color:"#2f353a"
},

infoRow:{
display:"flex",
alignItems:"center",
gap:"10px",
marginBottom:"7px",
color:"#6c757d",
fontSize:"14px"
},

icon:{
color:"#6c757d"
},

footer:{
display:"flex",
justifyContent:"space-between",
alignItems:"center",
marginTop:"12px"
},

actions:{
display:"flex",
gap:"8px"
},

actionBtn:{
borderRadius:"6px",
padding:"4px 8px"
}

}

return (

<div>

{/* HEADER */}

<div style={styles.header}>

<div style={styles.title}>
Phòng ban
</div>

<CButton color="primary">
+ Thêm phòng ban
</CButton>

</div>

{/* TEAM LIST */}

<CRow>

{teams.map(team => (

<CCol md={4} key={team.id}>

<CCard
style={styles.card}
className="mb-4"
onMouseEnter={(e)=>e.currentTarget.style.transform="translateY(-4px)"}
onMouseLeave={(e)=>e.currentTarget.style.transform="translateY(0)"}
>

<CCardBody>

<div style={styles.name}>
{team.name}
</div>

<div style={styles.infoRow}>
<CIcon icon={cilUser} style={styles.icon}/>
Quản lý: {team.manager}
</div>

<div style={styles.infoRow}>
<CIcon icon={cilPeople} style={styles.icon}/>
Nhân viên: {team.employees}
</div>

<div style={styles.infoRow}>
<CIcon icon={cilLocationPin} style={styles.icon}/>
Vị trí: {team.location}
</div>

<div style={styles.infoRow}>
<CIcon icon={cilCalendar} style={styles.icon}/>
Thành lập: {team.created}
</div>

<div style={styles.footer}>

<CBadge color={team.status==="Active"?"success":"secondary"}>
{team.status}
</CBadge>

<div style={styles.actions}>

<CButton size="sm" color="light" style={styles.actionBtn}>
<CIcon icon={cilPencil}/>
</CButton>

<CButton size="sm" color="light" style={styles.actionBtn}>
<CIcon icon={cilTrash}/>
</CButton>

</div>

</div>

</CCardBody>

</CCard>

</CCol>

))}

</CRow>

</div>

)

}