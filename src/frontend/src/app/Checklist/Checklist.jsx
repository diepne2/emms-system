import React, { useMemo, useState } from 'react'
import axios from 'axios'
import './Checklist.css'
import {
  FiSearch,
  FiPlus,
  FiTrash2,
  FiSave,
  FiX,
  FiCheckSquare,
  FiEdit2,
  FiAlertTriangle,
  FiInfo,
  FiRotateCcw,
} from 'react-icons/fi'

const API_BASE_URL = 'http://localhost:8080/api/checklists'

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
  const authoritiesRaw =
    localStorage.getItem('authorities') || sessionStorage.getItem('authorities')
  const permissionsRaw =
    localStorage.getItem('permissions') || sessionStorage.getItem('permissions')
  const roleRaw = localStorage.getItem('role') || sessionStorage.getItem('role') || ''

  const user = safeJsonParse(userRaw, {})
  const roles = normalizeToArray(safeJsonParse(rolesRaw, rolesRaw || user?.roles || []))
  const authorities = normalizeToArray(
    safeJsonParse(authoritiesRaw, authoritiesRaw || user?.authorities || []),
  )
  const permissions = normalizeToArray(
    safeJsonParse(permissionsRaw, permissionsRaw || user?.permissions || []),
  )
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

  return {
    user,
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

const TASK_TYPE_OPTIONS = [
  { value: 'PASS_FAIL', label: 'Pass / Fail' },
  { value: 'NUMBER', label: 'Number' },
  { value: 'TEXT', label: 'Text' },
]

const emptyTask = {
  label: '',
  description: '',
  taskType: 'PASS_FAIL',
}

const emptyChecklistForm = {
  name: '',
  description: '',
  appliesTo: '',
  active: true,
  tasks: [{ ...emptyTask }],
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

const getTaskTypeLabel = (value) => {
  const found = TASK_TYPE_OPTIONS.find((item) => item.value === value)
  return found?.label || value || 'Pass / Fail'
}

export default function Checklist() {
  const { grants } = useMemo(() => getUserContext(), [])
  const isAuthenticated = Boolean(getToken())

  const canCreate = isAuthenticated
  const canEdit = isAuthenticated
  const canDelete = isAuthenticated
  const canViewDetail = true

  const [lookupId, setLookupId] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [checklist, setChecklist] = useState(null)

  const [createOpen, setCreateOpen] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [createError, setCreateError] = useState('')
  const [createForm, setCreateForm] = useState(emptyChecklistForm)

  const [editOpen, setEditOpen] = useState(false)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const [editForm, setEditForm] = useState(emptyChecklistForm)

  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const [deleteError, setDeleteError] = useState('')

  const pageTitle = 'Checklist'

  const buildPayload = (form) => ({
    name: form.name?.trim() || '',
    description: form.description?.trim() || '',
    appliesTo: form.appliesTo?.trim() || '',
    active: Boolean(form.active),
    tasks: (form.tasks || [])
      .map((task) => ({
        label: task.label?.trim() || '',
        description: task.description?.trim() || '',
        taskType: task.taskType || 'PASS_FAIL',
      }))
      .filter((task) => task.label),
  })

  const handleLoadChecklist = async () => {
    if (!lookupId.trim()) {
      setError('Vui lòng nhập Checklist ID.')
      setChecklist(null)
      return
    }

    try {
      setLoading(true)
      setError('')
      const response = await api.get(`/${lookupId.trim()}`, getAuthConfig())
      setChecklist(response?.data || null)
    } catch (err) {
      setChecklist(null)
      setError(extractErrorMessage(err, 'Không thể tải checklist.'))
    } finally {
      setLoading(false)
    }
  }

  const openCreateModal = () => {
    if (!canCreate) return
    setCreateError('')
    setCreateForm({
      ...emptyChecklistForm,
      tasks: [{ ...emptyTask }],
    })
    setCreateOpen(true)
  }

  const closeCreateModal = () => {
    if (createLoading) return
    setCreateOpen(false)
    setCreateError('')
    setCreateForm(emptyChecklistForm)
  }

  const openEditModal = () => {
    if (!canEdit || !checklist) return

    setEditError('')
    setEditForm({
      name: checklist?.name || '',
      description: checklist?.description || '',
      appliesTo: checklist?.appliesTo || '',
      active: checklist?.active ?? true,
      tasks:
        Array.isArray(checklist?.tasks) && checklist.tasks.length > 0
          ? checklist.tasks.map((task) => ({
              label: task?.title || task?.label || '',
              description: task?.description || '',
              taskType: task?.taskType || 'PASS_FAIL',
            }))
          : [{ ...emptyTask }],
    })
    setEditOpen(true)
  }

  const closeEditModal = () => {
    if (editLoading) return
    setEditOpen(false)
    setEditError('')
  }

  const openDeleteModal = () => {
    if (!canDelete || !checklist?.id) return
    setDeleteError('')
    setDeleteOpen(true)
  }

  const closeDeleteModal = () => {
    if (deleteLoading) return
    setDeleteOpen(false)
    setDeleteError('')
  }

  const handleCreateFormChange = (field, value) => {
    setCreateForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleEditFormChange = (field, value) => {
    setEditForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleTaskChange = (mode, index, field, value) => {
    if (mode === 'create') {
      setCreateForm((prev) => {
        const nextTasks = [...prev.tasks]
        nextTasks[index] = {
          ...nextTasks[index],
          [field]: value,
        }
        return {
          ...prev,
          tasks: nextTasks,
        }
      })
      return
    }

    setEditForm((prev) => {
      const nextTasks = [...prev.tasks]
      nextTasks[index] = {
        ...nextTasks[index],
        [field]: value,
      }
      return {
        ...prev,
        tasks: nextTasks,
      }
    })
  }

  const handleAddTask = (mode) => {
    if (mode === 'create') {
      setCreateForm((prev) => ({
        ...prev,
        tasks: [...prev.tasks, { ...emptyTask }],
      }))
      return
    }

    setEditForm((prev) => ({
      ...prev,
      tasks: [...prev.tasks, { ...emptyTask }],
    }))
  }

  const handleRemoveTask = (mode, index) => {
    if (mode === 'create') {
      setCreateForm((prev) => {
        const nextTasks = prev.tasks.filter((_, i) => i !== index)
        return {
          ...prev,
          tasks: nextTasks.length ? nextTasks : [{ ...emptyTask }],
        }
      })
      return
    }

    setEditForm((prev) => {
      const nextTasks = prev.tasks.filter((_, i) => i !== index)
      return {
        ...prev,
        tasks: nextTasks.length ? nextTasks : [{ ...emptyTask }],
      }
    })
  }

  const handleCreateSubmit = async () => {
    if (!canCreate) return

    try {
      setCreateLoading(true)
      setCreateError('')
      const response = await api.post('', buildPayload(createForm), getAuthConfig())
      const created = response?.data || null
      setChecklist(created)
      if (created?.id) {
        setLookupId(String(created.id))
      }
      closeCreateModal()
    } catch (err) {
      setCreateError(extractErrorMessage(err, 'Không thể tạo checklist.'))
    } finally {
      setCreateLoading(false)
    }
  }

  const handleEditSubmit = async () => {
    if (!canEdit || !checklist?.id) return

    try {
      setEditLoading(true)
      setEditError('')
      const response = await api.patch(`/${checklist.id}`, buildPayload(editForm), getAuthConfig())
      setChecklist(response?.data || null)
      closeEditModal()
    } catch (err) {
      setEditError(extractErrorMessage(err, 'Không thể cập nhật checklist.'))
    } finally {
      setEditLoading(false)
    }
  }

  const handleDeleteConfirm = async () => {
    if (!canDelete || !checklist?.id) return

    try {
      setDeleteLoading(true)
      setDeleteError('')
      await api.delete(`/${checklist.id}`, getAuthConfig())
      setChecklist(null)
      setLookupId('')
      closeDeleteModal()
    } catch (err) {
      setDeleteError(extractErrorMessage(err, 'Không thể xóa checklist.'))
    } finally {
      setDeleteLoading(false)
    }
  }

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
                    <FiCheckSquare size={18} />
                  </div>
                  <div>
                    <div className="filters-panel__title">Tra cứu checklist</div>
                  </div>
                </div>

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

              <div className="filters-grid filters-grid--4">
                <div className="filter-field">
                  <label className="filter-label">Checklist ID</label>
                  <div className="search-box">
                    <FiSearch size={16} />
                    <input
                      type="text"
                      placeholder="Nhập checklist id..."
                      value={lookupId}
                      onChange={(e) => setLookupId(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') handleLoadChecklist()
                      }}
                    />
                  </div>
                </div>

                <div className="filter-field filter-field--actions">
                  <label className="filter-label filter-label--ghost">Thao tác</label>
                  <div className="filter-actions-row">
                    <button
                      className="btn btn-primary btn-search-compact"
                      onClick={handleLoadChecklist}
                      type="button"
                    >
                      <FiSearch size={15} />
                      <span>Tải checklist</span>
                    </button>

                    <button
                      className="btn btn-light btn-icon-only"
                      onClick={() => {
                        setLookupId('')
                        setChecklist(null)
                        setError('')
                      }}
                      title="Reset"
                      aria-label="Reset"
                      type="button"
                    >
                      <FiRotateCcw size={16} />
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
          ) : !checklist ? (
            <div className="assets-message">
              Chưa có checklist nào được tải.
            </div>
          ) : (
            <div className="list-section">
              <div className="list-section__title">
                Checklist chi tiết
                <span className="list-badge">{checklist?.id || '-'}</span>
              </div>

              <div className="drawer-body" style={{ maxHeight: 'unset' }}>
                <div className="detail-hero">
                  <div className="detail-hero__left">
                    <div className="detail-hero__icon">
                      <FiCheckSquare size={30} />
                    </div>

                    <div className="detail-hero__content">
                      <h3>{checklist?.name || '-'}</h3>
                      <p>{checklist?.description || 'Không có mô tả'}</p>

                      <div className="detail-hero__meta">
                        <span className={`badge ${checklist?.active ? 'badge--success' : 'badge--default'}`}>
                          {checklist?.active ? 'Đang hoạt động' : 'Ngưng sử dụng'}
                        </span>
                        <span className="hero-chip">
                          <FiInfo size={14} />
                          Applies To: {checklist?.appliesTo || '-'}
                        </span>
                        <span className="hero-chip">
                          <FiTag size={14} />
                          Tasks: {Array.isArray(checklist?.tasks) ? checklist.tasks.length : 0}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="detail-section">
                  <div className="detail-section__title">Thông tin checklist</div>
                  <div className="detail-grid detail-grid--2">
                    <DetailItem icon={<FiTag size={16} />} label="Checklist ID" value={checklist?.id} />
                    <DetailItem icon={<FiInfo size={16} />} label="Applies To" value={checklist?.appliesTo} />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Mô tả"
                      value={checklist?.description}
                      full
                    />
                  </div>
                </div>

                <div className="detail-section">
                  <div className="detail-section__title">Danh sách task</div>
                  {Array.isArray(checklist?.tasks) && checklist.tasks.length > 0 ? (
                    <div className="detail-grid">
                      {checklist.tasks.map((task, index) => (
                        <div key={task?.id || index} className="detail-item detail-item--full">
                          <div className="detail-item__label">
                            <span className="detail-item__icon">
                              <FiCheckSquare size={16} />
                            </span>
                            <span>
                              Task {index + 1} - {task?.title || task?.label || '-'}
                            </span>
                          </div>
                          <div className="detail-item__value">
                            <div><strong>Loại:</strong> {getTaskTypeLabel(task?.taskType)}</div>
                            <div><strong>Mô tả:</strong> {task?.description || '-'}</div>
                            <div><strong>Required:</strong> {task?.required ? 'Có' : 'Không'}</div>
                            <div><strong>Thứ tự:</strong> {task?.displayOrder || index + 1}</div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="assets-message">Checklist chưa có task nào.</div>
                  )}
                </div>

                <div className="drawer-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
                  {canEdit && (
                    <button className="btn btn-secondary" onClick={openEditModal} type="button">
                      <FiEdit2 size={16} />
                      <span>Chỉnh sửa</span>
                    </button>
                  )}

                  {canDelete && (
                    <button className="btn btn-danger-solid" onClick={openDeleteModal} type="button">
                      <FiTrash2 size={16} />
                      <span>Xóa checklist</span>
                    </button>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {createOpen && (
        <div className="drawer-overlay" onClick={closeCreateModal}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Thêm mới checklist</h2>
                <p>Tạo checklist đúng payload backend hiện tại</p>
              </div>
              <button className="drawer-close" onClick={closeCreateModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {createError && (
                <div className="drawer-message drawer-message--error drawer-message--inline">
                  {createError}
                </div>
              )}

              <div className="form-section">
                <div className="detail-section__title">Thông tin checklist</div>
                <div className="form-grid">
                  <FormField label="Tên checklist">
                    <input
                      className="form-input"
                      value={createForm.name}
                      onChange={(e) => handleCreateFormChange('name', e.target.value)}
                      placeholder="Tìm kiếm"
                    />
                  </FormField>

                  <FormField label="Applies To">
                    <input
                      className="form-input"
                      value={createForm.appliesTo}
                      onChange={(e) => handleCreateFormChange('appliesTo', e.target.value)}
                      placeholder="Ví dụ: ASSET / WORK_ORDER"
                    />
                  </FormField>

                  <FormField label="Trạng thái">
                    <select
                      className="form-input"
                      value={String(createForm.active)}
                      onChange={(e) => handleCreateFormChange('active', e.target.value === 'true')}
                    >
                      <option value="true">Đang hoạt động</option>
                      <option value="false">Ngưng sử dụng</option>
                    </select>
                  </FormField>

                  <FormField label="Mô tả" full>
                    <textarea
                      className="form-input form-textarea"
                      value={createForm.description}
                      onChange={(e) => handleCreateFormChange('description', e.target.value)}
                      placeholder="Nhập mô tả checklist"
                    />
                  </FormField>
                </div>
              </div>

              <div className="form-section">
                <div className="detail-section__title">Danh sách task</div>

                <div className="detail-grid">
                  {createForm.tasks.map((task, index) => (
                    <div key={`create-task-${index}`} className="detail-item detail-item--full">
                      <div className="detail-item__label">
                        <span className="detail-item__icon">
                          <FiCheckSquare size={16} />
                        </span>
                        <span>Task {index + 1}</span>
                      </div>

                      <div className="form-grid">
                        <FormField label="Tiêu đề task">
                          <input
                            className="form-input"
                            value={task.label}
                            onChange={(e) => handleTaskChange('create', index, 'label', e.target.value)}
                            placeholder="Nhập title"
                          />
                        </FormField>

                        <FormField label="Loại task">
                          <select
                            className="form-input"
                            value={task.taskType}
                            onChange={(e) => handleTaskChange('create', index, 'taskType', e.target.value)}
                          >
                            {TASK_TYPE_OPTIONS.map((item) => (
                              <option key={item.value} value={item.value}>
                                {item.label}
                              </option>
                            ))}
                          </select>
                        </FormField>

                        <FormField label="Mô tả" full>
                          <textarea
                            className="form-input form-textarea"
                            value={task.description}
                            onChange={(e) =>
                              handleTaskChange('create', index, 'description', e.target.value)
                            }
                            placeholder="Nhập mô tả task"
                          />
                        </FormField>
                      </div>

                      <div className="drawer-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
                        <button
                          className="btn btn-light"
                          onClick={() => handleRemoveTask('create', index)}
                          type="button"
                        >
                          <FiTrash2 size={16} />
                          <span>Xóa task</span>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>

                <div style={{ marginTop: 12 }}>
                  <button className="btn btn-secondary" onClick={() => handleAddTask('create')} type="button">
                    <FiPlus size={16} />
                    <span>Thêm task</span>
                  </button>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeCreateModal} disabled={createLoading} type="button">
                Hủy
              </button>
              <button className="btn btn-primary" onClick={handleCreateSubmit} disabled={createLoading} type="button">
                <FiSave size={16} />
                <span>{createLoading ? 'Đang lưu...' : 'Lưu checklist'}</span>
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
                <h2>Cập nhật checklist</h2>
                <p>Patch checklist theo đúng backend hiện tại</p>
              </div>
              <button className="drawer-close" onClick={closeEditModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {editError && (
                <div className="drawer-message drawer-message--error drawer-message--inline">
                  {editError}
                </div>
              )}

              <div className="form-section">
                <div className="detail-section__title">Thông tin checklist</div>
                <div className="form-grid">
                  <FormField label="Tên checklist">
                    <input
                      className="form-input"
                      value={editForm.name}
                      onChange={(e) => handleEditFormChange('name', e.target.value)}
                      placeholder="Nhập tên checklist"
                    />
                  </FormField>

                  <FormField label="Applies To">
                    <input
                      className="form-input"
                      value={editForm.appliesTo}
                      onChange={(e) => handleEditFormChange('appliesTo', e.target.value)}
                      placeholder="Ví dụ: ASSET / WORK_ORDER"
                    />
                  </FormField>

                  <FormField label="Trạng thái">
                    <select
                      className="form-input"
                      value={String(editForm.active)}
                      onChange={(e) => handleEditFormChange('active', e.target.value === 'true')}
                    >
                      <option value="true">Đang hoạt động</option>
                      <option value="false">Ngưng sử dụng</option>
                    </select>
                  </FormField>

                  <FormField label="Mô tả" full>
                    <textarea
                      className="form-input form-textarea"
                      value={editForm.description}
                      onChange={(e) => handleEditFormChange('description', e.target.value)}
                      placeholder="Nhập mô tả checklist"
                    />
                  </FormField>
                </div>
              </div>

              <div className="form-section">
                <div className="detail-section__title">Danh sách task</div>

                <div className="detail-grid">
                  {editForm.tasks.map((task, index) => (
                    <div key={`edit-task-${index}`} className="detail-item detail-item--full">
                      <div className="detail-item__label">
                        <span className="detail-item__icon">
                          <FiCheckSquare size={16} />
                        </span>
                        <span>Task {index + 1}</span>
                      </div>

                      <div className="form-grid">
                        <FormField label="Tiêu đề task">
                          <input
                            className="form-input"
                            value={task.label}
                            onChange={(e) => handleTaskChange('edit', index, 'label', e.target.value)}
                            placeholder="Nhập title"
                          />
                        </FormField>

                        <FormField label="Loại task">
                          <select
                            className="form-input"
                            value={task.taskType}
                            onChange={(e) => handleTaskChange('edit', index, 'taskType', e.target.value)}
                          >
                            {TASK_TYPE_OPTIONS.map((item) => (
                              <option key={item.value} value={item.value}>
                                {item.label}
                              </option>
                            ))}
                          </select>
                        </FormField>

                        <FormField label="Mô tả" full>
                          <textarea
                            className="form-input form-textarea"
                            value={task.description}
                            onChange={(e) =>
                              handleTaskChange('edit', index, 'description', e.target.value)
                            }
                            placeholder="Nhập mô tả task"
                          />
                        </FormField>
                      </div>

                      <div className="drawer-footer" style={{ paddingLeft: 0, paddingRight: 0, paddingBottom: 0 }}>
                        <button
                          className="btn btn-light"
                          onClick={() => handleRemoveTask('edit', index)}
                          type="button"
                        >
                          <FiTrash2 size={16} />
                          <span>Xóa task</span>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>

                <div style={{ marginTop: 12 }}>
                  <button className="btn btn-secondary" onClick={() => handleAddTask('edit')} type="button">
                    <FiPlus size={16} />
                    <span>Thêm task</span>
                  </button>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeEditModal} disabled={editLoading} type="button">
                Hủy
              </button>
              <button className="btn btn-primary" onClick={handleEditSubmit} disabled={editLoading} type="button">
                <FiSave size={16} />
                <span>{editLoading ? 'Đang lưu...' : 'Lưu cập nhật'}</span>
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
                <h2>Xóa checklist</h2>
                <p>Xác nhận trước khi xóa dữ liệu</p>
              </div>
              <button className="drawer-close" onClick={closeDeleteModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {deleteError && (
                <div className="drawer-message drawer-message--error drawer-message--inline">
                  {deleteError}
                </div>
              )}

              <div className="delete-box">
                <div className="delete-box__icon">
                  <FiAlertTriangle size={28} />
                </div>
                <div className="delete-box__content">
                  <h3>{checklist?.name || 'Checklist'}</h3>
                  <p>
                    Bạn có chắc muốn xóa checklist này không?
                  </p>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeDeleteModal} disabled={deleteLoading} type="button">
                Hủy
              </button>
              <button className="btn btn-danger-solid" onClick={handleDeleteConfirm} disabled={deleteLoading} type="button">
                <FiTrash2 size={16} />
                <span>{deleteLoading ? 'Đang xóa...' : 'Xóa checklist'}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}