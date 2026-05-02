import { useEffect, useRef, useState } from 'react'
import axios from 'axios'
import dayjs from 'dayjs'
import {
  Button,
  Card,
  Empty,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd'
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  StopOutlined,
} from '@ant-design/icons'
import './PreventiveMaintenance.css'

const { Title, Text } = Typography
const { TextArea } = Input

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://emms-system-production-4239.up.railway.app',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token =
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken') ||
    localStorage.getItem('jwt')

  if (token) config.headers.Authorization = `Bearer ${token}`

  return config
})

const statusColor = {
  OPEN: 'blue',
  IN_PROGRESS: 'processing',
  ON_HOLD: 'orange',
  PENDING: 'purple',
  DONE: 'green',
  CANCELLED: 'red',
  OVERDUE: 'red',
  COMPLETED_LATE: 'volcano',
}

const statusLabel = {
  OPEN: 'Mới',
  IN_PROGRESS: 'Đang làm',
  ON_HOLD: 'Tạm dừng',
  PENDING: 'Chờ duyệt',
  DONE: 'Hoàn tất',
  CANCELLED: 'Đã hủy',
  OVERDUE: 'Quá hạn',
  COMPLETED_LATE: 'Hoàn tất trễ',
}

const nextActions = {
  OPEN: [
    { status: 'IN_PROGRESS', label: 'Start', icon: <PlayCircleOutlined /> },
    { status: 'ON_HOLD', label: 'Hold', icon: <PauseCircleOutlined /> },
  ],
  IN_PROGRESS: [
    { status: 'PENDING', label: 'Submit', icon: <ClockCircleOutlined /> },
    { status: 'ON_HOLD', label: 'Hold', icon: <PauseCircleOutlined /> },
  ],
  ON_HOLD: [
    { status: 'IN_PROGRESS', label: 'Resume', icon: <PlayCircleOutlined /> },
    { status: 'OPEN', label: 'Reopen', icon: <ReloadOutlined /> },
  ],
  PENDING: [
    { status: 'DONE', label: 'Approve', icon: <CheckCircleOutlined /> },
    { status: 'IN_PROGRESS', label: 'Return', icon: <PlayCircleOutlined /> },
  ],
  DONE: [],
  CANCELLED: [],
}

const isTerminalStatus = (status) => ['DONE', 'CANCELLED'].includes(status)

const isOverdue = (wo) => {
  if (!wo?.dueDate) return false
  if (isTerminalStatus(wo.status)) return false
  return dayjs(wo.dueDate).isBefore(dayjs(), 'day')
}

const isCompletedLate = (wo) => {
  if (wo?.status !== 'DONE') return false
  if (!wo?.dueDate || !wo?.completedOn) return false
  return dayjs(wo.completedOn).isAfter(dayjs(wo.dueDate), 'day')
}

const getViewStatus = (wo) => {
  if (isCompletedLate(wo)) return 'COMPLETED_LATE'
  if (isOverdue(wo)) return 'OVERDUE'
  return wo?.status
}

const formatDate = (value) => {
  if (!value) return '-'
  const d = dayjs(value)
  return d.isValid() ? d.format('DD/MM/YYYY') : value
}

