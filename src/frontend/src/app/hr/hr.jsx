import React, { useCallback, useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import './hr.css'

const API_BASE = 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE,
})

api.interceptors.request.use((config) => {
  const token =
    localStorage.getItem('accessToken') ||
    localStorage.getItem('token') ||
    localStorage.getItem('jwtToken') ||
    ''

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

const EMPTY_INVITE_FORM = {
  emailsText: '',
  roleId: '',
}

const EMPTY_EDIT_FORM = {
  status: '',
  enabled: 'true',
  roleId: '',
}

function normalizeText(value) {
  return (value || '').toString().trim()
}

function toBoolean(value) {
  if (typeof value === 'boolean') return value

  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (normalized === 'true') return true
    if (normalized === 'false') return false
  }

  if (typeof value === 'number') {
    return value === 1
  }

  return !!value
}

function formatDateTime(value) {
  if (!value) return '—'
  try {
    return new Date(value).toLocaleString('vi-VN')
  } catch {
    return value
  }
}

function getDisplayName(user) {
  return (
    normalizeText(user?.fullName) ||
    `${normalizeText(user?.firstName)} ${normalizeText(user?.lastName)}`.trim() ||
    normalizeText(user?.username) ||
    '—'
  )
}

function parseEmails(text) {
  return text
    .split(/[\n,;]+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function normalizeRole(role) {
  return normalizeText(role).replace(/^ROLE_/, '').toUpperCase()
}

function displayRole(role) {
  const normalized = normalizeRole(role)

  switch (normalized) {
    case 'ADMIN':
      return 'ADMIN'
    case 'TECHNICAL_MANAGER':
      return 'QUẢN LÝ KỸ THUẬT'
    case 'TECHNICIAN':
      return 'NHÂN VIÊN KỸ THUẬT'
    case 'OPERATOR':
      return 'NHÂN VIÊN VẬN HÀNH'
    case 'REQUESTER':
      return 'NGƯỜI YÊU CẦU'
    case 'VIEWER':
      return 'NGƯỜI XEM'
    default:
      return normalized || '—'
  }
}

function displayStatus(status) {
  const normalized = normalizeText(status).toUpperCase()

  switch (normalized) {
    case 'ACTIVE':
      return 'ĐANG HOẠT ĐỘNG'
    case 'INACTIVE':
      return 'NGƯNG HOẠT ĐỘNG'
    case 'LOCKED':
      return 'ĐÃ KHÓA'
    default:
      return normalized || '—'
  }
}

function displayEnabled(enabled) {
  return toBoolean(enabled) ? 'ĐANG BẬT' : 'ĐANG TẮT'
}

function canCreateOrEditUsers(role) {
  return ['ADMIN', 'TECHNICAL_MANAGER'].includes(normalizeRole(role))
}

function canDeleteUsers(role) {
  return normalizeRole(role) === 'ADMIN'
}

function badgeClassByStatus(status) {
  const s = normalizeText(status).toUpperCase()
  if (s === 'ACTIVE') return 'badge badge--success'
  if (s === 'INACTIVE') return 'badge badge--warning'
  if (s === 'LOCKED') return 'badge badge--danger'
  return 'badge badge--default'
}

function badgeClassByEnabled(enabled) {
  return toBoolean(enabled) ? 'badge badge--success' : 'badge badge--danger'
}

function buildPageNumbers(currentPage, totalPages) {
  if (totalPages <= 0) return []

  const pages = []
  const start = Math.max(0, currentPage - 2)
  const end = Math.min(totalPages - 1, currentPage + 2)

  if (start > 0) {
    pages.push(0)
    if (start > 1) pages.push('...')
  }

  for (let i = start; i <= end; i += 1) {
    pages.push(i)
  }

  if (end < totalPages - 1) {
    if (end < totalPages - 2) pages.push('...')
    pages.push(totalPages - 1)
  }

  return pages
}

function EyeIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <path
        d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" strokeWidth="1.8" />
    </svg>
  )
}

function EditIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <path
        d="M4 20h4l10-10-4-4L4 16v4Z"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M12 6l4 4"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function DeleteIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <path
        d="M5 7h14"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
      <path
        d="M9 7V5h6v2"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M8 7l1 12h6l1-12"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M10 11v5M14 11v5"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  )
}

