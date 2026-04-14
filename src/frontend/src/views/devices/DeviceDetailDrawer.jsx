import React, { useEffect, useState } from 'react'
import { deviceApi } from '../../api/deviceApi'
import { getCurrentUser, hasAnyRole, ROLE } from '../../api/auth'
import DeviceDetailModal from './DeviceDetailModal'
import './devices.css'

const defaultFilter = {
  name: '',
  code: '',
  serial: '',
  manufacturer: '',
  site: '',
  status: '',
  priority: '',
  page: 0,
  size: 5,
  sortBy: 'deviceId',
  asc: false,
}

function getStatusClass(status) {
  switch (status) {
    case 'ACTIVE':
      return 'status-pill status-active'
    case 'MAINTENANCE':
      return 'status-pill status-maintenance'
    case 'BROKEN':
      return 'status-pill status-broken'
    case 'NEW':
      return 'status-pill status-new'
    case 'RETIRED':
      return 'status-pill status-retired'
    default:
      return 'status-pill'
  }
}

function getStatusLabel(status) {
  switch (status) {
    case 'ACTIVE':
      return 'Hoạt động'
    case 'MAINTENANCE':
      return 'Bảo trì'
    case 'BROKEN':
      return 'Hỏng'
    case 'NEW':
      return 'Mới'
    case 'RETIRED':
      return 'Ngừng dùng'
    default:
      return status || '-'
  }
}

function getPriorityClass(priority) {
  switch (priority) {
    case 'HIGH':
      return 'priority-pill priority-high'
    case 'MEDIUM':
      return 'priority-pill priority-medium'
    case 'LOW':
      return 'priority-pill priority-low'
    default:
      return 'priority-pill'
  }
}

function getPriorityLabel(priority) {
  switch (priority) {
    case 'HIGH':
      return 'Cao'
    case 'MEDIUM':
      return 'Trung bình'
    case 'LOW':
      return 'Thấp'
    default:
      return priority || '-'
  }
}

function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '-'
  return Number(value).toLocaleString('vi-VN') + ' đ'
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('vi-VN')
}

