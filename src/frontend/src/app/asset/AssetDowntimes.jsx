import React, { useCallback, useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import './AssetDowntimes.css'
import {
  FiSearch,
  FiChevronLeft,
  FiChevronRight,
  FiEye,
  FiX,
  FiClock,
  FiTool,
  FiTag,
  FiInfo,
  FiCalendar,
  FiEdit2,
  FiPlus,
  FiTrash2,
  FiAlertTriangle,
  FiFilter,
  FiRotateCcw,
  FiSave,
} from 'react-icons/fi'

const API_BASE_URL = 'http://localhost:8080/api/asset-downtimes'
const ASSET_API_BASE_URL = 'http://localhost:8080/api/assets'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

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

const safeJsonParse = (value, fallback) => {
  try {
    return JSON.parse(value)
  } catch {
    return fallback
  }
}

const normalizeToArray = (value) => {
  if (!value) return []
  if (Array.isArray(value)) return value
  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) return []
    return [trimmed]
  }
  return []
}

const normalizeGrant = (value) => {
  if (!value) return ''
  let raw = String(value).trim().toUpperCase()
  if (!raw) return ''
  if (raw.startsWith('ROLE_')) raw = raw.substring(5)
  return raw
}

const extractGrantValue = (item) => {
  if (!item) return null
  if (typeof item === 'string') return item.trim()
  if (typeof item === 'object') {
    return item.authority || item.name || item.code || item.role || item.permission || null
  }
  return null
}

const getUserContext = () => {
  const userRaw = localStorage.getItem('user') || sessionStorage.getItem('user')
  const rolesRaw = localStorage.getItem('roles') || sessionStorage.getItem('roles')
  const authoritiesRaw = localStorage.getItem('authorities') || sessionStorage.getItem('authorities')
  const permissionsRaw = localStorage.getItem('permissions') || sessionStorage.getItem('permissions')
  const roleRaw = localStorage.getItem('role') || sessionStorage.getItem('role') || ''

  const user = safeJsonParse(userRaw, {})
  const roles = normalizeToArray(safeJsonParse(rolesRaw, rolesRaw || user?.roles || []))
  const authorities = normalizeToArray(safeJsonParse(authoritiesRaw, authoritiesRaw || user?.authorities || []))
  const permissions = normalizeToArray(safeJsonParse(permissionsRaw, permissionsRaw || user?.permissions || []))
  const singleRole = normalizeToArray(roleRaw)

  const merged = [
    ...roles,
    ...authorities,
    ...permissions,
    ...singleRole,
    ...(Array.isArray(user?.roles) ? user.roles : []),
    ...(Array.isArray(user?.authorities) ? user.authorities : []),
    ...(Array.isArray(user?.permissions) ? user.permissions : []),
  ]
    .map(extractGrantValue)
    .filter(Boolean)
    .map(normalizeGrant)
    .filter(Boolean)

  return { user, grants: Array.from(new Set(merged)) }
}

const hasAnyGrant = (grants, expected = []) => {
  if (!Array.isArray(grants) || !expected.length) return false
  const normalizedUserGrants = grants.map(normalizeGrant)
  const normalizedExpected = expected.map(normalizeGrant)
  return normalizedExpected.some((item) => normalizedUserGrants.includes(item))
}

const extractErrorMessage = (err, fallback) => {
  if (!err) return fallback
  if (err.response) {
    const data = err.response.data
    if (typeof data === 'string' && data.trim()) return `HTTP ${err.response.status}: ${data}`
    if (data?.message) return `HTTP ${err.response.status}: ${data.message}`
    if (data?.error) return `HTTP ${err.response.status}: ${data.error}`
    return `HTTP ${err.response.status}: ${fallback}`
  }
  if (err.request) return 'Không nhận được phản hồi từ backend. Kiểm tra backend/CORS/network.'
  return err.message || fallback
}

const REASON_OPTIONS = [
  { value: '', label: 'Tất cả nguyên nhân' },
  { value: 'BREAKDOWN', label: 'Sự cố / Hỏng hóc' },
  { value: 'MAINTENANCE', label: 'Bảo trì' },
  { value: 'POWER_FAILURE', label: 'Mất điện' },
  { value: 'CALIBRATION', label: 'Hiệu chuẩn' },
  { value: 'OTHER', label: 'Khác' },
]

