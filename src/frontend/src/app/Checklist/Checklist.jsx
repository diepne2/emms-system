import React, { useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import {
  FiCheckSquare,
  FiClipboard,
  FiEdit3,
  FiPlus,
  FiRefreshCcw,
  FiSearch,
  FiTrash2,
  FiX,
} from 'react-icons/fi'
import './Checklist.css'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const checklistApi = axios.create({
  baseURL: `${API_BASE_URL}/api/checklists`,
})

function getToken() {
  return (
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken') ||
    localStorage.getItem('access_token') ||
    localStorage.getItem('jwt')
  )
}

function authConfig() {
  const token = getToken()

  return token
    ? {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    : {}
}

function getErrorMessage(err, fallback) {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    fallback
  )
}

function getApplyLabel(value) {
  const map = {
    WORK_ORDER: 'Work Order',
    MAINTENANCE_PLAN: 'Preventive Maintenance',
    ASSET: 'Asset',
    GENERAL: 'General',
  }

  return map[value] || value || 'General'
}

function getTaskTitle(task) {
  return task?.title || task?.label || '-'
}

const emptyTask = {
  label: '',
  description: '',
  taskType: 'PASS_FAIL',
}

const emptyForm = {
  name: '',
  description: '',
  appliesTo: 'WORK_ORDER',
  active: true,
  tasks: [{ ...emptyTask }],
}

export default function Checklist() {
  const [keyword, setKeyword] = useState('')
  const [results, setResults] = useState([])
  const [selectedChecklist, setSelectedChecklist] = useState(null)

  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(emptyForm)

  const taskCount = useMemo(() => {
    return selectedChecklist?.tasks?.length || 0
  }, [selectedChecklist])

  useEffect(() => {
    loadChecklists('')
  }, [])

  async function loadChecklists(q = keyword) {
    try {
      setLoading(true)
      setError('')

      const cleanKeyword = String(q || '').trim()

      const res = await checklistApi.get('', {
        ...authConfig(),
        params: cleanKeyword ? { q: cleanKeyword } : {},
      })

      const list = Array.isArray(res.data) ? res.data : []

      setResults(list)

      if (list.length === 1) {
        setSelectedChecklist(list[0])
      } else {
        setSelectedChecklist(null)
      }

      if (list.length === 0) {
        setError('Không tìm thấy checklist phù hợp.')
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Không thể tải danh sách checklist.'))
      setResults([])
      setSelectedChecklist(null)
    } finally {
      setLoading(false)
    }
  }

  function resetSearch() {
    setKeyword('')
    setError('')
    setSelectedChecklist(null)
    loadChecklists('')
  }

  function openCreate() {
    setEditingId(null)
    setForm({
      ...emptyForm,
      tasks: [{ ...emptyTask }],
    })
    setShowForm(true)
  }

  function openEdit(item) {
    setEditingId(item.id)

    setForm({
      name: item.name || '',
      description: item.description || '',
      appliesTo: item.appliesTo || 'WORK_ORDER',
      active: item.active !== false,
      tasks:
        item.tasks?.length > 0
          ? item.tasks.map((task) => ({
              label: task.title || task.label || '',
              description: task.description || '',
              taskType: task.taskType || 'PASS_FAIL',
            }))
          : [{ ...emptyTask }],
    })

    setShowForm(true)
  }

  function closeForm() {
    setShowForm(false)
    setEditingId(null)
    setForm({
      ...emptyForm,
      tasks: [{ ...emptyTask }],
    })
  }

  function updateTask(index, field, value) {
    setForm((prev) => {
      const tasks = [...prev.tasks]

      tasks[index] = {
        ...tasks[index],
        [field]: value,
      }

      return {
        ...prev,
        tasks,
      }
    })
  }

  function addTask() {
    setForm((prev) => ({
      ...prev,
      tasks: [...prev.tasks, { ...emptyTask }],
    }))
  }

  function removeTask(index) {
    setForm((prev) => ({
      ...prev,
      tasks:
        prev.tasks.length === 1
          ? [{ ...emptyTask }]
          : prev.tasks.filter((_, i) => i !== index),
    }))
  }

  async function submitForm(e) {
    e.preventDefault()

    if (!form.name.trim()) {
      setError('Tên checklist không được để trống.')
      return
    }

    const validTasks = form.tasks
      .filter((task) => task.label?.trim())
      .map((task) => ({
        label: task.label.trim(),
        description: task.description?.trim() || null,
        taskType: task.taskType || 'PASS_FAIL',
      }))

    if (validTasks.length === 0) {
      setError('Checklist cần ít nhất 1 task.')
      return
    }

    const payload = {
      name: form.name.trim(),
      description: form.description?.trim() || null,
      appliesTo: form.appliesTo,
      active: form.active,
      tasks: validTasks,
    }

    try {
      setSaving(true)
      setError('')

      if (editingId) {
        await checklistApi.patch(`/${editingId}`, payload, authConfig())
      } else {
        await checklistApi.post('', payload, authConfig())
      }

      closeForm()
      await loadChecklists(keyword)
    } catch (err) {
      setError(getErrorMessage(err, 'Không thể lưu checklist.'))
    } finally {
      setSaving(false)
    }
  }

  async function deleteChecklist(id) {
    const ok = window.confirm('Bạn có chắc muốn xóa checklist này không?')
    if (!ok) return

    try {
      setLoading(true)
      setError('')

      await checklistApi.delete(`/${id}`, authConfig())

      if (selectedChecklist?.id === id) {
        setSelectedChecklist(null)
      }

      await loadChecklists(keyword)
    } catch (err) {
      setError(getErrorMessage(err, 'Không thể xóa checklist.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="ck-page">
      <div className="ck-shell">
        <header className="ck-header">
          <div>
            <h1>Quản lý Checklist</h1>
          </div>

          <button className="ck-btn ck-btn-primary" onClick={openCreate}>
            <FiPlus />
            Thêm checklist
          </button>
        </header>

        <section className="ck-filter-card">
          <div className="ck-filter-head">
            <div className="ck-filter-icon">
              <FiSearch size={22} />
            </div>

            <div>
              <h2>Tìm kiếm checklist</h2>
            </div>
          </div>

          <div className="ck-filter-grid">
            <label className="ck-field">
              <span>Từ khóa</span>

              <div className="ck-search-box">
                <FiSearch />

                <input
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      loadChecklists(keyword)
                    }
                  }}
                  placeholder="Nhập từ khóa"
                />
              </div>
            </label>

            <div className="ck-actions">
              <button
                className="ck-btn ck-btn-primary"
                onClick={() => loadChecklists(keyword)}
                disabled={loading}
              >
                <FiSearch />
                {loading ? 'Đang tìm...' : 'Tìm kiếm'}
              </button>

              <button className="ck-btn ck-btn-light" onClick={resetSearch}>
                <FiRefreshCcw />
                Làm mới
              </button>
            </div>
          </div>
        </section>

        {error && <div className="ck-alert ck-alert-error">{error}</div>}

        <section className="ck-detail-card">
          <div className="ck-card-head">
            <div>
              <h2>Danh sách checklist</h2>
              <p>{results.length} checklist</p>
            </div>
          </div>

          <div className="ck-detail-body">
            {loading ? (
              <div className="ck-empty">
                <strong>Đang tải dữ liệu...</strong>
                <span>Vui lòng chờ trong giây lát.</span>
              </div>
            ) : results.length === 0 ? (
              <div className="ck-empty">
                <strong>Chưa có checklist</strong>
                <span>Hãy thêm checklist mới hoặc đổi từ khóa tìm kiếm.</span>
              </div>
            ) : (
              <div className="ck-task-list">
                {results.map((item) => (
                  <div
                    className={`ck-task-card ${
                      selectedChecklist?.id === item.id ? 'ck-task-card-active' : ''
                    }`}
                    key={item.id}
                    onClick={() => setSelectedChecklist(item)}
                  >
                    <div className="ck-task-head">
                      <div className="ck-task-title">
                        <FiCheckSquare size={17} />
                        <span>
                          #{item.id} - {item.name || 'Không có tên'}
                        </span>
                      </div>

                      <span
                        className={`ck-badge ${
                          item.active ? 'ck-badge-success' : 'ck-badge-muted'
                        }`}
                      >
                        {item.active ? 'Đang hoạt động' : 'Ngưng sử dụng'}
                      </span>
                    </div>

                    <div className="ck-task-content">
                      <div>
                        <strong>Áp dụng:</strong> {getApplyLabel(item.appliesTo)}
                      </div>
                      <div>
                        <strong>Số task:</strong> {item.tasks?.length || 0}
                      </div>
                      <div>
                        <strong>Mô tả:</strong> {item.description || '-'}
                      </div>
                    </div>

                    <div className="ck-card-actions">
                      <button
                        className="ck-icon-btn"
                        onClick={(e) => {
                          e.stopPropagation()
                          openEdit(item)
                        }}
                        title="Sửa checklist"
                      >
                        <FiEdit3 />
                      </button>

                      <button
                        className="ck-icon-btn ck-icon-danger"
                        onClick={(e) => {
                          e.stopPropagation()
                          deleteChecklist(item.id)
                        }}
                        title="Xóa checklist"
                      >
                        <FiTrash2 />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        {selectedChecklist && (
          <section className="ck-detail-card ck-detail-selected">
            <div className="ck-card-head">
              <div>
                <h2>Chi tiết checklist</h2>
                <p>Thông tin template và các task kiểm tra.</p>
              </div>

              <div className="ck-actions">
                <button
                  className="ck-btn ck-btn-light"
                  onClick={() => openEdit(selectedChecklist)}
                >
                  <FiEdit3 />
                  Sửa
                </button>
              </div>
            </div>

            <div className="ck-detail-body">
              <div className="ck-hero">
                <div className="ck-hero-icon">
                  <FiClipboard size={28} />
                </div>

                <div className="ck-hero-content">
                  <h3>{selectedChecklist.name}</h3>
                  <p>{selectedChecklist.description || 'Không có mô tả.'}</p>

                  <div className="ck-hero-meta">
                    <span className="ck-chip">
                      ID: #{selectedChecklist.id}
                    </span>
                    <span className="ck-chip">
                      {getApplyLabel(selectedChecklist.appliesTo)}
                    </span>
                    <span
                      className={`ck-badge ${
                        selectedChecklist.active
                          ? 'ck-badge-success'
                          : 'ck-badge-muted'
                      }`}
                    >
                      {selectedChecklist.active
                        ? 'Đang hoạt động'
                        : 'Ngưng sử dụng'}
                    </span>
                    <span className="ck-chip">{taskCount} task</span>
                  </div>
                </div>
              </div>

              <h3 className="ck-section-title">Danh sách task</h3>

              {taskCount === 0 ? (
                <div className="ck-empty ck-empty-small">
                  Checklist này chưa có task.
                </div>
              ) : (
                <div className="ck-task-list">
                  {selectedChecklist.tasks.map((task, index) => (
                    <div className="ck-task-card" key={task.id || index}>
                      <div className="ck-task-head">
                        <div className="ck-task-title">
                          <FiCheckSquare size={16} />
                          <span>
                            {index + 1}. {getTaskTitle(task)}
                          </span>
                        </div>

                        <span className="ck-badge ck-badge-muted">
                          {task.taskType || 'PASS_FAIL'}
                        </span>
                      </div>

                      <div className="ck-task-content">
                        {task.description || 'Không có mô tả.'}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </section>
        )}
      </div>

      {showForm && (
        <div className="ck-modal-overlay">
          <div className="ck-modal ck-modal-wide">
            <div className="ck-modal-head">
              <div>
                <h2>{editingId ? 'Cập nhật checklist' : 'Thêm checklist'}</h2>
                <p>
                  Nhập tên checklist, loại áp dụng và danh sách task kiểm tra.
                </p>
              </div>

              <button className="ck-close-btn" onClick={closeForm}>
                <FiX />
              </button>
            </div>

            <form onSubmit={submitForm}>
              <div className="ck-modal-body">
                <div className="ck-form-section">
                  <div className="ck-form-grid">
                    <label className="ck-form-field">
                      <span>Tên checklist *</span>
                      <input
                        className="ck-input"
                        value={form.name}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            name: e.target.value,
                          }))
                        }
                        placeholder="VD: Kiểm tra máy bơm định kỳ"
                      />
                    </label>

                    <label className="ck-form-field">
                      <span>Áp dụng cho</span>
                      <select
                        className="ck-input"
                        value={form.appliesTo}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            appliesTo: e.target.value,
                          }))
                        }
                      >
                        <option value="WORK_ORDER">Work Order</option>
                        <option value="MAINTENANCE_PLAN">
                          Preventive Maintenance
                        </option>
                        <option value="ASSET">Asset</option>
                        <option value="GENERAL">General</option>
                      </select>
                    </label>

                    <label className="ck-form-field ck-form-field-full">
                      <span>Mô tả</span>
                      <textarea
                        className="ck-input ck-textarea"
                        value={form.description}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            description: e.target.value,
                          }))
                        }
                        placeholder="Mô tả mục đích của checklist..."
                      />
                    </label>

                    <label className="ck-form-field">
                      <span>Trạng thái</span>
                      <select
                        className="ck-input"
                        value={form.active ? 'true' : 'false'}
                        onChange={(e) =>
                          setForm((prev) => ({
                            ...prev,
                            active: e.target.value === 'true',
                          }))
                        }
                      >
                        <option value="true">Đang hoạt động</option>
                        <option value="false">Ngưng sử dụng</option>
                      </select>
                    </label>
                  </div>
                </div>

                <h3 className="ck-section-title">Task kiểm tra</h3>

                <div className="ck-task-list">
                  {form.tasks.map((task, index) => (
                    <div className="ck-task-card" key={index}>
                      <div className="ck-task-head">
                        <div className="ck-task-title">
                          <FiCheckSquare />
                          <span>Task {index + 1}</span>
                        </div>

                        <button
                          type="button"
                          className="ck-icon-btn ck-icon-danger"
                          onClick={() => removeTask(index)}
                        >
                          <FiTrash2 />
                        </button>
                      </div>

                      <div className="ck-form-grid">
                        <label className="ck-form-field">
                          <span>Tên task *</span>
                          <input
                            className="ck-input"
                            value={task.label}
                            onChange={(e) =>
                              updateTask(index, 'label', e.target.value)
                            }
                            placeholder="VD: Kiểm tra rò rỉ dầu"
                          />
                        </label>

                        <label className="ck-form-field">
                          <span>Loại task</span>
                          <select
                            className="ck-input"
                            value={task.taskType}
                            onChange={(e) =>
                              updateTask(index, 'taskType', e.target.value)
                            }
                          >
                            <option value="PASS_FAIL">PASS / FAIL</option>
                            <option value="NUMBER">Nhập số</option>
                            <option value="TEXT">Nhập text</option>
                          </select>
                        </label>

                        <label className="ck-form-field ck-form-field-full">
                          <span>Mô tả task</span>
                          <textarea
                            className="ck-input ck-textarea"
                            value={task.description}
                            onChange={(e) =>
                              updateTask(index, 'description', e.target.value)
                            }
                            placeholder="Mô tả cách kiểm tra..."
                          />
                        </label>
                      </div>
                    </div>
                  ))}
                </div>

                <button
                  type="button"
                  className="ck-btn ck-btn-light ck-add-task-btn"
                  onClick={addTask}
                >
                  <FiPlus />
                  Thêm task
                </button>
              </div>

              <div className="ck-modal-footer">
                <button
                  type="button"
                  className="ck-btn ck-btn-light"
                  onClick={closeForm}
                >
                  Hủy
                </button>

                <button
                  type="submit"
                  className="ck-btn ck-btn-primary"
                  disabled={saving}
                >
                  {saving ? 'Đang lưu...' : editingId ? 'Cập nhật' : 'Tạo mới'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}