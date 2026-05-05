import React, { useEffect, useState } from 'react'
import axios from 'axios'
import './location.css'

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  'https://emms-system-production-4239.up.railway.app'

const api = axios.create({
  baseURL: `${API_BASE}/api/locations`,
})

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

const getErrorMessage = (err, fallback) => {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    fallback
  )
}

const Location = () => {
  const [locations, setLocations] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(false)


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

      if (err.response?.status === 401) {
        alert('Phiên đăng nhập hết hạn')
      } else {
        alert(getErrorMessage(err, 'Không tải được dữ liệu'))
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchLocations()
  }, [search])


  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa?')) return

    try {
      await api.delete(`/${id}`, getAuthConfig())

      alert('Xóa thành công')
      fetchLocations()
    } catch (err) {
      console.error(err)

      if (err.response?.status === 409) {
        alert(
          getErrorMessage(
            err,
            'Không thể xóa vì đang được sử dụng (meter / asset)'
          )
        )
        return
      }

      if (err.response?.status === 403) {
        alert('Không có quyền xóa')
        return
      }

      if (err.response?.status === 401) {
        alert('Hết phiên đăng nhập')
        return
      }

      alert(getErrorMessage(err, 'Xóa thất bại'))
    }
  }

  return (
    <div className="location-page">
      <div className="card">
        {/* HEADER */}
        <div className="assets-header">
          <div className="assets-header__top">
            <div className="assets-header__intro">
              <div className="assets-header__mini-title">
                Location Management
              </div>
            </div>
          </div>
        </div>

        {/* SEARCH */}
        <div className="filters-panel">
          <div className="filters-grid filters-grid--1">
            <div className="filter-field">
              <label className="filter-label">Tìm kiếm</label>
              <div className="search-box">
                <input
                  type="text"
                  placeholder="Nhập tên, địa chỉ, vị trí cha..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </div>
            </div>
          </div>
        </div>

        {/* TABLE */}
        <div className="list-section">
          <div className="list-section__title">
            Danh sách vị trí
            <span className="list-badge">{locations.length}</span>
          </div>

          <div className="table-wrap">
            <table className="assets-table">
              <thead>
                <tr>
                  <th>Tên</th>
                  <th>Địa chỉ</th>
                  <th>Vị trí cha</th>
                  <th>Thao tác</th>
                </tr>
              </thead>

              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="4">Loading...</td>
                  </tr>
                ) : locations.length === 0 ? (
                  <tr>
                    <td colSpan="4">Không có dữ liệu</td>
                  </tr>
                ) : (
                  locations.map((l) => (
                    <tr key={l.id}>
                      <td>
                        <div className="asset-name-cell">
                          <strong>{l.name || '-'}</strong>
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
    </div>
  )
}

export default Location