import { useEffect, useMemo, useState } from 'react'
import { getErrorMessage, inventoryApi } from './inventoryApi'
import './inventory.css'

export default function InventoryCountPage() {
  const [parts, setParts] = useState([])
  const [createdCount, setCreatedCount] = useState(null)

  const [countForm, setCountForm] = useState({
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    note: '',
  })

  const [itemForm, setItemForm] = useState({
    partId: '',
    actualQuantity: '',
    note: '',
  })

  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const normalizeList = (raw) => {
    if (Array.isArray(raw)) return raw
    if (Array.isArray(raw?.data)) return raw.data
    if (Array.isArray(raw?.content)) return raw.content
    return []
  }

  const selectedPart = useMemo(
    () => parts.find((p) => String(p.id) === String(itemForm.partId)),
    [parts, itemForm.partId],
  )

  const loadParts = async () => {
    const res = await inventoryApi.get('/parts')
    setParts(normalizeList(res.data))
  }

  useEffect(() => {
    loadParts().catch((err) => setError(getErrorMessage(err)))
  }, [])

  const createCount = async (e) => {
    e.preventDefault()
    setMessage('')
    setError('')

    if (!countForm.year || !countForm.month) {
      setError('Vui lòng nhập đầy đủ năm và tháng.')
      return
    }

    if (countForm.month < 1 || countForm.month > 12) {
      setError('Tháng phải nằm trong khoảng 1 đến 12.')
      return
    }

    setLoading(true)

    try {
      const res = await inventoryApi.post('/parts/inventory-counts', null, {
        params: {
          year: countForm.year,
          month: countForm.month,
          note: countForm.note,
        },
      })

      setCreatedCount(res.data)
      setMessage('Tạo phiếu kiểm kê thành công.')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const addItem = async (e) => {
    e.preventDefault()
    setMessage('')
    setError('')

    if (!createdCount?.id) {
      setError('Vui lòng tạo phiếu kiểm kê trước.')
      return
    }

    if (!itemForm.partId || itemForm.actualQuantity === '') {
      setError('Vui lòng chọn vật tư và nhập số lượng thực tế.')
      return
    }

    if (Number(itemForm.actualQuantity) < 0) {
      setError('Số lượng thực tế không được âm.')
      return
    }

    setLoading(true)

    try {
      await inventoryApi.post(`/parts/inventory-counts/${createdCount.id}/items`, {
        partId: Number(itemForm.partId),
        actualQuantity: Number(itemForm.actualQuantity),
        note: itemForm.note,
      })

      setMessage('Thêm dòng kiểm kê thành công.')
      setItemForm({ partId: '', actualQuantity: '', note: '' })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const confirmCount = async () => {
    setMessage('')
    setError('')

    if (!createdCount?.id) {
      setError('Chưa có phiếu kiểm kê để duyệt.')
      return
    }

    setLoading(true)

    try {
      const res = await inventoryApi.put(`/parts/inventory-counts/${createdCount.id}/confirm`)
      setCreatedCount(res.data)
      setMessage('Duyệt kiểm kê thành công. Tồn kho đã được điều chỉnh.')
      await loadParts()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const diffQuantity =
    selectedPart && itemForm.actualQuantity !== ''
      ? Number(itemForm.actualQuantity) - Number(selectedPart.quantity ?? 0)
      : null

  return (
    <div className="inventory-page">
      <div className="inventory-hero">
        <span className="inventory-badge">Kho vật tư</span>
        <h2>Kiểm kê kho</h2>
        <p>
          So sánh tồn kho hệ thống với số lượng thực tế và điều chỉnh tồn kho
          khi phiếu kiểm kê được duyệt.
        </p>
      </div>

      {message && <div className="inventory-alert success">{message}</div>}
      {error && <div className="inventory-alert error">{error}</div>}

      <div className="inventory-card">
        <h3>Tạo phiếu kiểm kê</h3>

        <form className="inventory-form" onSubmit={createCount}>
          <div className="inventory-grid">
            <label>
              <span className="inventory-label-text">
                Năm <span className="inventory-required">*</span>
              </span>

              <input
                type="number"
                value={countForm.year}
                onChange={(e) =>
                  setCountForm({
                    ...countForm,
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
                value={countForm.month}
                onChange={(e) =>
                  setCountForm({
                    ...countForm,
                    month: Number(e.target.value),
                  })
                }
              />
            </label>
          </div>

          <label>
            <span className="inventory-label-text">Ghi chú</span>

            <textarea
              value={countForm.note}
              onChange={(e) =>
                setCountForm({
                  ...countForm,
                  note: e.target.value,
                })
              }
              placeholder="Ví dụ: Kiểm kê cuối tháng"
            />
          </label>

          <div className="inventory-actions">
            <button type="submit" disabled={loading}>
              {loading ? 'Đang tạo...' : 'Tạo phiếu kiểm kê'}
            </button>
          </div>
        </form>
      </div>

      {createdCount && (
        <div className="inventory-card">
          <div className="inventory-count-header">
            <div>
              <strong>{createdCount.code}</strong>
              <p>
                Kỳ kho: {createdCount.month}/{createdCount.year} — Trạng thái:{' '}
                {createdCount.status}
              </p>
            </div>

            <button type="button" onClick={confirmCount} disabled={loading}>
              {loading ? 'Đang duyệt...' : 'Duyệt kiểm kê'}
            </button>
          </div>

          <form className="inventory-form" onSubmit={addItem}>
            <div className="inventory-grid">
              <label>
                <span className="inventory-label-text">
                  Vật tư <span className="inventory-required">*</span>
                </span>

                <select
                  value={itemForm.partId}
                  onChange={(e) =>
                    setItemForm({
                      ...itemForm,
                      partId: e.target.value,
                    })
                  }
                >
                  <option value="">-- Chọn vật tư --</option>
                  {parts.map((part) => (
                    <option key={part.id} value={part.id}>
                      {part.name} — Tồn: {part.quantity ?? 0}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span className="inventory-label-text">
                  Số lượng thực tế <span className="inventory-required">*</span>
                </span>

                <input
                  type="number"
                  min="0"
                  value={itemForm.actualQuantity}
                  onChange={(e) =>
                    setItemForm({
                      ...itemForm,
                      actualQuantity: e.target.value,
                    })
                  }
                />
              </label>
            </div>

            {selectedPart && (
              <div className="inventory-meta">
                <span>Tồn hệ thống: {selectedPart.quantity ?? 0}</span>
                <span>Chênh lệch: {diffQuantity}</span>
              </div>
            )}

            <label>
              <span className="inventory-label-text">Ghi chú dòng kiểm kê</span>

              <textarea
                value={itemForm.note}
                onChange={(e) =>
                  setItemForm({
                    ...itemForm,
                    note: e.target.value,
                  })
                }
                placeholder="Ví dụ: Chênh lệch do vật tư hỏng hoặc hao hụt thực tế"
              />
            </label>

            <div className="inventory-actions">
              <button type="submit" disabled={loading}>
                {loading ? 'Đang thêm...' : 'Thêm dòng kiểm kê'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}