const HR = () => {
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [users, setUsers] = useState([])
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [sortBy, setSortBy] = useState('id')
  const [sortDir, setSortDir] = useState('asc')

  const [keywordDraft, setKeywordDraft] = useState('')
  const [keyword, setKeyword] = useState('')
  const [roleCode, setRoleCode] = useState('')
  const [status, setStatus] = useState('')
  const [enabled, setEnabled] = useState('')

  const [selectedUser, setSelectedUser] = useState(null)

  const [detailOpen, setDetailOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const [inviteOpen, setInviteOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)

  const [inviteForm, setInviteForm] = useState(EMPTY_INVITE_FORM)
  const [editForm, setEditForm] = useState(EMPTY_EDIT_FORM)

  const [me, setMe] = useState(null)

  const currentRole = useMemo(() => normalizeRole(me?.role), [me])
  const allowCreateEdit = useMemo(() => canCreateOrEditUsers(currentRole), [currentRole])
  const allowDelete = useMemo(() => canDeleteUsers(currentRole), [currentRole])

  const roleOptions = useMemo(() => {
    const set = new Set(['ADMIN', 'TECHNICAL_MANAGER', 'TECHNICIAN', 'OPERATOR'])
    users.forEach((u) => {
      const role = normalizeText(u?.role).toUpperCase()
      if (role) set.add(role)
    })
    return Array.from(set)
  }, [users])

  const clearMessages = () => {
    setError('')
    setSuccess('')
  }

  const loadMe = useCallback(async () => {
    try {
      const res = await api.get('/api/users/me')
      setMe(res?.data || null)
    } catch (err) {
      console.error('Không tải được hồ sơ hiện tại:', err)
      setMe(null)
    }
  }, [])

  const loadUsers = useCallback(async () => {
    setLoading(true)
    clearMessages()

    try {
      const params = {
        page,
        size,
        sortBy,
        sortDir,
      }

      if (normalizeText(keyword)) params.keyword = keyword.trim()
      if (normalizeText(roleCode)) params.roleCode = roleCode.trim()
      if (normalizeText(status)) params.status = status.trim()
      if (enabled !== '') params.enabled = enabled === 'true'

      const res = await api.get('/api/users', { params })
      const data = res?.data || {}

      setUsers(Array.isArray(data?.content) ? data.content : [])
      setTotalPages(Number(data?.totalPages || 0))
      setTotalElements(Number(data?.totalElements || 0))
    } catch (err) {
      console.error(err)
      const statusCode = err?.response?.status

      if (statusCode === 401) {
        setError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
      } else if (statusCode === 403) {
        setError('Bạn không có quyền truy cập màn hình Nhân sự.')
      } else {
        setError(
          err?.response?.data?.message ||
            'Không tải được danh sách nhân sự. Kiểm tra token hoặc endpoint backend.'
        )
      }

      setUsers([])
      setTotalPages(0)
      setTotalElements(0)
    } finally {
      setLoading(false)
    }
  }, [page, size, sortBy, sortDir, keyword, roleCode, status, enabled])

  useEffect(() => {
    loadMe()
  }, [loadMe])

  useEffect(() => {
    loadUsers()
  }, [loadUsers])

  const activeFilterCount = useMemo(() => {
    let count = 0
    if (normalizeText(keyword)) count += 1
    if (normalizeText(roleCode)) count += 1
    if (normalizeText(status)) count += 1
    if (enabled !== '') count += 1
    return count
  }, [keyword, roleCode, status, enabled])

  const pageNumbers = useMemo(() => buildPageNumbers(page, totalPages), [page, totalPages])

  const stats = useMemo(() => {
    return {
      active: users.filter((u) => normalizeText(u?.status).toUpperCase() === 'ACTIVE').length,
      locked: users.filter((u) => normalizeText(u?.status).toUpperCase() === 'LOCKED').length,
      enabledCount: users.filter((u) => toBoolean(u?.enabled)).length,
      managerCount: users.filter((u) =>
        ['ADMIN', 'TECHNICAL_MANAGER'].includes(normalizeText(u?.role).toUpperCase())
      ).length,
    }
  }, [users])

  const handleSearch = () => {
    setPage(0)
    setKeyword(keywordDraft)
  }

  const handleResetFilters = () => {
    setKeyword('')
    setKeywordDraft('')
    setRoleCode('')
    setStatus('')
    setEnabled('')
    setPage(0)
    setSize(10)
    setSortBy('id')
    setSortDir('asc')
  }

  const handleOpenDetail = async (id) => {
    setSubmitting(true)
    clearMessages()

    try {
      const res = await api.get(`/api/users/${id}`)
      setSelectedUser(res?.data || null)
      setDetailOpen(true)
    } catch (err) {
      console.error(err)
      const statusCode = err?.response?.status

      if (statusCode === 403) {
        setError('Bạn không có quyền xem chi tiết nhân sự này.')
      } else {
        setError(err?.response?.data?.message || 'Không tải được chi tiết nhân sự.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const handleOpenEdit = (user) => {
    setSelectedUser(user)
    setEditForm({
      status: user?.status || '',
      enabled: toBoolean(user?.enabled) ? 'true' : 'false',
      roleId: '',
    })
    setEditOpen(true)
    clearMessages()
  }

  const handleUpdateEnabled = async () => {
    if (!selectedUser) return

    setSubmitting(true)
    clearMessages()

    try {
      await api.put(`/api/users/${selectedUser.id}/enabled`, null, {
        params: { enabled: editForm.enabled === 'true' },
      })
      setSuccess('Cập nhật trạng thái kích hoạt thành công.')
      setEditOpen(false)
      loadUsers()
    } catch (err) {
      console.error(err)
      const statusCode = err?.response?.status

      if (statusCode === 403) {
        setError('Bạn không có quyền cập nhật kích hoạt nhân sự.')
      } else {
        setError(err?.response?.data?.message || 'Không cập nhật được trạng thái kích hoạt.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const handleUpdateStatus = async () => {
    if (!selectedUser || !normalizeText(editForm.status)) {
      setError('Vui lòng chọn trạng thái.')
      return
    }

    setSubmitting(true)
    clearMessages()

    try {
      await api.put(`/api/users/${selectedUser.id}/status`, null, {
        params: { status: editForm.status },
      })
      setSuccess('Cập nhật trạng thái thành công.')
      setEditOpen(false)
      loadUsers()
    } catch (err) {
      console.error(err)
      const statusCode = err?.response?.status

      if (statusCode === 403) {
        setError('Bạn không có quyền cập nhật trạng thái nhân sự.')
      } else {
        setError(err?.response?.data?.message || 'Không cập nhật được trạng thái.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const handleUpdateRole = async () => {
    if (!selectedUser || !normalizeText(editForm.roleId)) {
      setError('Vui lòng nhập roleId.')
      return
    }

    setSubmitting(true)
    clearMessages()

    try {
      await api.put(`/api/users/${selectedUser.id}/role`, null, {
        params: { roleId: Number(editForm.roleId) },
      })
      setSuccess('Cập nhật vai trò thành công.')
      setEditOpen(false)
      loadUsers()
    } catch (err) {
      console.error(err)
      const statusCode = err?.response?.status

      if (statusCode === 403) {
        setError('Bạn không có quyền cập nhật vai trò nhân sự.')
      } else {
        setError(err?.response?.data?.message || 'Không cập nhật được vai trò.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const handleOpenDelete = (user) => {
    setSelectedUser(user)
    setDeleteOpen(true)
    clearMessages()
  }

  const handleDeleteUser = async () => {
    if (!selectedUser) return

    setSubmitting(true)
    clearMessages()

    try {
      await api.delete(`/api/users/${selectedUser.id}`)
      setSuccess('Xóa nhân sự thành công.')
      setDeleteOpen(false)
      setSelectedUser(null)
      loadUsers()
    } catch (err) {
      console.error(err)
      const statusCode = err?.response?.status

      if (statusCode === 403) {
        setError('Bạn không có quyền xóa nhân sự.')
      } else {
        setError(err?.response?.data?.message || 'Không xóa được nhân sự.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const handleInvite = async (e) => {
    e.preventDefault()

    const emails = parseEmails(inviteForm.emailsText)
    if (!emails.length) {
      setError('Vui lòng nhập ít nhất một email.')
      return
    }
    if (!normalizeText(inviteForm.roleId)) {
      setError('Vui lòng nhập roleId.')
      return
    }

    setSubmitting(true)
    clearMessages()

    try {
      await api.post('/api/users/invite', {
        emails,
        roleId: Number(inviteForm.roleId),
      })
      setSuccess('Gửi lời mời thành công.')
      setInviteForm(EMPTY_INVITE_FORM)
      setInviteOpen(false)
      loadUsers()
    } catch (err) {
      console.error(err)
      const statusCode = err?.response?.status

      if (statusCode === 403) {
        setError('Bạn không có quyền mời nhân sự.')
      } else {
        setError(err?.response?.data?.message || 'Không gửi được lời mời.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="assets-page hr-page-skin">
      <div className="assets-card">
        <div className="assets-header">
          <div className="assets-header__top">
            <div className="assets-header__intro">
              <div className="filters-panel__icon">👥</div>
              <div>
                <h1 className="assets-header__mini-title">Nhân sự</h1>
              </div>
            </div>

            <div className="filters-panel__header-right">
              <div className="filters-active-chip">Tổng: {totalElements}</div>
              <button className="btn btn-light" onClick={loadUsers} disabled={loading || submitting}>
                Làm mới
              </button>
              {allowCreateEdit && (
                <button
                  className="btn btn-primary btn-create-header"
                  onClick={() => {
                    clearMessages()
                    setInviteOpen(true)
                  }}
                >
                  + Mời nhân sự
                </button>
              )}
            </div>
          </div>
        </div>

        {error ? <div className="assets-message assets-message--error">{error}</div> : null}
        {success ? <div className="assets-message hr-success-box">{success}</div> : null}

        <div className="filters-panel">
          <div className="filters-grid filters-grid--5">
            <div className="filter-field">
              <label className="filter-label">Từ khóa</label>
              <div className="search-box">
                <input
                  type="text"
                  value={keywordDraft}
                  onChange={(e) => setKeywordDraft(e.target.value)}
                  placeholder="Nhập từ khóa"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') handleSearch()
                  }}
                />
              </div>
            </div>

            <div className="filter-field">
              <label className="filter-label">Vai trò</label>
              <select
                className="filter-select"
                value={roleCode}
                onChange={(e) => {
                  setRoleCode(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">Tất cả vai trò</option>
                {roleOptions.map((item) => (
                  <option key={item} value={item}>
                    {displayRole(item)}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-field">
              <label className="filter-label">Trạng thái</label>
              <select
                className="filter-select"
                value={status}
                onChange={(e) => {
                  setStatus(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">Tất cả trạng thái</option>
                <option value="ACTIVE">ĐANG HOẠT ĐỘNG</option>
                <option value="INACTIVE">NGƯNG HOẠT ĐỘNG</option>
                <option value="LOCKED">ĐÃ KHÓA</option>
              </select>
            </div>

            <div className="filter-field">
              <label className="filter-label">Kích hoạt</label>
              <select
                className="filter-select"
                value={enabled}
                onChange={(e) => {
                  setEnabled(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">Tất cả</option>
                <option value="true">ĐANG BẬT</option>
                <option value="false">ĐANG TẮT</option>
              </select>
            </div>

            <div className="filter-field filter-field--actions">
              <label className="filter-label filter-label--ghost">Hành động</label>
              <div className="filter-actions-row">
                <button className="btn btn-soft-blue btn-search-compact" onClick={handleSearch}>
                  Tìm kiếm
                </button>
                <button className="btn btn-light btn-icon-only" onClick={handleResetFilters} title="Xóa lọc">
                  ↺
                </button>
              </div>
            </div>
          </div>

          {activeFilterCount > 0 && (
            <div className="applied-filters">
              {keyword ? (
                <div className="applied-filter-chip">
                  Từ khóa: <strong>{keyword}</strong>
                </div>
              ) : null}
              {roleCode ? (
                <div className="applied-filter-chip">
                  Vai trò: <strong>{displayRole(roleCode)}</strong>
                </div>
              ) : null}
              {status ? (
                <div className="applied-filter-chip">
                  Trạng thái: <strong>{displayStatus(status)}</strong>
                </div>
              ) : null}
              {enabled !== '' ? (
                <div className="applied-filter-chip">
                  Kích hoạt: <strong>{enabled === 'true' ? 'ĐANG BẬT' : 'ĐANG TẮT'}</strong>
                </div>
              ) : null}
            </div>
          )}
        </div>

        <div className="hr-metrics-row">
          <div className="hr-metric-card">
            <div className="hr-metric-card__label">Đang hoạt động</div>
            <div className="hr-metric-card__value">{stats.active}</div>
          </div>
          <div className="hr-metric-card">
            <div className="hr-metric-card__label">Đã khóa</div>
            <div className="hr-metric-card__value">{stats.locked}</div>
          </div>
          <div className="hr-metric-card">
            <div className="hr-metric-card__label">Đang bật</div>
            <div className="hr-metric-card__value">{stats.enabledCount}</div>
          </div>
          <div className="hr-metric-card">
            <div className="hr-metric-card__label">Admin / Quản lý</div>
            <div className="hr-metric-card__value">{stats.managerCount}</div>
          </div>
        </div>

        <div className="list-section">
          <div className="list-section__title">
            Danh sách nhân sự
            <span className="list-badge">{totalElements}</span>
          </div>

          <div className="table-wrap">
            <table className="assets-table hr-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Họ tên</th>
                  <th>Email</th>
                  <th>Vai trò</th>
                  <th>Chức danh</th>
                  <th>Trạng thái</th>
                  <th>Kích hoạt</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="8" className="text-muted hr-empty-cell">
                      Đang tải dữ liệu...
                    </td>
                  </tr>
                ) : users.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="text-muted hr-empty-cell">
                      Không có dữ liệu nhân sự.
                    </td>
                  </tr>
                ) : (
                  users.map((user) => (
                    <tr key={user.id}>
                      <td>{user.id}</td>
                      <td>
                        <div className="asset-name-cell">
                          <strong>{getDisplayName(user)}</strong>
                          <small>{user.phone || 'Chưa có số điện thoại'}</small>
                        </div>
                      </td>
                      <td>{user.email || '—'}</td>
                      <td>
                        <span className="badge badge--info">{displayRole(user.role)}</span>
                      </td>
                      <td>{user.jobTitle || '—'}</td>
                      <td>
                        <span className={badgeClassByStatus(user.status)}>
                          {displayStatus(user.status)}
                        </span>
                      </td>
                      <td>
                        <span className={badgeClassByEnabled(user.enabled)}>
                          {displayEnabled(user.enabled)}
                        </span>
                      </td>
                      <td>
                        <div className="action-group">
                          <button
                            className="icon-btn"
                            title="Xem chi tiết"
                            onClick={() => handleOpenDetail(user.id)}
                          >
                            <EyeIcon />
                          </button>

                          {allowCreateEdit && (
                            <button
                              className="icon-btn"
                              title="Chỉnh sửa"
                              onClick={() => handleOpenEdit(user)}
                            >
                              <EditIcon />
                            </button>
                          )}

                          {allowDelete && (
                            <button
                              className="icon-btn icon-btn--danger"
                              title="Xóa"
                              onClick={() => handleOpenDelete(user)}
                            >
                              <DeleteIcon />
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

          <div className="pagination-bar">
            <div className="pagination-info">
              Tổng <strong>{totalElements}</strong> bản ghi
            </div>

            <div className="pagination-right">
              <select
                className="page-size-select page-size-select--bottom"
                value={size}
                onChange={(e) => {
                  setSize(Number(e.target.value))
                  setPage(0)
                }}
              >
                <option value={5}>5 / trang</option>
                <option value={10}>10 / trang</option>
                <option value={20}>20 / trang</option>
                <option value={50}>50 / trang</option>
              </select>

              <div className="pagination-controls">
                <button
                  className="page-btn"
                  disabled={page <= 0}
                  onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                >
                  ‹
                </button>

                {pageNumbers.map((item, index) =>
                  item === '...' ? (
                    <span className="page-ellipsis" key={`ellipsis-${index}`}>
                      ...
                    </span>
                  ) : (
                    <button
                      key={item}
                      className={`page-number ${item === page ? 'active' : ''}`}
                      onClick={() => setPage(item)}
                    >
                      {item + 1}
                    </button>
                  )
                )}

                <button
                  className="page-btn"
                  disabled={page >= totalPages - 1 || totalPages === 0}
                  onClick={() =>
                    setPage((prev) => Math.min(prev + 1, Math.max(totalPages - 1, 0)))
                  }
                >
                  ›
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {detailOpen && selectedUser && (
        <div className="drawer-overlay" onClick={() => setDetailOpen(false)}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Chi tiết nhân sự</h2>
                <p>Xem thông tin tài khoản và lịch sử cập nhật gần nhất.</p>
              </div>
              <button className="drawer-close" onClick={() => setDetailOpen(false)}>
                ✕
              </button>
            </div>

            <div className="drawer-body">
              <div className="detail-hero">
                <div className="detail-hero__left">
                  <div className="detail-hero__icon">👤</div>
                  <div className="detail-hero__content">
                    <h3>{getDisplayName(selectedUser)}</h3>
                    <p>{selectedUser.email || '—'}</p>
                    <div className="detail-hero__meta">
                      <span className="hero-chip">Vai trò: {displayRole(selectedUser.role)}</span>
                      <span className="hero-chip">Trạng thái: {displayStatus(selectedUser.status)}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="detail-section">
                <div className="detail-section__title">Thông tin tổng quan</div>
                <div className="detail-grid detail-grid--2">
                  <div className="detail-item">
                    <div className="detail-item__label">ID</div>
                    <div className="detail-item__value">{selectedUser.id}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Họ</div>
                    <div className="detail-item__value">{selectedUser.firstName || '—'}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Tên</div>
                    <div className="detail-item__value">{selectedUser.lastName || '—'}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Số điện thoại</div>
                    <div className="detail-item__value">{selectedUser.phone || '—'}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Email</div>
                    <div className="detail-item__value">{selectedUser.email || '—'}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Chức danh</div>
                    <div className="detail-item__value">{selectedUser.jobTitle || '—'}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Vai trò</div>
                    <div className="detail-item__value">{displayRole(selectedUser.role)}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Kích hoạt</div>
                    <div className="detail-item__value">{displayEnabled(selectedUser.enabled)}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Trạng thái</div>
                    <div className="detail-item__value">{displayStatus(selectedUser.status)}</div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Số lần đăng nhập sai</div>
                    <div className="detail-item__value">{selectedUser.failedAttempts ?? 0}</div>
                  </div>
                  <div className="detail-item detail-item--full">
                    <div className="detail-item__label">Ngày tạo</div>
                    <div className="detail-item__value">{formatDateTime(selectedUser.createdAt)}</div>
                  </div>
                  <div className="detail-item detail-item--full">
                    <div className="detail-item__label">Cập nhật gần nhất</div>
                    <div className="detail-item__value">{formatDateTime(selectedUser.updatedAt)}</div>
                  </div>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-light" onClick={() => setDetailOpen(false)}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {editOpen && selectedUser && allowCreateEdit && (
        <div className="drawer-overlay" onClick={() => setEditOpen(false)}>
          <div className="drawer drawer--small" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Chỉnh sửa nhân sự</h2>
                <p>{getDisplayName(selectedUser)}</p>
              </div>
              <button className="drawer-close" onClick={() => setEditOpen(false)}>
                ✕
              </button>
            </div>

            <div className="drawer-body">
              <div className="form-section">
                <div className="form-grid">
                  <div className="form-field">
                    <label className="form-label">Trạng thái</label>
                    <select
                      className="filter-select"
                      value={editForm.status}
                      onChange={(e) => setEditForm((prev) => ({ ...prev, status: e.target.value }))}
                    >
                      <option value="">Chọn trạng thái</option>
                      <option value="ACTIVE">ĐANG HOẠT ĐỘNG</option>
                      <option value="INACTIVE">NGƯNG HOẠT ĐỘNG</option>
                      <option value="LOCKED">ĐÃ KHÓA</option>
                    </select>
                  </div>

                  <div className="form-field">
                    <label className="form-label">Kích hoạt</label>
                    <select
                      className="filter-select"
                      value={editForm.enabled}
                      onChange={(e) => setEditForm((prev) => ({ ...prev, enabled: e.target.value }))}
                    >
                      <option value="true">ĐANG BẬT</option>
                      <option value="false">ĐANG TẮT</option>
                    </select>
                  </div>

                  <div className="form-field form-field--full">
                    <label className="form-label">roleId</label>
                    <input
                      className="form-input"
                      type="number"
                      value={editForm.roleId}
                      onChange={(e) => setEditForm((prev) => ({ ...prev, roleId: e.target.value }))}
                      placeholder="Nhập roleId để đổi vai trò"
                    />
                  </div>
                </div>

                <div className="hr-edit-note">
                  Ghi chú: backend hiện tại đổi vai trò bằng <strong>roleId</strong>, nên FE nhập trực tiếp ID role.
                </div>
              </div>
            </div>

            <div className="drawer-footer hr-drawer-footer-wrap">
              <button className="btn btn-light" onClick={() => setEditOpen(false)}>
                Hủy
              </button>
              <button className="btn btn-secondary" onClick={handleUpdateEnabled} disabled={submitting}>
                Lưu kích hoạt
              </button>
              <button className="btn btn-soft-blue" onClick={handleUpdateStatus} disabled={submitting}>
                Lưu trạng thái
              </button>
              <button className="btn btn-primary" onClick={handleUpdateRole} disabled={submitting}>
                Đổi vai trò
              </button>
            </div>
          </div>
        </div>
      )}

      {inviteOpen && allowCreateEdit && (
        <div className="drawer-overlay" onClick={() => setInviteOpen(false)}>
          <div className="drawer drawer--small" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Mời nhân sự</h2>
                <p>Gửi lời mời cho nhiều email cùng lúc.</p>
              </div>
              <button className="drawer-close" onClick={() => setInviteOpen(false)}>
                ✕
              </button>
            </div>

            <form onSubmit={handleInvite}>
              <div className="drawer-body">
                <div className="form-grid">
                  <div className="form-field form-field--full">
                    <label className="form-label">Danh sách email</label>
                    <textarea
                      className="form-textarea"
                      value={inviteForm.emailsText}
                      onChange={(e) =>
                        setInviteForm((prev) => ({ ...prev, emailsText: e.target.value }))
                      }
                      placeholder="Nhập email, ngăn cách bằng dấu phẩy, xuống dòng hoặc dấu chấm phẩy"
                    />
                  </div>

                  <div className="form-field form-field--full">
                    <label className="form-label">roleId</label>
                    <input
                      className="form-input"
                      type="number"
                      value={inviteForm.roleId}
                      onChange={(e) =>
                        setInviteForm((prev) => ({ ...prev, roleId: e.target.value }))
                      }
                      placeholder="Nhập roleId"
                    />
                  </div>
                </div>
              </div>

              <div className="drawer-footer">
                <button type="button" className="btn btn-light" onClick={() => setInviteOpen(false)}>
                  Hủy
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  Gửi lời mời
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {deleteOpen && selectedUser && allowDelete && (
        <div className="drawer-overlay" onClick={() => setDeleteOpen(false)}>
          <div className="drawer drawer--small" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Xóa nhân sự</h2>
                <p>Thao tác này chỉ dành cho Admin.</p>
              </div>
              <button className="drawer-close" onClick={() => setDeleteOpen(false)}>
                ✕
              </button>
            </div>

            <div className="drawer-body">
              <div className="delete-box">
                <div className="delete-box__icon">⚠️</div>
                <div className="delete-box__content">
                  <h3>Bạn có chắc muốn xóa?</h3>
                  <p>
                    Nhân sự <strong>{getDisplayName(selectedUser)}</strong> sẽ bị xóa khỏi hệ thống.
                    Hãy kiểm tra kỹ trước khi xác nhận.
                  </p>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-light" onClick={() => setDeleteOpen(false)}>
                Hủy
              </button>
              <button className="btn btn-danger-solid" onClick={handleDeleteUser} disabled={submitting}>
                Xóa nhân sự
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default HR