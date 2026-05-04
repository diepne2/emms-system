import { useEffect, useMemo, useRef, useState } from 'react'
import axios from 'axios'
import dayjs from 'dayjs'
import {
  Button,
  Empty,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd'
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  InboxOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  SearchOutlined,
  StopOutlined,
  ToolOutlined,
  WarningOutlined,
} from '@ant-design/icons'
import './PreventiveMaintenance.css'

const { Text } = Typography
const { TextArea } = Input

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://emms-system-production-4239.up.railway.app',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token =
    localStorage.getItem('accessToken') ||
    localStorage.getItem('token') ||
    localStorage.getItem('jwt') ||
    localStorage.getItem('access_token')

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
    { status: 'IN_PROGRESS', label: 'Bắt đầu', icon: <PlayCircleOutlined /> },
    { status: 'ON_HOLD', label: 'Tạm giữ', icon: <PauseCircleOutlined /> },
  ],
  IN_PROGRESS: [
    { status: 'PENDING', label: 'Nộp duyệt', icon: <ClockCircleOutlined /> },
    { status: 'ON_HOLD', label: 'Tạm giữ', icon: <PauseCircleOutlined /> },
  ],
  ON_HOLD: [
    { status: 'IN_PROGRESS', label: 'Tiếp tục', icon: <PlayCircleOutlined /> },
    { status: 'OPEN', label: 'Mở lại', icon: <ReloadOutlined /> },
  ],
  PENDING: [
    { status: 'DONE', label: 'Duyệt', icon: <CheckCircleOutlined /> },
    { status: 'IN_PROGRESS', label: 'Trả về', icon: <PlayCircleOutlined /> },
  ],
  DONE: [],
  CANCELLED: [],
}

const isTerminal = (s) => ['DONE', 'CANCELLED'].includes(s)

const isOverdue = (wo) => {
  if (!wo?.dueDate || isTerminal(wo.status)) return false
  return dayjs(wo.dueDate).isBefore(dayjs(), 'day')
}

const isLateDone = (wo) => {
  if (wo?.status !== 'DONE' || !wo?.dueDate || !wo?.completedOn) return false
  return dayjs(wo.completedOn).isAfter(dayjs(wo.dueDate), 'day')
}

const getViewStatus = (wo) => {
  if (isLateDone(wo)) return 'COMPLETED_LATE'
  if (isOverdue(wo)) return 'OVERDUE'
  return wo?.status || 'OPEN'
}

const formatDate = (v) => {
  if (!v) return '-'
  const d = dayjs(v)
  return d.isValid() ? d.format('DD/MM/YYYY') : v
}

const getErrMsg = (err, fallback) =>
  err?.response?.data?.message ||
  err?.response?.data?.error ||
  (typeof err?.response?.data === 'string' ? err.response.data : '') ||
  `${fallback} (${err?.response?.status || 'network'})`