const FORM_REASON_OPTIONS = REASON_OPTIONS.filter((item) => item.value)

const getReasonLabel = (reason) => {
  const found = REASON_OPTIONS.find((item) => item.value === reason)
  return found?.label || reason || 'Không xác định'
}

const getDateSortLabel = (value) => {
  switch (value) {
    case 'newest':
      return 'Ngày gần nhất'
    case 'oldest':
      return 'Ngày xa nhất'
    default:
      return 'Mặc định'
  }
}

const getOpenStatusLabel = (isOpen) => (isOpen ? 'Đang mở' : 'Đã đóng')

const getOpenBadgeClass = (isOpen) => (isOpen ? 'badge badge--warning' : 'badge badge--success')

const getVisiblePages = (currentPage, totalPages) => {
  const pages = []
  if (totalPages <= 7) {
    for (let i = 0; i < totalPages; i += 1) pages.push(i)
    return pages
  }
  const start = Math.max(0, currentPage - 2)
  const end = Math.min(totalPages - 1, currentPage + 2)
  if (start > 0) pages.push(0)
  if (start > 1) pages.push('left-ellipsis')
  for (let i = start; i <= end; i += 1) pages.push(i)
  if (end < totalPages - 2) pages.push('right-ellipsis')
  if (end < totalPages - 1) pages.push(totalPages - 1)
  return pages
}

const emptyForm = {
  assetId: '',
  workOrderId: '',
  reason: 'BREAKDOWN',
  startsOn: '',
  endsOn: '',
  note: '',
}

function DetailItem({ icon, label, value, full = false, compact = false }) {
  return (
    <div className={`detail-item ${full ? 'detail-item--full' : ''} ${compact ? 'detail-item--compact' : ''}`}>
      <div className="detail-item__label">
        <span className="detail-item__icon">{icon}</span>
        <span>{label}</span>
      </div>
      <div className="detail-item__value">{value || '-'}</div>
    </div>
  )
}

function FormField({ label, children, full = false }) {
  return (
    <div className={`form-field ${full ? 'form-field--full' : ''}`}>
      <label className="form-label">{label}</label>
      {children}
    </div>
  )
}

