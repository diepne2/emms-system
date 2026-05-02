import React, { useCallback, useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import {
  FiPlus,
  FiTool,
  FiPackage,
  FiRefreshCw,
  FiAlertTriangle,
} from 'react-icons/fi'
import './WorkOrderPartSection.css'

const WORK_ORDER_PART_API_BASE_URL = 'https://emms-system-production-4239.up.railway.app/api/work-order-parts'
const PART_API_BASE_URL = 'https://emms-system-production-4239.up.railway.app/parts'

const getToken = () =>
  localStorage.getItem('accessToken') ||
  localStorage.getItem('token') ||
  localStorage.getItem('access_token') ||
  sessionStorage.getItem('accessToken') ||
  sessionStorage.getItem('token') ||
  sessionStorage.getItem('access_token') ||
  ''

const getAuthConfig = () => {
  const token = getToken()
  return {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }
}

const extractErrorMessage = (err, fallback) => {
  if (!err) return fallback

  if (err.response) {
    const data = err.response.data
    if (typeof data === 'string' && data.trim()) {
      return `HTTP ${err.response.status}: ${data}`
    }
    if (data?.message) {
      return `HTTP ${err.response.status}: ${data.message}`
    }
    if (data?.error) {
      return `HTTP ${err.response.status}: ${data.error}`
    }
    return `HTTP ${err.response.status}: ${fallback}`
  }

  if (err.request) {
    return 'Không nhận được phản hồi từ backend.'
  }

  return err.message || fallback
}

const formatDateTimeValue = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

const formatMoney = (value) => {
  const amount = Number(value || 0)
  return amount.toLocaleString('vi-VN')
}

export default function WorkOrderPartSection({
  workOrderId,
  canManageParts = true,
  onChanged,
}) {
  const [partOptions, setPartOptions] = useState([])
  const [partsLoading, setPartsLoading] = useState(false)

  const [usedParts, setUsedParts] = useState([])
  const [usedPartsLoading, setUsedPartsLoading] = useState(false)
  const [usedPartsError, setUsedPartsError] = useState('')

  const [submitLoading, setSubmitLoading] = useState(false)
  const [submitError, setSubmitError] = useState('')

  const [form, setForm] = useState({
    partId: '',
    quantity: 1,
  })

  const selectedPart = useMemo(() => {
    return (
      partOptions.find((item) => String(item.id) === String(form.partId)) || null
    )
  }, [partOptions, form.partId])

  const loadPartOptions = useCallback(async () => {
    try {
      setPartsLoading(true)
      const response = await axios.get(PART_API_BASE_URL, getAuthConfig())
      const data = Array.isArray(response?.data) ? response.data : []
      setPartOptions(data)
    } catch (err) {
      console.error('Load parts failed:', err)
      setPartOptions([])
    } finally {
      setPartsLoading(false)
    }
  }, [])

  const loadUsedParts = useCallback(async () => {
    if (!workOrderId) return

    try {
      setUsedPartsLoading(true)
      setUsedPartsError('')
      const response = await axios.get(
        `${WORK_ORDER_PART_API_BASE_URL}/${workOrderId}`,
        getAuthConfig(),
      )
      const data = Array.isArray(response?.data) ? response.data : []
      setUsedParts(data)
    } catch (err) {
      setUsedParts([])
      setUsedPartsError(
        extractErrorMessage(err, 'Không thể tải danh sách vật tư đã dùng.'),
      )
    } finally {
      setUsedPartsLoading(false)
    }
  }, [workOrderId])

  useEffect(() => {
    if (!workOrderId) return
    loadPartOptions()
    loadUsedParts()
  }, [workOrderId, loadPartOptions, loadUsedParts])

  const handleChange = (field, value) => {
    setForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const resetForm = () => {
    setForm({
      partId: '',
      quantity: 1,
    })
    setSubmitError('')
  }

  const handleSubmit = async () => {
    if (!workOrderId) {
      setSubmitError('Không xác định được work order.')
      return
    }

    if (!form.partId) {
      setSubmitError('Vui lòng chọn vật tư.')
      return
    }

    const qty = Number(form.quantity)
    if (!qty || qty <= 0) {
      setSubmitError('Số lượng phải lớn hơn 0.')
      return
    }

    if (
      selectedPart &&
      typeof selectedPart.quantity === 'number' &&
      qty > selectedPart.quantity
    ) {
      setSubmitError(`Số lượng dùng vượt quá tồn kho hiện tại (${selectedPart.quantity}).`)
      return
    }

    try {
      setSubmitLoading(true)
      setSubmitError('')

      await axios.post(
        `${WORK_ORDER_PART_API_BASE_URL}/${workOrderId}`,
        {
          partId: Number(form.partId),
          quantity: qty,
        },
        getAuthConfig(),
      )

      resetForm()
      await loadUsedParts()
      await loadPartOptions()

      if (onChanged) {
        await onChanged()
      }
    } catch (err) {
      setSubmitError(
        extractErrorMessage(err, 'Không thể thêm vật tư vào work order.'),
      )
    } finally {
      setSubmitLoading(false)
    }
  }

  return (
    <div className="wo-parts-section">
      <div className="wo-parts-section__header">
        <div className="wo-parts-section__title-wrap">
          <div className="wo-parts-section__icon">
            <FiTool size={18} />
          </div>

          <div>
            <div className="wo-parts-section__title">Vật tư sử dụng</div>
            <div className="wo-parts-section__subtitle">
              Thêm vật tư vào work order và backend sẽ tự trừ kho
            </div>
          </div>
        </div>

        <button
          className="wo-parts-refresh-btn"
          type="button"
          onClick={loadUsedParts}
          disabled={usedPartsLoading}
        >
          <FiRefreshCw size={14} />
          <span>{usedPartsLoading ? 'Đang tải...' : 'Tải lại'}</span>
        </button>
      </div>

      {canManageParts && (
        <div className="wo-parts-form-card">
          {submitError && (
            <div className="wo-parts-alert wo-parts-alert--error">
              <FiAlertTriangle size={16} />
              <span>{submitError}</span>
            </div>
          )}

          <div className="wo-parts-form-grid">
            <div className="wo-parts-field">
              <label className="wo-parts-label">
                Chọn vật tư <span>*</span>
              </label>

              <select
                className="wo-parts-input"
                value={form.partId}
                onChange={(e) => handleChange('partId', e.target.value)}
                disabled={partsLoading || submitLoading}
              >
                <option value="">
                  {partsLoading ? 'Đang tải vật tư...' : 'Chọn vật tư'}
                </option>

                {partOptions.map((part) => (
                  <option key={part.id} value={part.id}>
                    {part.name || part.partNumber || `Part #${part.id}`}
                    {typeof part.quantity === 'number' ? ` - Tồn: ${part.quantity}` : ''}
                  </option>
                ))}
              </select>
            </div>

            <div className="wo-parts-field">
              <label className="wo-parts-label">
                Số lượng <span>*</span>
              </label>

              <input
                type="number"
                min="1"
                className="wo-parts-input"
                value={form.quantity}
                onChange={(e) => handleChange('quantity', e.target.value)}
                disabled={submitLoading}
              />
            </div>
          </div>

          {selectedPart && (
            <div className="wo-parts-meta">
              <div className="wo-parts-chip">
                <FiPackage size={14} />
                <span>
                  Vật tư:{' '}
                  <strong>
                    {selectedPart.name ||
                      selectedPart.partNumber ||
                      `Part #${selectedPart.id}`}
                  </strong>
                </span>
              </div>

              {selectedPart.partNumber && (
                <div className="wo-parts-chip">
                  Mã vật tư: <strong>{selectedPart.partNumber}</strong>
                </div>
              )}

              {typeof selectedPart.quantity === 'number' && (
                <div className="wo-parts-chip">
                  Tồn kho: <strong>{selectedPart.quantity}</strong>
                </div>
              )}

              {selectedPart.cost != null && (
                <div className="wo-parts-chip">
                  Đơn giá: <strong>{formatMoney(selectedPart.cost)}</strong>
                </div>
              )}

              {selectedPart.category && (
                <div className="wo-parts-chip">
                  Nhóm: <strong>{selectedPart.category}</strong>
                </div>
              )}
            </div>
          )}

          <div className="wo-parts-actions">
            <button
              className="wo-parts-add-btn"
              onClick={handleSubmit}
              disabled={submitLoading}
              type="button"
            >
              <FiPlus size={16} />
              <span>{submitLoading ? 'Đang thêm...' : 'Thêm vật tư'}</span>
            </button>
          </div>
        </div>
      )}

      {usedPartsError ? (
        <div className="wo-parts-alert wo-parts-alert--error">
          <FiAlertTriangle size={16} />
          <span>{usedPartsError}</span>
        </div>
      ) : usedPartsLoading ? (
        <div className="wo-parts-empty">Đang tải danh sách vật tư đã dùng...</div>
      ) : usedParts.length === 0 ? (
        <div className="wo-parts-empty">
          Chưa có vật tư nào được sử dụng cho work order này.
        </div>
      ) : (
        <div className="wo-parts-table-wrap">
          <table className="wo-parts-table">
            <thead>
              <tr>
                <th>STT</th>
                <th>Vật tư</th>
                <th>Mã vật tư</th>
                <th>Số lượng dùng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
                <th>Used At</th>
              </tr>
            </thead>

            <tbody>
              {usedParts.map((row, index) => (
                <tr key={row?.id || index}>
                  <td>{index + 1}</td>

                  <td>
                    <div className="wo-parts-name-cell">
                      <strong>
                        {row?.partName || `Part #${row?.partId || '-'}`}
                      </strong>
                      {row?.category && <small>{row.category}</small>}
                    </div>
                  </td>

                  <td>{row?.partNumber || '-'}</td>
                  <td>{row?.quantityUsed ?? 0}</td>
                  <td>{formatMoney(row?.unitCost)}</td>
                  <td>{formatMoney(row?.lineTotal)}</td>
                  <td>{formatDateTimeValue(row?.usedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}