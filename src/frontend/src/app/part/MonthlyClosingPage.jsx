import { useState } from 'react'
import { getErrorMessage, inventoryApi } from './inventoryApi'
import './inventory.css'

export default function MonthlyClosingPage() {
  const [form, setForm] = useState({
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    note: '',
  })

  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setMessage('')
    setError('')

    if (!form.year || !form.month) {
      setError('Vui lòng nhập đầy đủ năm và tháng.')
      return
    }

    if (form.month < 1 || form.month > 12) {
      setError('Tháng phải nằm trong khoảng 1 đến 12.')
      return
    }

    setLoading(true)

    try {
      await inventoryApi.post('/parts/monthly-closing', null, {
        params: {
          year: form.year,
          month: form.month,
          note: form.note,
        },
      })

      setMessage('Chốt sổ kho tháng thành công.')
      setForm({
        year: new Date().getFullYear(),
        month: new Date().getMonth() + 1,
        note: '',
      })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="inventory-page">
      <div className="inventory-hero">
        <span className="inventory-badge">Kho vật tư</span>
        <h2>Chốt sổ kho</h2>
        <p>
          Chốt kỳ kho theo tháng sau khi phiếu kiểm kê đã được duyệt.
        </p>
      </div>

      {message && <div className="inventory-alert success">{message}</div>}
      {error && <div className="inventory-alert error">{error}</div>}

      <div className="inventory-card">
        <h3>Thông tin chốt sổ</h3>

        <form className="inventory-form" onSubmit={submit}>
          <div className="inventory-grid">
            <label>
              <span className="inventory-label-text">
                Năm <span className="inventory-required">*</span>
              </span>

              <input
                type="number"
                value={form.year}
                onChange={(e) =>
                  setForm({
                    ...form,
                    year: Number(e.target.value),
                  })
                }
              />
            </label>

            <label>
              <span className="inventory-label-text">
                Tháng <span className="inventory-required">*</span>
              </span>

              <input
                type="number"
                min="1"
                max="12"
                value={form.month}
                onChange={(e) =>
                  setForm({
                    ...form,
                    month: Number(e.target.value),
                  })
                }
              />
            </label>
          </div>

          <label>
            <span className="inventory-label-text">Ghi chú</span>

            <textarea
              value={form.note}
              onChange={(e) =>
                setForm({
                  ...form,
                  note: e.target.value,
                })
              }
              placeholder="Nhập ghi chú"
            />
          </label>

          <div className="inventory-note">
            Chỉ có thể chốt sổ khi kỳ kho đã có phiếu kiểm kê được duyệt.
          </div>

          <div className="inventory-actions">
            <button type="submit" disabled={loading}>
              {loading ? 'Đang chốt...' : 'Chốt sổ kho'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}