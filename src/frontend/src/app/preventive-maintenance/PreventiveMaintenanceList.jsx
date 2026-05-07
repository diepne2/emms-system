import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import axios from 'axios'
import dayjs from 'dayjs'
import {
  Button,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Tooltip,
  message,
} from 'antd'
import {
  PlusOutlined,
  ReloadOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  SaveOutlined,
  SearchOutlined,
  BarChartOutlined,
  CheckCircleOutlined,
  StopOutlined,
  FilterOutlined,
  DownloadOutlined,
  CalendarOutlined,
  FileTextOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import './PreventiveMaintenance.css'

const { TextArea } = Input

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  'https://emms-system-production-4239.up.railway.app'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const token =
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken') ||
    localStorage.getItem('access_token') ||
    localStorage.getItem('jwt')

  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

const priorityColor = {
  LOW: 'default',
  MEDIUM: 'blue',
  HIGH: 'orange',
  URGENT: 'red',
}

const priorityLabel = {
  LOW: 'THẤP',
  MEDIUM: 'TRUNG BÌNH',
  HIGH: 'CAO',
  URGENT: 'KHẨN CẤP',
}

const recurrenceLabel = {
  DAILY: 'Hằng ngày',
  WEEKLY: 'Hằng tuần',
  MONTHLY: 'Hằng tháng',
  YEARLY: 'Hằng năm',
}

const PM_FILTERS = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'DAILY', label: 'Hàng ngày' },
  { key: 'WEEKLY', label: 'Hàng tuần' },
  { key: 'MONTHLY', label: 'Hàng tháng' },
  { key: 'YEARLY', label: 'Hàng năm' },
]

const normalizeRows = (raw) => {
  if (Array.isArray(raw)) return raw
  if (Array.isArray(raw?.content)) return raw.content
  if (Array.isArray(raw?.data)) return raw.data
  if (Array.isArray(raw?.items)) return raw.items
  return []
}

const getErrorMessage = (err, fallback) => {
  const raw =
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    (typeof err?.response?.data === 'string' ? err.response.data : '') ||
    err?.message ||
    fallback

  if (
    raw?.includes('đã phát sinh Work Order') ||
    raw?.includes('foreign key constraint') ||
    raw?.includes('preventive_maintenances') ||
    raw?.includes('work_orders')
  ) {
    return 'Kế hoạch bảo trì đã phát sinh Work Order nên không thể xóa. Hệ thống đã tắt lịch và chuyển kế hoạch sang ngưng hoạt động.'
  }

  return raw || fallback
}

const toDateTimeString = (value) => {
  if (!value) return null
  return dayjs(value).format('YYYY-MM-DDTHH:mm:ss')
}

const buildCreatePayload = (values) => ({
  title: values.title?.trim(),
  description: values.description?.trim() || null,
  assetId: values.assetId || null,
  assignedToId: values.assignedToId || null,
  estimatedHours: values.estimatedHours ?? null,
  startsOn: toDateTimeString(values.startsOn),
  endsOn: values.endsOn ? toDateTimeString(values.endsOn) : null,
  recurrenceRule: {
    type: values.type || 'DAILY',
    basedOn: values.basedOn || 'SCHEDULED_DATE',
    frequency: Number(values.frequency || 1),
    dueDateDelay: Number(values.dueDateDelay || 0),
    daysOfWeek: values.type === 'WEEKLY' ? values.daysOfWeek || [] : null,
    priority: values.priority || 'MEDIUM',
  },
})

const buildUpdatePayload = (values) => ({
  title: values.title?.trim(),
  description: values.description?.trim() || null,
  assetId: values.assetId || null,
  assignedToId: values.assignedToId || null,
  estimatedHours: values.estimatedHours ?? null,
  active: values.active ?? true,
  priority: values.priority || 'MEDIUM',
  startsOn: values.startsOn ? toDateTimeString(values.startsOn) : null,
  endsOn: values.endsOn ? toDateTimeString(values.endsOn) : null,
  recurrenceRule: {
    type: values.type || 'DAILY',
    basedOn: values.basedOn || 'SCHEDULED_DATE',
    frequency: Number(values.frequency || 1),
    dueDateDelay: Number(values.dueDateDelay || 0),
    daysOfWeek: values.type === 'WEEKLY' ? values.daysOfWeek || [] : null,
    priority: values.priority || 'MEDIUM',
  },
})

