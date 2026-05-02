import React, { useCallback, useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import './location.css'
import {
  FiSearch,
  FiEye,
  FiEdit2,
  FiTrash2,
  FiLayers,
  FiPlus,
  FiX,
  FiSave,
  FiMapPin,
  FiInfo,
  FiTag,
} from 'react-icons/fi'

const API_BASE_URL = 'http://localhost:8080/api/locations'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
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
    headers: token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : {},
  }
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status

    if (status === 401) {
      alert('Phiên đăng nhập hết hạn hoặc chưa đăng nhập!')
      localStorage.clear()
      sessionStorage.clear()
      window.location.href = '/login'
    }

    return Promise.reject(error)
  },
)

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

const decodeBase64Url = (value) => {
  try {
    let base64 = String(value).replace(/-/g, '+').replace(/_/g, '/')
    const padding = base64.length % 4
    if (padding) base64 += '='.repeat(4 - padding)
    return atob(base64)
  } catch {
    return ''
  }
}

const parseJwtPayload = () => {
  try {
    const token = getToken()
    if (!token) return {}
    const parts = token.split('.')
    if (parts.length < 2) return {}
    const decoded = decodeBase64Url(parts[1])
    if (!decoded) return {}
    return JSON.parse(decoded)
  } catch {
    return {}
  }
}

const getUserContext = () => {
  const userRaw = localStorage.getItem('user') || sessionStorage.getItem('user')
  const rolesRaw = localStorage.getItem('roles') || sessionStorage.getItem('roles')
  const authoritiesRaw =
    localStorage.getItem('authorities') || sessionStorage.getItem('authorities')
  const permissionsRaw =
    localStorage.getItem('permissions') || sessionStorage.getItem('permissions')
  const roleRaw = localStorage.getItem('role') || sessionStorage.getItem('role') || ''

  const storedUser = safeJsonParse(userRaw, {})
  const jwtPayload = parseJwtPayload()

  const roles = normalizeToArray(safeJsonParse(rolesRaw, rolesRaw || storedUser?.roles || []))
  const authorities = normalizeToArray(
    safeJsonParse(authoritiesRaw, authoritiesRaw || storedUser?.authorities || []),
  )
  const permissions = normalizeToArray(
    safeJsonParse(permissionsRaw, permissionsRaw || storedUser?.permissions || []),
  )
  const singleRole = normalizeToArray(roleRaw)

  const jwtRoles = [
    ...(Array.isArray(jwtPayload?.roles) ? jwtPayload.roles : []),
    ...(Array.isArray(jwtPayload?.role)
      ? jwtPayload.role
      : jwtPayload?.role
      ? [jwtPayload.role]
      : []),
    ...(Array.isArray(jwtPayload?.authorities)
      ? jwtPayload.authorities
      : jwtPayload?.authorities
      ? [jwtPayload.authorities]
      : []),
    ...(typeof jwtPayload?.scope === 'string' ? jwtPayload.scope.split(' ') : []),
  ]

  const merged = [
    ...roles,
    ...authorities,
    ...permissions,
    ...singleRole,
    ...(Array.isArray(storedUser?.roles) ? storedUser.roles : []),
    ...(Array.isArray(storedUser?.authorities) ? storedUser.authorities : []),
    ...(Array.isArray(storedUser?.permissions) ? storedUser.permissions : []),
    ...jwtRoles,
  ]
    .map(extractGrantValue)
    .filter(Boolean)
    .map(normalizeGrant)
    .filter(Boolean)

  return {
    user: {
      ...jwtPayload,
      ...storedUser,
    },
    grants: Array.from(new Set(merged)),
  }
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
    return 'Không nhận được phản hồi từ backend. Kiểm tra backend/CORS/network.'
  }

  return err.message || fallback
}

const emptyLocationForm = {
  name: '',
  address: '',
  parentLocation: '',
  vendors: '',
  contractors: '',
}

const normalizeText = (value) => {
  if (value == null) return '-'
  if (Array.isArray(value)) {
    const joined = value.join(', ').trim()
    return joined || '-'
  }
  const str = String(value).trim()
  return str || '-'
}

