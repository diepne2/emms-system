import React, { useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import './location.css'

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  'https://emms-system-production-4239.up.railway.app'

const api = axios.create({
  baseURL: `${API_BASE}/api/locations`,
})

const emptyForm = {
  name: '',
  address: '',
  description: '',
  parentLocation: '',
}

const getToken = () =>
  localStorage.getItem('token') ||
  localStorage.getItem('accessToken') ||
  localStorage.getItem('access_token') ||
  localStorage.getItem('jwt')

const getAuthConfig = () => {
  const token = getToken()
  return {
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
    },
  }
}

const getErrorMessage = (err, fallback) =>
  err?.response?.data?.message || err?.response?.data?.error || fallback

const Location = () => {
  const [locations, setLocations] = useState([])
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(false)

  const [drawerMode, setDrawerMode] = useState(null)
  const [selected, setSelected] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)

  const filteredLocations = useMemo(() => locations, [locations])

  const fetchLocations = async () => {
    try {
      setLoading(true)

      const keyword = search.trim()

      const res = await api.get('', {
        ...getAuthConfig(),
        params: keyword ? { keyword } : {},
      })

      setLocations(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      console.error(err)
      alert(getErrorMessage(err, 'Không tải được dữ liệu vị trí'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchLocations()
  }, [search])

  const handleSearch = () => {
    setSearch(searchInput.trim())
  }

  const handleClearSearch = () => {
    setSearchInput('')
    setSearch('')
  }

  const closeDrawer = () => {
    setDrawerMode(null)
    setSelected(null)
    setForm(emptyForm)
  }

  const openCreate = () => {
    setSelected(null)
    setForm(emptyForm)
    setDrawerMode('create')
  }

  const openView = (location) => {
    setSelected(location)
    setDrawerMode('view')
  }

  const openEdit = (location) => {
    setSelected(location)
    setForm({
      name: location.name || '',
      address: location.address || '',
      description: location.description || '',
      parentLocation: location.parentLocation || '',
    })
    setDrawerMode('edit')
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!form.name.trim()) {
      alert('Tên vị trí không được để trống')
      return
    }

    const payload = {
      name: form.name.trim(),
      address: form.address.trim(),
      description: form.description.trim(),
      parentLocation: form.parentLocation.trim(),
    }

    try {
      setSaving(true)

      if (drawerMode === 'create') {
        await api.post('', payload, getAuthConfig())
        alert('Thêm vị trí thành công')
      } else {
        await api.put(`/${selected.id}`, payload, getAuthConfig())
        alert('Cập nhật vị trí thành công')
      }

      closeDrawer()
      fetchLocations()
    } catch (err) {
      console.error(err)

      if (err.response?.status === 403) {
        alert('Không có quyền thực hiện thao tác này')
        return
      }

      alert(getErrorMessage(err, 'Lưu vị trí thất bại'))
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa vị trí này?')) return

    try {
      await api.delete(`/${id}`, getAuthConfig())
      alert('Xóa thành công')
      fetchLocations()
    } catch (err) {
      console.error(err)

      if (err.response?.status === 409) {
        alert(getErrorMessage(err, 'Không thể xóa vì vị trí đang được sử dụng'))
        return
      }

      if (err.response?.status === 403) {
        alert('Không có quyền xóa')
        return
      }

      alert(getErrorMessage(err, 'Xóa thất bại'))
    }
  }

  return (
    <div className="location-page">
      <div className="card">
        <div className="assets-header">
          <div className="assets-header__top">
            <div className="assets-header__intro">
              <div className="assets-header__mini-title">
                Quản lý vị trí
              </div>
            </div>

            <button
              type="button"
              className="btn btn-primary btn-create-header"
              onClick={openCreate}
            >
              + Thêm vị trí
            </button>
          </div>
        </div>

        <div className="filters-panel">
          <div className="filters-grid filters-grid--1">
            <div className="filter-field">
              <label className="filter-label">Tìm kiếm</label>

              <div className="search-box">
                <input
                  type="text"
                  placeholder="Nhập tìm kiếm"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') handleSearch()
                  }}
                />

                {searchInput && (
                  <button
                    type="button"
                    className="btn btn-secondary btn-search"
                    onClick={handleClearSearch}
                  >
                    Xóa
                  </button>
                )}

                <button
                  type="button"
                  className="btn btn-search"
                  onClick={handleSearch}
                >
                  Tìm kiếm
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="list-section">
          <div className="list-section__title">
            Danh sách vị trí
            <span className="list-badge">{filteredLocations.length}</span>
          </div>

          <div className="table-wrap">
            <table className="assets-table">
              <thead>
                <tr>
                  <th>Tên vị trí</th>
                  <th>Địa chỉ</th>
                  <th>Vị trí cha</th>
                  <th style={{ textAlign: 'center' }}>Thao tác</th>
                </tr>
              </thead>

              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="4">Đang tải dữ liệu...</td>
                  </tr>
                ) : filteredLocations.length === 0 ? (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center' }}>
                      Không có dữ liệu
                    </td>
                  </tr>
                ) : (
                  filteredLocations.map((l) => (
                    <tr key={l.id}>
                      <td>
                        <div className="asset-name-cell">
                          <strong>{l.name || '-'}</strong>
                          {l.description && <span>{l.description}</span>}
                        </div>
                      </td>

                      <td>{l.address || '-'}</td>

                      <td>
                        <span className="badge badge--info">
                          {l.parentLocation || 'Không có'}
                        </span>
                      </td>

                      <td>
                        <div className="action-group">
                          <button
                            type="button"
                            className="icon-btn"
                            onClick={() => openView(l)}
                            title="Xem chi tiết"
                          >
                            👁
                          </button>

                          <button
                            type="button"
                            className="icon-btn"
                            onClick={() => openEdit(l)}
                            title="Sửa"
                          >
                            ✏️
                          </button>

                          <button
                            type="button"
                            className="icon-btn icon-btn--danger"
                            onClick={() => handleDelete(l.id)}
                            title="Xóa"
                          >
                            🗑
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {drawerMode === 'view' && selected && (
        <div className="drawer-overlay">
          <div className="drawer drawer--wide">
            <div className="drawer-header">
              <div>
                <h2>Chi tiết vị trí</h2>
                <p>Xem thông tin đầy đủ của vị trí</p>
              </div>

              <button
                type="button"
                className="drawer-close"
                onClick={closeDrawer}
              >
                ×
              </button>
            </div>

            <div className="drawer-body">
              <div className="detail-hero">
                <div className="detail-hero__left">
                  <div className="detail-hero__icon">📍</div>

                  <div className="detail-hero__content">
                    <h3>{selected.name || '-'}</h3>
                    <p>{selected.description || 'Không có mô tả'}</p>

                    <div className="detail-hero__meta">
                      <span className="hero-chip">ID: {selected.id}</span>
                      <span className="hero-chip">
                        Cha: {selected.parentLocation || 'Không có'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="detail-section">
                <div className="detail-section__title">Thông tin vị trí</div>

                <div className="detail-grid detail-grid--2">
                  <div className="detail-item">
                    <div className="detail-item__label">Tên vị trí</div>
                    <div className="detail-item__value">
                      {selected.name || '-'}
                    </div>
                  </div>

                  <div className="detail-item">
                    <div className="detail-item__label">Vị trí cha</div>
                    <div className="detail-item__value">
                      {selected.parentLocation || 'Không có'}
                    </div>
                  </div>

                  <div className="detail-item detail-item--full">
                    <div className="detail-item__label">Địa chỉ</div>
                    <div className="detail-item__value">
                      {selected.address || '-'}
                    </div>
                  </div>

                  <div className="detail-item detail-item--full">
                    <div className="detail-item__label">Mô tả</div>
                    <div className="detail-item__value">
                      {selected.description || 'Không có mô tả'}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={closeDrawer}
              >
                Đóng
              </button>

              <button
                type="button"
                className="btn btn-primary"
                onClick={() => openEdit(selected)}
              >
                Sửa vị trí
              </button>
            </div>
          </div>
        </div>
      )}

      {(drawerMode === 'create' || drawerMode === 'edit') && (
        <div className="drawer-overlay">
          <div className="drawer drawer--wide">
            <div className="drawer-header">
              <div>
                <h2>
                  {drawerMode === 'create'
                    ? 'Thêm vị trí'
                    : 'Cập nhật vị trí'}
                </h2>
                <p>Nhập thông tin vị trí trong hệ thống EMMS</p>
              </div>

              <button
                type="button"
                className="drawer-close"
                onClick={closeDrawer}
              >
                ×
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="drawer-body">
                <div className="form-section">
                  <div className="form-grid">
                    <div className="form-field">
                      <label className="form-label">Tên vị trí *</label>
                      <input
                        className="form-input"
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        placeholder="Nhập tên vị trí"
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Vị trí cha</label>
                      <select
                        className="form-input"
                        name="parentLocation"
                        value={form.parentLocation}
                        onChange={handleChange}
                      >
                        <option value="">Không có</option>

                        {locations
                          .filter((l) => l.id !== selected?.id)
                          .map((l) => (
                            <option key={l.id} value={l.id}>
                              {l.name}
                            </option>
                          ))}
                      </select>
                    </div>

                    <div className="form-field form-field--full">
                      <label className="form-label">Địa chỉ</label>
                      <input
                        className="form-input"
                        name="address"
                        value={form.address}
                        onChange={handleChange}
                        placeholder="Nhập địa chỉ"
                      />
                    </div>

                    <div className="form-field form-field--full">
                      <label className="form-label">Mô tả</label>
                      <textarea
                        className="form-textarea"
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        placeholder="Nhập mô tả vị trí"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <div className="drawer-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={closeDrawer}
                >
                  Hủy
                </button>

                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={saving}
                >
                  {saving ? 'Đang lưu...' : 'Lưu'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default Location