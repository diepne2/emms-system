import { useEffect, useState } from 'react'
import { getErrorMessage, inventoryApi } from './inventoryApi'
import './inventory.css'

export default function InventoryTransactionsPage() {
  const [parts, setParts] = useState([])
  const [partId, setPartId] = useState('')
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const loadParts = async () => {
    const res = await inventoryApi.get('/parts')
    setParts(Array.isArray(res.data) ? res.data : [])
  }

  useEffect(() => {
    loadParts().catch((err) => setError(getErrorMessage(err)))
  }, [])

  const loadTransactions = async () => {
    setError('')

    if (!partId) {
      setError('Vui lòng chọn vật tư.')
      return
    }

    setLoading(true)
    try {
      const res = await inventoryApi.get(`/parts/${partId}/transactions`)
      setTransactions(Array.isArray(res.data) ? res.data : [])
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
        <h2>Lịch sử giao dịch kho</h2>
      </div>

      {error && <div className="inventory-alert error">{error}</div>}

      <div className="inventory-card">
        <h3>Bộ lọc lịch sử</h3>

        <div className="inventory-filter">
          <select value={partId} onChange={(e) => setPartId(e.target.value)}>
            <option value="">-- Chọn vật tư --</option>
            {parts.map((part) => (
              <option key={part.id} value={part.id}>
                {part.name} — Tồn: {part.quantity ?? 0}
              </option>
            ))}
          </select>

          <button type="button" onClick={loadTransactions} disabled={loading}>
            {loading ? 'Đang tải...' : 'Xem lịch sử'}
          </button>
        </div>

        <div className="inventory-table-wrap">
          <table className="inventory-table">
            <thead>
              <tr>
                <th>Loại</th>
                <th>SL</th>
                <th>Trước</th>
                <th>Sau</th>
                <th>Work Order</th>
                <th>Ghi chú</th>
                <th>Thời gian</th>
              </tr>
            </thead>

            <tbody>
              {transactions.map((item) => (
                <tr key={item.id}>
                  <td>
                    <span className={`transaction-badge ${item.type}`}>
                      {item.type}
                    </span>
                  </td>
                  <td>{item.quantity}</td>
                  <td>{item.beforeQuantity}</td>
                  <td>{item.afterQuantity}</td>
                  <td>{item.workOrderId || '-'}</td>
                  <td>{item.note || '-'}</td>
                  <td>{item.createdAt ? new Date(item.createdAt).toLocaleString() : '-'}</td>
                </tr>
              ))}

              {transactions.length === 0 && (
                <tr>
                  <td colSpan={7} className="inventory-empty">
                    Chưa có dữ liệu lịch sử.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}