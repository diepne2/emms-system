import './PreventiveMaintenanceDetail.css'
import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import axios from 'axios'
import dayjs from 'dayjs'
import {
  Badge,
  Button,
  Calendar,
  Card,
  DatePicker,
  Descriptions,
  Empty,
  Spin,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd'
import {
  ArrowLeftOutlined,
  CalendarOutlined,
  EditOutlined,
  LaptopOutlined,
  ReloadOutlined,
  ThunderboltOutlined,
  ToolOutlined,
} from '@ant-design/icons'

const { Title, Text } = Typography

const getToken = () =>
  localStorage.getItem('accessToken') ||
  localStorage.getItem('token') ||
  localStorage.getItem('access_token') ||
  localStorage.getItem('jwt')

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
    const t = value.trim()
    return t ? [t] : []
  }
  return []
}

const normalizeGrant = (value) => {
  if (!value) return ''
  let raw = String(value).trim().toUpperCase()
  if (raw.startsWith('ROLE_')) raw = raw.slice(5)
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
  const authRaw = localStorage.getItem('authorities') || sessionStorage.getItem('authorities')
  const permRaw = localStorage.getItem('permissions') || sessionStorage.getItem('permissions')
  const roleRaw = localStorage.getItem('role') || sessionStorage.getItem('role') || ''

  const user = safeJsonParse(userRaw, {})
  const roles = normalizeToArray(safeJsonParse(rolesRaw, rolesRaw || user?.roles || []))
  const authorities = normalizeToArray(safeJsonParse(authRaw, authRaw || user?.authorities || []))
  const permissions = normalizeToArray(safeJsonParse(permRaw, permRaw || user?.permissions || []))

  const merged = [
    ...roles,
    ...authorities,
    ...permissions,
    ...normalizeToArray(roleRaw),
    user?.role,
    user?.roleCode,
    ...(Array.isArray(user?.roles) ? user.roles : []),
  ]
    .map(extractGrantValue)
    .filter(Boolean)
    .map(normalizeGrant)
    .filter(Boolean)

  return { user, grants: Array.from(new Set(merged)) }
}

const hasAnyGrant = (grants, expected = []) =>
  expected.map(normalizeGrant).some((g) => grants.map(normalizeGrant).includes(g))

const priorityColor = {
  LOW: 'green',
  MEDIUM: 'blue',
  HIGH: 'orange',
  URGENT: 'red',
}

const priorityLabel = {
  LOW: 'Thấp',
  MEDIUM: 'Trung bình',
  HIGH: 'Cao',
  URGENT: 'Khẩn cấp',
}

const dayMap = {
  1: 'Thứ 2',
  2: 'Thứ 3',
  3: 'Thứ 4',
  4: 'Thứ 5',
  5: 'Thứ 6',
  6: 'Thứ 7',
  7: 'Chủ nhật',
}