export default function WorkOrderList({ mode = 'my' }) {
  const didLoad = useRef(false)

  const [data, setData] = useState([])
  const [filtered, setFiltered] = useState([])
  const [loading, setLoading] = useState(false)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const [feedbackOpen, setFeedbackOpen] = useState(false)
  const [selected, setSelected] = useState(null)
  const [nextStatus, setNextStatus] = useState(null)
  const [feedback, setFeedback] = useState('')

  const load = async () => {
    try {
      setLoading(true)
      const url = mode === 'my' ? '/api/work-orders/my' : '/api/work-orders'
      const res = await api.get(url)
      const rows = Array.isArray(res.data)
        ? res.data
        : Array.isArray(res.data?.content)
          ? res.data.content
          : []

      setData(rows)
      setFiltered(rows)
    } catch (err) {
      console.error('WORK ORDER LOAD ERROR:', err)
      message.error(getErrMsg(err, 'Không tải được Work Orders'))
      setData([])
      setFiltered([])
    } finally {
      setLoading(false)
    }
  }

  const stats = useMemo(() => {
    const total = data.length
    const active = data.filter((x) => ['OPEN', 'IN_PROGRESS', 'PENDING', 'ON_HOLD'].includes(x.status)).length
    const overdue = data.filter((x) => getViewStatus(x) === 'OVERDUE').length
    const done = data.filter((x) => getViewStatus(x) === 'DONE' || getViewStatus(x) === 'COMPLETED_LATE').length
    return { total, active, overdue, done }
  }, [data])

  const applyFilter = () => {
    let rows = [...data]

    if (statusFilter !== 'ALL') {
      rows = rows.filter((x) => getViewStatus(x) === statusFilter)
    }

    if (keyword.trim()) {
      const q = keyword.trim().toLowerCase()
      rows = rows.filter((x) =>
        [x.id, x.title, x.description, x.assetName, x.locationName]
          .map((v) => String(v || '').toLowerCase())
          .some((s) => s.includes(q)),
      )
    }

    setFiltered(rows)
  }

  const openChangeModal = (record, status) => {
    setSelected(record)
    setNextStatus(status)
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
      message.error(getErrMsg(err, 'Cập nhật trạng thái thất bại'))
    }
  }

  const cancelWO = (record) => {
    Modal.confirm({
      title: 'Hủy Work Order?',
      content: `Bạn chắc chắn muốn hủy "${record.title || `WO-${record.id}`}"?`,
      okText: 'Hủy WO',
      okButtonProps: { danger: true },
      cancelText: 'Đóng',
      async onOk() {
        try {
          await api.patch(`/api/work-orders/${record.id}/status`, {
            status: 'CANCELLED',
            feedback: 'Cancelled from UI',
          })
          message.success('Đã hủy Work Order')
          await load()
        } catch (err) {
          message.error(getErrMsg(err, 'Hủy Work Order thất bại'))
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
  }, [keyword, statusFilter, data])

  const columns = [
    {
      title: 'Mã',
      dataIndex: 'id',
      width: 110,
      render: (id) => <span className="wo-code">WO-{id}</span>,
    },
    {
      title: 'Work Order',
      dataIndex: 'title',
      render: (_, r) => (
        <div className="wo-title-cell">
          <div className="wo-title-icon"><ToolOutlined /></div>
          <div>
            <div className="wo-title-text">{r.title || '-'}</div>
            <div className="wo-title-sub">{r.description || 'Không có mô tả'}</div>
          </div>
        </div>
      ),
    },
    {
      title: 'Thiết bị',
      dataIndex: 'assetName',
      width: 190,
      render: (v) => v ? <span className="wo-asset-pill">{v}</span> : <span className="wo-muted">-</span>,
    },
    {
      title: 'Vị trí',
      dataIndex: 'locationName',
      width: 160,
      render: (v) => v || <span className="wo-muted">-</span>,
    },
    {
      title: 'Hạn xử lý',
      dataIndex: 'dueDate',
      width: 140,
      render: (v, record) => {
        const overdue = isOverdue(record)
        return (
          <span className={overdue ? 'wo-date-overdue' : 'wo-date'}>
            {overdue && <WarningOutlined />} {formatDate(v)}
          </span>
        )
      },
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      width: 150,
      render: (_, record) => {
        const vs = getViewStatus(record)
        return <Tag className="wo-status-tag" color={statusColor[vs] || 'default'}>{statusLabel[vs] || vs || '-'}</Tag>
      },
    },
    {
      title: 'Thao tác',
      width: 250,
      render: (_, record) => (
        <Space wrap size={6}>
          {(nextActions[record.status] || []).map((action) => (
            <Tooltip key={action.status} title={`Chuyển sang: ${statusLabel[action.status]}`}>
              <Button
                size="small"
                icon={action.icon}
                className="wo-action-btn"
                onClick={() => openChangeModal(record, action.status)}
              >
                {action.label}
              </Button>
            </Tooltip>
          ))}

          {!isTerminal(record.status) && (
            <Button
              danger
              size="small"
              icon={<StopOutlined />}
              className="wo-action-btn"
              onClick={() => cancelWO(record)}
            >
              Hủy
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div className="wo-page-shell">
      <div className="wo-page">
        <div className="wo-hero-card">
          <div className="wo-hero-left">
            <h1>{mode === 'my' ? 'Work Orders của tôi' : 'Tất cả Work Orders'}</h1>
          </div>

          <Button icon={<ReloadOutlined />} onClick={load} loading={loading} className="wo-reload-btn">
            Tải lại
          </Button>
        </div>

        <div className="wo-stat-grid">
          <div className="wo-stat-card">
            <span>Tổng Work Orders</span>
            <b>{stats.total}</b>
            <small>Tất cả công việc được phân công</small>
          </div>
          <div className="wo-stat-card wo-stat-blue">
            <span>Đang xử lý</span>
            <b>{stats.active}</b>
            <small>OPEN / IN PROGRESS / PENDING</small>
          </div>
          <div className="wo-stat-card wo-stat-red">
            <span>Quá hạn</span>
            <b>{stats.overdue}</b>
            <small>Cần ưu tiên kiểm tra</small>
          </div>
          <div className="wo-stat-card wo-stat-green">
            <span>Hoàn tất</span>
            <b>{stats.done}</b>
            <small>Đã đóng hoặc hoàn tất trễ</small>
          </div>
        </div>

        <div className="wo-panel">
          <div className="wo-panel-head">
            <div>
              <h2>Danh sách Work Orders</h2>
              <p>{filtered.length} kết quả đang hiển thị</p>
            </div>
          </div>

          <div className="wo-toolbar">
            <Input
              allowClear
              prefix={<SearchOutlined />}
              placeholder="Tìm kiếm"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="wo-search"
            />

            <Select
              value={statusFilter}
              onChange={setStatusFilter}
              className="wo-status-select"
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
            rowClassName={(record) => (getViewStatus(record) === 'OVERDUE' ? 'wo-row-overdue' : '')}
            pagination={{ pageSize: 10, showSizeChanger: true }}
            scroll={{ x: 1100 }}
            locale={{
              emptyText: (
                <div className="wo-empty">
                  <InboxOutlined />
                  <h3>Không có Work Order nào</h3>
                  <p>Hiện chưa có công việc bảo trì phù hợp với bộ lọc.</p>
                </div>
              ),
            }}
          />
        </div>
      </div>

      <Modal
        title="Cập nhật trạng thái"
        open={feedbackOpen}
        onCancel={() => setFeedbackOpen(false)}
        onOk={submitChange}
        okText="Xác nhận"
        cancelText="Hủy"
        className="wo-feedback-modal"
      >
        <Space direction="vertical" size={14} style={{ width: '100%' }}>
          <div>
            <Text type="secondary" className="wo-modal-label">Work Order</Text>
            <div className="wo-modal-value">{selected?.title || '-'}</div>
          </div>

          <div>
            <Text type="secondary" className="wo-modal-label">Trạng thái mới</Text>
            <div style={{ marginTop: 6 }}>
              <Tag color={statusColor[nextStatus] || 'default'}>{statusLabel[nextStatus] || nextStatus || '-'}</Tag>
            </div>
          </div>

          <div>
            <Text type="secondary" className="wo-modal-label">Ghi chú / Feedback</Text>
            <TextArea
              rows={4}
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              placeholder="Nhập ghi chú hoặc lý do..."
              className="wo-feedback-textarea"
            />
          </div>
        </Space>
      </Modal>
    </div>
  )
}
