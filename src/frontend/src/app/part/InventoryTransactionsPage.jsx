import { useEffect, useMemo, useState } from 'react'
import { getErrorMessage, inventoryApi } from './inventoryApi'
import './inventory.css'

export default function InventoryTransactionsPage() {
  const [parts, setParts] = useState([])
  const [partId, setPartId] = useState('')
  const [type, setType] = useState('')
  const [keyword, setKeyword] = useState('')
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const normalizeList = (raw) => {
    if (Array.isArray(raw)) return raw
    if (Array.isArray(raw?.data)) return raw.data
    if (Array.isArray(raw?.content)) return raw.content
    return []
  }

  const loadParts = async () => {
    const res = await inventoryApi.get('/parts')
    setParts(normalizeList(res.data))
  }

  const loadTransactions = async () => {
    setError('')
    setLoading(true)

    try {
      const res = await inventoryApi.get('/parts/transactions', {
        params: {
          keyword: keyword.trim() || undefined,
          type: type || undefined,
        },
      })

      setTransactions(normalizeList(res.data))
    } catch (err) {
      setError(getErrorMessage(err))
      setTransactions([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadParts().catch((err) => setError(getErrorMessage(err)))
    loadTransactions()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const filteredTransactions = useMemo(() => {
    if (!partId) return transactions

    return transactions.filter(
      (item) => String(item.partId) === String(partId),
    )
  }, [transactions, partId])

  const selectedPart = useMemo(() => {
    if (!partId) return null
    return parts.find((part) => String(part.id) === String(partId))
  }, [parts, partId])

  const formatDateTime = (value) => {
    if (!value) return '-'

    try {
      return new Date(value).toLocaleString('vi-VN')
    } catch {
      return value
    }
  }

  const renderTypeText = (value) => {
    if (value === 'IMPORT') return 'Nhập kho'
    if (value === 'USE_FOR_WORK_ORDER') return 'Xuất cho WO'
    if (value === 'ADJUSTMENT') return 'Điều chỉnh'
    return value || '-'
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

        <div className="inventory-filter inventory-filter-wide">
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Tìm kiếm"
          />

          <select value={partId} onChange={(e) => setPartId(e.target.value)}>
            <option value="">Tất cả vật tư</option>
            {parts.map((part) => (
              <option key={part.id} value={part.id}>
                {part.name} — Tồn: {part.quantity ?? 0}
              </option>
            ))}
          </select>

          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">Tất cả loại</option>
            <option value="IMPORT">Nhập kho</option>
            <option value="USE_FOR_WORK_ORDER">Xuất cho Work Order</option>
            <option value="ADJUSTMENT">Điều chỉnh kiểm kê</option>
          </select>

          <button type="button" onClick={loadTransactions} disabled={loading}>
            {loading ? 'Đang tải...' : 'Tìm kiếm'}
          </button>
        </div>

        {selectedPart && (
          <div className="inventory-meta">
            <span>Vật tư: {selectedPart.name}</span>
            <span>Tồn hiện tại: {selectedPart.quantity ?? 0}</span>
            <span>Số giao dịch: {filteredTransactions.length}</span>
          </div>
        )}

        {!selectedPart && (
          <div className="inventory-meta">
            <span>Tổng giao dịch: {filteredTransactions.length}</span>
          </div>
        )}

        <div className="inventory-table-wrap">
          <table className="inventory-table">
            <thead>
              <tr>
                <th>Loại</th>
                <th>Mã vật tư</th>
                <th>SL</th>
                <th>Trước</th>
                <th>Sau</th>
                <th>Work Order</th>
                <th>Người thao tác</th>
                <th>Ghi chú</th>
                <th>Thời gian</th>
              </tr>
            </thead>

            <tbody>
              {filteredTransactions.map((item) => (
                <tr key={item.id}>
                  <td>
                    <span className={`transaction-badge ${item.type}`}>
                      {renderTypeText(item.type)}
                    </span>
                  </td>
                  <td>{item.partId || '-'}</td>
                  <td>{item.quantity ?? 0}</td>
                  <td>{item.beforeQuantity ?? 0}</td>
                  <td>{item.afterQuantity ?? 0}</td>
                  <td>{item.workOrderId || '-'}</td>
                  <td>{item.createdBy || 'SYSTEM'}</td>
                  <td>{item.note || '-'}</td>
                  <td>{formatDateTime(item.createdAt)}</td>
                </tr>
              ))}

              {filteredTransactions.length === 0 && (
                <tr>
                  <td colSpan={9} className="inventory-empty">
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