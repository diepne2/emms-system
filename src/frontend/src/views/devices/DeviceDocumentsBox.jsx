import React, { useEffect, useState } from 'react'
import { deviceApi } from '../../api/deviceApi'
import DeviceDocumentsBox from './DeviceDocumentsBox'

function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '-'
  return Number(value).toLocaleString('vi-VN') + ' đ'
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('vi-VN')
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

export default function DeviceDetailModal({ open, device, onClose, canManage }) {
  const [detail, setDetail] = useState(device)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const fetchDetail = async () => {
      if (!open || !device?.deviceId) return

      try {
        setLoading(true)
        const res = await deviceApi.getById(device.deviceId)
        setDetail(res)
      } catch (error) {
        console.error(error)
        setDetail(device)
      } finally {
        setLoading(false)
      }
    }

    fetchDetail()
  }, [open, device])

  useEffect(() => {
    const onEsc = (e) => {
      if (e.key === 'Escape') onClose()
    }

    if (open) {
      window.addEventListener('keydown', onEsc)
    }

    return () => window.removeEventListener('keydown', onEsc)
  }, [open, onClose])

  if (!open || !device) return null

  return (
    <div className="device-modal-backdrop" onClick={onClose}>
      <div className="device-modal" onClick={(e) => e.stopPropagation()}>
        <div className="device-modal-header">
          <h2>Xem chi tiết thiết bị</h2>
          <button className="modal-x-btn" onClick={onClose}>×</button>
        </div>

        {loading ? (
          <div className="device-modal-loading">Đang tải chi tiết...</div>
        ) : (
          <div className="device-modal-body">
            <div className="device-info-box">
              <div className="device-info-grid">
                <div className="device-info-item">
                  <div className="label">Mã thiết bị</div>
                  <div className="value">{detail?.deviceCode || '-'}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Tên thiết bị</div>
                  <div className="value">{detail?.deviceName || '-'}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Serial</div>
                  <div className="value">{detail?.serialNumber || '-'}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Hãng sản xuất</div>
                  <div className="value">{detail?.manufacturer || '-'}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Site</div>
                  <div className="value">{detail?.site || '-'}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Trạng thái</div>
                  <div className="value">{getStatusLabel(detail?.status)}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Người phụ trách</div>
                  <div className="value">{detail?.responsibleStaff || '-'}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Ưu tiên</div>
                  <div className="value">{detail?.priority || '-'}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Ngày sản xuất</div>
                  <div className="value">{formatDate(detail?.manufactureDate)}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Ngày đưa vào sử dụng</div>
                  <div className="value">{formatDate(detail?.commissionDate)}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Ngày hết bảo hành</div>
                  <div className="value">{formatDate(detail?.warrantyEndDate)}</div>
                </div>

                <div className="device-info-item">
                  <div className="label">Chi phí</div>
                  <div className="value">{formatMoney(detail?.cost)}</div>
                </div>
              </div>
            </div>

            <div className="device-text-sections">
              <div className="device-text-card">
                <div className="text-card-title">Mô tả</div>
                <div className="text-card-content">{detail?.description || '-'}</div>
              </div>

              <div className="device-text-card">
                <div className="text-card-title">Thông số kỹ thuật</div>
                <div className="text-card-content">{detail?.specifications || '-'}</div>
              </div>

              <div className="device-text-card">
                <div className="text-card-title">Datasheet</div>
                <div className="text-card-content">{detail?.datasheet || '-'}</div>
              </div>

              <div className="device-text-card">
                <div className="text-card-title">Lịch bảo trì</div>
                <div className="text-card-content">{detail?.maintenanceSchedule || '-'}</div>
              </div>

              <div className="device-text-card">
                <div className="text-card-title">Lịch sử bảo trì</div>
                <div className="text-card-content">{detail?.maintenanceHistory || '-'}</div>
              </div>

              <div className="device-text-card">
                <div className="text-card-title">Ghi chú</div>
                <div className="text-card-content">{detail?.note || '-'}</div>
              </div>
            </div>

            <DeviceDocumentsBox
              deviceId={detail?.deviceId}
              canManage={canManage}
            />
          </div>
        )}

        <div className="device-modal-footer">
          <button className="btn-outline" onClick={onClose}>Đóng</button>
        </div>
      </div>
    </div>
  )
}