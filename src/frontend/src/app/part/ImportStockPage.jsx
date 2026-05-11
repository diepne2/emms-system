import { useEffect, useMemo, useState } from 'react'
import { getErrorMessage, inventoryApi } from './inventoryApi'
import './inventory.css'

export default function ImportStockPage() {
  const [parts, setParts] = useState([])
  const [form, setForm] = useState({
    partId: '',
    quantity: '',
    note: '',
  })

  const [loading, setLoading] = useState(false)
  const [partsLoading, setPartsLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const selectedPart = useMemo(
    () => parts.find((p) => String(p.id) === String(form.partId)),
    [parts, form.partId],
  )

  const normalizeList = (raw) => {
    if (Array.isArray(raw)) return raw
    if (Array.isArray(raw?.data)) return raw.data
    if (Array.isArray(raw?.content)) return raw.content
    return []
  }

  const loadParts = async () => {
    setPartsLoading(true)
    setError('')

    try {
      const res = await inventoryApi.get('/parts')
      setParts(normalizeList(res.data))
    } catch (err) {
      setParts([])
      setError(getErrorMessage(err))
    } finally {
      setPartsLoading(false)
    }
  }

  useEffect(() => {
    loadParts()
  }, [])

  const submit = async (e) => {
    e.preventDefault()
    setMessage('')
    setError('')

    if (!form.partId) {
      setError('Vui lòng chọn vật tư.')
      return
    }

    if (!form.quantity || Number(form.quantity) <= 0) {
      setError('Số lượng nhập phải lớn hơn 0.')
      return
    }

    setLoading(true)

    try {
      await inventoryApi.put(`/parts/${form.partId}/import-stock`, {
        quantity: Number(form.quantity),
        note: form.note,
      })

      setMessage('Nhập kho thành công.')
      setForm({
        partId: '',
        quantity: '',
        note: '',
      })

      await loadParts()
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
        <h2>Nhập kho vật tư</h2>
        <p>Ghi nhận số lượng vật tư nhập kho và cập nhật tồn kho hệ thống.</p>
      </div>

      {message && <div className="inventory-alert success">{message}</div>}
      {error && <div className="inventory-alert error">{error}</div>}

      <div className="inventory-card">
        <h3>Thông tin nhập kho</h3>

        <form className="inventory-form" onSubmit={submit}>
          <div className="inventory-grid">
            <label>
              <span className="inventory-label-text">
                Vật tư <span className="inventory-required">*</span>
              </span>

              <select
                value={form.partId}
                disabled={partsLoading}
                onChange={(e) =>
                  setForm({
                    ...form,
                    partId: e.target.value,
                  })
                }
              >
                <option value="">
                  {partsLoading ? 'Đang tải vật tư...' : '-- Chọn vật tư --'}
                </option>

                {parts.map((part) => (
                  <option key={part.id} value={part.id}>
                    {part.name} — Tồn: {part.quantity ?? 0}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span className="inventory-label-text">
                Số lượng nhập <span className="inventory-required">*</span>
              </span>

              <input
                type="number"
                min="1"
                value={form.quantity}
                onChange={(e) =>
                  setForm({
                    ...form,
                    quantity: e.target.value,
                  })
                }
                placeholder="Nhập số lượng"
              />
            </label>
          </div>

          {selectedPart && (
            <div className="inventory-meta">
              <span>Tồn hiện tại: {selectedPart.quantity ?? 0}</span>
              <span>Danh mục: {selectedPart.category || 'N/A'}</span>
              <span>Nhà cung cấp: {selectedPart.vendor || 'N/A'}</span>
              <span>Mã vật tư: {selectedPart.partNumber || 'N/A'}</span>
            </div>
          )}

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

          <div className="inventory-actions">
            <button type="submit" disabled={loading || partsLoading}>
              {loading ? 'Đang xử lý...' : 'Xác nhận nhập kho'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}