const getAssetName = (record) =>
  record.asset?.name || record.asset?.assetName || record.assetName || '-'

const getAssetCode = (record) =>
  record.asset?.code || record.asset?.assetCode || record.assetCode || ''

const getAssignedName = (record) =>
  record.assignedTo?.fullName ||
  record.assignedTo?.username ||
  record.assignedTo?.email ||
  record.assignedToName ||
  '-'

const initialsOf = (name) => {
  if (!name || name === '-') return '?'
  const words = String(name).trim().split(/\s+/)
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase()
  return `${words[0][0]}${words[words.length - 1][0]}`.toUpperCase()
}

const normalizeRecurrenceType = (value) => {
  if (!value) return ''

  const raw = String(value).trim().toUpperCase()

  if (raw.includes('WEEK')) return 'WEEKLY'
  if (raw.includes('MONTH')) return 'MONTHLY'
  if (raw.includes('YEAR')) return 'YEARLY'
  if (raw.includes('DAY')) return 'DAILY'

  if (raw.includes('TUẦN')) return 'WEEKLY'
  if (raw.includes('THÁNG')) return 'MONTHLY'
  if (raw.includes('NĂM')) return 'YEARLY'
  if (raw.includes('NGÀY')) return 'DAILY'

  return raw
}

const getRecurrenceType = (record) =>
  normalizeRecurrenceType(
    record?.recurrenceRule?.type ||
      record?.recurrenceType ||
      record?.schedule?.recurrenceType ||
      record?.schedule?.type ||
      record?.type ||
      '',
  )

const getFrequencyText = (record) => {
  const type = getRecurrenceType(record)
  const frequency =
    record?.recurrenceRule?.frequency ||
    record?.schedule?.frequency ||
    record?.frequency

  if (!type && !frequency) return record?.description || 'Chưa có thông tin lịch lặp'

  const label = recurrenceLabel[type] || type || 'Lặp'
  return frequency && Number(frequency) > 1 ? `${label} / ${frequency}` : label
}

const isPmExpired = (record) => {
  const endsOn = record?.endsOn || record?.schedule?.endsOn || record?.scheduleEndDate
  if (!endsOn) return false
  return dayjs().isAfter(dayjs(endsOn), 'day')
}

