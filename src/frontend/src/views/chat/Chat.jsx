import React, { useState } from 'react'
import { CCard, CCardBody, CFormInput, CButton } from '@coreui/react'

const EMPLOYEES = [
  { id: 1, name: 'Nguyễn Văn A' },
  { id: 2, name: 'Trần Thị B' },
  { id: 3, name: 'Lê Văn C' },
  { id: 4, name: 'Phạm Thị D' },
]

export default function Chat() {
  const [keyword, setKeyword] = useState('')
  const [activeUser, setActiveUser] = useState(EMPLOYEES[1])
  const [messages, setMessages] = useState([
    { from: 'other', text: 'Chào bạn!' },
    { from: 'me', text: 'Chào anh, em cần hỗ trợ.' },
  ])
  const [input, setInput] = useState('')

  const filteredEmployees = EMPLOYEES.filter((e) =>
    e.name.toLowerCase().includes(keyword.toLowerCase()),
  )

  const sendMessage = () => {
    if (!input.trim()) return
    setMessages([...messages, { from: 'me', text: input }])
    setInput('')
  }

  return (
    <div
      style={{
        minHeight: 'calc(100vh - 140px)', // trừ header + breadcrumb CoreUI
        padding: '1.5rem',
        background: 'linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%)',
        display: 'flex',
        alignItems: 'stretch',
      }}
    >
      <CCard
        className="mx-auto w-100"
        style={{ maxWidth: 1100, borderRadius: 16, display: 'flex' }}
      >
        <CCardBody className="d-flex flex-column w-100">
          <div className="d-flex flex-grow-1" style={{ gap: 16 }}>
            {/* LEFT: Employee list */}
            <div
              style={{
                width: 260,
                borderRight: '1px solid #eee',
                paddingRight: 12,
                display: 'flex',
                flexDirection: 'column',
              }}
            >
              <h6 className="mb-2 text-primary">Nhân viên</h6>

              <CFormInput
                size="sm"
                placeholder="Tìm nhân viên..."
                className="mb-3"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />

              <div style={{ overflowY: 'auto', flexGrow: 1 }}>
                {filteredEmployees.map((e) => (
                  <div
                    key={e.id}
                    onClick={() => {
                      setActiveUser(e)
                      setMessages([{ from: 'other', text: `Chào, tôi là ${e.name}` }])
                    }}
                    style={{
                      padding: '8px 12px',
                      borderRadius: 8,
                      cursor: 'pointer',
                      marginBottom: 6,
                      background: activeUser.id === e.id ? '#7c6ef6' : '#f7f8fa',
                      color: activeUser.id === e.id ? '#fff' : '#333',
                    }}
                  >
                    {e.name}
                  </div>
                ))}
              </div>
            </div>

            {/* RIGHT: Chat box */}
            <div className="flex-grow-1 d-flex flex-column">
              <div className="mb-2 fw-semibold text-primary">
                Chat với: {activeUser.name}
              </div>

              <div
                className="flex-grow-1 p-3 mb-2"
                style={{
                  background: '#f8f9ff',
                  borderRadius: 12,
                  overflowY: 'auto',
                }}
              >
                {messages.map((m, i) => (
                  <div
                    key={i}
                    style={{
                      display: 'flex',
                      justifyContent: m.from === 'me' ? 'flex-end' : 'flex-start',
                      marginBottom: 8,
                    }}
                  >
                    <div
                      style={{
                        padding: '8px 12px',
                        borderRadius: 12,
                        maxWidth: '70%',
                        background: m.from === 'me' ? '#7c6ef6' : '#fff',
                        color: m.from === 'me' ? '#fff' : '#333',
                        boxShadow: '0 2px 6px rgba(0,0,0,0.05)',
                      }}
                    >
                      {m.text}
                    </div>
                  </div>
                ))}
              </div>

              <div className="d-flex gap-2">
                <CFormInput
                  placeholder="Nhập tin nhắn..."
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
                />
                <CButton color="primary" onClick={sendMessage}>
                  Gửi
                </CButton>
              </div>
            </div>
          </div>
        </CCardBody>
      </CCard>
    </div>
  )
}
