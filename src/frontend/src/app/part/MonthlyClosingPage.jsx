import { useState } from 'react'
import { getErrorMessage, inventoryApi } from './inventoryApi'
import './inventory.css'

export default function MonthlyClosingPage() {
  const [form, setForm] = useState({
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    note: '',
  })

  const [closingResult, setClosingResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [reopening, setReopening] = useState(false)
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

    if (Number(form.month) < 1 || Number(form.month) > 12) {
      setError('Tháng phải nằm trong khoảng 1 đến 12.')
      return
    }

    if (
      !window.confirm(
        `Bạn có chắc muốn chốt sổ kho tháng ${form.month}/${form.year} không?`,
      )
    ) {
      return
    }

    setLoading(true)

    try {
      const res = await inventoryApi.post('/parts/monthly-closing', null, {
        params: {
          year: Number(form.year),
          month: Number(form.month),
          note: form.note || undefined,
        },
      })

      setClosingResult(res.data)
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

  const reopenClosing = async () => {
    setMessage('')
    setError('')

    if (!closingResult?.id) {
      setError('Không tìm thấy kỳ chốt kho để mở lại.')
      return
    }

    if (
      !window.confirm(
        `Bạn có chắc muốn mở lại kỳ kho ${closingResult.month}/${closingResult.year} không?`,
      )
    ) {
      return
    }

    setReopening(true)

    try {
      const res = await inventoryApi.put(
        `/parts/monthly-closing/${closingResult.id}/reopen`,
      )

      setClosingResult(res.data)
      setMessage('Mở lại kỳ kho thành công.')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setReopening(false)
    }
  }

  const formatDateTime = (value) => {
    if (!value) return '-'

    try {
      return new Date(value).toLocaleString('vi-VN')
    } catch {
      return value
    }
  }

  return (
    <div className="inventory-page">
      <div className="inventory-hero">
        <span className="inventory-badge">Kho vật tư</span>
        <h2>Chốt sổ kho</h2>
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

      {closingResult && (
        <div className="inventory-card">
          <h3>Kỳ kho vừa xử lý</h3>

          <div className="inventory-meta">
            <span>
              Kỳ kho: {closingResult.month}/{closingResult.year}
            </span>
            <span>Trạng thái: {closingResult.status}</span>
            <span>Thời gian chốt: {formatDateTime(closingResult.closedAt)}</span>
          </div>

          {closingResult.note && (
            <div className="inventory-note">
              Ghi chú: {closingResult.note}
            </div>
          )}

          {closingResult.status === 'CLOSED' && (
            <div className="inventory-actions">
              <button
                type="button"
                className="inventory-danger-btn"
                onClick={reopenClosing}
                disabled={reopening}
              >
                {reopening ? 'Đang mở lại...' : 'Mở lại kỳ kho'}
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}