const toDateTimeLocal = (value) => {
  if (!value) return ''
  return String(value).slice(0, 16)
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

const formatDuration = (seconds) => {
  if (seconds === null || seconds === undefined || Number.isNaN(Number(seconds))) return '-'
  const totalSeconds = Number(seconds)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  if (hours <= 0) return `${minutes} phút`
  if (minutes === 0) return `${hours} giờ`
  return `${hours} giờ ${minutes} phút`
}

export default function AssetDowntimes() {
  const { grants } = useMemo(() => getUserContext(), [])
  const isAuthenticated = Boolean(getToken())

  const canViewDetail = isAuthenticated
  const canCreate = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canEdit = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canDelete = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])

  const [filterForm, setFilterForm] = useState({
    keyword: '',
    reason: '',
    dateSort: 'newest',
    openOnly: '',
  })
  const [appliedFilters, setAppliedFilters] = useState({
    keyword: '',
    reason: '',
    dateSort: 'newest',
    openOnly: '',
  })

  const [downtimes, setDowntimes] = useState([])
  const [assetOptions, setAssetOptions] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [pageNum, setPageNum] = useState(0)
  const [pageSize, setPageSize] = useState(5)

  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [selectedDowntime, setSelectedDowntime] = useState(null)

  const [createOpen, setCreateOpen] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [createError, setCreateError] = useState('')
  const [createForm, setCreateForm] = useState(emptyForm)

  const [editOpen, setEditOpen] = useState(false)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const [editForm, setEditForm] = useState(emptyForm)
  const [editingDowntimeId, setEditingDowntimeId] = useState(null)

  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteError, setDeleteError] = useState('')
  const [deleteLoadingId, setDeleteLoadingId] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)

  const pageTitle = 'Asset Downtime'

  const hasActiveFilters = useMemo(
    () =>
      Boolean(
        appliedFilters.keyword?.trim() ||
          appliedFilters.reason?.trim() ||
          appliedFilters.dateSort?.trim() ||
          appliedFilters.openOnly?.trim(),
      ),
    [appliedFilters],
  )

  const activeFilterCount = useMemo(() => {
    let count = 0
    if (appliedFilters.keyword?.trim()) count += 1
    if (appliedFilters.reason?.trim()) count += 1
    if (appliedFilters.dateSort?.trim()) count += 1
    if (appliedFilters.openOnly?.trim()) count += 1
    return count
  }, [appliedFilters])

  const loadAssetOptions = useCallback(async () => {
    try {
      const response = await axios.post(
        `${ASSET_API_BASE_URL}/search`,
        { filterFields: [], direction: 'DESC', pageNum: 0, pageSize: 1000, sortField: 'id' },
        getAuthConfig(),
      )
      const content = Array.isArray(response?.data?.content) ? response.data.content : []
      setAssetOptions(content)
    } catch {
      setAssetOptions([])
    }
  }, [])

  const loadData = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const response = await api.get('', getAuthConfig())
      const items = Array.isArray(response?.data) ? response.data : []
      setDowntimes(items)
    } catch (err) {
      setError(extractErrorMessage(err, 'Không thể tải danh sách downtime.'))
    } finally {
      setLoading(false)
    }
  }, [])

  const loadDetail = useCallback(async (id) => {
    try {
      setDetailLoading(true)
      setDetailError('')
      setDetailOpen(true)
      const response = await api.get(`/${id}`, getAuthConfig())
      setSelectedDowntime(response?.data || null)
    } catch (err) {
      setSelectedDowntime(null)
      setDetailError(extractErrorMessage(err, 'Không thể tải chi tiết downtime.'))
    } finally {
      setDetailLoading(false)
    }
  }, [])

  useEffect(() => {
    loadAssetOptions()
  }, [loadAssetOptions])

  useEffect(() => {
    loadData()
  }, [loadData])

  const filteredDowntimes = useMemo(() => {
    let result = [...downtimes]

    const keyword = appliedFilters.keyword?.trim().toLowerCase()
    const reason = appliedFilters.reason?.trim()
    const openOnly = appliedFilters.openOnly?.trim()
    const dateSort = appliedFilters.dateSort || 'newest'

    if (keyword) {
      result = result.filter((item) =>
        [
          item.assetName,
          item.reason,
          item.note,
          item.id ? String(item.id) : '',
          item.workOrderId ? String(item.workOrderId) : '',
        ]
          .filter(Boolean)
          .some((value) => String(value).toLowerCase().includes(keyword)),
      )
    }

    if (reason) {
      result = result.filter((item) => String(item.reason || '').toUpperCase() === reason.toUpperCase())
    }

    if (openOnly === 'OPEN') {
      result = result.filter((item) => Boolean(item.open))
    } else if (openOnly === 'CLOSED') {
      result = result.filter((item) => !item.open)
    }

    result.sort((a, b) => {
      const timeA = a?.startsOn ? new Date(a.startsOn).getTime() : 0
      const timeB = b?.startsOn ? new Date(b.startsOn).getTime() : 0
      return dateSort === 'oldest' ? timeA - timeB : timeB - timeA
    })

    return result
  }, [downtimes, appliedFilters])

  const totalElements = filteredDowntimes.length
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / pageSize)
  const lastPage = totalPages === 0 ? true : pageNum >= totalPages - 1

  const pagedDowntimes = useMemo(() => {
    const start = pageNum * pageSize
    return filteredDowntimes.slice(start, start + pageSize)
  }, [filteredDowntimes, pageNum, pageSize])

  useEffect(() => {
    if (pageNum > 0 && pageNum >= totalPages && totalPages > 0) {
      setPageNum(totalPages - 1)
    }
  }, [pageNum, totalPages])

  const handleFilterChange = (field, value) => {
    setFilterForm((prev) => ({ ...prev, [field]: value }))
  }

  const handleSearch = () => {
    setPageNum(0)
    setAppliedFilters({
      keyword: filterForm.keyword.trim(),
      reason: filterForm.reason,
      dateSort: filterForm.dateSort || 'newest',
      openOnly: filterForm.openOnly,
    })
  }

  const handleResetFilters = () => {
    const reset = { keyword: '', reason: '', dateSort: 'newest', openOnly: '' }
    setFilterForm(reset)
    setAppliedFilters(reset)
    setPageNum(0)
  }

  const handlePageChange = (nextPage) => {
    if (nextPage < 0 || nextPage >= totalPages || nextPage === pageNum) return
    setPageNum(nextPage)
  }

  const handlePageSizeChange = (e) => {
    const nextSize = Number(e.target.value)
    setPageNum(0)
    setPageSize(nextSize)
  }

  const handleViewDetails = (item) => {
    if (!item?.id || !canViewDetail) return
    loadDetail(item.id)
  }

  const openCreateModal = () => {
    if (!canCreate) return
    setCreateError('')
    setCreateForm(emptyForm)
    setCreateOpen(true)
  }

  const closeCreateModal = () => {
    if (createLoading) return
    setCreateOpen(false)
    setCreateError('')
    setCreateForm(emptyForm)
  }

  const handleCreateFormChange = (field, value) => {
    setCreateForm((prev) => ({ ...prev, [field]: value }))
  }

  const buildPayload = (form) => ({
    assetId: form.assetId ? Number(form.assetId) : null,
    workOrderId: form.workOrderId ? Number(form.workOrderId) : null,
    reason: form.reason?.trim() || '',
    startsOn: form.startsOn || null,
    endsOn: form.endsOn || null,
    note: form.note?.trim() || '',
  })

  const handleCreateSubmit = async () => {
    if (!canCreate) return
    try {
      setCreateLoading(true)
      setCreateError('')
      await api.post('', buildPayload(createForm), getAuthConfig())
      closeCreateModal()
      await loadData()
    } catch (err) {
      setCreateError(extractErrorMessage(err, 'Không thể tạo downtime.'))
    } finally {
      setCreateLoading(false)
    }
  }

  const openEditModal = (item) => {
    if (!canEdit) return
    setEditError('')
    setEditingDowntimeId(item?.id)
    setEditForm({
      assetId: item?.assetId || '',
      workOrderId: item?.workOrderId || '',
      reason: item?.reason || 'BREAKDOWN',
      startsOn: toDateTimeLocal(item?.startsOn),
      endsOn: toDateTimeLocal(item?.endsOn),
      note: item?.note || '',
    })
    setEditOpen(true)
  }

  const closeEditModal = () => {
    if (editLoading) return
    setEditOpen(false)
    setEditError('')
    setEditingDowntimeId(null)
    setEditForm(emptyForm)
  }

  const handleEditFormChange = (field, value) => {
    setEditForm((prev) => ({ ...prev, [field]: value }))
  }

  const handleEditSubmit = async () => {
    if (!editingDowntimeId || !canEdit) return
    try {
      setEditLoading(true)
      setEditError('')
      await api.put(`/${editingDowntimeId}`, buildPayload(editForm), getAuthConfig())
      closeEditModal()
      await loadData()
    } catch (err) {
      setEditError(extractErrorMessage(err, 'Không thể cập nhật downtime.'))
    } finally {
      setEditLoading(false)
    }
  }

  const openDeleteModal = (item) => {
    if (!canDelete) return
    setDeleteError('')
    setDeleteTarget(item)
    setDeleteOpen(true)
  }

  const closeDeleteModal = () => {
    if (deleteLoadingId) return
    setDeleteOpen(false)
    setDeleteError('')
    setDeleteTarget(null)
  }

  const handleDeleteConfirm = async () => {
    const id = deleteTarget?.id
    if (!id || !canDelete) return
    try {
      setDeleteLoadingId(id)
      setDeleteError('')
      await api.delete(`/${id}`, getAuthConfig())
      closeDeleteModal()
      await loadData()
    } catch (err) {
      setDeleteError(extractErrorMessage(err, 'Không thể xóa downtime.'))
    } finally {
      setDeleteLoadingId(null)
    }
  }

  const closeDetail = () => {
    setDetailOpen(false)
    setDetailError('')
    setSelectedDowntime(null)
  }

  const visiblePages = useMemo(() => getVisiblePages(pageNum, totalPages), [pageNum, totalPages])
  const startItem = totalElements === 0 ? 0 : pageNum * pageSize + 1
  const endItem = totalElements === 0 ? 0 : Math.min((pageNum + 1) * pageSize, totalElements)
  const showAnyAction = canViewDetail || canEdit || canDelete

  return (
    <>
      <div className="assets-page">
        <div className="assets-card">
          <div className="assets-header">
            <div className="assets-header__top">
              <div className="assets-header__intro">
                <div className="assets-header__mini-title">{pageTitle}</div>
              </div>
            </div>

            <div className="filters-panel">
              <div className="filters-panel__header">
                <div className="filters-panel__title-wrap">
                  <div className="filters-panel__icon">
                    <FiFilter size={18} />
                  </div>
                  <div>
                    <div className="filters-panel__title">Bộ lọc downtime</div>
                  </div>
                </div>

                <div className="filters-panel__header-right">
                  {hasActiveFilters && <div className="filters-active-chip">Đang áp dụng {activeFilterCount} bộ lọc</div>}
                  {canCreate && (
                    <button className="btn btn-soft-blue btn-create-header" onClick={openCreateModal} type="button">
                      <FiPlus size={16} />
                      <span>Thêm mới</span>
                    </button>
                  )}
                </div>
              </div>

              <div className="filters-grid filters-grid--5">
                <div className="filter-field">
                  <label className="filter-label">Từ khóa</label>
                  <div className="search-box">
                    <FiSearch size={16} />
                    <input
                      type="text"
                      placeholder="Tìm kiếm"
                      value={filterForm.keyword}
                      onChange={(e) => handleFilterChange('keyword', e.target.value)}
                      onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    />
                  </div>
                </div>

                <div className="filter-field">
                  <label className="filter-label">Nguyên nhân</label>
                  <select className="filter-select" value={filterForm.reason} onChange={(e) => handleFilterChange('reason', e.target.value)}>
                    {REASON_OPTIONS.map((option) => (
                      <option key={option.value || 'all'} value={option.value}>{option.label}</option>
                    ))}
                  </select>
                </div>

                <div className="filter-field">
                  <label className="filter-label">Trạng thái</label>
                  <select className="filter-select" value={filterForm.openOnly} onChange={(e) => handleFilterChange('openOnly', e.target.value)}>
                    <option value="">Tất cả</option>
                    <option value="OPEN">Đang mở</option>
                    <option value="CLOSED">Đã đóng</option>
                  </select>
                </div>

                <div className="filter-field">
                  <label className="filter-label">Ngày bắt đầu</label>
                  <select className="filter-select" value={filterForm.dateSort} onChange={(e) => handleFilterChange('dateSort', e.target.value)}>
                    <option value="newest">Gần nhất</option>
                    <option value="oldest">Xa nhất</option>
                  </select>
                </div>

                <div className="filter-field filter-field--actions">
                  <label className="filter-label filter-label--ghost">Thao tác</label>
                  <div className="filter-actions-row">
                    <button className="btn btn-primary btn-search-compact" onClick={handleSearch} type="button">
                      <FiSearch size={15} />
                      <span>Tìm kiếm</span>
                    </button>
                    <button className="btn btn-light btn-icon-only" onClick={handleResetFilters} title="Xóa bộ lọc" aria-label="Xóa bộ lọc" type="button">
                      <FiRotateCcw size={16} />
                    </button>
                  </div>
                </div>
              </div>

              {hasActiveFilters && (
                <div className="applied-filters">
                  {appliedFilters.keyword && (
                    <span className="applied-filter-chip">Từ khóa: <strong>{appliedFilters.keyword}</strong></span>
                  )}
                  {appliedFilters.reason && (
                    <span className="applied-filter-chip">Nguyên nhân: <strong>{getReasonLabel(appliedFilters.reason)}</strong></span>
                  )}
                  {appliedFilters.openOnly && (
                    <span className="applied-filter-chip">Trạng thái: <strong>{appliedFilters.openOnly === 'OPEN' ? 'Đang mở' : 'Đã đóng'}</strong></span>
                  )}
                  {appliedFilters.dateSort && (
                    <span className="applied-filter-chip">Sắp xếp ngày: <strong>{getDateSortLabel(appliedFilters.dateSort)}</strong></span>
                  )}
                </div>
              )}
            </div>
          </div>

          {loading ? (
            <div className="assets-message">Đang tải dữ liệu...</div>
          ) : error ? (
            <div className="assets-message assets-message--error">{error}</div>
          ) : pagedDowntimes.length === 0 ? (
            <div className="assets-message">Không có downtime nào{hasActiveFilters ? ' phù hợp với bộ lọc hiện tại.' : '.'}</div>
          ) : (
            <div className="list-section">
              <div className="list-section__title">
                Lịch sử downtime
                <span className="list-badge">{totalElements}</span>
              </div>

              <div className="table-wrap">
                <table className="assets-table asset-downtime-table">
                  <thead>
                    <tr>
                      <th>STT</th>
                      <th>Thiết bị</th>
                      <th>Nguyên nhân</th>
                      <th>Trạng thái</th>
                      <th>Bắt đầu</th>
                      <th>Kết thúc</th>
                      <th>Thời lượng</th>
                      <th>Work Order</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pagedDowntimes.map((item, index) => (
                      <tr key={item.id}>
                        <td>{pageNum * pageSize + index + 1}</td>
                        <td>
                          <div className="asset-name-cell">
                            <strong>{item.assetName || '-'}</strong>
                            <small>Asset ID: {item.assetId || '-'}</small>
                          </div>
                        </td>
                        <td>
                          <div className="asset-name-cell">
                            <strong>{getReasonLabel(item.reason)}</strong>
                            {item.note && <small>{item.note}</small>}
                          </div>
                        </td>
                        <td><span className={getOpenBadgeClass(item.open)}>{getOpenStatusLabel(item.open)}</span></td>
                        <td>{formatDateTime(item.startsOn)}</td>
                        <td>{formatDateTime(item.endsOn)}</td>
                        <td>{formatDuration(item.durationSeconds)}</td>
                        <td>{item.workOrderId || '-'}</td>
                        <td>
                          {showAnyAction ? (
                            <div className="action-group">
                              {canViewDetail && (
                                <button className="icon-btn" onClick={() => handleViewDetails(item)} title="Xem chi tiết" type="button">
                                  <FiEye size={16} />
                                </button>
                              )}
                              {canEdit && (
                                <button className="icon-btn" onClick={() => openEditModal(item)} title="Sửa" type="button">
                                  <FiEdit2 size={16} />
                                </button>
                              )}
                              {canDelete && (
                                <button className="icon-btn icon-btn--danger" onClick={() => openDeleteModal(item)} title="Xóa" disabled={deleteLoadingId === item.id} type="button">
                                  {deleteLoadingId === item.id ? <FiAlertTriangle size={16} /> : <FiTrash2 size={16} />}
                                </button>
                              )}
                            </div>
                          ) : (
                            <span className="text-muted">-</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="pagination-bar">
                <div className="pagination-info">
                  Hiển thị <strong>{startItem}</strong> - <strong>{endItem}</strong> / <strong>{totalElements}</strong> bản ghi
                </div>
                <div className="pagination-right">
                  {totalPages > 0 && (
                    <div className="pagination-controls">
                      <button className="page-btn" disabled={pageNum === 0} onClick={() => handlePageChange(pageNum - 1)} type="button"><FiChevronLeft size={16} /></button>
                      {visiblePages.map((page, index) =>
                        typeof page === 'string' ? (
                          <span key={`${page}-${index}`} className="page-ellipsis">...</span>
                        ) : (
                          <button key={page} className={`page-number ${page === pageNum ? 'active' : ''}`} onClick={() => handlePageChange(page)} type="button">
                            {page + 1}
                          </button>
                        ),
                      )}
                      <button className="page-btn" disabled={lastPage || totalPages === 0} onClick={() => handlePageChange(pageNum + 1)} type="button"><FiChevronRight size={16} /></button>
                    </div>
                  )}
                  <select className="page-size-select page-size-select--bottom" value={pageSize} onChange={handlePageSizeChange} disabled={loading}>
                    <option value={5}>05 / Trang</option>
                    <option value={10}>10 / Trang</option>
                    <option value={20}>20 / Trang</option>
                  </select>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {detailOpen && (
        <div className="drawer-overlay" onClick={closeDetail}>
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Chi tiết downtime</h2>
                <p>Xem thông tin đầy đủ của downtime thiết bị</p>
              </div>
              <button className="drawer-close" onClick={closeDetail} type="button"><FiX size={22} /></button>
            </div>

            {detailLoading ? (
              <div className="drawer-message">Đang tải chi tiết...</div>
            ) : detailError ? (
              <div className="drawer-message drawer-message--error">{detailError}</div>
            ) : !selectedDowntime ? (
              <div className="drawer-message">Không có dữ liệu chi tiết.</div>
            ) : (
              <div className="drawer-body">
                <div className="detail-hero">
                  <div className="detail-hero__left">
                    <div className="detail-hero__icon"><FiClock size={30} /></div>
                    <div className="detail-hero__content">
                      <h3>{selectedDowntime.assetName || 'Asset Downtime'}</h3>
                      <p>{getReasonLabel(selectedDowntime.reason)}</p>
                      <div className="detail-hero__meta">
                        <span className={getOpenBadgeClass(selectedDowntime.open)}>{getOpenStatusLabel(selectedDowntime.open)}</span>
                        <span className="hero-chip"><FiTool size={14} />Asset ID: {selectedDowntime.assetId || '-'}</span>
                        <span className="hero-chip"><FiTag size={14} />WO: {selectedDowntime.workOrderId || '-'}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="detail-section">
                  <div className="detail-section__title">Thông tin downtime</div>
                  <div className="detail-grid detail-grid--2">
                    <DetailItem icon={<FiTool size={16} />} label="Thiết bị" value={selectedDowntime.assetName} compact />
                    <DetailItem icon={<FiTag size={16} />} label="Asset ID" value={selectedDowntime.assetId} compact />
                    <DetailItem icon={<FiTag size={16} />} label="Work Order ID" value={selectedDowntime.workOrderId} compact />
                    <DetailItem icon={<FiInfo size={16} />} label="Nguyên nhân" value={getReasonLabel(selectedDowntime.reason)} compact />
                    <DetailItem icon={<FiCalendar size={16} />} label="Bắt đầu" value={formatDateTime(selectedDowntime.startsOn)} compact />
                    <DetailItem icon={<FiCalendar size={16} />} label="Kết thúc" value={formatDateTime(selectedDowntime.endsOn)} compact />
                    <DetailItem icon={<FiClock size={16} />} label="Thời lượng" value={formatDuration(selectedDowntime.durationSeconds)} compact />
                    <DetailItem icon={<FiCalendar size={16} />} label="Tạo lúc" value={formatDateTime(selectedDowntime.createdAt)} compact />
                    <DetailItem icon={<FiInfo size={16} />} label="Ghi chú" value={selectedDowntime.note} full />
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {createOpen && (
        <div className="drawer-overlay" onClick={closeCreateModal}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Thêm mới downtime</h2>
                <p>Tạo downtime mới theo đúng BE hiện tại</p>
              </div>
              <button className="drawer-close" onClick={closeCreateModal} type="button"><FiX size={22} /></button>
            </div>
            <div className="drawer-body">
              {createError && <div className="drawer-message drawer-message--error drawer-message--inline">{createError}</div>}
              <div className="form-section">
                <div className="detail-section__title">Thông tin downtime</div>
                <div className="form-grid">
                  <FormField label="Thiết bị">
                    <select className="form-input" value={createForm.assetId} onChange={(e) => handleCreateFormChange('assetId', e.target.value)}>
                      <option value="">Chọn thiết bị</option>
                      {assetOptions.map((asset) => (
                        <option key={asset.id} value={asset.id}>
                          {asset.name || asset.barcode || `Asset #${asset.id}`}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Work Order ID">
                    <input className="form-input" value={createForm.workOrderId} onChange={(e) => handleCreateFormChange('workOrderId', e.target.value)} placeholder="Có thể bỏ trống" />
                  </FormField>

                  <FormField label="Nguyên nhân">
                    <select className="form-input" value={createForm.reason} onChange={(e) => handleCreateFormChange('reason', e.target.value)}>
                      {FORM_REASON_OPTIONS.map((item) => (
                        <option key={item.value} value={item.value}>{item.label}</option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Bắt đầu">
                    <input type="datetime-local" className="form-input" value={createForm.startsOn} onChange={(e) => handleCreateFormChange('startsOn', e.target.value)} />
                  </FormField>

                  <FormField label="Kết thúc">
                    <input type="datetime-local" className="form-input" value={createForm.endsOn} onChange={(e) => handleCreateFormChange('endsOn', e.target.value)} />
                  </FormField>

                  <FormField label="Ghi chú" full>
                    <textarea className="form-input form-textarea" value={createForm.note} onChange={(e) => handleCreateFormChange('note', e.target.value)} placeholder="Nhập ghi chú downtime" />
                  </FormField>
                </div>
              </div>
            </div>
            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeCreateModal} disabled={createLoading} type="button">Hủy</button>
              <button className="btn btn-primary" onClick={handleCreateSubmit} disabled={createLoading} type="button"><FiSave size={16} /><span>{createLoading ? 'Đang lưu...' : 'Lưu downtime'}</span></button>
            </div>
          </div>
        </div>
      )}

      {editOpen && (
        <div className="drawer-overlay" onClick={closeEditModal}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Cập nhật downtime</h2>
                <p>Chỉnh sửa downtime và gửi đúng API backend</p>
              </div>
              <button className="drawer-close" onClick={closeEditModal} type="button"><FiX size={22} /></button>
            </div>
            <div className="drawer-body">
              {editError && <div className="drawer-message drawer-message--error drawer-message--inline">{editError}</div>}
              <div className="form-section">
                <div className="detail-section__title">Thông tin downtime</div>
                <div className="form-grid">
                  <FormField label="Thiết bị">
                    <select className="form-input" value={editForm.assetId} onChange={(e) => handleEditFormChange('assetId', e.target.value)}>
                      <option value="">Chọn thiết bị</option>
                      {assetOptions.map((asset) => (
                        <option key={asset.id} value={asset.id}>
                          {asset.name || asset.barcode || `Asset #${asset.id}`}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Work Order ID">
                    <input className="form-input" value={editForm.workOrderId} onChange={(e) => handleEditFormChange('workOrderId', e.target.value)} placeholder="Có thể bỏ trống" />
                  </FormField>

                  <FormField label="Nguyên nhân">
                    <select className="form-input" value={editForm.reason} onChange={(e) => handleEditFormChange('reason', e.target.value)}>
                      {FORM_REASON_OPTIONS.map((item) => (
                        <option key={item.value} value={item.value}>{item.label}</option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Bắt đầu">
                    <input type="datetime-local" className="form-input" value={editForm.startsOn} onChange={(e) => handleEditFormChange('startsOn', e.target.value)} />
                  </FormField>

                  <FormField label="Kết thúc">
                    <input type="datetime-local" className="form-input" value={editForm.endsOn} onChange={(e) => handleEditFormChange('endsOn', e.target.value)} />
                  </FormField>

                  <FormField label="Ghi chú" full>
                    <textarea className="form-input form-textarea" value={editForm.note} onChange={(e) => handleEditFormChange('note', e.target.value)} placeholder="Nhập ghi chú downtime" />
                  </FormField>
                </div>
              </div>
            </div>
            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeEditModal} disabled={editLoading} type="button">Hủy</button>
              <button className="btn btn-primary" onClick={handleEditSubmit} disabled={editLoading} type="button"><FiSave size={16} /><span>{editLoading ? 'Đang lưu...' : 'Lưu cập nhật'}</span></button>
            </div>
          </div>
        </div>
      )}

      {deleteOpen && (
        <div className="drawer-overlay" onClick={closeDeleteModal}>
          <div className="drawer drawer--small" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Xóa downtime</h2>
                <p>Xác nhận xóa bản ghi downtime này</p>
              </div>
              <button className="drawer-close" onClick={closeDeleteModal} type="button"><FiX size={22} /></button>
            </div>
            <div className="drawer-body">
              {deleteError && <div className="drawer-message drawer-message--error drawer-message--inline">{deleteError}</div>}
              <div className="delete-box">
                <div className="delete-box__icon"><FiAlertTriangle size={28} /></div>
                <div className="delete-box__content">
                  <h3>{deleteTarget?.assetName || 'Asset Downtime'}</h3>
                  <p>Bạn có chắc muốn xóa downtime với nguyên nhân <strong>{getReasonLabel(deleteTarget?.reason)}</strong> không?</p>
                </div>
              </div>
            </div>
            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeDeleteModal} disabled={Boolean(deleteLoadingId)} type="button">Hủy</button>
              <button className="btn btn-danger-solid" onClick={handleDeleteConfirm} disabled={Boolean(deleteLoadingId)} type="button"><FiTrash2 size={16} /><span>{deleteLoadingId ? 'Đang xóa...' : 'Xóa downtime'}</span></button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}