export default function Devices() {
  const user = getCurrentUser()
  const canManage = hasAnyRole(user, [ROLE.ADMIN, ROLE.QUANLYKYTHUAT])

  const [filter, setFilter] = useState(defaultFilter)
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [pageData, setPageData] = useState({
    number: 0,
    totalPages: 0,
    totalElements: 0,
    size: 5,
  })

  const [selectedDevice, setSelectedDevice] = useState(null)
  const [detailOpen, setDetailOpen] = useState(false)

  const loadData = async (customFilter = filter) => {
    try {
      setLoading(true)
      const res = await deviceApi.filter(customFilter)

      setRows(res?.content || [])
      setPageData({
        number: res?.number || 0,
        totalPages: res?.totalPages || 0,
        totalElements: res?.totalElements || 0,
        size: res?.size || customFilter.size || 5,
      })
    } catch (error) {
      console.error(error)
      alert(error?.response?.data?.message || 'Không tải được danh sách thiết bị')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData(defaultFilter)
  }, [])

  const handleChangeFilter = (e) => {
    const { name, value } = e.target
    setFilter((prev) => ({
      ...prev,
      [name]: name === 'size' ? Number(value) : value,
    }))
  }

  const handleSearch = async () => {
    const next = { ...filter, page: 0 }
    setFilter(next)
    await loadData(next)
  }

  const handleReset = async () => {
    setFilter(defaultFilter)
    await loadData(defaultFilter)
  }

  const handlePageChange = async (page) => {
    const next = { ...filter, page }
    setFilter(next)
    await loadData(next)
  }

  const handleOpenDetail = async (device) => {
    try {
      setSelectedDevice(device)
      setDetailOpen(true)
    } catch (error) {
      alert('Không lấy được chi tiết thiết bị')
    }
  }

  const handleRetire = async (device) => {
    const ok = window.confirm(`Bạn có chắc muốn ngừng dùng thiết bị "${device.deviceName}"?`)
    if (!ok) return

    try {
      await deviceApi.retire(device.deviceId)
      alert('Cập nhật trạng thái thành công')
      await loadData(filter)
    } catch (error) {
      alert(error?.response?.data?.message || 'Không thể cập nhật thiết bị')
    }
  }

  return (
    <div className="device-screen">
      <div className="device-page-head">
        <div>
          <div className="device-breadcrumb">Danh mục &gt; Thiết bị</div>
          <h1 className="device-page-title">Danh sách thiết bị</h1>
        </div>

        {canManage && (
          <button className="btn-main">
            <span className="btn-plus">＋</span>
            Thêm mới
          </button>
        )}
      </div>

      <div className="device-filter-card">
        <div className="device-filter-grid">
          <div className="form-control-wrap">
            <label>Tên thiết bị</label>
            <input
              name="name"
              value={filter.name}
              onChange={handleChangeFilter}
              placeholder="Nhập tên thiết bị"
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
          </div>

          <div className="form-control-wrap">
            <label>Mã thiết bị</label>
            <input
              name="code"
              value={filter.code}
              onChange={handleChangeFilter}
              placeholder="Nhập mã thiết bị"
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
          </div>

          <div className="form-control-wrap">
            <label>Serial</label>
            <input
              name="serial"
              value={filter.serial}
              onChange={handleChangeFilter}
              placeholder="Nhập serial"
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
          </div>

          <div className="form-control-wrap">
            <label>Site</label>
            <input
              name="site"
              value={filter.site}
              onChange={handleChangeFilter}
              placeholder="Nhập site"
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
          </div>

          <div className="form-control-wrap">
            <label>Trạng thái</label>
            <select name="status" value={filter.status} onChange={handleChangeFilter}>
              <option value="">Tất cả</option>
              <option value="NEW">NEW</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="MAINTENANCE">MAINTENANCE</option>
              <option value="BROKEN">BROKEN</option>
              <option value="RETIRED">RETIRED</option>
            </select>
          </div>

          <div className="form-control-wrap">
            <label>Ưu tiên</label>
            <select name="priority" value={filter.priority} onChange={handleChangeFilter}>
              <option value="">Tất cả</option>
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
            </select>
          </div>

          <div className="form-control-wrap">
            <label>Số bản ghi / trang</label>
            <select name="size" value={filter.size} onChange={handleChangeFilter}>
              <option value={5}>05 /Trang</option>
              <option value={10}>10 /Trang</option>
              <option value={20}>20 /Trang</option>
            </select>
          </div>
        </div>

        <div className="device-filter-actions">
          <button className="btn-outline" onClick={handleReset}>
            Reset
          </button>
          <button className="btn-main" onClick={handleSearch}>
            Tìm kiếm
          </button>
        </div>
      </div>

      <div className="device-table-card">
        <div className="device-table-head">
          <div className="device-table-title">
            Danh sách thiết bị
            <span className="device-count">{pageData.totalElements}</span>
          </div>
        </div>

        <div className="device-table-wrap">
          <table className="device-table">
            <thead>
              <tr>
                <th>STT</th>
                <th>Mã thiết bị</th>
                <th>Tên thiết bị</th>
                <th>Serial</th>
                <th>Site</th>
                <th>Trạng thái</th>
                <th>Ưu tiên</th>
                <th>Bảo hành</th>
                <th>Chi phí</th>
                <th className="th-action">Thao tác</th>
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="10" className="empty-cell">Đang tải dữ liệu...</td>
                </tr>
              ) : rows.length === 0 ? (
                <tr>
                  <td colSpan="10" className="empty-cell">Không có dữ liệu</td>
                </tr>
              ) : (
                rows.map((item, index) => (
                  <tr key={item.deviceId}>
                    <td>{pageData.number * pageData.size + index + 1}</td>
                    <td>{item.deviceCode}</td>
                    <td className="td-name">
                      <div className="name-main">{item.deviceName}</div>
                      <div className="name-sub">{item.manufacturer || '-'}</div>
                    </td>
                    <td>{item.serialNumber}</td>
                    <td>{item.site || '-'}</td>
                    <td>
                      <span className={getStatusClass(item.status)}>
                        {getStatusLabel(item.status)}
                      </span>
                    </td>
                    <td>
                      <span className={getPriorityClass(item.priority)}>
                        {getPriorityLabel(item.priority)}
                      </span>
                    </td>
                    <td>{formatDate(item.warrantyEndDate)}</td>
                    <td>{formatMoney(item.cost)}</td>
                    <td className="td-action">
                      <button className="icon-action-btn" onClick={() => handleOpenDetail(item)}>
                        ✎
                      </button>

                      {canManage && (
                        <button
                          className="text-action-btn"
                          onClick={() => handleRetire(item)}
                          disabled={item.status === 'RETIRED'}
                        >
                          Retire
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="device-pagination">
          <button
            className="page-nav-btn"
            disabled={pageData.number <= 0}
            onClick={() => handlePageChange(pageData.number - 1)}
          >
            &lt;
          </button>

          <div className="page-current">{pageData.number + 1}</div>

          <button
            className="page-nav-btn"
            disabled={pageData.number + 1 >= pageData.totalPages}
            onClick={() => handlePageChange(pageData.number + 1)}
          >
            &gt;
          </button>

          <div className="page-size-box">
            {(filter.size || 5).toString().padStart(2, '0')} /Trang
          </div>
        </div>
      </div>

      <DeviceDetailModal
        open={detailOpen}
        device={selectedDevice}
        onClose={() => setDetailOpen(false)}
        canManage={canManage}
      />
    </div>
  )
}