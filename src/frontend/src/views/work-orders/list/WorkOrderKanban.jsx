import React, { useState } from "react"
import { CCard, CCardHeader, CCardBody, CBadge } from "@coreui/react"

const INIT_DATA = {
  "To Do": [
    {
      id: "WO-001",
      title: "Fix air compressor",
      asset: "Compressor A1",
      priority: "High",
      assignee: "A",
      start: "18 Mar",
      due: "20 Mar",
      hours: 4
    }
  ],

  "In Progress": [
    {
      id: "WO-003",
      title: "Check cooling system",
      asset: "Chiller C1",
      priority: "Low",
      assignee: "C",
      start: "17 Mar",
      due: "19 Mar",
      hours: 2
    }
  ],

  "Done": [
    {
      id: "WO-002",
      title: "Replace belt conveyor",
      asset: "Conveyor B2",
      priority: "Medium",
      assignee: "B",
      start: "15 Mar",
      due: "22 Mar",
      hours: 6
    }
  ]
}

export default function WorkOrderKanban() {

  const [boards, setBoards] = useState(INIT_DATA)
  const [dragItem, setDragItem] = useState(null)

  const handleDragStart = (item, status) => {
    setDragItem({ item, status })
  }

  const handleDrop = (targetStatus) => {

    if (!dragItem) return

    const { item, status } = dragItem

    if (status === targetStatus) return

    setBoards(prev => {

      const newSource = prev[status].filter(i => i.id !== item.id)

      const newTarget = [...prev[targetStatus], item]

      return {
        ...prev,
        [status]: newSource,
        [targetStatus]: newTarget
      }

    })

    setDragItem(null)

  }

  return (

    <div style={styles.page}>

      <h5 style={styles.title}>Work Orders – Kanban</h5>

      <div style={styles.board}>

        {Object.entries(boards).map(([status, items]) => (

          <CCard
            key={status}
            style={{
              ...styles.column,
              borderTop: `4px solid ${columnColor(status)}`,
              background: columnBg(status)
            }}
            onDragOver={(e) => e.preventDefault()}
            onDrop={() => handleDrop(status)}
          >

            <CCardHeader style={styles.header}>

              {status}

              <CBadge color={badgeColor(status)}>
                {items.length}
              </CBadge>

            </CCardHeader>

            <CCardBody>

              {items.map((item) => (

                <div
                  key={item.id}
                  draggable
                  onDragStart={() => handleDragStart(item, status)}
                  style={{
                    ...styles.card,
                    borderLeft: `5px solid ${priorityColor(item.priority)}`
                  }}
                >

                  <div style={styles.ticketId}>
                    {item.id}
                  </div>

                  <div style={styles.cardTitle}>
                    {item.title}
                  </div>

                  <div style={styles.asset}>
                    {item.asset}
                  </div>

                  <div style={styles.row}>
                    Assignee: {item.assignee}
                    <span>{item.hours}h</span>
                  </div>

                  <div style={styles.row}>
                    {item.start} → {item.due}
                  </div>

                </div>

              ))}

            </CCardBody>

          </CCard>

        ))}

      </div>

    </div>

  )
}

/* STATUS COLORS */

const badgeColor = (s) =>
  s === "To Do"
    ? "primary"
    : s === "In Progress"
    ? "warning"
    : "success"

const columnColor = (s) =>
  s === "To Do"
    ? "#321fdb"
    : s === "In Progress"
    ? "#f9b115"
    : "#2eb85c"

const columnBg = (s) =>
  s === "To Do"
    ? "#eef2ff"
    : s === "In Progress"
    ? "#fff9e6"
    : "#e8f7ee"

/* PRIORITY */

const priorityColor = (p) =>
  p === "High"
    ? "#e55353"
    : p === "Medium"
    ? "#f9b115"
    : "#2eb85c"

/* STYLE */

const styles = {

  page:{
    padding:24
  },

  title:{
    fontWeight:600,
    marginBottom:18
  },

  board:{
    display:"flex",
    gap:20
  },

  column:{
    flex:1,
    minHeight:420,
    borderRadius:10,
    boxShadow:"0 6px 20px rgba(0,0,0,.05)"
  },

  header:{
    display:"flex",
    justifyContent:"space-between",
    fontWeight:600
  },

  card:{
    padding:12,
    borderRadius:8,
    background:"#fff",
    marginBottom:12,
    boxShadow:"0 2px 6px rgba(0,0,0,.08)",
    cursor:"grab"
  },

  ticketId:{
    fontWeight:600,
    fontSize:13
  },

  cardTitle:{
    fontWeight:500,
    marginTop:4
  },

  asset:{
    color:"#6c757d",
    fontSize:12,
    marginBottom:6
  },

  row:{
    fontSize:12,
    color:"#6c757d",
    display:"flex",
    justifyContent:"space-between"
  }

}