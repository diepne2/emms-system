import React from "react"

const PageHeader = ({ title, subtitle, actions }) => {
  return (
    <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
      <div>
        <h4 className="mb-0 fw-bold" style={{ color: "#1b3c66" }}>
          {title}
        </h4>
        {subtitle && <span className="text-muted small">{subtitle}</span>}
      </div>
      <div className="d-flex gap-2">{actions}</div>
    </div>
  )
}

export default PageHeader