export default function WorkOrderList({ mode = 'my' }) {
  const didLoad = useRef(false)

  const [data, setData] = useState([])
  const [filtered, setFiltered] = useState([])
  const [loading, setLoading] = useState(false)

  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState('ALL')

  const [feedbackOpen, setFeedbackOpen] = useState(false)
  const [selected, setSelected] = useState(null)
  const [nextStatus, setNextStatus] = useState(null)
  const [feedback, setFeedback] = useState('')

  const getErrorMessage = (err, fallback) =>
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    (typeof err?.response?.data === 'string' ? err.response.data : '') ||
    `${fallback} (${err?.response?.status || 'network'})`

  const load = async () => {
    try {
      setLoading(true)

      const url = mode === 'my' ? '/api/work-orders/my' : '/api/work-orders'
      const res = await api.get(url)

      const rows = Array.isArray(res.data) ? res.data : []
      setData(rows)
      setFiltered(rows)
    } catch (err) {
      console.error('WORK_ORDER_ERROR:', err?.response || err)
      message.error(getErrorMessage(err, 'Không tải được Work Orders'))
      setData([])
      setFiltered([])
    } finally {
      setLoading(false)
    }
  }

  const applyFilter = () => {
    let rows = [...data]

    if (status !== 'ALL') {
      rows = rows.filter((x) => getViewStatus(x) === status)
    }

    if (keyword.trim()) {
      const q = keyword.trim().toLowerCase()

      rows = rows.filter((x) => {
        const id = String(x.id || '').toLowerCase()
        const title = String(x.title || '').toLowerCase()
        const assetName = String(x.assetName || '').toLowerCase()
        const locationName = String(x.locationName || '').toLowerCase()

        return (
          id.includes(q) ||
          title.includes(q) ||
          assetName.includes(q) ||
          locationName.includes(q)
        )
      })
    }

    setFiltered(rows)
  }

  const openChangeModal = (record, statusValue) => {
    setSelected(record)
    setNextStatus(statusValue)
    setFeedback('')
    setFeedbackOpen(true)
  }

  const submitChange = async () => {
    if (!selected || !nextStatus) return

    try {
      await api.patch(`/api/work-orders/${selected.id}/status`, {
        status: nextStatus,
        feedback,
      })

      message.success('Cập nhật trạng thái thành công')
      setFeedbackOpen(false)
      await load()
    } catch (err) {
      console.error('CHANGE_WO_STATUS_ERROR:', err?.response || err)
      message.error(getErrorMessage(err, 'Cập nhật trạng thái thất bại'))
    }
  }

  const cancelWO = (record) => {
    Modal.confirm({
      title: 'Hủy Work Order?',
      content: `Bạn chắc chắn muốn hủy "${record.title}"?`,
      okText: 'Hủy WO',
      cancelText: 'Đóng',
      okButtonProps: { danger: true },
      async onOk() {
        try {
          await api.patch(`/api/work-orders/${record.id}/status`, {
            status: 'CANCELLED',
            feedback: 'Cancelled from UI',
          })

          message.success('Đã hủy Work Order')
          await load()
        } catch (err) {
          console.error('CANCEL_WO_ERROR:', err?.response || err)
          message.error(getErrorMessage(err, 'Hủy Work Order thất bại'))
        }
      },
    })
  }

  useEffect(() => {
    if (didLoad.current) return
    didLoad.current = true
    load()
  }, [mode])

  useEffect(() => {
    applyFilter()
  }, [keyword, status, data])

  const columns = [
    {
      title: 'Mã',
      dataIndex: 'id',
      width: 90,
      render: (id) => <Text strong>WO-{id}</Text>,
    },
    {
      title: 'Work Order',
      dataIndex: 'title',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.title || '-'}</Text>
          <Text type="secondary">{record.description || 'Không có mô tả'}</Text>
        </Space>
      ),
    },
    {
      title: 'Thiết bị',
      dataIndex: 'assetName',
      width: 180,
      render: (v) => v || '-',
    },
    {
      title: 'Vị trí',
      dataIndex: 'locationName',
      width: 160,
      render: (v) => v || '-',
    },
    {
      title: 'Hạn xử lý',
      dataIndex: 'dueDate',
      width: 130,
      render: (v) => formatDate(v),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 150,
      render: (_, record) => {
        const viewStatus = getViewStatus(record)

        return (
          <Tag color={statusColor[viewStatus] || 'default'}>
            {statusLabel[viewStatus] || viewStatus || '-'}
          </Tag>
        )
      },
    },
    {
      title: 'Thao tác',
      width: 260,
      render: (_, record) => (
        <Space wrap>
          {(nextActions[record.status] || []).map((action) => (
            <Button
              key={action.status}
              size="small"
              icon={action.icon}
              onClick={() => openChangeModal(record, action.status)}
            >
              {action.label}
            </Button>
          ))}

          {!['DONE', 'CANCELLED'].includes(record.status) && (
            <Button
              danger
              size="small"
              icon={<StopOutlined />}
              onClick={() => cancelWO(record)}
            >
              Cancel
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div className="pm-shell">
      <Card className="pm-card-pro">
        <div className="pm-list-header">
          <div>
            <Title level={3} className="pm-title">
              {mode === 'my' ? 'Work Orders của tôi' : 'Tất cả Work Orders'}
            </Title>
          </div>

          <Button icon={<ReloadOutlined />} onClick={load} loading={loading}>
            Tải lại
          </Button>
        </div>

        <div className="pm-stats">
          <div className="pm-stat-box">
            <span>Tổng WO</span>
            <b>{data.length}</b>
          </div>

          <div className="pm-stat-box">
            <span>Đang xử lý</span>
            <b>{data.filter((x) => x.status === 'IN_PROGRESS').length}</b>
          </div>

          <div className="pm-stat-box">
            <span>Quá hạn</span>
            <b>{data.filter((x) => getViewStatus(x) === 'OVERDUE').length}</b>
          </div>

          <div className="pm-stat-box">
            <span>Hoàn tất trễ</span>
            <b>{data.filter((x) => getViewStatus(x) === 'COMPLETED_LATE').length}</b>
          </div>
        </div>

        <div className="pm-filter-bar">
          <Input.Search
            allowClear
            placeholder="Tìm kiếm"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ maxWidth: 360 }}
          />

          <Select
            value={status}
            onChange={setStatus}
            style={{ width: 190 }}
            options={[
              { value: 'ALL', label: 'Tất cả trạng thái' },
              { value: 'OPEN', label: 'Mới' },
              { value: 'IN_PROGRESS', label: 'Đang làm' },
              { value: 'ON_HOLD', label: 'Tạm dừng' },
              { value: 'PENDING', label: 'Chờ duyệt' },
              { value: 'DONE', label: 'Hoàn tất' },
              { value: 'COMPLETED_LATE', label: 'Hoàn tất trễ' },
              { value: 'OVERDUE', label: 'Quá hạn' },
              { value: 'CANCELLED', label: 'Đã hủy' },
            ]}
          />
        </div>

        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={filtered}
          pagination={{ pageSize: 8 }}
          locale={{
            emptyText: <Empty description="Không có Work Order" />,
          }}
        />
      </Card>

      <Modal
        title="Cập nhật trạng thái Work Order"
        open={feedbackOpen}
        onCancel={() => setFeedbackOpen(false)}
        onOk={submitChange}
        okText="Cập nhật"
        cancelText="Hủy"
      >
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <div>
            <Text type="secondary">Work Order</Text>
            <div>
              <Text strong>{selected?.title || '-'}</Text>
            </div>
          </div>

          <div>
            <Text type="secondary">Trạng thái mới</Text>
            <div>
              <Tag color={statusColor[nextStatus] || 'default'}>
                {statusLabel[nextStatus] || nextStatus || '-'}
              </Tag>
            </div>
          </div>

          <TextArea
            rows={4}
            value={feedback}
            onChange={(e) => setFeedback(e.target.value)}
            placeholder="Nhập ghi chú / feedback..."
          />
        </Space>
      </Modal>
    </div>
  )
}