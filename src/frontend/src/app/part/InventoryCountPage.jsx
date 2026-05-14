import { useEffect, useMemo, useState } from 'react'
import { getErrorMessage, inventoryApi } from './inventoryApi'
import './inventory.css'

export default function InventoryCountPage() {
  const [parts, setParts] = useState([])
  const [createdCount, setCreatedCount] = useState(null)
  const [rows, setRows] = useState([])

  const [countForm, setCountForm] = useState({
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    note: '',
  })

  const [loading, setLoading] = useState(false)
  const [partsLoading, setPartsLoading] = useState(false)
  const [savingItems, setSavingItems] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

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

  const totalDifferentRows = useMemo(() => {
    return rows.filter(
      (row) => Number(row.actualQuantity || 0) !== Number(row.systemQuantity || 0),
    ).length
  }, [rows])

  const createRowsFromParts = (list) => {
    return list.map((part) => {
      const systemQty = Number(part.quantity ?? 0)

      return {
        partId: part.id,
        partName: part.name,
        partNumber: part.partNumber,
        category: part.category,
        systemQuantity: systemQty,
        actualQuantity: systemQty,
        note: '',
      }
    })
  }

  const createCount = async (e) => {
    e.preventDefault()
    setMessage('')
    setError('')

    if (!countForm.year || !countForm.month) {
      setError('Vui lòng nhập đầy đủ năm và tháng.')
      return
    }

    if (Number(countForm.month) < 1 || Number(countForm.month) > 12) {
      setError('Tháng phải nằm trong khoảng 1 đến 12.')
      return
    }

    if (!parts.length) {
      setError('Chưa có danh sách vật tư để tạo kiểm kê.')
      return
    }

    setLoading(true)

    try {
      const res = await inventoryApi.post('/parts/inventory-counts', null, {
        params: {
          year: Number(countForm.year),
          month: Number(countForm.month),
          note: countForm.note || undefined,
        },
      })

      setCreatedCount(res.data)
      setRows(createRowsFromParts(parts))
      setMessage('Tạo phiếu kiểm kê thành công. Tồn hệ thống đã được tự động lấy từ kho.')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const updateRow = (partId, field, value) => {
    setRows((prev) =>
      prev.map((row) =>
        row.partId === partId
          ? {
              ...row,
              [field]: value,
            }
          : row,
      ),
    )
  }

  const validateRows = () => {
    if (!createdCount?.id) {
      setError('Vui lòng tạo phiếu kiểm kê trước.')
      return false
    }

    if (!rows.length) {
      setError('Phiếu kiểm kê chưa có vật tư.')
      return false
    }

    const invalidRow = rows.find(
      (row) => row.actualQuantity === '' || Number(row.actualQuantity) < 0,
    )

    if (invalidRow) {
      setError(`Số lượng thực tế của "${invalidRow.partName}" không hợp lệ.`)
      return false
    }

    return true
  }

  const saveRowsToServer = async () => {
    for (const row of rows) {
      await inventoryApi.post(`/parts/inventory-counts/${createdCount.id}/items`, {
        partId: Number(row.partId),
        actualQuantity: Number(row.actualQuantity),
        note: row.note || undefined,
      })
    }
  }

  const saveAllItems = async () => {
    setMessage('')
    setError('')

    if (!validateRows()) return

    setSavingItems(true)

    try {
      await saveRowsToServer()
      setMessage('Đã lưu toàn bộ dòng kiểm kê.')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSavingItems(false)
    }
  }

  const confirmCount = async () => {
    setMessage('')
    setError('')

    if (!validateRows()) return

    if (
      !window.confirm(
        'Duyệt kiểm kê sẽ lưu các dòng kiểm kê và cập nhật tồn kho theo số lượng thực tế. Tiếp tục?',
      )
    ) {
      return
    }

    setLoading(true)

    try {
      await saveRowsToServer()

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

  const deleteCount = async () => {
    setMessage('')
    setError('')

    if (!createdCount?.id) {
      setError('Chưa có phiếu kiểm kê để xóa.')
      return
    }

    if (!window.confirm('Bạn có chắc muốn xóa phiếu kiểm kê này không?')) {
      return
    }

    setLoading(true)

    try {
      await inventoryApi.delete(`/parts/inventory-counts/${createdCount.id}`)
      setCreatedCount(null)
      setRows([])
      setMessage('Xóa phiếu kiểm kê thành công.')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
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

  const printInventoryCount = () => {
    if (!createdCount || !rows.length) {
      setError('Không có dữ liệu phiếu kiểm kê để in.')
      return
    }

    const printContent = `
      <html>
        <head>
          <title>Phiếu kiểm kê kho</title>
          <style>
            body {
              font-family: Arial, sans-serif;
              padding: 24px;
              color: #111827;
            }

            h2 {
              text-align: center;
              margin: 0 0 6px;
              font-size: 22px;
            }

            .subtitle {
              text-align: center;
              margin-bottom: 22px;
              font-size: 14px;
            }

            .info {
              margin-bottom: 18px;
              line-height: 1.8;
              font-size: 14px;
            }

            table {
              width: 100%;
              border-collapse: collapse;
              margin-top: 12px;
            }

            th, td {
              border: 1px solid #333;
              padding: 8px;
              font-size: 13px;
              text-align: left;
            }

            th {
              background: #f1f5f9;
              text-align: center;
            }

            td.number {
              text-align: right;
            }

            .signatures {
              display: grid;
              grid-template-columns: repeat(3, 1fr);
              gap: 24px;
              margin-top: 48px;
              text-align: center;
              font-size: 14px;
            }

            .sign-box {
              height: 80px;
            }

            @media print {
              button {
                display: none;
              }
            }
          </style>
        </head>

        <body>
          <h2>PHIẾU KIỂM KÊ KHO</h2>

          <div class="subtitle">
            Kỳ kho: ${createdCount.month}/${createdCount.year}
          </div>

          <div class="info">
            <div><strong>Mã phiếu:</strong> ${createdCount.code || '-'}</div>
            <div><strong>Trạng thái:</strong> ${createdCount.status || '-'}</div>
            <div><strong>Ngày duyệt:</strong> ${formatDateTime(createdCount.confirmedAt)}</div>
            <div><strong>Ghi chú:</strong> ${createdCount.note || countForm.note || '-'}</div>
          </div>

          <table>
            <thead>
              <tr>
                <th>STT</th>
                <th>Vật tư</th>
                <th>Mã vật tư</th>
                <th>Danh mục</th>
                <th>Tồn hệ thống</th>
                <th>Thực tế</th>
                <th>Chênh lệch</th>
                <th>Ghi chú</th>
              </tr>
            </thead>

            <tbody>
              ${rows
                .map((row, index) => {
                  const systemQty = Number(row.systemQuantity || 0)
                  const actualQty = Number(row.actualQuantity || 0)
                  const diff = actualQty - systemQty

                  return `
                    <tr>
                      <td class="number">${index + 1}</td>
                      <td>${row.partName || '-'}</td>
                      <td>${row.partNumber || '-'}</td>
                      <td>${row.category || '-'}</td>
                      <td class="number">${systemQty}</td>
                      <td class="number">${actualQty}</td>
                      <td class="number">${diff > 0 ? `+${diff}` : diff}</td>
                      <td>${row.note || '-'}</td>
                    </tr>
                  `
                })
                .join('')}
            </tbody>
          </table>

          <div class="signatures">
            <div>
              <strong>Người lập phiếu</strong>
              <div class="sign-box"></div>
              <div>Ký, ghi rõ họ tên</div>
            </div>

            <div>
              <strong>Thủ kho</strong>
              <div class="sign-box"></div>
              <div>Ký, ghi rõ họ tên</div>
            </div>

            <div>
              <strong>Quản lý kỹ thuật</strong>
              <div class="sign-box"></div>
              <div>Ký, ghi rõ họ tên</div>
            </div>
          </div>
        </body>
      </html>
    `

    const printWindow = window.open('', '_blank')

    if (!printWindow) {
      setError('Trình duyệt đã chặn cửa sổ in. Vui lòng cho phép popup.')
      return
    }

    printWindow.document.write(printContent)
    printWindow.document.close()
    printWindow.focus()
    printWindow.print()
  }

  const renderDifference = (row) => {
    const systemQty = Number(row.systemQuantity || 0)
    const actualQty = Number(row.actualQuantity || 0)
    const diff = actualQty - systemQty

    return (
      <div className="inventory-diff-wrap">
        <span
          className={
            diff === 0
              ? 'inventory-diff zero'
              : diff > 0
                ? 'inventory-diff plus'
                : 'inventory-diff minus'
          }
        >
          {diff > 0 ? `+${diff}` : diff}
        </span>

        {diff > 0 && <small className="inventory-diff-text">Thừa vật tư</small>}
        {diff < 0 && <small className="inventory-diff-text">Thiếu vật tư</small>}
        {diff === 0 && <small className="inventory-diff-text">Khớp tồn kho</small>}
      </div>
    )
  }

  return (
    <div className="inventory-page">
      <div className="inventory-hero">
        <span className="inventory-badge">Kho vật tư</span>
        <h2>Kiểm kê kho</h2>
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
              placeholder="Nhập ghi chú"
            />
          </label>

          <div className="inventory-note">
            Khi tạo phiếu, hệ thống sẽ tự sinh danh sách vật tư và lấy tồn hệ thống hiện tại.
          </div>

          <div className="inventory-actions">
            <button type="submit" disabled={loading || partsLoading || !!createdCount}>
              {loading ? 'Đang tạo...' : partsLoading ? 'Đang tải vật tư...' : 'Tạo phiếu kiểm kê'}
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

              <p>Số dòng chênh lệch: {totalDifferentRows}</p>
            </div>

            <div className="inventory-count-actions">
              {createdCount.status === 'DRAFT' && (
                <>
                  <button type="button" onClick={saveAllItems} disabled={savingItems || loading}>
                    {savingItems ? 'Đang lưu...' : 'Lưu dòng kiểm kê'}
                  </button>

                  <button type="button" onClick={confirmCount} disabled={loading || savingItems}>
                    {loading ? 'Đang duyệt...' : 'Duyệt kiểm kê'}
                  </button>

                  <button
                    type="button"
                    className="inventory-danger-btn"
                    onClick={deleteCount}
                    disabled={loading || savingItems}
                  >
                    Xóa phiếu
                  </button>
                </>
              )}

              {createdCount.status === 'CONFIRMED' && (
                <button type="button" onClick={printInventoryCount}>
                  In phiếu kiểm kê
                </button>
              )}
            </div>
          </div>

          <div className="inventory-table-wrap">
            <table className="inventory-table">
              <thead>
                <tr>
                  <th>Vật tư</th>
                  <th>Mã vật tư</th>
                  <th>Danh mục</th>
                  <th>Tồn HT</th>
                  <th>Thực tế</th>
                  <th>Chênh lệch</th>
                  <th>Ghi chú</th>
                </tr>
              </thead>

              <tbody>
                {rows.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="inventory-empty">
                      Chưa có dữ liệu vật tư.
                    </td>
                  </tr>
                ) : (
                  rows.map((row) => (
                    <tr key={row.partId}>
                      <td>{row.partName || '-'}</td>
                      <td>{row.partNumber || '-'}</td>
                      <td>{row.category || '-'}</td>
                      <td>
                        <strong>{row.systemQuantity}</strong>
                      </td>
                      <td>
                        <input
                          className="inventory-table-input"
                          type="number"
                          min="0"
                          value={row.actualQuantity}
                          disabled={createdCount.status !== 'DRAFT'}
                          onChange={(e) =>
                            updateRow(row.partId, 'actualQuantity', e.target.value)
                          }
                        />
                      </td>
                      <td>{renderDifference(row)}</td>
                      <td>
                        <input
                          className="inventory-table-input"
                          value={row.note}
                          disabled={createdCount.status !== 'DRAFT'}
                          onChange={(e) => updateRow(row.partId, 'note', e.target.value)}
                          placeholder="Ghi chú"
                        />
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}