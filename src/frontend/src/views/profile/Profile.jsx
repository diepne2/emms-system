import React, { useState } from 'react'

const Profile = () => {
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState({
    fullName: 'Diệp',
    email: 'admin@emms.vn',
    phone: '0901234567',
    department: 'Phòng Kỹ thuật',
    position: 'Nhân viên vận hành',
  })

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  return (
    <>
      <style>{`
        .page { padding: 24px; max-width: 800px; }
        .title { font-size: 24px; font-weight: 600; margin-bottom: 20px; }

        .card {
          background: #fff;
          border-radius: 12px;
          padding: 24px;
          box-shadow: 0 4px 12px rgba(0,0,0,.06);
        }

        .row {
          display: grid;
          grid-template-columns: 160px 1fr;
          gap: 12px;
          margin-bottom: 14px;
          align-items: center;
        }

        .label { color: #6b7280; font-weight: 500; }

        .value {
          padding: 8px 10px;
          background: #f9fafb;
          border-radius: 6px;
        }

        .input {
          width: 100%;
          height: 36px;
          padding: 0 10px;
          border-radius: 6px;
          border: 1px solid #d0d7de;
          outline: none;
        }

        .actions {
          display: flex;
          gap: 12px;
          margin-top: 20px;
        }

        .btn-primary {
          background: #6f42c1;
          color: #fff;
          border: none;
          border-radius: 6px;
          height: 36px;
          padding: 0 16px;
          cursor: pointer;
        }

        .btn-secondary {
          background: #f3f4f6;
          border: 1px solid #e5e7eb;
          border-radius: 6px;
          height: 36px;
          padding: 0 16px;
          cursor: pointer;
        }
      `}</style>

      <div className="page">
        <div className="title">Thông tin cá nhân</div>

        <div className="card">
          <div className="row">
            <div className="label">Họ và tên</div>
            {editing ? (
              <input className="input" name="fullName" value={form.fullName} onChange={onChange} />
            ) : (
              <div className="value">{form.fullName}</div>
            )}
          </div>

          <div className="row">
            <div className="label">Email</div>
            {editing ? (
              <input className="input" name="email" value={form.email} onChange={onChange} />
            ) : (
              <div className="value">{form.email}</div>
            )}
          </div>

          <div className="row">
            <div className="label">Số điện thoại</div>
            {editing ? (
              <input className="input" name="phone" value={form.phone} onChange={onChange} />
            ) : (
              <div className="value">{form.phone}</div>
            )}
          </div>

          <div className="row">
            <div className="label">Phòng ban</div>
            {editing ? (
              <input className="input" name="department" value={form.department} onChange={onChange} />
            ) : (
              <div className="value">{form.department}</div>
            )}
          </div>

          <div className="row">
            <div className="label">Chức vụ</div>
            {editing ? (
              <input className="input" name="position" value={form.position} onChange={onChange} />
            ) : (
              <div className="value">{form.position}</div>
            )}
          </div>

          <div className="actions">
            {!editing && (
              <button className="btn-primary" onClick={() => setEditing(true)}>
                Chỉnh sửa
              </button>
            )}

            {editing && (
              <>
                <button className="btn-primary" onClick={() => setEditing(false)}>
                  Lưu
                </button>
                <button className="btn-secondary" onClick={() => setEditing(false)}>
                  Hủy
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </>
  )
}

export default Profile