const splitCommaValues = (value) => {
  const raw = value == null ? '' : String(value)
  return raw
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function DetailItem({ icon, label, value, full = false }) {
  return (
    <div className={`detail-item ${full ? 'detail-item--full' : ''}`}>
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

export default function Location() {
  const token = getToken()
  const { user, grants } = useMemo(() => getUserContext(), [token])
  const isAuthenticated = Boolean(token)
  const jwtPayload = useMemo(() => parseJwtPayload(), [token])

  const fallbackRoles = [
    ...(Array.isArray(jwtPayload?.roles) ? jwtPayload.roles : []),
    ...(Array.isArray(jwtPayload?.authorities)
      ? jwtPayload.authorities
      : jwtPayload?.authorities
      ? [jwtPayload.authorities]
      : []),
    ...(jwtPayload?.role ? [jwtPayload.role] : []),
  ]
    .map(extractGrantValue)
    .filter(Boolean)
    .map(normalizeGrant)

  const effectiveGrants = grants.length > 0 ? grants : Array.from(new Set(fallbackRoles))

  const canViewDetail = isAuthenticated
  const canCreate = hasAnyGrant(effectiveGrants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canEdit = hasAnyGrant(effectiveGrants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canDelete = hasAnyGrant(effectiveGrants, ['ADMIN'])
  const canViewHierarchy = isAuthenticated

  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [locations, setLocations] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [detailOpen, setDetailOpen] = useState(false)
  const [selectedLocation, setSelectedLocation] = useState(null)

  const [createOpen, setCreateOpen] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [createError, setCreateError] = useState('')
  const [createForm, setCreateForm] = useState(emptyLocationForm)

  const [editOpen, setEditOpen] = useState(false)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const [editForm, setEditForm] = useState(emptyLocationForm)
  const [editingId, setEditingId] = useState(null)

  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteError, setDeleteError] = useState('')
  const [deleteLoadingId, setDeleteLoadingId] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)

  const handleSearch = () => {
    setSearch(searchInput.trim())
  }

  const filteredLocations = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    if (!keyword) return locations

    return locations.filter((item) => {
      const fields = [
        item?.name,
        item?.address,
        item?.parentLocation,
        item?.vendors,
        item?.contractors,
      ]

      return fields.some((field) => String(field || '').toLowerCase().includes(keyword))
    })
  }, [locations, search])

  const loadLocations = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const response = await api.get('', getAuthConfig())
      setLocations(Array.isArray(response?.data) ? response.data : [])
    } catch (err) {
      setError(extractErrorMessage(err, 'Không thể tải danh sách vị trí.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadLocations()
  }, [loadLocations])

  const openCreateModal = () => {
    if (!canCreate) {
      alert('Bạn không có quyền thêm mới vị trí.')
      return
    }
    setCreateError('')
    setCreateForm(emptyLocationForm)
    setCreateOpen(true)
  }

  const closeCreateModal = () => {
    if (createLoading) return
    setCreateOpen(false)
    setCreateError('')
    setCreateForm(emptyLocationForm)
  }

  const handleCreateFormChange = (field, value) => {
    setCreateForm((prev) => ({ ...prev, [field]: value }))
  }

  const openEditModal = (location) => {
    if (!canEdit) {
      alert('Bạn không có quyền sửa vị trí.')
      return
    }

    setEditError('')
    setEditingId(location?.id || null)
    setEditForm({
      name: location?.name || '',
      address: location?.address || '',
      parentLocation: location?.parentLocation || '',
      vendors: location?.vendors || '',
      contractors: location?.contractors || '',
    })
    setEditOpen(true)
  }

  const closeEditModal = () => {
    if (editLoading) return
    setEditOpen(false)
    setEditError('')
    setEditingId(null)
    setEditForm(emptyLocationForm)
  }

  const handleEditFormChange = (field, value) => {
    setEditForm((prev) => ({ ...prev, [field]: value }))
  }

  const buildPayload = (form) => ({
    name: form.name?.trim() || '',
    address: form.address?.trim() || '',
    parentLocation: form.parentLocation?.trim() || '',
    vendors: form.vendors?.trim() || '',
    contractors: form.contractors?.trim() || '',
  })

  const handleCreateSubmit = async () => {
    if (!canCreate) return

    if (!createForm.name.trim()) {
      setCreateError('Tên vị trí là bắt buộc.')
      return
    }

    try {
      setCreateLoading(true)
      setCreateError('')
      await api.post('', buildPayload(createForm), getAuthConfig())
      closeCreateModal()
      await loadLocations()
    } catch (err) {
      setCreateError(extractErrorMessage(err, 'Không thể tạo vị trí.'))
    } finally {
      setCreateLoading(false)
    }
  }

  const handleEditSubmit = async () => {
    if (!editingId || !canEdit) return

    if (!editForm.name.trim()) {
      setEditError('Tên vị trí là bắt buộc.')
      return
    }

    try {
      setEditLoading(true)
      setEditError('')
      await api.put(`/${editingId}`, buildPayload(editForm), getAuthConfig())
      closeEditModal()
      await loadLocations()
    } catch (err) {
      setEditError(extractErrorMessage(err, 'Không thể cập nhật vị trí.'))
    } finally {
      setEditLoading(false)
    }
  }

  const openDeleteModal = (location) => {
    if (!canDelete) {
      alert('Chỉ ADMIN mới có quyền xóa vị trí.')
      return
    }
    setDeleteError('')
    setDeleteTarget(location)
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
      await loadLocations()
    } catch (err) {
      setDeleteError(extractErrorMessage(err, 'Không thể xóa vị trí.'))
    } finally {
      setDeleteLoadingId(null)
    }
  }

  const handleViewDetails = (location) => {
    if (!canViewDetail) return
    setSelectedLocation(location)
    setDetailOpen(true)
  }

  const closeDetail = () => {
    setDetailOpen(false)
    setSelectedLocation(null)
  }

  const handleViewHierarchy = (location) => {
    if (!canViewHierarchy) return
    alert(`Xem cây vị trí: ${location?.name || '-'}`)
  }

  return (
    <>
      <div className="location-page">
        <div className="card">
          <div className="assets-header">
            <div className="assets-header__top">
              <div className="assets-header__intro">
                <div className="assets-header__mini-title">Vị trí</div>
              </div>
            </div>

            <div className="filters-panel">
              <div className="filters-panel__header">
                <div className="filters-panel__title-wrap">
                  <div className="filters-panel__icon">
                    <FiMapPin size={18} />
                  </div>

                  <div>
                    <div className="filters-panel__title">Danh sách vị trí</div>
                  </div>
                </div>

                <div className="filters-panel__header-right">
                  {canCreate && (
                    <button
                      className="btn btn-soft-blue btn-create-header"
                      onClick={openCreateModal}
                      type="button"
                    >
                      <FiPlus size={16} />
                      <span>Thêm mới</span>
                    </button>
                  )}
                </div>
              </div>

              <div className="filters-grid filters-grid--1">
                <div className="filter-field">
                  <label className="filter-label">Tìm kiếm</label>
                  <div className="search-box">
                    <FiSearch size={16} />
                    <input
                      type="text"
                      placeholder="Tìm kiếm"
                      value={searchInput}
                      onChange={(e) => setSearchInput(e.target.value)}
                      onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    />
                    <button className="btn btn-search" onClick={handleSearch} type="button">
                      Tìm kiếm
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {loading ? (
            <div className="assets-message">Đang tải dữ liệu...</div>
          ) : error ? (
            <div className="assets-message assets-message--error">{error}</div>
          ) : filteredLocations.length === 0 ? (
            <div className="assets-message">Không có vị trí nào.</div>
          ) : (
            <div className="list-section">
              <div className="list-section__title">
                Danh sách vị trí
                <span className="list-badge">{filteredLocations.length}</span>
              </div>

              <div className="table-wrap">
                <table className="assets-table">
                  <thead>
                    <tr>
                      <th>STT</th>
                      <th>Tên</th>
                      <th>Địa chỉ</th>
                      <th>Parent</th>
                      <th>Vendors</th>
                      <th>Contractors</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>

                  <tbody>
                    {filteredLocations.map((location, index) => (
                      <tr key={location?.id}>
                        <td>{index + 1}</td>
                        <td>
                          <div className="asset-name-cell">
                            <strong>{normalizeText(location?.name)}</strong>
                          </div>
                        </td>
                        <td>{normalizeText(location?.address)}</td>
                        <td>
                          <span className="badge badge--info">
                            {normalizeText(location?.parentLocation)}
                          </span>
                        </td>
                        <td>
                          {splitCommaValues(location?.vendors).length > 0 ? (
                            <div className="tag-list">
                              {splitCommaValues(location?.vendors).map((item, idx) => (
                                <span key={`${item}-${idx}`} className="tag-chip vendor-chip">
                                  {item}
                                </span>
                              ))}
                            </div>
                          ) : (
                            '-'
                          )}
                        </td>
                        <td>
                          {splitCommaValues(location?.contractors).length > 0 ? (
                            <div className="tag-list">
                              {splitCommaValues(location?.contractors).map((item, idx) => (
                                <span key={`${item}-${idx}`} className="tag-chip contractor-chip">
                                  {item}
                                </span>
                              ))}
                            </div>
                          ) : (
                            'Không có'
                          )}
                        </td>
                        <td>
                          <div className="action-group">
                            {canViewDetail && (
                              <button
                                className="icon-btn"
                                onClick={() => handleViewDetails(location)}
                                title="Xem chi tiết"
                                type="button"
                              >
                                <FiEye size={16} />
                              </button>
                            )}

                            {canEdit && (
                              <button
                                className="icon-btn"
                                onClick={() => openEditModal(location)}
                                title="Sửa"
                                type="button"
                              >
                                <FiEdit2 size={16} />
                              </button>
                            )}

                            {canDelete && (
                              <button
                                className="icon-btn icon-btn--danger"
                                onClick={() => openDeleteModal(location)}
                                title="Xóa"
                                disabled={deleteLoadingId === location?.id}
                                type="button"
                              >
                                <FiTrash2 size={16} />
                              </button>
                            )}

                            {canViewHierarchy && (
                              <button
                                className="icon-btn"
                                onClick={() => handleViewHierarchy(location)}
                                title="Xem cây vị trí"
                                type="button"
                              >
                                <FiLayers size={16} />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>

      {detailOpen && (
        <div className="drawer-overlay" onClick={closeDetail}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Chi tiết vị trí</h2>
                <p>Xem thông tin đầy đủ của location</p>
              </div>
              <button className="drawer-close" onClick={closeDetail} type="button">
                <FiX size={22} />
              </button>
            </div>

            {!selectedLocation ? (
              <div className="drawer-message">Không có dữ liệu chi tiết.</div>
            ) : (
              <div className="drawer-body">
                <div className="detail-hero">
                  <div className="detail-hero__left">
                    <div className="detail-hero__icon">
                      <FiMapPin size={30} />
                    </div>

                    <div className="detail-hero__content">
                      <h3>{normalizeText(selectedLocation.name)}</h3>
                      <p>{normalizeText(selectedLocation.address)}</p>

                      <div className="detail-hero__meta">
                        <span className="hero-chip">
                          <FiMapPin size={14} />
                          {normalizeText(selectedLocation.parentLocation)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="detail-section">
                  <div className="detail-section__title">Thông tin cơ bản</div>
                  <div className="detail-grid detail-grid--2">
                    <DetailItem
                      icon={<FiMapPin size={16} />}
                      label="Tên vị trí"
                      value={normalizeText(selectedLocation.name)}
                    />
                    <DetailItem
                      icon={<FiMapPin size={16} />}
                      label="Địa chỉ"
                      value={normalizeText(selectedLocation.address)}
                    />
                    <DetailItem
                      icon={<FiLayers size={16} />}
                      label="Parent"
                      value={normalizeText(selectedLocation.parentLocation)}
                    />
                  </div>
                </div>

                <div className="detail-section">
                  <div className="detail-section__title">Thông tin mở rộng</div>
                  <div className="detail-grid detail-grid--2">
                    <DetailItem
                      icon={<FiTag size={16} />}
                      label="Vendors"
                      value={normalizeText(selectedLocation.vendors)}
                      full
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Contractors"
                      value={normalizeText(selectedLocation.contractors)}
                      full
                    />
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
                <h2>Thêm vị trí</h2>
              </div>
              <button className="drawer-close" onClick={closeCreateModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {createError && <div className="assets-message assets-message--error">{createError}</div>}

              <div className="form-section">
                <div className="detail-section__title">Thông tin chính</div>
                <div className="form-grid">
                  <FormField label="Tên vị trí">
                    <input
                      className="form-input"
                      value={createForm.name}
                      onChange={(e) => handleCreateFormChange('name', e.target.value)}
                      placeholder="Nhập tên vị trí"
                    />
                  </FormField>

                  <FormField label="Địa chỉ">
                    <input
                      className="form-input"
                      value={createForm.address}
                      onChange={(e) => handleCreateFormChange('address', e.target.value)}
                      placeholder="Nhập địa chỉ"
                    />
                  </FormField>

                  <FormField label="Parent location">
                    <input
                      className="form-input"
                      value={createForm.parentLocation}
                      onChange={(e) => handleCreateFormChange('parentLocation', e.target.value)}
                      placeholder="Nhập parent location"
                    />
                  </FormField>

                  <FormField label="Vendors">
                    <input
                      className="form-input"
                      value={createForm.vendors}
                      onChange={(e) => handleCreateFormChange('vendors', e.target.value)}
                      placeholder="Nhập nhà cung cấp"
                    />
                  </FormField>

                  <FormField label="Contractors" full>
                    <input
                      className="form-input"
                      value={createForm.contractors}
                      onChange={(e) => handleCreateFormChange('contractors', e.target.value)}
                      placeholder="Nhập nhà thầu"
                    />
                  </FormField>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeCreateModal} type="button">
                Hủy
              </button>
              <button
                className="btn btn-primary"
                onClick={handleCreateSubmit}
                disabled={createLoading}
                type="button"
              >
                <FiSave size={16} />
                <span>{createLoading ? 'Đang lưu...' : 'Lưu vị trí'}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {editOpen && (
        <div className="drawer-overlay" onClick={closeEditModal}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Cập nhật vị trí</h2>
                <p>Chỉnh sửa thông tin location</p>
              </div>
              <button className="drawer-close" onClick={closeEditModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {editError && <div className="assets-message assets-message--error">{editError}</div>}

              <div className="form-section">
                <div className="detail-section__title">Thông tin chính</div>
                <div className="form-grid">
                  <FormField label="Tên vị trí">
                    <input
                      className="form-input"
                      value={editForm.name}
                      onChange={(e) => handleEditFormChange('name', e.target.value)}
                      placeholder="Nhập tên vị trí"
                    />
                  </FormField>

                  <FormField label="Địa chỉ">
                    <input
                      className="form-input"
                      value={editForm.address}
                      onChange={(e) => handleEditFormChange('address', e.target.value)}
                      placeholder="Nhập địa chỉ"
                    />
                  </FormField>

                  <FormField label="Parent location">
                    <input
                      className="form-input"
                      value={editForm.parentLocation}
                      onChange={(e) => handleEditFormChange('parentLocation', e.target.value)}
                      placeholder="Nhập parent location"
                    />
                  </FormField>

                  <FormField label="Vendors">
                    <input
                      className="form-input"
                      value={editForm.vendors}
                      onChange={(e) => handleEditFormChange('vendors', e.target.value)}
                      placeholder="VD: Corning, Schneider Electric"
                    />
                  </FormField>

                  <FormField label="Contractors" full>
                    <input
                      className="form-input"
                      value={editForm.contractors}
                      onChange={(e) => handleEditFormChange('contractors', e.target.value)}
                      placeholder="VD: Red River Power Co."
                    />
                  </FormField>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeEditModal} type="button">
                Hủy
              </button>
              <button
                className="btn btn-primary"
                onClick={handleEditSubmit}
                disabled={editLoading}
                type="button"
              >
                <FiSave size={16} />
                <span>{editLoading ? 'Đang cập nhật...' : 'Lưu thay đổi'}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {deleteOpen && (
        <div className="drawer-overlay" onClick={closeDeleteModal}>
          <div className="drawer drawer--small" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Xóa vị trí</h2>
                <p>Xác nhận thao tác xóa location</p>
              </div>
              <button className="drawer-close" onClick={closeDeleteModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {deleteError && <div className="assets-message assets-message--error">{deleteError}</div>}

              <div className="delete-box">
                <div className="filters-panel__icon">
                  <FiInfo size={18} />
                </div>
                <div className="delete-box__content">
                  <h3>{normalizeText(deleteTarget?.name)}</h3>
                  <p>Bạn có chắc muốn xóa vị trí này không? Thao tác này không thể hoàn tác.</p>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeDeleteModal} type="button">
                Hủy
              </button>
              <button
                className="btn btn-danger-solid"
                onClick={handleDeleteConfirm}
                disabled={Boolean(deleteLoadingId)}
                type="button"
              >
                <FiTrash2 size={16} />
                <span>{deleteLoadingId ? 'Đang xóa...' : 'Xóa vị trí'}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}