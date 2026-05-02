import { useEffect, useState } from 'react'
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
} from '@ant-design/icons'
import './PreventiveMaintenance.css'

const { TextArea } = Input

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
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
  LOW: 'green',
  MEDIUM: 'blue',
  HIGH: 'orange',
  URGENT: 'red',
}

const normalizeAssetRows = (raw) => {
  if (Array.isArray(raw)) return raw
  if (Array.isArray(raw?.content)) return raw.content
  if (Array.isArray(raw?.data)) return raw.data
  if (Array.isArray(raw?.items)) return raw.items
  return []
}

const isPmExpired = (record) => {
  const endsOn = record?.schedule?.endsOn || record?.endsOn || record?.scheduleEndDate

  if (!endsOn) return false

  return dayjs().isAfter(dayjs(endsOn), 'day')
}

const uniqueAssets = (rows) => {
  const map = new Map()

  rows.forEach((asset) => {
    const id = asset?.id || asset?.assetId
    if (id) map.set(String(id), asset)
  })

  return Array.from(map.values())
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

  const [openModal, setOpenModal] = useState(false)
  const [editingItem, setEditingItem] = useState(null)

  const [assetSearch, setAssetSearch] = useState('')
  const [userSearch, setUserSearch] = useState('')
  const [assetDropdownOpen, setAssetDropdownOpen] = useState(false)
  const [userDropdownOpen, setUserDropdownOpen] = useState(false)

  const getErrorMessage = (err, fallback) =>
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    (typeof err?.response?.data === 'string' ? err.response.data : '') ||
    err?.message ||
    fallback

  const loadData = async () => {
    try {
      setLoading(true)
      const res = await api.get('/preventive-maintenances')
      setItems(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      message.error(getErrorMessage(err, 'Không tải được danh sách PM'))
    } finally {
      setLoading(false)
    }
  }

  const loadAllAssets = async () => {
    const res = await api.get('/api/assets')
    return uniqueAssets(normalizeAssetRows(res.data))
  }

  const loadOptions = async () => {
    const [assetRes, userRes] = await Promise.allSettled([
      loadAllAssets(),
      api.get('/api/users/technicians'),
    ])

    if (assetRes.status === 'fulfilled') {
      setAssets(assetRes.value)
    } else {
      setAssets([])
      message.warning('Không tải được danh sách thiết bị')
    }

    if (userRes.status === 'fulfilled') {
      const rows = userRes.value.data
      setUsers(Array.isArray(rows) ? rows : [])
    } else {
      setUsers([])
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
      descriptionLong: '',
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

      form.setFieldsValue({
        title: pm.title || '',
        description: pm.description || '',
        descriptionLong: pm.description || '',
        assetId: pm.assetId || pm.asset?.id || pm.asset?.assetId,
        assignedToId: pm.assignedToId || pm.assignedTo?.userId || pm.assignedTo?.id,
        estimatedHours: pm.estimatedHours ?? 0,
        priority: pm.priority || 'MEDIUM',
        active: pm.active ?? true,
      })

      setOpenModal(true)
    } catch (err) {
      message.error(getErrorMessage(err, 'Không tải được dữ liệu PM để sửa'))
    }
  }

  const closeModal = () => {
    setOpenModal(false)
    setEditingItem(null)
    form.resetFields()

    if (window.location.hash.includes('/preventive-maintenance/new')) {
      navigate('/preventive-maintenance')
    }
  }

  const handleSave = async () => {
    try {
      const values = await form.validateFields()

      setSaving(true)

      if (editingItem) {
        const payload = {
          title: values.title?.trim(),
          description: values.descriptionLong?.trim() || values.description?.trim() || '',
          estimatedHours: Number(values.estimatedHours || 0),
          assetId: values.assetId || null,
          assignedToId: values.assignedToId || null,
          priority: values.priority || 'MEDIUM',
          active: values.active ?? true,
        }

        await api.put(`/preventive-maintenances/${editingItem.id}`, payload)
        message.success('Cập nhật kế hoạch bảo trì thành công')
      } else {
        const payload = {
          title: values.title?.trim(),
          description: values.descriptionLong?.trim() || values.description?.trim() || '',
          estimatedHours: Number(values.estimatedHours || 0),
          assetId: values.assetId || null,
          assignedToId: values.assignedToId || null,
          startsOn: values.startsOn.format('YYYY-MM-DDTHH:mm:ss'),
          endsOn: values.endsOn ? values.endsOn.format('YYYY-MM-DDTHH:mm:ss') : null,
          recurrenceRule: {
            type: values.type,
            frequency: Number(values.frequency || 1),
            priority: values.priority || 'MEDIUM',
            dueDateDelay: Number(values.dueDateDelay || 0),
            basedOn: values.basedOn || 'SCHEDULED_DATE',
            daysOfWeek: values.type === 'WEEKLY' ? values.daysOfWeek || [] : [],
          },
        }

        await api.post('/preventive-maintenances', payload)
        message.success('Tạo kế hoạch bảo trì thành công')
      }

      closeModal()
      await loadData()
    } catch (err) {
      if (err?.errorFields) return
      message.error(getErrorMessage(err, editingItem ? 'Cập nhật PM thất bại' : 'Tạo PM thất bại'))
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = (id) => {
    Modal.confirm({
      title: 'Xóa kế hoạch bảo trì?',
      content: 'Hành động này không thể hoàn tác.',
      okText: 'Xóa',
      cancelText: 'Hủy',
      okButtonProps: { danger: true },
      async onOk() {
        try {
          await api.delete(`/preventive-maintenances/${id}`)
          message.success('Đã xóa kế hoạch')
          await loadData()
        } catch (err) {
          message.error(getErrorMessage(err, 'Xóa thất bại'))
        }
      },
    })
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

  const assetOptions = assets
    .filter((asset) => asset?.id || asset?.assetId)
    .map((asset) => {
      const id = asset.id || asset.assetId
      const code = asset.code || asset.assetCode || asset.barcode || ''
      const name = asset.name || asset.assetName || asset.title || `Asset #${id}`

      return {
        value: id,
        label: code ? `${name} - ${code}` : `${name} #${id}`,
      }
    })

  const userOptions = users
    .filter((user) => user?.userId || user?.id)
    .map((user) => {
      const id = user.userId || user.id
      const name =
        user.fullName ||
        user.name ||
        user.username ||
        user.email ||
        `User #${id}`

      return {
        value: id,
        label: `${name} #${id}`,
      }
    })

  const filterOptions = (options, searchValue) => {
    const q = searchValue.trim().toLowerCase()
    if (!q) return options

    return options.filter((option) =>
      String(option?.label || '').toLowerCase().includes(q),
    )
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
    <div
      className="pm-select-dropdown-inner"
      onMouseDown={(e) => e.preventDefault()}
    >
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

  const columns = [
    {
      title: 'Mã',
      dataIndex: 'id',
      width: 110,
      render: (_, record) => <b>{record.code || `PM-${record.id}`}</b>,
    },
    {
      title: 'Kế hoạch bảo trì',
      dataIndex: 'title',
      render: (_, record) => (
        <div>
          <Link className="pm-table-title" to={`/preventive-maintenance/${record.id}`}>
            {record.title}
          </Link>
          <div className="pm-table-subtitle">
            {record.description || 'Không có mô tả'}
          </div>
        </div>
      ),
    },
    {
      title: 'Thiết bị',
      render: (_, record) =>
        record.asset?.name ||
        record.asset?.assetName ||
        record.assetName ||
        '-',
    },
    {
      title: 'Người phụ trách',
      render: (_, record) =>
        record.assignedTo?.fullName ||
        record.assignedTo?.username ||
        record.assignedTo?.email ||
        record.assignedToName ||
        '-',
    },
    {
      title: 'Ưu tiên',
      dataIndex: 'priority',
      width: 120,
      render: (priority = 'MEDIUM') => (
        <Tag color={priorityColor[priority] || 'default'}>{priority}</Tag>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'active',
      width: 120,
      render: (active) =>
        active ? <Tag color="green">Active</Tag> : <Tag>Inactive</Tag>,
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
      <div className="pm-card-pro">
        <div className="pm-list-header">
          <div>
            <h2>Kế hoạch bảo trì định kỳ</h2>
          </div>

          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadData} loading={loading}>
              Tải lại
            </Button>

            <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
              Thêm kế hoạch
            </Button>
          </Space>
        </div>

        <div className="pm-stats">
          <div className="pm-stat-box">
            <span>Tổng kế hoạch</span>
            <b>{items.length}</b>
          </div>

          <div className="pm-stat-box">
            <span>Đang active</span>
            <b>{items.filter((x) => x.active).length}</b>
          </div>

          <div className="pm-stat-box">
            <span>Inactive</span>
            <b>{items.filter((x) => !x.active).length}</b>
          </div>
        </div>

        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={items}
          pagination={{ pageSize: 8 }}
        />
      </div>

      <Modal
        open={openModal}
        onCancel={closeModal}
        footer={null}
        width={680}
        centered
        destroyOnClose
        maskClosable={false}
        className="pm-create-modal"
        rootClassName="pm-create-modal-root"
        closeIcon={<span className="pm-modal-close">×</span>}
      >
        <div className="pm-modal-head">
          <h2>{editingItem ? 'Chỉnh sửa kế hoạch bảo trì' : 'Thêm kế hoạch bảo trì'}</h2>
          <p>
            {editingItem
              ? 'Cập nhật thông tin preventive maintenance'
              : 'Tạo mới preventive maintenance trong hệ thống'}
          </p>
        </div>

        <div className="pm-modal-body">
          <Form form={form} layout="vertical">
            <h3>Thông tin chính</h3>

            <div className="pm-form-grid">
              <Form.Item
                label="Tên kế hoạch"
                name="title"
                rules={[{ required: true, message: 'Nhập tên kế hoạch' }]}
              >
                <Input placeholder="Nhập tên kế hoạch" />
              </Form.Item>

              <Form.Item label="Mô tả" name="description">
                <Input placeholder="Nhập mô tả ngắn" />
              </Form.Item>

              <Form.Item
                label="Thiết bị"
                name="assetId"
                rules={[{ required: true, message: 'Chọn thiết bị' }]}
              >
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

              <Form.Item
                label="Người phụ trách"
                name="assignedToId"
                rules={[{ required: true, message: 'Chọn người phụ trách' }]}
              >
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

              {editingItem && (
                <Form.Item label="Trạng thái" name="active">
                  <Select
                    options={[
                      { value: true, label: 'Active' },
                      { value: false, label: 'Inactive' },
                    ]}
                  />
                </Form.Item>
              )}

              {!editingItem && (
                <>
                  <Form.Item
                    label="Ngày bắt đầu"
                    name="startsOn"
                    rules={[{ required: true, message: 'Chọn ngày bắt đầu' }]}
                  >
                    <DatePicker
                      showTime
                      style={{ width: '100%' }}
                      format="DD/MM/YYYY HH:mm"
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
                      placeholder="Không giới hạn nếu bỏ trống"
                    />
                  </Form.Item>
                </>
              )}
            </div>

            {!editingItem && (
              <>
                <h3>Lịch lặp</h3>

                <div className="pm-form-grid">
                  <Form.Item label="Kiểu lặp" name="type">
                    <Select
                      options={[
                        { value: 'DAILY', label: 'Hằng ngày' },
                        { value: 'WEEKLY', label: 'Hằng tuần' },
                        { value: 'MONTHLY', label: 'Hằng tháng' },
                        { value: 'YEARLY', label: 'Hằng năm' },
                      ]}
                    />
                  </Form.Item>

                  <Form.Item label="Tần suất" name="frequency">
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
              </>
            )}

            <Form.Item label="Ghi chú chi tiết" name="descriptionLong">
              <TextArea rows={4} placeholder="Nhập ghi chú nếu cần" />
            </Form.Item>
          </Form>
        </div>

        <div className="pm-modal-footer">
          <Button onClick={closeModal}>Hủy</Button>

          <Button
            type="primary"
            icon={<SaveOutlined />}
            loading={saving}
            onClick={handleSave}
          >
            {editingItem ? 'Cập nhật' : 'Lưu kế hoạch'}
          </Button>
        </div>
      </Modal>
    </div>
  )
}