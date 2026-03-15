import React, { useState, useEffect } from "react"
import {
CRow,
CCol,
CCard,
CCardHeader,
CCardBody,
CButton,
CFormSelect,
CFormInput,
CTable
} from "@coreui/react"

import {
Chart as ChartJS,
CategoryScale,
LinearScale,
BarElement,
ArcElement,
Tooltip,
Legend
} from "chart.js"

import { Bar, Doughnut } from "react-chartjs-2"

import * as XLSX from "xlsx"
import { saveAs } from "file-saver"

ChartJS.register(
CategoryScale,
LinearScale,
BarElement,
ArcElement,
Tooltip,
Legend
)

/* ---------------- MOCK TECHNICIAN DATA ---------------- */

function generateTechnicians(){

let data=[]

for(let i=1;i<=200;i++){

let month=Math.floor(Math.random()*12)+1
let year=2026

data.push({
technician:"Technician "+i,
hours:Math.floor(Math.random()*160),
completedWO:Math.floor(Math.random()*40),
month:month,
year:year
})

}

return data
}

const technicians=generateTechnicians()

/* ---------------- ASSET DATA ---------------- */

const assets=[

{
assetId:"AST-001",
asset:"Compressor A1",
category:"HVAC",
location:"Plant 1",
manufacturer:"Atlas Copco",
installDate:"2022-03-10",
warranty:"2027-03-10",
status:"Running",
repairCount:6,
failures:5,
wo:12,
cost:12000
},

{
assetId:"AST-002",
asset:"Pump B2",
category:"Pump",
location:"Plant 2",
manufacturer:"Grundfos",
installDate:"2021-07-12",
warranty:"2026-07-12",
status:"Running",
repairCount:3,
failures:2,
wo:7,
cost:8000
},

{
assetId:"AST-003",
asset:"Motor C3",
category:"Motor",
location:"Plant 1",
manufacturer:"Siemens",
installDate:"2020-01-22",
warranty:"2025-01-22",
status:"Maintenance",
repairCount:5,
failures:4,
wo:9,
cost:5000
},

{
assetId:"AST-004",
asset:"Generator D4",
category:"Power",
location:"Plant 3",
manufacturer:"Cummins",
installDate:"2019-11-02",
warranty:"2024-11-02",
status:"Running",
repairCount:2,
failures:1,
wo:3,
cost:20000
}

]

