import React, { useState } from "react"
import FullCalendar from "@fullcalendar/react"
import dayGridPlugin from "@fullcalendar/daygrid"
import timeGridPlugin from "@fullcalendar/timegrid"
import interactionPlugin from "@fullcalendar/interaction"

import {
  CCard,
  CCardHeader,
  CCardBody,
  CButton,
  CFormInput,
  CRow,
  CCol,
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody
} from "@coreui/react"

const initialEvents = [
  {
    id: "WO-001",
    title: "Chiller A1",
    start: "2025-12-10",
    technician: "Nguyễn Văn A",
    recurring: "Monthly",
    color: "#ff6b6b"
  },
  {
    id: "WO-002",
    title: "Boiler B1",
    start: "2025-12-15",
    technician: "Trần Văn B",
    recurring: "None",
    color: "#ffd43b"
  },
  {
    id: "WO-003",
    title: "Cooling Tower",
    start: "2025-12-20",
    technician: "Lê Văn C",
    recurring: "Weekly",
    color: "#51cf66"
  }
]

const styles = {
  card: {
    borderRadius: "12px",
    boxShadow: "0 6px 18px rgba(0,0,0,0.08)",
    border: "none"
  },

  header: {
    background: "#3c4b64",
    color: "white",
    fontWeight: "600",
    fontSize: "16px"
  },

  searchInput: {
    height: "40px",
    borderRadius: "8px",
    border: "1px solid #d8dbe0",
    background: "#fafbff"
  },

  searchButton: {
    width: "100%",
    height: "40px",
    borderRadius: "8px",
    border: "none",
    fontWeight: "600",
    background: "#4f6bed",
    color: "white"
  }
}

export default function MaintenanceCalendar() {

  const [events, setEvents] = useState(initialEvents)
  const [search, setSearch] = useState("")
  const [selectedEvent, setSelectedEvent] = useState(null)

  const filteredEvents = events.filter(
    e =>
      e.title.toLowerCase().includes(search.toLowerCase()) ||
      e.technician.toLowerCase().includes(search.toLowerCase())
  )

  const handleEventDrop = (info) => {

    const updated = events.map(e =>
      e.id === info.event.id
        ? { ...e, start: info.event.startStr }
        : e
    )

    setEvents(updated)
  }

  const handleEventClick = (info) => {

    const event = events.find(e => e.id === info.event.id)
    setSelectedEvent(event)
  }

  return (

    <CCard style={styles.card}>

      <CCardHeader style={styles.header}>
        📅 Lịch bảo trì
      </CCardHeader>

      <CCardBody>

        {/* SEARCH */}

        <CRow className="mb-3">

          <CCol md={10}>
            <CFormInput
              placeholder="Tìm kiếm"
              value={search}
              onChange={(e)=>setSearch(e.target.value)}
              style={styles.searchInput}
            />
          </CCol>

          <CCol md={2}>
            <CButton style={styles.searchButton}>
              Tìm kiếm
            </CButton>
          </CCol>

        </CRow>

        {/* CALENDAR */}

        <FullCalendar
          plugins={[dayGridPlugin,timeGridPlugin,interactionPlugin]}
          initialView="dayGridMonth"
          editable
          events={filteredEvents}
          eventDrop={handleEventDrop}
          eventClick={handleEventClick}
          height="600px"
          headerToolbar={{
            left:"prev,next today",
            center:"title",
            right:"dayGridMonth,timeGridWeek,timeGridDay"
          }}
        />

      </CCardBody>

      {/* DETAIL MODAL */}

      <CModal
        visible={selectedEvent !== null}
        onClose={()=>setSelectedEvent(null)}
      >

        <CModalHeader>
          <CModalTitle>Chi tiết bảo trì</CModalTitle>
        </CModalHeader>

        <CModalBody>

          {selectedEvent && (

            <>
              <p><b>Thiết bị:</b> {selectedEvent.title}</p>

              <p>
                <b>Work Order:</b>{" "}
                <a
                  href={`/work-order/${selectedEvent.id}`}
                  style={{
                    color:"#4f6bed",
                    fontWeight:"600",
                    textDecoration:"none"
                  }}
                >
                  {selectedEvent.id}
                </a>
              </p>

              <p><b>Technician:</b> {selectedEvent.technician}</p>
              <p><b>Recurring:</b> {selectedEvent.recurring}</p>
              <p><b>Ngày bảo trì:</b> {selectedEvent.start}</p>

            </>

          )}

        </CModalBody>

      </CModal>

      {/* CALENDAR STYLE */}

      <style>{`

.fc .fc-toolbar-title{
font-size:22px;
font-weight:600;
color:#3c4b64;
}

.fc-button{
background:#4f6bed !important;
border:none !important;
padding:6px 12px !important;
}

.fc-button:hover{
background:#3d57d6 !important;
}

.fc-daygrid-day:hover{
background:#f7f8ff;
cursor:pointer;
}

.fc-col-header-cell{
background:#f3f5fb;
font-weight:600;
}

.fc-day-today{
background:#eef2ff !important;
}

.fc-event{
border:none !important;
border-radius:4px;
font-size:12px;
}

`}</style>

    </CCard>
  )
}