import React, { useState } from "react"
import CIcon from "@coreui/icons-react"
import { cilInfo, cilPencil, cilTrash, cilPlus } from "@coreui/icons"

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

const styles={

card:{
borderRadius:"10px",
boxShadow:"0 2px 12px rgba(0,0,0,0.08)"
},

header:{
display:"flex",
justifyContent:"space-between",
alignItems:"center"
},

title:{
fontSize:"20px",
fontWeight:"600"
},

filterBar:{
display:"flex",
gap:"15px",
marginBottom:"20px",
flexWrap:"wrap"
},

input:{
maxWidth:"250px"
},

select:{
padding:"8px 12px",
borderRadius:"6px",
border:"1px solid #ced4da",
minWidth:"160px"
},

tableHead:{
background:"#f8f9fa",
fontWeight:"600",
cursor:"pointer"
},

actions:{
display:"flex",
gap:"14px",
justifyContent:"center",
cursor:"pointer"
},

iconView:{color:"#0d6efd"},
iconEdit:{color:"#f9b115"},
iconDelete:{color:"#e55353"},

modal:{
borderRadius:"12px"
},

modalBody:{
display:"grid",
gridTemplateColumns:"1fr 1fr",
gap:"16px"
}

}

export default function UserList(){

/* DATA */

const [users,setUsers]=useState([

{
id:1,
name:"Nguyễn Văn A",
role:"TPKT",
department:"Bảo trì",
email:"vana@company.com",
phone:"0901234567",
status:"Active"
},

{
id:2,
name:"Trần Thị B",
role:"NVKT",
department:"Bảo trì",
email:"thib@company.com",
phone:"0912345678",
status:"Active"
},

{
id:3,
name:"Lê Văn C",
role:"Admin",
department:"IT",
email:"vanc@company.com",
phone:"0988888888",
status:"Inactive"
}

])

/* FILTER */

const [search,setSearch]=useState("")
const [department,setDepartment]=useState("")
const [role,setRole]=useState("")
const [status,setStatus]=useState("")

/* SORT */

const [sortField,setSortField]=useState("")
const [sortAsc,setSortAsc]=useState(true)

/* MODAL */

const [visible,setVisible]=useState(false)
const [selected,setSelected]=useState(null)

const [formVisible,setFormVisible]=useState(false)
const [editUser,setEditUser]=useState(null)

/* FORM */

const [form,setForm]=useState({
name:"",
role:"",
department:"",
email:"",
phone:"",
status:"Active"
})

/* FILTER LIST */

const departments=[...new Set(users.map(u=>u.department))]
const roles=[...new Set(users.map(u=>u.role))]
const statuses=[...new Set(users.map(u=>u.status))]

/* FILTER DATA */

let filtered=users.filter(u=>{

const matchSearch=
u.name.toLowerCase().includes(search.toLowerCase())||
u.email.toLowerCase().includes(search.toLowerCase())

const matchDepartment=
department===""||u.department===department

const matchRole=
role===""||u.role===role

const matchStatus=
status===""||u.status===status

return matchSearch && matchDepartment && matchRole && matchStatus

})

/* SORT DATA */

if(sortField){

filtered=[...filtered].sort((a,b)=>{

if(a[sortField]<b[sortField]) return sortAsc?-1:1
if(a[sortField]>b[sortField]) return sortAsc?1:-1
return 0

})

}

const sortData=(field)=>{

const asc=field===sortField?!sortAsc:true
setSortField(field)
setSortAsc(asc)

}

/* DETAIL */

const openDetail=(user)=>{
setSelected(user)
setVisible(true)
}

/* ADD */

const openAdd=()=>{
setEditUser(null)

setForm({
name:"",
role:"",
department:"",
email:"",
phone:"",
status:"Active"
})

setFormVisible(true)
}

/* EDIT */

const openEdit=(user)=>{
setEditUser(user)
setForm(user)
setFormVisible(true)
}

/* SAVE */

const saveUser=()=>{

if(editUser){

setUsers(users.map(u=>
u.id===editUser.id?{...form,id:u.id}:u
))

}else{

const newUser={
...form,
id:Date.now()
}

setUsers([...users,newUser])

}

setFormVisible(false)

}

/* DELETE */

const deleteUser=(id)=>{

if(window.confirm("Xóa nhân viên?")){

setUsers(users.filter(u=>u.id!==id))

}

}

return(

<>

<CCard style={styles.card}>

<CCardHeader style={styles.header}>

<div style={styles.title}>
👤 Người sử dụng thiết bị
</div>

<CButton color="primary" onClick={openAdd}>
<CIcon icon={cilPlus}/> Thêm nhân viên
</CButton>

</CCardHeader>

<CCardBody>

{/* FILTER */}

<div style={styles.filterBar}>

<CFormInput
placeholder="🔍 Tìm nhân viên..."
value={search}
onChange={e=>setSearch(e.target.value)}
style={styles.input}
/>

<select
value={department}
onChange={e=>setDepartment(e.target.value)}
style={styles.select}
>
<option value="">Tất cả nhóm</option>
{departments.map(d=>(
<option key={d}>{d}</option>
))}
</select>

<select
value={role}
onChange={e=>setRole(e.target.value)}
style={styles.select}
>
<option value="">Tất cả vai trò</option>
{roles.map(r=>(
<option key={r}>{r}</option>
))}
</select>

<select
value={status}
onChange={e=>setStatus(e.target.value)}
style={styles.select}
>
<option value="">Tất cả trạng thái</option>
{statuses.map(s=>(
<option key={s}>{s}</option>
))}
</select>

</div>

{/* TABLE */}

<CTable hover responsive>

<CTableHead style={styles.tableHead}>

<CTableRow>

<CTableHeaderCell onClick={()=>sortData("name")}>
Họ tên
</CTableHeaderCell>

<CTableHeaderCell onClick={()=>sortData("role")}>
Vai trò
</CTableHeaderCell>

<CTableHeaderCell onClick={()=>sortData("department")}>
Nhóm
</CTableHeaderCell>

<CTableHeaderCell>
Liên hệ
</CTableHeaderCell>

<CTableHeaderCell onClick={()=>sortData("status")}>
Trạng thái
</CTableHeaderCell>

<CTableHeaderCell className="text-center">
Thao tác
</CTableHeaderCell>

</CTableRow>

</CTableHead>

<CTableBody>

{filtered.map(user=>(

<CTableRow key={user.id}>

<CTableDataCell>
<b>{user.name}</b>
</CTableDataCell>

<CTableDataCell>
{user.role}
</CTableDataCell>

<CTableDataCell>
{user.department}
</CTableDataCell>

<CTableDataCell>

<div style={{fontSize:"14px"}}>

<div>📧 {user.email}</div>

<div style={{color:"#6c757d"}}>
📞 {user.phone}
</div>

</div>

</CTableDataCell>

<CTableDataCell>

<CBadge color={user.status==="Active"?"success":"secondary"}>
{user.status}
</CBadge>

</CTableDataCell>

<CTableDataCell>

<div style={styles.actions}>

<CIcon
icon={cilInfo}
style={styles.iconView}
onClick={()=>openDetail(user)}
/>

<CIcon
icon={cilPencil}
style={styles.iconEdit}
onClick={()=>openEdit(user)}
/>

<CIcon
icon={cilTrash}
style={styles.iconDelete}
onClick={()=>deleteUser(user.id)}
/>

</div>

</CTableDataCell>

</CTableRow>

))}

</CTableBody>

</CTable>

</CCardBody>

</CCard>

{/* DETAIL MODAL */}

<CModal
alignment="center"
visible={visible}
onClose={()=>setVisible(false)}
>

<CModalHeader>
<CModalTitle>Chi tiết nhân viên</CModalTitle>
</CModalHeader>

<CModalBody>

{selected &&(

<div>

<p><b>Họ tên:</b> {selected.name}</p>
<p><b>Vai trò:</b> {selected.role}</p>
<p><b>Nhóm:</b> {selected.department}</p>
<p><b>Email:</b> {selected.email}</p>
<p><b>SĐT:</b> {selected.phone}</p>
<p><b>Trạng thái:</b> {selected.status}</p>

</div>

)}

</CModalBody>

</CModal>

{/* ADD / EDIT MODAL */}

<CModal
alignment="center"
backdrop="static"
visible={formVisible}
onClose={()=>setFormVisible(false)}
style={styles.modal}
>

<CModalHeader>

<CModalTitle>
{editUser?"Sửa nhân viên":"Thêm nhân viên"}
</CModalTitle>

</CModalHeader>

<CModalBody style={styles.modalBody}>

<CFormInput
label="Họ tên"
value={form.name}
onChange={e=>setForm({...form,name:e.target.value})}
/>

<CFormInput
label="Email"
value={form.email}
onChange={e=>setForm({...form,email:e.target.value})}
/>

<CFormInput
label="SĐT"
value={form.phone}
onChange={e=>setForm({...form,phone:e.target.value})}
/>

<CFormInput
label="Nhóm"
value={form.department}
onChange={e=>setForm({...form,department:e.target.value})}
/>

<CFormInput
label="Vai trò"
value={form.role}
onChange={e=>setForm({...form,role:e.target.value})}
/>

<select
value={form.status}
onChange={e=>setForm({...form,status:e.target.value})}
style={styles.select}
>
<option value="Active">Active</option>
<option value="Inactive">Inactive</option>
</select>

</CModalBody>

<CModalFooter>

<CButton
color="secondary"
onClick={()=>setFormVisible(false)}
>
Hủy
</CButton>

<CButton
color="primary"
onClick={saveUser}
>
Lưu
</CButton>

</CModalFooter>

</CModal>

</>

)

}