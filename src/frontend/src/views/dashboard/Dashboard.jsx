import React from "react"
import {
CCard,
CCardHeader,
CCardBody,
CRow,
CCol,
CButton
} from "@coreui/react"

import {
Chart as ChartJS,
CategoryScale,
LinearScale,
BarElement,
LineElement,
PointElement,
ArcElement,
Tooltip,
Legend
} from "chart.js"

import { Bar, Line, Pie } from "react-chartjs-2"

import * as XLSX from "xlsx"
import { saveAs } from "file-saver"

ChartJS.register(
CategoryScale,
LinearScale,
BarElement,
LineElement,
PointElement,
ArcElement,
Tooltip,
Legend
)

export default function Dashboard(){

/* ================= KPI ================= */

const kpi = {
devices:120,
workorders:85,
progress:12,
completion:"90%",
mttr:4.2,
mtbf:36
}

/* ================= MTTR ================= */

const mttrData={
labels:["Jan","Feb","Mar","Apr","May"],
datasets:[{
label:"MTTR (hrs)",
data:[5.2,4.8,4.3,4.0,4.2],
backgroundColor:[
"#3b82f6",
"#6366f1",
"#8b5cf6",
"#a855f7",
"#ec4899"
]
}]
}

/* ================= MTBF ================= */

const mtbfData={
labels:["Jan","Feb","Mar","Apr","May"],
datasets:[{
label:"MTBF (days)",
data:[30,32,35,36,38],
borderColor:"#10b981",
backgroundColor:"rgba(16,185,129,0.2)",
tension:0.4,
fill:true
}]
}

/* ================= WORK ORDER STATUS ================= */

const statusData={
labels:["Completed","In Progress","Pending","Cancelled"],
datasets:[{
data:[65,20,10,5],
backgroundColor:[
"#22c55e",
"#3b82f6",
"#f59e0b",
"#ef4444"
]
}]
}

/* ================= MAINTENANCE TREND ================= */

const maintenanceTrend={
labels:["Jan","Feb","Mar","Apr","May","Jun"],
datasets:[{
label:"Maintenance Orders",
data:[12,19,15,22,18,25],
borderColor:"#6366f1",
backgroundColor:"rgba(99,102,241,0.2)",
tension:0.4,
fill:true
}]
}

/* ================= TOP DEVICES ================= */

const topDevices={
labels:["Machine A","Machine B","Machine C","Machine D"],
datasets:[{
label:"Failures",
data:[12,9,7,5],
backgroundColor:[
"#ef4444",
"#f97316",
"#f59e0b",
"#84cc16"
]
}]
}

/* ================= TOP TECHNICIANS ================= */

const topTech={
labels:["John","David","Anna","Mike"],
datasets:[{
label:"Work Orders",
data:[28,24,19,15],
backgroundColor:[
"#3b82f6",
"#6366f1",
"#8b5cf6",
"#ec4899"
]
}]
}

/* ================= EXPORT EXCEL ================= */

const exportExcel = () => {

const data = [

{Metric:"Total Devices",Value:kpi.devices},
{Metric:"Work Orders",Value:kpi.workorders},
{Metric:"In Progress",Value:kpi.progress},
{Metric:"Completion Rate",Value:kpi.completion},
{Metric:"MTTR (hrs)",Value:kpi.mttr},
{Metric:"MTBF (days)",Value:kpi.mtbf}

]

const worksheet = XLSX.utils.json_to_sheet(data)

const workbook = XLSX.utils.book_new()

XLSX.utils.book_append_sheet(workbook, worksheet, "KPI Report")

const excelBuffer = XLSX.write(workbook,{
bookType:"xlsx",
type:"array"
})

const fileData = new Blob([excelBuffer],{
type:"application/octet-stream"
})

saveAs(fileData,"KPI_Report.xlsx")

}

/* ================= UI ================= */

return(

<div>

{/* EXPORT BUTTON */}

<div className="d-flex justify-content-end mb-3">

<CButton color="success" onClick={exportExcel}>
Export Excel
</CButton>

</div>

{/* KPI CARDS */}

<CRow className="mb-4">

<CCol md={3}>
<CCard>
<CCardBody className="text-center">
<h3>{kpi.devices}</h3>
<p>Total Devices</p>
</CCardBody>
</CCard>
</CCol>

<CCol md={3}>
<CCard>
<CCardBody className="text-center">
<h3>{kpi.workorders}</h3>
<p>Work Orders</p>
</CCardBody>
</CCard>
</CCol>

<CCol md={3}>
<CCard>
<CCardBody className="text-center">
<h3>{kpi.progress}</h3>
<p>In Progress</p>
</CCardBody>
</CCard>
</CCol>

<CCol md={3}>
<CCard>
<CCardBody className="text-center">
<h3>{kpi.completion}</h3>
<p>Completion Rate</p>
</CCardBody>
</CCard>
</CCol>

</CRow>

{/* MTTR + MTBF */}

<CRow className="mb-4">

<CCol md={6}>

<CCard>

<CCardHeader>MTTR Trend</CCardHeader>

<CCardBody>

<Bar data={mttrData}/>

</CCardBody>

</CCard>

</CCol>

<CCol md={6}>

<CCard>

<CCardHeader>MTBF Trend</CCardHeader>

<CCardBody>

<Line data={mtbfData}/>

</CCardBody>

</CCard>

</CCol>

</CRow>

{/* STATUS + TREND */}

<CRow className="mb-4">

<CCol md={6}>

<CCard>

<CCardHeader>Work Order Status</CCardHeader>

<CCardBody>

<Pie data={statusData}/>

</CCardBody>

</CCard>

</CCol>

<CCol md={6}>

<CCard>

<CCardHeader>Maintenance Trend</CCardHeader>

<CCardBody>

<Line data={maintenanceTrend}/>

</CCardBody>

</CCard>

</CCol>

</CRow>

{/* TOP DEVICES + TECH */}

<CRow>

<CCol md={6}>

<CCard>

<CCardHeader>Top Failure Devices</CCardHeader>

<CCardBody>

<Bar data={topDevices}/>

</CCardBody>

</CCard>

</CCol>

<CCol md={6}>

<CCard>

<CCardHeader>Top Technicians</CCardHeader>

<CCardBody>

<Bar data={topTech}/>

</CCardBody>

</CCard>

</CCol>

</CRow>

</div>

)

}