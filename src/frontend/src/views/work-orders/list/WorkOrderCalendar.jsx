import React, { useState } from "react"
import { CCard, CCardBody } from "@coreui/react"

const EVENTS = [
  { date: "2026-03-20", title: "Fix compressor", priority: "High" },
  { date: "2026-03-20", title: "Inspect chiller", priority: "Low" },
  { date: "2026-03-22", title: "Replace conveyor belt", priority: "Medium" }
]

export default function WorkOrderCalendar() {

  const [currentDate, setCurrentDate] = useState(new Date())

  const month = currentDate.getMonth()
  const year = currentDate.getFullYear()

  // FIX start Monday
  let firstDay = new Date(year, month, 1).getDay()
  firstDay = firstDay === 0 ? 6 : firstDay - 1

  const daysInMonth = new Date(year, month + 1, 0).getDate()

  const prevMonth = () =>
    setCurrentDate(new Date(year, month - 1, 1))

  const nextMonth = () =>
    setCurrentDate(new Date(year, month + 1, 1))

  const getEvents = (day) => {

    const dateStr =
      `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`

    return EVENTS.filter(e => e.date === dateStr)

  }

  const days = []

  for (let i = 0; i < firstDay; i++) {
    days.push(null)
  }

  for (let d = 1; d <= daysInMonth; d++) {
    days.push(d)
  }

  return (

    <div style={styles.page}>

      <h5 style={styles.title}>Maintenance Calendar</h5>

      <CCard style={styles.card}>
        <CCardBody>

          <div style={styles.header}>

            <button style={styles.navBtn} onClick={prevMonth}>
              ◀
            </button>

            <h5>
              Tháng {month + 1} / {year}
            </h5>

            <button style={styles.navBtn} onClick={nextMonth}>
              ▶
            </button>

          </div>

          <div style={styles.weekHeader}>

            <div>Mon</div>
            <div>Tue</div>
            <div>Wed</div>
            <div>Thu</div>
            <div>Fri</div>
            <div>Sat</div>
            <div>Sun</div>

          </div>

          <div style={styles.grid}>

            {days.map((day, i) => {

              const events = day ? getEvents(day) : []

              return (

                <div
                  key={i}
                  style={styles.cell}
                  onMouseEnter={(e)=>e.currentTarget.style.background="#f6f7ff"}
                  onMouseLeave={(e)=>e.currentTarget.style.background="#ffffff"}
                >

                  {day && (

                    <>
                      <div style={styles.dayNumber}>
                        {day}
                      </div>

                      {events.map((e, index) => (

                        <div
                          key={index}
                          style={{
                            ...styles.event,
                            background: priorityColor(e.priority)
                          }}
                        >
                          {e.title}
                        </div>

                      ))}

                    </>

                  )}

                </div>

              )

            })}

          </div>

        </CCardBody>
      </CCard>

    </div>

  )

}

/* PRIORITY COLOR */

const priorityColor = (p) =>
  p === "High"
    ? "#e55353"
    : p === "Medium"
    ? "#f9b115"
    : "#2eb85c"

/* STYLE */

const styles = {

  page:{
    padding:20,
    background:"#f3f4f7"
  },

  title:{
    fontWeight:600,
    marginBottom:16
  },

  card:{
    borderRadius:12,
    boxShadow:"0 4px 16px rgba(0,0,0,.05)",
    border:"none"
  },

  header:{
    display:"flex",
    justifyContent:"space-between",
    alignItems:"center",
    marginBottom:12
  },

  navBtn:{
    border:"none",
    background:"#321fdb",
    color:"#fff",
    padding:"6px 12px",
    borderRadius:6,
    cursor:"pointer",
    fontSize:14
  },

  weekHeader:{
    display:"grid",
    gridTemplateColumns:"repeat(7,1fr)",
    textAlign:"center",
    fontWeight:600,
    color:"#6c757d",
    marginBottom:6
  },

  grid:{
    display:"grid",
    gridTemplateColumns:"repeat(7,1fr)",
    gap:8
  },

  cell:{
    minHeight:90,
    background:"#ffffff",
    borderRadius:10,
    padding:6,
    border:"1px solid #eef0f3",
    transition:"all .15s ease"
  },

  dayNumber:{
    fontWeight:600,
    fontSize:14,
    marginBottom:4
  },

  event:{
    color:"#fff",
    fontSize:11,
    padding:"3px 6px",
    borderRadius:5,
    marginBottom:3,
    whiteSpace:"nowrap",
    overflow:"hidden",
    textOverflow:"ellipsis"
  }

}