const formatDate = (value) => {
  if (!value) return '-'
  const d = dayjs(value)
  return d.isValid() ? d.format('DD/MM/YYYY') : value
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://emms-system-production-4239.up.railway.app',
  withCredentials: true,
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default function PreventiveMaintenanceDetail() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [pm, setPm] = useState(null)
  const [schedule, setSchedule] = useState(null)
  const [workOrders, setWorkOrders] = useState([])
  const [activeDates, setActiveDates] = useState({})
  const [loading, setLoading] = useState(false)
  const [calendarLoading, setCalendarLoading] = useState(false)
  const [generatingWO, setGeneratingWO] = useState(false)
  const [currentMonth, setCurrentMonth] = useState(dayjs())

  const { grants } = useMemo(() => getUserContext(), [])
  const canGenerateWO = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])

  const errMsg = (err, fallback) =>
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    (typeof err?.response?.data === 'string' ? err.response.data : '') ||
    err?.message ||
    fallback

  const loadWorkOrders = async () => {
    try {
      const res = await api.get(`/api/preventive-maintenances/${id}/work-orders`)
      setWorkOrders(Array.isArray(res.data) ? res.data : [])
    } catch {
      setWorkOrders([])
    }
  }

  const loadSchedule = async () => {
    try {
      const res = await api.get(`/api/schedules/by-preventive-maintenance/${id}`)
      setSchedule(res.data)
      return res.data
    } catch {
      setSchedule(null)
      return null
    }
  }

  const checkMonth = async (monthValue = currentMonth, scheduleId = schedule?.id) => {
    if (!scheduleId) return

    try {
      setCalendarLoading(true)

      const start = monthValue.startOf('month')
      const daysInMonth = monthValue.daysInMonth()

      const requests = Array.from({ length: daysInMonth }, (_, i) => {
        const date = start.add(i, 'day').format('YYYY-MM-DD')

        return api
          .get(`/api/schedules/${scheduleId}/active-on`, { params: { date } })
          .then((res) => [date, Boolean(res.data)])
          .catch(() => [date, false])
      })

      const results = await Promise.all(requests)
      setActiveDates(Object.fromEntries(results))
    } finally {
      setCalendarLoading(false)
    }
  }

  const load = async () => {
    try {
      setLoading(true)

      try {
        const pmRes = await api.get(`/preventive-maintenances/${id}`)
        setPm(pmRes.data)
      } catch (err) {
        message.error(errMsg(err, 'Không tải được thông tin kế hoạch'))
      }

      const scheduleData = await loadSchedule()
      await loadWorkOrders()

      if (scheduleData?.id) {
        await checkMonth(currentMonth, scheduleData.id)
      }
    } finally {
      setLoading(false)
    }
  }

  const isScheduleDisabled = Boolean(schedule?.disabled)
  const isPmInactive = pm && pm.active === false
  const isFutureSchedule = schedule?.startsOn && dayjs(schedule.startsOn).isAfter(dayjs(), 'day')

  const generateDisabled =
    !pm ||
    !schedule ||
    isPmInactive ||
    isScheduleDisabled ||
    isFutureSchedule ||
    generatingWO

  const generateTooltip = (() => {
    if (!pm) return 'Chưa tải được thông tin PM'
    if (!schedule) return 'PM này chưa có schedule'
    if (isPmInactive) return 'PM đang bị vô hiệu hoá'
    if (isScheduleDisabled) return 'Schedule đang bị tắt. Hãy bật lại trước khi tạo WO.'
    if (isFutureSchedule) return `Schedule chưa bắt đầu (${formatDate(schedule.startsOn)})`
    return 'Tạo Work Order từ kế hoạch bảo trì'
  })()

  const generateWO = async () => {
    if (!pm?.id) {
      message.warning('Chưa có kế hoạch bảo trì')
      return
    }

    if (generateDisabled) {
      message.warning(generateTooltip)
      return
    }

    try {
      setGeneratingWO(true)

      await api.post(`/api/preventive-maintenances/${pm.id}/generate-work-order`)

      message.success('Đã tạo Work Order và gửi thông báo')
      await loadWorkOrders()
    } catch (err) {
      message.error(errMsg(err, 'Tạo Work Order thất bại'))
    } finally {
      setGeneratingWO(false)
    }
  }

  const toggleSchedule = async () => {
    if (!schedule?.id) return

    try {
      if (isScheduleDisabled) {
        await api.put(`/api/schedules/${schedule.id}/enable`)
        message.success('Đã bật lịch')
      } else {
        await api.put(`/api/schedules/${schedule.id}/disable`)
        message.success('Đã tắt lịch bảo trì. Hệ thống sẽ không tự động sinh Work Order mới từ kế hoạch này.')
      }

      await load()
    } catch (err) {
      message.error(errMsg(err, 'Cập nhật lịch thất bại'))
    }
  }

  const dateCellRender = (value) => {
    const key = value.format('YYYY-MM-DD')
    if (!activeDates[key]) return null
    return <div className="pm-calendar-dot" />
  }

  const scheduleDays = schedule?.daysOfWeek?.length
    ? schedule.daysOfWeek.map((d) => dayMap[d] || d).join(', ')
    : '-'

  useEffect(() => {
    load()
  }, [id])

  return (
    <div className="pm-detail-page">
      <div className="pm-detail-toolbar">
        <Link to="/preventive-maintenance">
          <Button icon={<ArrowLeftOutlined />}>Quay lại danh sách</Button>
        </Link>

        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          {canGenerateWO && (
            <Tooltip title={generateTooltip}>
              <Button
                type="primary"
                className="pm-btn-generate"
                icon={<ThunderboltOutlined />}
                loading={generatingWO}
                disabled={generateDisabled}
                onClick={generateWO}
              >
                Generate WO
              </Button>
            </Tooltip>
          )}

          {pm && (
            <Button
              type="primary"
              className="pm-btn-edit"
              icon={<EditOutlined />}
              onClick={() => navigate('/preventive-maintenance', { state: { editId: pm.id } })}
            >
              Chỉnh sửa
            </Button>
          )}

          <Button icon={<ReloadOutlined />} onClick={load} loading={loading}>
            Tải lại
          </Button>
        </div>
      </div>

      <Spin spinning={loading}>
        <section className="pm-detail-hero">
          <div className="pm-detail-hero-left">
            <div className="pm-detail-code">{pm?.code || `PM-${id}`}</div>

            <Title level={2} className="pm-detail-main-title">
              {pm?.title || 'Chi tiết kế hoạch bảo trì'}
            </Title>

            <Text className="pm-detail-description">
              {pm?.description || 'Không có mô tả cho kế hoạch bảo trì này.'}
            </Text>
          </div>

          <div className="pm-detail-hero-tags">
            <Tag color={priorityColor[pm?.priority] || 'blue'} className="pm-big-tag">
              {priorityLabel[pm?.priority] || pm?.priority || 'MEDIUM'}
            </Tag>

            {pm?.active ? (
              <Tag color="green" className="pm-big-tag">
                Đang hoạt động
              </Tag>
            ) : (
              <Tag color="default" className="pm-big-tag">
                Không hoạt động
              </Tag>
            )}

            <Tag color={workOrders.length > 0 ? 'blue' : 'default'} className="pm-big-tag">
              {workOrders.length > 0 ? `${workOrders.length} Work Orders` : 'Chưa có WO'}
            </Tag>
          </div>
        </section>

        <div className="pm-detail-grid">
          <Card className="pm-detail-card" bordered={false}>
            <div className="pm-section-title">
              <ToolOutlined />
              <span>Thông tin kế hoạch</span>
            </div>

            {pm ? (
              <Descriptions column={1} className="pm-clean-desc" size="small">
                <Descriptions.Item label="ID">{pm.id}</Descriptions.Item>
                <Descriptions.Item label="Mã">{pm.code || '-'}</Descriptions.Item>
                <Descriptions.Item label="Tên kế hoạch">{pm.title || '-'}</Descriptions.Item>
                <Descriptions.Item label="Thời lượng dự kiến">
                  {pm.estimatedHours ?? '-'} giờ
                </Descriptions.Item>
                <Descriptions.Item label="Ưu tiên">
                  <Tag color={priorityColor[pm.priority] || 'blue'}>
                    {priorityLabel[pm.priority] || pm.priority || '-'}
                  </Tag>
                </Descriptions.Item>
              </Descriptions>
            ) : (
              <Empty description="Không có dữ liệu kế hoạch" />
            )}
          </Card>

          <Card className="pm-detail-card" bordered={false}>
            <div className="pm-section-title">
              <LaptopOutlined />
              <span>Thiết bị & người phụ trách</span>
            </div>

            <Descriptions column={1} className="pm-clean-desc" size="small">
              <Descriptions.Item label="Thiết bị">
                {pm?.assetName || pm?.asset?.name || pm?.asset?.assetName || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Người phụ trách">
                {pm?.assignedToName || pm?.assignedTo?.fullName || pm?.assignedTo?.username || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Asset ID">
                {pm?.assetId || pm?.asset?.id || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="User ID">
                {pm?.assignedToId || pm?.assignedTo?.id || '-'}
              </Descriptions.Item>
            </Descriptions>
          </Card>
        </div>

        <div className="pm-detail-grid pm-detail-grid-wide">
          <Card
            className="pm-detail-card"
            bordered={false}
            title={
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <CalendarOutlined />
                <span>Cấu hình lịch lặp</span>
              </div>
            }
            extra={
              schedule && (
                <Button
                  className={isScheduleDisabled ? 'pm-btn-toggle-on' : 'pm-btn-toggle-off'}
                  type={isScheduleDisabled ? 'primary' : 'default'}
                  onClick={toggleSchedule}
                  size="small"
                >
                  {isScheduleDisabled ? 'Bật lịch' : 'Tắt lịch'}
                </Button>
              )
            }
          >
            {!schedule ? (
              <Empty description="PM này chưa có cấu hình lịch" />
            ) : (
              <Descriptions column={2} className="pm-clean-desc" size="small">
                <Descriptions.Item label="Trạng thái">
                  {schedule.disabled ? <Tag color="red">Đang tắt</Tag> : <Tag color="green">Đang bật</Tag>}
                </Descriptions.Item>

                <Descriptions.Item label="Kiểu lặp">
                  <Tag color="blue">{schedule.recurrenceType || '-'}</Tag>
                </Descriptions.Item>

                <Descriptions.Item label="Tần suất">
                  Mỗi {schedule.frequency || 1} lần
                </Descriptions.Item>

                <Descriptions.Item label="Dựa trên">
                  {schedule.recurrenceBasedOn || '-'}
                </Descriptions.Item>

                <Descriptions.Item label="Ngày bắt đầu">
                  {formatDate(schedule.startsOn)}
                </Descriptions.Item>

                <Descriptions.Item label="Ngày kết thúc">
                  {formatDate(schedule.endsOn)}
                </Descriptions.Item>

                <Descriptions.Item label="Due Delay">
                  {schedule.dueDateDelay ?? 0} ngày
                </Descriptions.Item>

                <Descriptions.Item label="Ngày trong tuần">
                  {scheduleDays}
                </Descriptions.Item>
              </Descriptions>
            )}
          </Card>

          {schedule && (
            <Card
              className="pm-detail-card pm-calendar-card"
              bordered={false}
              title="Lịch chạy trong tháng"
              extra={
                <DatePicker
                  picker="month"
                  value={currentMonth}
                  size="small"
                  onChange={(value) => {
                    if (!value) return
                    setCurrentMonth(value)
                    checkMonth(value)
                  }}
                />
              }
            >
              <Spin spinning={calendarLoading}>
                <Calendar
                  fullscreen={false}
                  value={currentMonth}
                  cellRender={dateCellRender}
                  onPanelChange={(value) => {
                    setCurrentMonth(value)
                    checkMonth(value)
                  }}
                />
              </Spin>

              <div className="pm-calendar-note">
                <Badge color="#1B4FD8" text="Ngày có kế hoạch chạy" />
              </div>
            </Card>
          )}
        </div>
      </Spin>
    </div>
  )
}