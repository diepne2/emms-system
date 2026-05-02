import React, { useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import './request.css'

const API_BASE = 'http://localhost:8080/requests'
const LOCATION_API = 'http://localhost:8080/api/locations'
const ASSET_API = 'http://localhost:8080/api/assets'

const EMPTY_FORM = {
  title: '',
  description: '',
  dueDate: '',
  locationId: '',
  assetId: '',
  priority: 'MEDIUM',
}

const PRIORITY_OPTIONS = ['NONE', 'LOW', 'MEDIUM', 'HIGH', 'URGENT']

const Request = () => {
  const [requests, setRequests] = useState([])
  const [locations, setLocations] = useState([])
  const [assets, setAssets] = useState([])

  const [loading, setLoading] = useState(false)
  const [locationLoading, setLocationLoading] = useState(false)
  const [assetLoading, setAssetLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [actionLoadingId, setActionLoadingId] = useState(null)

  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [form, setForm] = useState(EMPTY_FORM)

  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [selectedRequest, setSelectedRequest] = useState(null)

  const [assetKeyword, setAssetKeyword] = useState('')
  const [assetOpen, setAssetOpen] = useState(false)

  const token =
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken') ||
    localStorage.getItem('access_token') ||
    localStorage.getItem('jwt')

  const rawRole =
    localStorage.getItem('role') ||
    localStorage.getItem('userRole') ||
    localStorage.getItem('authority') ||
    localStorage.getItem('authorities') ||
    ''

  const currentRole = String(rawRole).replace('ROLE_', '').toUpperCase()

  const canApproveRejectByRole =
    currentRole === 'ADMIN' ||
    currentRole === 'TECHNICAL_MANAGER' ||
    currentRole === 'QUANLYKYTHUAT'

  const axiosConfig = useMemo(() => {
    return {
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    }
  }, [token])

  const buildErrorMessage = (err, fallback) => {
    const status = err?.response?.status
    const message = err?.response?.data?.message

    if (status === 401) {
      return 'Phiên đăng nhập đã hết hạn hoặc token không hợp lệ. Hãy đăng nhập lại.'
    }

    if (status === 403) {
      return 'Bạn không có quyền truy cập chức năng này.'
    }

    return message || fallback
  }

  const clearMessages = () => {
    setError('')
    setSuccess('')
  }

  const fetchRequests = async () => {
    try {
      setLoading(true)
      clearMessages()

      const res = await axios.get(API_BASE, axiosConfig)
      setRequests(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      console.error('fetchRequests error:', err)
      setError(buildErrorMessage(err, 'Không tải được danh sách request.'))
    } finally {
      setLoading(false)
    }
  }

  const fetchLocations = async () => {
    try {
      setLocationLoading(true)

      const res = await axios.get(LOCATION_API, axiosConfig)
      const data = Array.isArray(res.data) ? res.data : []

      const normalized = data
        .map((item) => ({
          id: item.id ?? item.locationId ?? item.value,
          name:
            item.name ??
            item.title ??
            item.locationName ??
            `Location #${item.id ?? item.locationId ?? ''}`,
        }))
        .filter((item) => item.id != null)

      setLocations(normalized)
    } catch (err) {
      console.error('fetchLocations error:', err)
      setError(buildErrorMessage(err, 'Không tải được danh sách location.'))
    } finally {
      setLocationLoading(false)
    }
  }

  const fetchAssets = async () => {
    try {
      setAssetLoading(true)

      const res = await axios.get(ASSET_API, axiosConfig)

      const data = Array.isArray(res.data)
        ? res.data
        : Array.isArray(res.data?.content)
          ? res.data.content
          : Array.isArray(res.data?.data)
            ? res.data.data
            : []

      setAssets(data)
    } catch (err) {
      console.error('fetchAssets error:', err)
      setAssets([])
      setError(buildErrorMessage(err, 'Không tải được danh sách thiết bị.'))
    } finally {
      setAssetLoading(false)
    }
  }

  useEffect(() => {
    fetchRequests()
    fetchLocations()
    fetchAssets()
  }, [])

  const filteredAssets = useMemo(() => {
    const keyword = assetKeyword.trim().toLowerCase()

    return assets.filter((asset) => {
      if (!keyword) return true

      return (
        String(asset.id || '').includes(keyword) ||
        (asset.name || '').toLowerCase().includes(keyword) ||
        (asset.assetName || '').toLowerCase().includes(keyword) ||
        (asset.barcode || '').toLowerCase().includes(keyword) ||
        (asset.assetCode || '').toLowerCase().includes(keyword) ||
        (asset.serialNumber || '').toLowerCase().includes(keyword) ||
        (asset.locationName || '').toLowerCase().includes(keyword)
      )
    })
  }, [assets, assetKeyword])

  const handleInputChange = (e) => {
    const { name, value } = e.target

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSelectAsset = (asset) => {
    const id = asset.id ?? asset.assetId
    const name = asset.name || asset.assetName || `Asset #${id}`
    const code = asset.barcode || asset.assetCode || asset.code || ''

    setForm((prev) => ({
      ...prev,
      assetId: id,
    }))

    setAssetKeyword(code ? `${name} - ${code}` : name)
    setAssetOpen(false)
  }

  const resetForm = () => {
    setForm(EMPTY_FORM)
    setAssetKeyword('')
    setAssetOpen(false)
  }

  const handleCreateRequest = async (e) => {
    e.preventDefault()

    if (!form.title.trim()) {
      setError('Title không được để trống.')
      return
    }

    if (!form.assetId) {
      setError('Tên thiết bị không được để trống')
      return
    }

    try {
      setSubmitting(true)
      clearMessages()

      const payload = {
        title: form.title.trim(),
        description: form.description.trim() || null,
        dueDate: form.dueDate || null,
        locationId: form.locationId ? Number(form.locationId) : null,
        assetId: Number(form.assetId),
        priority: form.priority || 'MEDIUM',
      }

      await axios.post(API_BASE, payload, axiosConfig)

      setSuccess('Tạo request thành công.')
      resetForm()
      await fetchRequests()
    } catch (err) {
      console.error('createRequest error:', err)
      setError(buildErrorMessage(err, 'Tạo request thất bại.'))
    } finally {
      setSubmitting(false)
    }
  }

  const handleApprove = async (id) => {
    try {
      setActionLoadingId(id)
      clearMessages()

      await axios.post(`${API_BASE}/${id}/approve`, null, axiosConfig)
      setSuccess(`Approve request #${id} thành công và đã tạo Work Order.`)
      await fetchRequests()
    } catch (err) {
      console.error('approve error:', err)
      setError(buildErrorMessage(err, `Approve request #${id} thất bại.`))
    } finally {
      setActionLoadingId(null)
    }
  }

  const handleReject = async (id) => {
    const reason = window.prompt('Nhập lý do reject request:', '')
    if (reason === null) return

    try {
      setActionLoadingId(id)
      clearMessages()

      const url = reason.trim()
        ? `${API_BASE}/${id}/reject?reason=${encodeURIComponent(reason.trim())}`
        : `${API_BASE}/${id}/reject`

      await axios.post(url, null, axiosConfig)
      setSuccess(`Reject request #${id} thành công.`)
      await fetchRequests()
    } catch (err) {
      console.error('reject error:', err)
      setError(buildErrorMessage(err, `Reject request #${id} thất bại.`))
    } finally {
      setActionLoadingId(null)
    }
  }

  const handleCancel = async (id) => {
    const reason = window.prompt('Nhập lý do cancel request:', '')
    if (reason === null) return

    try {
      setActionLoadingId(id)
      clearMessages()

      const url = reason.trim()
        ? `${API_BASE}/${id}/cancel?reason=${encodeURIComponent(reason.trim())}`
        : `${API_BASE}/${id}/cancel`

      await axios.post(url, null, axiosConfig)
      setSuccess(`Cancel request #${id} thành công.`)
      await fetchRequests()
    } catch (err) {
      console.error('cancel error:', err)
      setError(buildErrorMessage(err, `Cancel request #${id} thất bại.`))
    } finally {
      setActionLoadingId(null)
    }
  }

  const handleViewDetail = async (id) => {
    try {
      clearMessages()
      const res = await axios.get(`${API_BASE}/${id}`, axiosConfig)
      setSelectedRequest(res.data)
    } catch (err) {
      console.error('detail error:', err)
      setError(buildErrorMessage(err, 'Không tải được chi tiết request.'))
    }
  }

  const closeModal = () => setSelectedRequest(null)

  const filteredRequests = useMemo(() => {
    return requests.filter((item) => {
      const keyword = search.trim().toLowerCase()

      const matchKeyword =
        !keyword ||
        String(item.id).includes(keyword) ||
        (item.title || '').toLowerCase().includes(keyword) ||
        (item.locationName || '').toLowerCase().includes(keyword) ||
        (item.assetName || '').toLowerCase().includes(keyword) ||
        (item.assetCode || '').toLowerCase().includes(keyword) ||
        (item.status || '').toLowerCase().includes(keyword) ||
        (item.priority || '').toLowerCase().includes(keyword)

      const matchStatus =
        statusFilter === 'ALL' || (item.status || '') === statusFilter

      return matchKeyword && matchStatus
    })
  }, [requests, search, statusFilter])

  const getStatusClass = (status) => {
    switch (status) {
      case 'PENDING':
        return 'pending'
      case 'APPROVED':
        return 'approved'
      case 'REJECTED':
        return 'rejected'
      case 'CANCELLED':
        return 'cancelled'
      case 'RESOLVED':
        return 'resolved'
      case 'OPEN':
        return 'open'
      case 'WAITING':
        return 'waiting'
      case 'ACCEPTED':
        return 'accepted'
      default:
        return 'default'
    }
  }

  const getPriorityClass = (priority) => {
    switch (priority) {
      case 'URGENT':
        return 'urgent'
      case 'HIGH':
        return 'high'
      case 'MEDIUM':
        return 'medium'
      case 'LOW':
        return 'low'
      case 'NONE':
        return 'none'
      default:
        return 'none'
    }
  }

  const canApproveOrReject = (item) => {
    return item.status === 'PENDING' && !item.workOrderId
  }

  const canCancel = (item) => {
    return item.status !== 'CANCELLED' && !item.workOrderId
  }

  return (
    <div className="request-page">
      <div className="request-shell">
        <div className="request-topbar">
          <div>
            <h1>Request Management</h1>
          </div>

          <button className="ghost-btn" onClick={fetchRequests} disabled={loading}>
            {loading ? 'Đang tải...' : 'Làm mới'}
          </button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <div className="request-grid">
          <section className="card request-form-card">
            <div className="card-header">
              <div>
                <h2>Tạo Yêu Cầu Sửa Chữa</h2>
              </div>
            </div>

            <form className="request-form" onSubmit={handleCreateRequest}>
              <div className="form-group">
                <label>
                  Title <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="title"
                  value={form.title}
                  onChange={handleInputChange}
                  placeholder="Nhập tiêu đề request"
                />
              </div>

              <div className="form-group">
                <label>
                  Thiết bị <span className="required">*</span>
                </label>

                <div className="asset-select">
                  <input
                    type="text"
                    className="asset-search"
                    placeholder={
                      assetLoading
                        ? 'Đang tải thiết bị...'
                        : 'Tìm thiết bị'
                    }
                    value={assetKeyword}
                    disabled={assetLoading}
                    onFocus={() => setAssetOpen(true)}
                    onChange={(e) => {
                      setAssetKeyword(e.target.value)
                      setAssetOpen(true)
                      setForm((prev) => ({ ...prev, assetId: '' }))
                    }}
                    onBlur={() => {
                      setTimeout(() => setAssetOpen(false), 150)
                    }}
                  />

                  {assetOpen && (
                    <div className="asset-dropdown">
                      {filteredAssets.length > 0 ? (
                        filteredAssets.map((asset) => {
                          const id = asset.id ?? asset.assetId
                          const name = asset.name || asset.assetName || `Asset #${id}`
                          const code =
                            asset.barcode || asset.assetCode || asset.code || ''

                          return (
                            <button
                              type="button"
                              key={id}
                              className={
                                String(form.assetId) === String(id)
                                  ? 'asset-item active'
                                  : 'asset-item'
                              }
                              onMouseDown={() => handleSelectAsset(asset)}
                            >
                              <strong>{name}</strong>
                              <span>
                                {code || 'Không có barcode'}
                                {asset.serialNumber
                                  ? ` • SN: ${asset.serialNumber}`
                                  : ''}
                                {asset.locationName
                                  ? ` • ${asset.locationName}`
                                  : ''}
                              </span>
                            </button>
                          )
                        })
                      ) : (
                        <div className="asset-empty">Không tìm thấy thiết bị</div>
                      )}
                    </div>
                  )}
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Priority</label>
                  <select
                    name="priority"
                    value={form.priority}
                    onChange={handleInputChange}
                  >
                    {PRIORITY_OPTIONS.map((item) => (
                      <option key={item} value={item}>
                        {item}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Due date</label>
                  <input
                    type="date"
                    name="dueDate"
                    value={form.dueDate}
                    onChange={handleInputChange}
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Location</label>
                <select
                  name="locationId"
                  value={form.locationId}
                  onChange={handleInputChange}
                  disabled={locationLoading}
                >
                  <option value="">
                    {locationLoading ? 'Đang tải location...' : 'Chọn location'}
                  </option>

                  {locations.map((location) => (
                    <option key={location.id} value={location.id}>
                      {location.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  name="description"
                  rows="5"
                  value={form.description}
                  onChange={handleInputChange}
                  placeholder="Mô tả"
                />
              </div>

              <div className="form-actions">
                <button type="submit" className="primary-btn" disabled={submitting}>
                  {submitting ? 'Đang tạo...' : 'Tạo Request'}
                </button>

                <button
                  type="button"
                  className="secondary-btn"
                  onClick={resetForm}
                  disabled={submitting}
                >
                  Hủy
                </button>
              </div>
            </form>
          </section>

          <section className="card request-list-card">
            <div className="card-header list-header">
              <div>
                <h2>Danh sách Request</h2>
                <span>{filteredRequests.length} request</span>
              </div>

              <div className="toolbar">
                <input
                  type="text"
                  placeholder="Tìm kiếm"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />

                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <option value="ALL">All status</option>
                  <option value="PENDING">PENDING</option>
                  <option value="APPROVED">APPROVED</option>
                  <option value="REJECTED">REJECTED</option>
                  <option value="CANCELLED">CANCELLED</option>
                  <option value="RESOLVED">RESOLVED</option>
                  <option value="OPEN">OPEN</option>
                  <option value="WAITING">WAITING</option>
                  <option value="ACCEPTED">ACCEPTED</option>
                </select>
              </div>
            </div>

            <div className="table-wrap">
              <table className="request-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Asset</th>
                    <th>Location</th>
                    <th>Due Date</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>WO</th>
                    <th>Actions</th>
                  </tr>
                </thead>

                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan="9" className="empty-cell">
                        Đang tải dữ liệu...
                      </td>
                    </tr>
                  ) : filteredRequests.length === 0 ? (
                    <tr>
                      <td colSpan="9" className="empty-cell">
                        Không có request nào
                      </td>
                    </tr>
                  ) : (
                    filteredRequests.map((item) => (
                      <tr key={item.id}>
                        <td data-label="ID">#{item.id}</td>

                        <td data-label="Title">
                          <div className="title-cell">
                            <strong>{item.title || 'N/A'}</strong>
                            <span>
                              {item.createdAt
                                ? new Date(item.createdAt).toLocaleString()
                                : '—'}
                            </span>
                          </div>
                        </td>

                        <td data-label="Asset">
                          {item.assetName ? (
                            <span className="asset-badge">
                              {item.assetName}
                              <span>{item.assetCode || 'Không có barcode'}</span>
                            </span>
                          ) : (
                            <span className="wo-empty">—</span>
                          )}
                        </td>

                        <td data-label="Location">{item.locationName || '—'}</td>
                        <td data-label="Due Date">{item.dueDate || '—'}</td>

                        <td data-label="Status">
                          <span className={`status-badge ${getStatusClass(item.status)}`}>
                            {item.status || 'UNKNOWN'}
                          </span>
                        </td>

                        <td data-label="Priority">
                          <span
                            className={`priority-badge ${getPriorityClass(item.priority)}`}
                          >
                            {item.priority || 'NONE'}
                          </span>
                        </td>

                        <td data-label="WO">
                          {item.workOrderId ? (
                            <span className="wo-link">WO #{item.workOrderId}</span>
                          ) : (
                            <span className="wo-empty">Chưa có</span>
                          )}
                        </td>

                        <td data-label="Actions">
                          <div className="action-group">
                            <button
                              type="button"
                              className="table-btn info"
                              onClick={() => handleViewDetail(item.id)}
                            >
                              View
                            </button>

                            {canApproveRejectByRole && canApproveOrReject(item) && (
                              <>
                                <button
                                  type="button"
                                  className="table-btn approve"
                                  onClick={() => handleApprove(item.id)}
                                  disabled={actionLoadingId === item.id}
                                >
                                  {actionLoadingId === item.id ? '...' : 'Approve'}
                                </button>

                                <button
                                  type="button"
                                  className="table-btn reject"
                                  onClick={() => handleReject(item.id)}
                                  disabled={actionLoadingId === item.id}
                                >
                                  Reject
                                </button>
                              </>
                            )}

                            {canCancel(item) && (
                              <button
                                type="button"
                                className="table-btn cancel"
                                onClick={() => handleCancel(item.id)}
                                disabled={actionLoadingId === item.id}
                              >
                                Cancel
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </div>

      {selectedRequest && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="request-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3>Request Detail #{selectedRequest.id}</h3>
                
              </div>

              <button type="button" className="close-btn" onClick={closeModal}>
                ×
              </button>
            </div>

            <div className="modal-content">
              <div className="detail-grid">
                <div className="detail-item">
                  <label>Title</label>
                  <span>{selectedRequest.title || '—'}</span>
                </div>

                <div className="detail-item">
                  <label>Status</label>
                  <span
                    className={`status-badge ${getStatusClass(selectedRequest.status)}`}
                  >
                    {selectedRequest.status || '—'}
                  </span>
                </div>

                <div className="detail-item">
                  <label>Priority</label>
                  <span
                    className={`priority-badge ${getPriorityClass(
                      selectedRequest.priority
                    )}`}
                  >
                    {selectedRequest.priority || '—'}
                  </span>
                </div>

                <div className="detail-item">
                  <label>Asset</label>
                  <div className="asset-detail">
                    <strong>{selectedRequest.assetName || '—'}</strong>
                    <span>
                      {selectedRequest.assetCode
                        ? `Barcode: ${selectedRequest.assetCode}`
                        : 'Không có barcode'}
                    </span>
                  </div>
                </div>

                <div className="detail-item">
                  <label>Location</label>
                  <span>{selectedRequest.locationName || '—'}</span>
                </div>

                <div className="detail-item">
                  <label>Due date</label>
                  <span>{selectedRequest.dueDate || '—'}</span>
                </div>

                <div className="detail-item">
                  <label>Work Order</label>
                  <span>
                    {selectedRequest.workOrderId
                      ? `WO #${selectedRequest.workOrderId}`
                      : 'Chưa tạo'}
                  </span>
                </div>

                <div className="detail-item full">
                  <label>Description</label>
                  <p>{selectedRequest.description || 'Không có mô tả'}</p>
                </div>

                <div className="detail-item full">
                  <label>Cancellation reason</label>
                  <p>{selectedRequest.cancellationReason || '—'}</p>
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button type="button" className="secondary-btn" onClick={closeModal}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default Request