export default function KPIReport(){

const [month,setMonth]=useState("")
const [year,setYear]=useState("")
const [searchTech,setSearchTech]=useState("")
const [searchAsset,setSearchAsset]=useState("")
const [page,setPage]=useState(1)

const pageSize=10

/* ---------------- YEAR LIST ---------------- */

const currentYear=new Date().getFullYear()

const years=[]

for(let y=2000;y<=currentYear;y++){
years.push(y)
}

/* ---------------- FILTER ---------------- */

const technicianFiltered=technicians.filter(t=>{

if(month && t.month!==parseInt(month)) return false
if(year && t.year!==parseInt(year)) return false

if(searchTech &&
!t.technician.toLowerCase().includes(searchTech.toLowerCase()))
return false

return true

})

const assetFiltered=assets.filter(a=>{

if(searchAsset &&
!a.asset.toLowerCase().includes(searchAsset.toLowerCase()))
return false

return true

})

/* ---------------- PAGINATION ---------------- */

const start=(page-1)*pageSize

const paginatedTech=technicianFiltered.slice(start,start+pageSize)

const totalPages=Math.ceil(technicianFiltered.length/pageSize)

useEffect(()=>{
setPage(1)
},[month,year])

/* ---------------- EXPORT EXCEL ---------------- */

const exportExcel=(data,name)=>{

const worksheet=XLSX.utils.json_to_sheet(data)

const workbook=XLSX.utils.book_new()

XLSX.utils.book_append_sheet(workbook,worksheet,"Report")

const buffer=XLSX.write(workbook,{bookType:"xlsx",type:"array"})

const file=new Blob([buffer],{type:"application/octet-stream"})

saveAs(file,name+".xlsx")

}

/* ---------------- CHART DATA ---------------- */

const chartHours={

labels:technicianFiltered.slice(0,10).map(t=>t.technician),

datasets:[
{
label:"Work Hours",
data:technicianFiltered.slice(0,10).map(t=>t.hours),
backgroundColor:"rgba(54,162,235,0.7)"
}
]

}

const assetFailureChart={

labels:assets.map(a=>a.asset),

datasets:[
{
label:"Failures",
data:assets.map(a=>a.failures),
backgroundColor:"#FF6384"
}
]

}

const woStatusChart={

labels:["Completed","In Progress","Open"],

datasets:[
{
data:[50,20,10],
backgroundColor:["#4CAF50","#2196F3","#FFC107"]
}
]

}

/* ---------------- UI ---------------- */

return(

<div className="report-container">

<CRow className="filter-bar">

<CCol md={2}>

<CFormSelect
value={month}
onChange={(e)=>setMonth(e.target.value)}
>

<option value="">Month</option>

{[...Array(12)].map((_,i)=>(
<option key={i+1} value={i+1}>
{String(i+1).padStart(2,"0")}
</option>
))}

</CFormSelect>

</CCol>

<CCol md={2}>

<CFormSelect
value={year}
onChange={(e)=>setYear(e.target.value)}
>

<option value="">Year</option>

{[...years].reverse().map((y)=>(
<option key={y} value={y}>
{y}
</option>
))}

</CFormSelect>

</CCol>

<CCol md={3}>

<CFormInput
placeholder="Search Technician..."
value={searchTech}
onChange={(e)=>setSearchTech(e.target.value)}
/>

</CCol>

<CCol md={3}>

<CFormInput
placeholder="Search Asset..."
value={searchAsset}
onChange={(e)=>setSearchAsset(e.target.value)}
/>

</CCol>

<CCol md={2}>

<CButton
color="success"
onClick={()=>exportExcel(technicianFiltered,"Technician_Report")}
>
Export Excel
</CButton>

</CCol>

</CRow>

{/* CHARTS */}

<CRow className="mt-3">

<CCol md={6}>

<CCard className="report-card">

<CCardHeader>Technician Work Hours</CCardHeader>

<CCardBody>

<Bar data={chartHours}/>

</CCardBody>

</CCard>

</CCol>

<CCol md={6}>

<CCard className="report-card">

<CCardHeader>Asset Failures</CCardHeader>

<CCardBody>

<Bar data={assetFailureChart}/>

</CCardBody>

</CCard>

</CCol>

</CRow>

<CRow className="mt-3">

<CCol md={4}>

<CCard className="report-card">

<CCardHeader>Work Order Status</CCardHeader>

<CCardBody>

<Doughnut data={woStatusChart}/>

</CCardBody>

</CCard>

</CCol>

</CRow>

{/* TECHNICIAN TABLE */}

<CRow className="mt-4">

<CCol md={12}>

<CCard className="report-card">

<CCardHeader className="card-flex">

Technician Reports

<CButton
size="sm"
color="success"
onClick={()=>exportExcel(technicianFiltered,"Technician_Report")}
>
Export Excel
</CButton>

</CCardHeader>

<CCardBody>

<CTable bordered hover>

<thead>

<tr>
<th>Technician</th>
<th>Month</th>
<th>Year</th>
<th>Total Hours</th>
<th>Completed WO</th>
</tr>

</thead>

<tbody>

{paginatedTech.map((t,i)=>(

<tr key={i}>
<td>{t.technician}</td>
<td>{t.month}</td>
<td>{t.year}</td>
<td>{t.hours}</td>
<td>{t.completedWO}</td>
</tr>

))}

</tbody>

</CTable>

<div className="pagination">

<CButton
size="sm"
disabled={page===1}
onClick={()=>setPage(page-1)}
>
Prev
</CButton>

<span>
Page {page} / {totalPages}
</span>

<CButton
size="sm"
disabled={page===totalPages}
onClick={()=>setPage(page+1)}
>
Next
</CButton>

</div>

</CCardBody>

</CCard>

</CCol>

</CRow>

{/* ASSET TABLE */}

<CRow className="mt-4">

<CCol md={12}>

<CCard className="report-card">

<CCardHeader className="card-flex">

Asset Reports

<CButton
size="sm"
color="success"
onClick={()=>exportExcel(assetFiltered,"Asset_Report")}
>
Export Excel
</CButton>

</CCardHeader>

<CCardBody>

<CTable bordered hover responsive>

<thead>

<tr>

<th>Asset ID</th>
<th>Asset</th>
<th>Category</th>
<th>Location</th>
<th>Manufacturer</th>
<th>Install Date</th>
<th>Warranty</th>
<th>Status</th>
<th>Repairs</th>
<th>Failures</th>
<th>WO</th>
<th>Cost ($)</th>

</tr>

</thead>

<tbody>

{assetFiltered.map((a,i)=>(

<tr key={i}>

<td>{a.assetId}</td>
<td>{a.asset}</td>
<td>{a.category}</td>
<td>{a.location}</td>
<td>{a.manufacturer}</td>
<td>{a.installDate}</td>
<td>{a.warranty}</td>

<td>

<span className={
a.status==="Running"
? "badge bg-success"
: "badge bg-warning"
}>
{a.status}
</span>

</td>

<td>{a.repairCount}</td>
<td>{a.failures}</td>
<td>{a.wo}</td>
<td>${a.cost}</td>

</tr>

))}

</tbody>

</CTable>

</CCardBody>

</CCard>

</CCol>

</CRow>

<style>{`

.report-container{
padding:20px;
background:#f4f6f9;
}

.filter-bar{
background:#fff;
padding:15px;
border-radius:8px;
margin-bottom:20px;
box-shadow:0 2px 6px rgba(0,0,0,0.08);
}

.report-card{
border-radius:10px;
box-shadow:0 2px 10px rgba(0,0,0,0.08);
}

.card-flex{
display:flex;
justify-content:space-between;
align-items:center;
}

thead{
background:#f7f9fc;
font-weight:600;
}

tbody tr:hover{
background:#f9fbff;
}

.pagination{
display:flex;
justify-content:center;
gap:10px;
margin-top:15px;
}

`}</style>

</div>

)

}