export default function PreventiveMaintenanceList({ autoOpenCreate = false }) {
  const navigate = useNavigate()
  const location = useLocation()
  const [form] = Form.useForm()

  const [items, setItems] = useState([])
  const [assets, setAssets] = useState([])
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [filterType, setFilterType] = useState('ALL')

  const [openModal, setOpenModal] = useState(false)
  const [editingItem, setEditingItem] = useState(null)

  const [assetSearch, setAssetSearch] = useState('')
  const [userSearch, setUserSearch] = useState('')
  const [assetDropdownOpen, setAssetDropdownOpen] = useState(false)
  const [userDropdownOpen, setUserDropdownOpen] = useState(false)

  const loadData = async () => {
    try {
      setLoading(true)
      const res = await api.get('/preventive-maintenances')
      setItems(normalizeRows(res.data))
    } catch (err) {
      message.error(getErrorMessage(err, 'Không tải được danh sách kế hoạch bảo trì'))
    } finally {
      setLoading(false)
    }
  }

  const loadOptions = async () => {
    const [assetRes, userRes] = await Promise.allSettled([
      api.get('/api/assets'),
      api.get('/api/users/technicians'),
    ])

    setAssets(assetRes.status === 'fulfilled' ? normalizeRows(assetRes.value.data) : [])
    setUsers(userRes.status === 'fulfilled' ? normalizeRows(userRes.value.data) : [])
  }

  useEffect(() => {
    loadData()
    loadOptions()
  }, [])

  useEffect(() => {
    if (autoOpenCreate) openCreateModal()
  }, [autoOpenCreate])

  useEffect(() => {
    const editId = location.state?.editId
    if (!editId || !items.length) return

    const item = items.find((x) => String(x.id) === String(editId))
    if (item) openEditModal(item)

    navigate('/preventive-maintenance', { replace: true, state: null })
  }, [location.state, items])

  const closeModal = () => {
    setOpenModal(false)
    setEditingItem(null)
    form.resetFields()

    if (window.location.hash.includes('/preventive-maintenance/new')) {
      navigate('/preventive-maintenance')
    }
  }

  const openCreateModal = () => {
    setEditingItem(null)
    form.resetFields()
    form.setFieldsValue({
      title: '',
      description: '',
      assetId: undefined,
      assignedToId: undefined,
      estimatedHours: 1,
      startsOn: dayjs(),
      endsOn: null,
      type: 'DAILY',
      frequency: 1,
      priority: 'MEDIUM',
      dueDateDelay: 0,
      basedOn: 'SCHEDULED_DATE',
      daysOfWeek: [],
      active: true,
    })
    setOpenModal(true)
  }

  const openEditModal = async (record) => {
    try {
      setEditingItem(record)
      form.resetFields()

      const res = await api.get(`/preventive-maintenances/${record.id}`)
      const pm = res.data || record
      const schedule = pm.schedule || {}
      const recurrenceRule = pm.recurrenceRule || {}

      form.setFieldsValue({
        title: pm.title || '',
        description: pm.description || '',
        assetId: pm.assetId || pm.asset?.id || pm.asset?.assetId || null,
        assignedToId:
          pm.assignedToId ||
          pm.assignedTo?.id ||
          pm.assignedTo?.userId ||
          null,
        estimatedHours: pm.estimatedHours ?? 1,
        active: pm.active ?? true,
        priority: pm.priority || recurrenceRule.priority || 'MEDIUM',
        startsOn: pm.startsOn
          ? dayjs(pm.startsOn)
          : schedule.startsOn
            ? dayjs(schedule.startsOn)
            : null,
        endsOn: pm.endsOn
          ? dayjs(pm.endsOn)
          : schedule.endsOn
            ? dayjs(schedule.endsOn)
            : null,
        type: recurrenceRule.type || schedule.recurrenceType || schedule.type || 'DAILY',
        basedOn:
          recurrenceRule.basedOn ||
          schedule.recurrenceBasedOn ||
          schedule.basedOn ||
          'SCHEDULED_DATE',
        frequency: recurrenceRule.frequency || schedule.frequency || 1,
        dueDateDelay: recurrenceRule.dueDateDelay ?? schedule.dueDateDelay ?? 0,
        daysOfWeek: recurrenceRule.daysOfWeek || schedule.daysOfWeek || [],
      })

      setOpenModal(true)
    } catch (err) {
      message.error(getErrorMessage(err, 'Không tải được dữ liệu kế hoạch để sửa'))
    }
  }

  const handleSave = async () => {
    try {
      const values = await form.validateFields()

      if (values.endsOn && values.startsOn && values.endsOn.isBefore(values.startsOn)) {
        message.error('Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu')
        return
      }

      setSaving(true)

      if (editingItem?.id) {
        await api.put(`/preventive-maintenances/${editingItem.id}`, buildUpdatePayload(values))
        message.success('Cập nhật kế hoạch bảo trì thành công')
      } else {
        await api.post('/preventive-maintenances', buildCreatePayload(values))
        message.success('Tạo kế hoạch bảo trì thành công')
      }

      closeModal()
      await loadData()
    } catch (err) {
      if (err?.errorFields) return
      message.error(getErrorMessage(err, editingItem ? 'Cập nhật thất bại' : 'Tạo thất bại'))
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = (id) => {
    Modal.confirm({
      title: 'Xóa kế hoạch bảo trì?',
      content:
        'Nếu kế hoạch đã phát sinh Work Order, hệ thống sẽ không xóa cứng mà tắt lịch và chuyển kế hoạch sang ngưng hoạt động.',
      okText: 'Xóa',
      cancelText: 'Hủy',
      okButtonProps: { danger: true },
      async onOk() {
        try {
          await api.delete(`/preventive-maintenances/${id}`)
          message.success('Đã xóa kế hoạch')
          await loadData()
        } catch (err) {
          message.warning(getErrorMessage(err, 'Xóa thất bại'))
          await loadData()
        }
      },
    })
  }

  const assetOptions = assets
    .filter((asset) => asset?.id || asset?.assetId)
    .map((asset) => {
      const id = asset.id || asset.assetId
      const code = asset.code || asset.assetCode || asset.barcode || ''
      const name = asset.name || asset.assetName || asset.title || `Asset #${id}`
      return { value: id, label: code ? `${name} - ${code}` : `${name} #${id}` }
    })

  const userOptions = users
    .filter((user) => user?.userId || user?.id)
    .map((user) => {
      const id = user.userId || user.id
      const name = user.fullName || user.name || user.username || user.email || `User #${id}`
      return { value: id, label: `${name} #${id}` }
    })

  const filterOptions = (options, searchValue) => {
    const q = searchValue.trim().toLowerCase()
    if (!q) return options
    return options.filter((option) => String(option?.label || '').toLowerCase().includes(q))
  }

  const assetOptionsFiltered = filterOptions(assetOptions, assetSearch)
  const userOptionsFiltered = filterOptions(userOptions, userSearch)

  const selectAsset = (option) => {
    form.setFieldValue('assetId', option.value)
    setAssetDropdownOpen(false)
    setAssetSearch('')
  }

  const selectUser = (option) => {
    form.setFieldValue('assignedToId', option.value)
    setUserDropdownOpen(false)
    setUserSearch('')
  }

  const renderSearchDropdown = ({
    value,
    onChange,
    placeholder,
    options,
    onSelect,
    emptyText,
  }) => (
    <div className="pm-select-dropdown-inner" onMouseDown={(e) => e.preventDefault()}>
      <Input
        allowClear
        autoFocus
        className="pm-dropdown-search"
        prefix={<SearchOutlined />}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onMouseDown={(e) => e.stopPropagation()}
        onClick={(e) => e.stopPropagation()}
        onKeyDown={(e) => e.stopPropagation()}
      />

      <div className="pm-select-menu-scroll">
        {options.length ? (
          options.map((option) => (
            <button
              key={option.value}
              type="button"
              className="pm-select-option"
              onMouseDown={(e) => {
                e.preventDefault()
                e.stopPropagation()
                onSelect(option)
              }}
            >
              {option.label}
            </button>
          ))
        ) : (
          <div className="pm-select-empty">{emptyText}</div>
        )}
      </div>
    </div>
  )

  const filteredItems = useMemo(() => {
    if (filterType === 'ALL') return items
    return items.filter((item) => getRecurrenceType(item) === filterType)
  }, [items, filterType])

  const totalPM = items.length
  const activePM = items.filter((x) => x.active).length
  const inactivePM = items.filter((x) => !x.active).length

  const columns = [
    {
      title: 'Mã',
      dataIndex: 'code',
      width: 120,
      render: (_, record) => (
        <span className="pm-code-text">{record.code || `PM-${record.id}`}</span>
      ),
    },
    {
      title: 'Kế hoạch bảo trì',
      dataIndex: 'title',
      width: 260,
      render: (_, record) => (
        <div className="pm-plan-cell">
          <Link className="pm-table-title" to={`/preventive-maintenance/${record.id}`}>
            {record.title || 'Không có tiêu đề'}
          </Link>
          <div className="pm-table-subtitle">{getFrequencyText(record)}</div>
        </div>
      ),
    },
    {
      title: 'Thiết bị',
      width: 220,
      render: (_, record) => (
        <div className="pm-asset-cell">
          <span className="pm-asset-icon">⚙</span>
          <div>
            <div className="pm-asset-name">{getAssetCode(record) || getAssetName(record)}</div>
            {getAssetCode(record) ? (
              <div className="pm-table-subtitle">{getAssetName(record)}</div>
            ) : null}
          </div>
        </div>
      ),
    },
    {
      title: 'Người phụ trách',
      width: 190,
      render: (_, record) => {
        const name = getAssignedName(record)
        return (
          <div className="pm-user-cell">
            <span className="pm-avatar">{initialsOf(name)}</span>
            <span>{name}</span>
          </div>
        )
      },
    },
    {
      title: 'Ưu tiên',
      dataIndex: 'priority',
      width: 135,
      render: (priority = 'MEDIUM') => (
        <Tag className="pm-priority-tag" color={priorityColor[priority] || 'default'}>
          {priorityLabel[priority] || priority}
        </Tag>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'active',
      width: 135,
      render: (active) =>
        active ? (
          <span className="pm-status pm-status--active">
            <i />
            Active
          </span>
        ) : (
          <span className="pm-status pm-status--inactive">
            <i />
            Inactive
          </span>
        ),
    },
    {
      title: 'Thao tác',
      width: 150,
      align: 'center',
      render: (_, record) => (
        <Space size={4}>
          <Tooltip title="Xem chi tiết">
            <Link to={`/preventive-maintenance/${record.id}`}>
              <Button
                className="pm-action-btn pm-action-view"
                type="text"
                shape="circle"
                icon={<EyeOutlined />}
              />
            </Link>
          </Tooltip>

          <Tooltip title="Chỉnh sửa">
            <Button
              className="pm-action-btn pm-action-edit"
              type="text"
              shape="circle"
              icon={<EditOutlined />}
              onClick={() => openEditModal(record)}
            />
          </Tooltip>

          <Tooltip title="Xóa">
            <Button
              className="pm-action-btn pm-action-delete"
              type="text"
              shape="circle"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDelete(record.id)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ]

  return (
    <div className="pm-shell">
      <div className="pm-page-pro">
        <div className="pm-hero-row">
          <div>
            <h1>Kế hoạch bảo trì định kỳ</h1>
          </div>

          <Button
            type="primary"
            size="large"
            icon={<PlusOutlined />}
            onClick={openCreateModal}
            className="pm-add-btn"
          >
            Thêm kế hoạch mới
          </Button>
        </div>

        <div className="pm-summary-grid">
          <div className="pm-summary-card">
            <div>
              <span>Tổng kế hoạch</span>
              <b>{totalPM}</b>
              <small>Tổng số kế hoạch trong hệ thống</small>
            </div>
            <div className="pm-summary-icon pm-summary-icon--blue">
              <BarChartOutlined />
            </div>
          </div>

          <div className="pm-summary-card">
            <div>
              <span>Đang active</span>
              <b>{activePM}</b>
              <small>Hoạt động bình thường</small>
            </div>
            <div className="pm-summary-icon pm-summary-icon--green">
              <CheckCircleOutlined />
            </div>
          </div>

          <div className="pm-summary-card">
            <div>
              <span>Inactive</span>
              <b>{inactivePM}</b>
              <small>Đang tạm dừng bảo trì</small>
            </div>
            <div className="pm-summary-icon pm-summary-icon--gray">
              <StopOutlined />
            </div>
          </div>
        </div>

        <div className="pm-list-panel">
          <div className="pm-list-panel__head">
            <div className="pm-list-tabs">
              <h2>Danh sách PM</h2>

              {PM_FILTERS.map((item) => (
                <button
                  key={item.key}
                  type="button"
                  className={`pm-tab ${filterType === item.key ? 'active' : ''}`}
                  onClick={() => setFilterType(item.key)}
                >
                  {item.label}
                </button>
              ))}
            </div>

            <Space wrap>
              <Button icon={<FilterOutlined />} className="pm-light-btn">
                Bộ lọc
              </Button>
              <Button icon={<DownloadOutlined />} className="pm-light-btn">
                Xuất dữ liệu
              </Button>
              <Button icon={<ReloadOutlined />} onClick={loadData} loading={loading}>
                Tải lại
              </Button>
            </Space>
          </div>

          <Table
            rowKey="id"
            loading={loading}
            columns={columns}
            dataSource={filteredItems}
            rowClassName={(record) => (isPmExpired(record) ? 'pm-expired-row' : '')}
            scroll={{ x: 1180 }}
            pagination={{
              pageSize: 8,
              showSizeChanger: false,
            }}
          />
        </div>
      </div>

      <Modal
        open={openModal}
        onCancel={closeModal}
        footer={null}
        centered={false}
        closable={false}
        destroyOnClose
        maskClosable={false}
        className="pm-create-modal"
        rootClassName="pm-create-modal-root"
      >
        <div className="pm-modal-head">
          <div className="pm-modal-title-wrap">
            <h2>{editingItem ? 'Chỉnh sửa kế hoạch bảo trì' : 'Thêm kế hoạch bảo trì'}</h2>
            <p>
              {editingItem
                ? 'Cập nhật thông tin và lịch lặp của preventive maintenance.'
                : 'Tạo mới kế hoạch bảo trì định kỳ cho thiết bị.'}
            </p>
          </div>

          <button
            type="button"
            className="pm-modal-x-btn"
            onClick={closeModal}
            aria-label="Đóng"
          >
            ×
          </button>
        </div>

        <div className="pm-modal-scroll-body">
          <Form form={form} layout="vertical">
            <div className="pm-modal-section-card">
              <div className="pm-modal-section-title">
                <SettingOutlined />
                <div>
                  <h3>Thông tin chính</h3>
                  <p>Thông tin cơ bản của kế hoạch bảo trì.</p>
                </div>
              </div>

              <div className="pm-form-grid">
                <Form.Item
                  label="Tên kế hoạch"
                  name="title"
                  rules={[{ required: true, message: 'Nhập tên kế hoạch' }]}
                >
                  <Input placeholder="Nhập tên kế hoạch" />
                </Form.Item>

                <Form.Item label="Thiết bị" name="assetId">
                  <Select
                    allowClear
                    showSearch={false}
                    open={assetDropdownOpen}
                    onOpenChange={(open) => {
                      setAssetDropdownOpen(open)
                      if (!open) setAssetSearch('')
                    }}
                    placeholder="Chọn thiết bị"
                    options={assetOptions}
                    popupClassName="pm-select-dropdown"
                    getPopupContainer={() => document.body}
                    dropdownRender={() =>
                      renderSearchDropdown({
                        value: assetSearch,
                        onChange: setAssetSearch,
                        placeholder: 'Tìm thiết bị...',
                        options: assetOptionsFiltered,
                        onSelect: selectAsset,
                        emptyText: 'Không có thiết bị',
                      })
                    }
                  />
                </Form.Item>

                <Form.Item label="Người phụ trách" name="assignedToId">
                  <Select
                    allowClear
                    showSearch={false}
                    open={userDropdownOpen}
                    onOpenChange={(open) => {
                      setUserDropdownOpen(open)
                      if (!open) setUserSearch('')
                    }}
                    placeholder="Chọn người phụ trách"
                    options={userOptions}
                    popupClassName="pm-select-dropdown"
                    getPopupContainer={() => document.body}
                    dropdownRender={() =>
                      renderSearchDropdown({
                        value: userSearch,
                        onChange: setUserSearch,
                        placeholder: 'Tìm người phụ trách...',
                        options: userOptionsFiltered,
                        onSelect: selectUser,
                        emptyText: 'Không có người phụ trách',
                      })
                    }
                  />
                </Form.Item>

                <Form.Item label="Estimated Hours" name="estimatedHours">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>

                <Form.Item label="Ưu tiên" name="priority">
                  <Select
                    options={[
                      { value: 'LOW', label: 'LOW' },
                      { value: 'MEDIUM', label: 'MEDIUM' },
                      { value: 'HIGH', label: 'HIGH' },
                      { value: 'URGENT', label: 'URGENT' },
                    ]}
                  />
                </Form.Item>

                <Form.Item label="Trạng thái" name="active">
                  <Select
                    options={[
                      { value: true, label: 'Active' },
                      { value: false, label: 'Inactive' },
                    ]}
                  />
                </Form.Item>

                <Form.Item
                  label="Ngày bắt đầu"
                  name="startsOn"
                  rules={[{ required: true, message: 'Chọn ngày bắt đầu' }]}
                >
                  <DatePicker
                    showTime
                    style={{ width: '100%' }}
                    format="DD/MM/YYYY HH:mm"
                    placeholder="Chọn ngày bắt đầu"
                  />
                </Form.Item>

                <Form.Item
                  label="Ngày kết thúc"
                  name="endsOn"
                  dependencies={['startsOn']}
                  rules={[
                    ({ getFieldValue }) => ({
                      validator(_, value) {
                        const startsOn = getFieldValue('startsOn')
                        if (!value || !startsOn) return Promise.resolve()
                        if (value.isBefore(startsOn)) {
                          return Promise.reject(
                            new Error('Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu'),
                          )
                        }
                        return Promise.resolve()
                      },
                    }),
                  ]}
                >
                  <DatePicker
                    showTime
                    allowClear
                    style={{ width: '100%' }}
                    format="DD/MM/YYYY HH:mm"
                    placeholder="Ngày kết thúc"
                  />
                </Form.Item>
              </div>
            </div>

            <div className="pm-modal-section-card">
              <div className="pm-modal-section-title">
                <CalendarOutlined />
                <div>
                  <h3>Lịch lặp</h3>
                  <p>Thiết lập chu kỳ sinh công việc bảo trì.</p>
                </div>
              </div>

              <div className="pm-form-grid">
                <Form.Item
                  label="Kiểu lặp"
                  name="type"
                  rules={[{ required: true, message: 'Chọn kiểu lặp' }]}
                >
                  <Select
                    options={[
                      { value: 'DAILY', label: 'Hằng ngày' },
                      { value: 'WEEKLY', label: 'Hằng tuần' },
                      { value: 'MONTHLY', label: 'Hằng tháng' },
                      { value: 'YEARLY', label: 'Hằng năm' },
                    ]}
                  />
                </Form.Item>

                <Form.Item
                  label="Tần suất"
                  name="frequency"
                  rules={[{ required: true, message: 'Nhập tần suất' }]}
                >
                  <InputNumber min={1} style={{ width: '100%' }} />
                </Form.Item>

                <Form.Item label="Due Date Delay" name="dueDateDelay">
                  <InputNumber min={0} addonAfter="ngày" style={{ width: '100%' }} />
                </Form.Item>

                <Form.Item label="Based On" name="basedOn">
                  <Select
                    options={[
                      { value: 'SCHEDULED_DATE', label: 'Scheduled Date' },
                      { value: 'COMPLETED_DATE', label: 'Completed Date' },
                    ]}
                  />
                </Form.Item>

                <Form.Item noStyle shouldUpdate={(prev, cur) => prev.type !== cur.type}>
                  {({ getFieldValue }) =>
                    getFieldValue('type') === 'WEEKLY' ? (
                      <Form.Item
                        label="Ngày trong tuần"
                        name="daysOfWeek"
                        rules={[{ required: true, message: 'Chọn ít nhất một ngày' }]}
                      >
                        <Select
                          mode="multiple"
                          placeholder="Chọn ngày"
                          options={[
                            { value: 1, label: 'Thứ 2' },
                            { value: 2, label: 'Thứ 3' },
                            { value: 3, label: 'Thứ 4' },
                            { value: 4, label: 'Thứ 5' },
                            { value: 5, label: 'Thứ 6' },
                            { value: 6, label: 'Thứ 7' },
                            { value: 7, label: 'Chủ nhật' },
                          ]}
                        />
                      </Form.Item>
                    ) : (
                      <div />
                    )
                  }
                </Form.Item>
              </div>
            </div>

            <div className="pm-modal-section-card">
              <div className="pm-modal-section-title">
                <FileTextOutlined />
                <div>
                  <h3>Mô tả</h3>
                  <p>Ghi chú thêm cho kế hoạch bảo trì nếu cần.</p>
                </div>
              </div>

              <Form.Item label="Mô tả" name="description">
                <TextArea rows={4} placeholder="Nhập mô tả" />
              </Form.Item>
            </div>
          </Form>
        </div>

        <div className="pm-modal-footer">
          <Button onClick={closeModal}>Hủy</Button>
          <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
            {editingItem ? 'Cập nhật' : 'Lưu kế hoạch'}
          </Button>
        </div>
      </Modal>
    </div>
  )
}