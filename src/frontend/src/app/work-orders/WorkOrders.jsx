import React, { useCallback, useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import './WorkOrders.css'
import WorkOrderPartSection from './WorkOrderPartSection'
import {
  FiSearch,
  FiChevronLeft,
  FiChevronRight,
  FiEye,
  FiX,
  FiClipboard,
  FiMapPin,
  FiUser,
  FiTag,
  FiInfo,
  FiCalendar,
  FiEdit2,
  FiPlus,
  FiTrash2,
  FiAlertTriangle,
  FiFilter,
  FiRotateCcw,
  FiSave,
  FiCheckCircle,
  FiArchive,
} from 'react-icons/fi'

const API_BASE_URL = 'https://emms-system-production-4239.up.railway.app/api/work-orders'
const ASSET_API_BASE_URL = 'https://emms-system-production-4239.up.railway.app/api/assets'
const USER_API_BASE_URL = 'https://emms-system-production-4239.up.railway.app/api/users'
const LOCATION_API_BASE_URL = 'https://emms-system-production-4239.up.railway.app/api/locations'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

const getToken = () =>
  localStorage.getItem('accessToken') ||
  localStorage.getItem('token') ||
  localStorage.getItem('access_token') ||
  sessionStorage.getItem('accessToken') ||
  sessionStorage.getItem('token') ||
  sessionStorage.getItem('access_token') ||
  ''

const getAuthConfig = () => {
  const token = getToken()
  return {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  }
}

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
    const trimmed = value.trim()
    if (!trimmed) return []
    return [trimmed]
  }
  return []
}

const normalizeGrant = (value) => {
  if (!value) return ''
  let raw = String(value).trim().toUpperCase()
  if (!raw) return ''
  if (raw.startsWith('ROLE_')) raw = raw.substring(5)
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
  const authoritiesRaw = localStorage.getItem('authorities') || sessionStorage.getItem('authorities')
  const permissionsRaw = localStorage.getItem('permissions') || sessionStorage.getItem('permissions')
  const roleRaw = localStorage.getItem('role') || sessionStorage.getItem('role') || ''

  const user = safeJsonParse(userRaw, {})
  const roles = normalizeToArray(safeJsonParse(rolesRaw, rolesRaw || user?.roles || []))
  const authorities = normalizeToArray(
    safeJsonParse(authoritiesRaw, authoritiesRaw || user?.authorities || []),
  )
  const permissions = normalizeToArray(
    safeJsonParse(permissionsRaw, permissionsRaw || user?.permissions || []),
  )
  const singleRole = normalizeToArray(roleRaw)

  const merged = [
    ...roles,
    ...authorities,
    ...permissions,
    ...singleRole,
    ...(Array.isArray(user?.roles) ? user.roles : []),
    ...(Array.isArray(user?.authorities) ? user.authorities : []),
    ...(Array.isArray(user?.permissions) ? user.permissions : []),
  ]
    .map(extractGrantValue)
    .filter(Boolean)
    .map(normalizeGrant)
    .filter(Boolean)

  return { user, grants: Array.from(new Set(merged)) }
}

const hasAnyGrant = (grants, expected = []) => {
  if (!Array.isArray(grants) || !expected.length) return false
  const normalizedUserGrants = grants.map(normalizeGrant)
  const normalizedExpected = expected.map(normalizeGrant)
  return normalizedExpected.some((item) => normalizedUserGrants.includes(item))
}

const extractErrorMessage = (err, fallback) => {
  if (!err) return fallback

  if (err.response) {
    const data = err.response.data
    if (typeof data === 'string' && data.trim()) return `HTTP ${err.response.status}: ${data}`
    if (data?.message) return `HTTP ${err.response.status}: ${data.message}`
    if (data?.error) return `HTTP ${err.response.status}: ${data.error}`
    return `HTTP ${err.response.status}: ${fallback}`
  }

  if (err.request) {
    return 'Không nhận được phản hồi từ backend.'
  }

  return err.message || fallback
}

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'OPEN', label: 'Mở' },
  { value: 'ON_HOLD', label: 'Tạm dừng' },
  { value: 'IN_PROGRESS', label: 'Đang thực hiện' },
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'DONE', label: 'Hoàn thành' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]

const PRIORITY_OPTIONS = [
  { value: '', label: 'Không chọn' },
  { value: 'LOW', label: 'Thấp' },
  { value: 'MEDIUM', label: 'Trung bình' },
  { value: 'HIGH', label: 'Cao' },
  { value: 'URGENT', label: 'Khẩn cấp' },
  { value: 'NONE', label: 'None' },
]

const FORM_STATUS_OPTIONS = STATUS_OPTIONS.filter((item) => item.value)
const FORM_PRIORITY_OPTIONS = PRIORITY_OPTIONS.filter((item) => item.value)

const TECHNICIAN_ALLOWED_STATUS_BY_CURRENT = {
  OPEN: ['IN_PROGRESS', 'ON_HOLD'],
  IN_PROGRESS: ['ON_HOLD'],
  ON_HOLD: ['IN_PROGRESS', 'OPEN'],
  DONE: [],
}

const MANAGER_ALLOWED_STATUS_BY_CURRENT = {
  OPEN: ['IN_PROGRESS', 'ON_HOLD', 'CANCELLED'],
  IN_PROGRESS: ['ON_HOLD', 'PENDING', 'CANCELLED'],
  ON_HOLD: ['IN_PROGRESS', 'OPEN', 'CANCELLED'],
  PENDING: ['DONE', 'IN_PROGRESS', 'ON_HOLD', 'CANCELLED'],
  DONE: [],
  CANCELLED: [],
}

const getStatusLabel = (status) => {
  const found = STATUS_OPTIONS.find((item) => item.value === status)
  return found?.label || status || '-'
}

const getPriorityLabel = (priority) => {
  const found = PRIORITY_OPTIONS.find((item) => item.value === priority)
  return found?.label || priority || '-'
}

const getStatusBadgeClass = (status) => {
  switch ((status || '').toUpperCase()) {
    case 'OPEN':
      return 'badge badge--default'
    case 'ON_HOLD':
      return 'badge badge--warning'
    case 'IN_PROGRESS':
      return 'badge badge--info'
    case 'PENDING':
      return 'badge badge--warning'
    case 'DONE':
      return 'badge badge--success'
    case 'CANCELLED':
      return 'badge badge--danger'
    default:
      return 'badge badge--default'
  }
}

const getVisiblePages = (currentPage, totalPages) => {
  const pages = []

  if (totalPages <= 7) {
    for (let i = 0; i < totalPages; i += 1) pages.push(i)
    return pages
  }

  const start = Math.max(0, currentPage - 2)
  const end = Math.min(totalPages - 1, currentPage + 2)

  if (start > 0) pages.push(0)
  if (start > 1) pages.push('left-ellipsis')

  for (let i = start; i <= end; i += 1) pages.push(i)

  if (end < totalPages - 2) pages.push('right-ellipsis')
  if (end < totalPages - 1) pages.push(totalPages - 1)

  return pages
}

const getUserId = (user) => user?.userId ?? user?.id ?? null

const getUserDisplayName = (user) =>
  user?.fullName ||
  [user?.firstName, user?.lastName].filter(Boolean).join(' ') ||
  user?.username ||
  user?.email ||
  (getUserId(user) ? `User #${getUserId(user)}` : 'Unknown')

const getWorkOrderAsset = (item) => item?.asset || null
const getWorkOrderLocation = (item) => item?.location || null
const getWorkOrderCategory = (item) => item?.category || null
const getWorkOrderPrimaryUser = (item) => item?.primaryUser || null

const getWorkOrderAssignedUser = (item) => {
  if (!item?.assignedTo || Array.isArray(item.assignedTo)) return null
  return item.assignedTo
}

const getWorkOrderAssignedUsers = (item) => {
  const user = getWorkOrderAssignedUser(item)
  if (!user) return '-'
  return getUserDisplayName(user)
}

const getWorkOrderAssignedFirstId = (item) => {
  const user = getWorkOrderAssignedUser(item)
  return getUserId(user) ?? item?.assignedToId ?? ''
}

const getWorkOrderAssetId = (item) => getWorkOrderAsset(item)?.id ?? item?.assetId ?? ''

const getWorkOrderAssetName = (item) =>
  getWorkOrderAsset(item)?.name ||
  getWorkOrderAsset(item)?.title ||
  getWorkOrderAsset(item)?.assetName ||
  item?.assetName ||
  '-'

const getWorkOrderLocationName = (item) =>
  getWorkOrderLocation(item)?.name || getWorkOrderLocation(item)?.title || item?.locationName || ''

const getWorkOrderCategoryName = (item) =>
  getWorkOrderCategory(item)?.name || getWorkOrderCategory(item)?.title || item?.categoryName || ''

const getWorkOrderPrimaryUserName = (item) => {
  const user = getWorkOrderPrimaryUser(item)
  if (!user) return item?.primaryUserName || ''
  return getUserDisplayName(user)
}

const formatDateTimeValue = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

const toDateInputValue = (value) => {
  if (!value) return ''
  const str = String(value)
  return str.includes('T') ? str.split('T')[0] : str.slice(0, 10)
}

const emptyWorkOrderForm = {
  title: '',
  description: '',
  priority: 'MEDIUM',
  dueDate: '',
  estimatedDuration: '',
  requiresSignature: false,
  category: '',
  locationName: '',
  teamName: '',
  primaryUser: '',
  assignedToId: '',
  assetId: '',
  contractors: '',
}

function DetailItem({ icon, label, value, full = false, compact = false }) {
  return (
    <div
      className={`detail-item ${full ? 'detail-item--full' : ''} ${
        compact ? 'detail-item--compact' : ''
      }`}
    >
      <div className="detail-item__label">
        <span className="detail-item__icon">{icon}</span>
        <span>{label}</span>
      </div>
      <div className="detail-item__value">{value || '-'}</div>
    </div>
  )
}

function FormField({ label, children, full = false, required = false }) {
  return (
    <div className={`form-field ${full ? 'form-field--full' : ''}`}>
      <label className="form-label">
        {label} {required ? <span style={{ color: '#dc2626' }}>*</span> : null}
      </label>
      {children}
    </div>
  )
}

export default function WorkOrders() {
  const { user, grants } = useMemo(() => getUserContext(), [])
  const isAuthenticated = Boolean(getToken())

  const currentUserId = getUserId(user)
  const isAdmin = hasAnyGrant(grants, ['ADMIN'])
  const isTechnicalManager = hasAnyGrant(grants, ['TECHNICAL_MANAGER'])
  const isManager = isAdmin || isTechnicalManager
  const isTechnician = hasAnyGrant(grants, ['TECHNICIAN'])

  const canViewDetail = isAuthenticated
  const canCreate = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER', 'TECHNICIAN', 'OPERATOR'])
  const canEdit = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canDelete = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canArchive = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])

  const [filterForm, setFilterForm] = useState({
    keyword: '',
    status: '',
    priority: '',
    archived: '',
  })
  const [appliedFilters, setAppliedFilters] = useState({
    keyword: '',
    status: '',
    priority: '',
    archived: '',
  })

  const [workOrders, setWorkOrders] = useState([])
  const [assetOptions, setAssetOptions] = useState([])
  const [assetOptionsLoading, setAssetOptionsLoading] = useState(false)
  const [createAssetKeyword, setCreateAssetKeyword] = useState('')
  const [editAssetKeyword, setEditAssetKeyword] = useState('')
  const [createAssetOpen, setCreateAssetOpen] = useState(false)
  const [editAssetOpen, setEditAssetOpen] = useState(false)
  const [locationOptions, setLocationOptions] = useState([])
  const [locationsLoading, setLocationsLoading] = useState(false)
  const [technicians, setTechnicians] = useState([])
  const [techniciansLoading, setTechniciansLoading] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [pageNum, setPageNum] = useState(0)
  const [pageSize, setPageSize] = useState(5)

  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [selectedWorkOrder, setSelectedWorkOrder] = useState(null)

  const [createOpen, setCreateOpen] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [createError, setCreateError] = useState('')
  const [createForm, setCreateForm] = useState(emptyWorkOrderForm)

  const [editOpen, setEditOpen] = useState(false)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const [editForm, setEditForm] = useState(emptyWorkOrderForm)
  const [editingWorkOrderId, setEditingWorkOrderId] = useState(null)

  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteError, setDeleteError] = useState('')
  const [deleteLoadingId, setDeleteLoadingId] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)

  const [statusOpen, setStatusOpen] = useState(false)
  const [statusError, setStatusError] = useState('')
  const [statusLoading, setStatusLoading] = useState(false)
  const [statusTarget, setStatusTarget] = useState(null)
  const [statusForm, setStatusForm] = useState({
    status: 'OPEN',
    feedback: '',
  })
  const [statusAllowedOptions, setStatusAllowedOptions] = useState(FORM_STATUS_OPTIONS)

  const [completeOpen, setCompleteOpen] = useState(false)
  const [completeError, setCompleteError] = useState('')
  const [completeLoading, setCompleteLoading] = useState(false)
  const [completeTarget, setCompleteTarget] = useState(null)
  const [completeForm, setCompleteForm] = useState({
    completedByUserId: '',
    feedback: '',
  })

  const hasActiveFilters = useMemo(
    () =>
      Boolean(
        appliedFilters.keyword?.trim() ||
          appliedFilters.status?.trim() ||
          appliedFilters.priority?.trim() ||
          appliedFilters.archived?.trim(),
      ),
    [appliedFilters],
  )

  const activeFilterCount = useMemo(() => {
    let count = 0
    if (appliedFilters.keyword?.trim()) count += 1
    if (appliedFilters.status?.trim()) count += 1
    if (appliedFilters.priority?.trim()) count += 1
    if (appliedFilters.archived?.trim()) count += 1
    return count
  }, [appliedFilters])

  const getAssetOptionLabel = useCallback((asset) => {
    if (!asset) return '-'
    const name = asset.name || `Asset #${asset.id}`
    const barcode = asset.barcode ? ` | Code: ${asset.barcode}` : ''
    const serial = asset.serialNumber ? ` | SN: ${asset.serialNumber}` : ''
    return `${name}${barcode}${serial}`
  }, [])

  const loadAssetOptions = useCallback(async (keyword = '') => {
    try {
      setAssetOptionsLoading(true)

      const normalizedKeyword = String(keyword || '').trim()

      const response = await axios.post(
        `${ASSET_API_BASE_URL}/search`,
        {
          filterFields: normalizedKeyword
            ? [
                {
                  field: 'name',
                  operation: 'cn',
                  value: normalizedKeyword,
                },
              ]
            : [],
          direction: 'ASC',
          pageNum: 0,
          pageSize: 100,
          sortField: 'name',
        },
        getAuthConfig(),
      )

      const content = Array.isArray(response?.data?.content) ? response.data.content : []
      setAssetOptions(content)
    } catch (err) {
      console.error('Load asset options failed:', err)
      setAssetOptions([])
    } finally {
      setAssetOptionsLoading(false)
    }
  }, [])

  const loadLocations = useCallback(async () => {
    try {
      setLocationsLoading(true)
      const response = await axios.get(`${LOCATION_API_BASE_URL}/summary`, getAuthConfig())
      const data = Array.isArray(response?.data) ? response.data : []
      setLocationOptions(data)
    } catch (err) {
      console.error('Load locations failed:', err)
      setLocationOptions([])
    } finally {
      setLocationsLoading(false)
    }
  }, [])

  const loadTechnicians = useCallback(async () => {
    try {
      setTechniciansLoading(true)

      const response = await axios.get(USER_API_BASE_URL, {
        ...getAuthConfig(),
        params: {
          roleCode: 'TECHNICIAN',
          enabled: true,
          status: 'ACTIVE',
          page: 0,
          size: 1000,
          sortBy: 'userId',
          sortDir: 'asc',
        },
      })

      const pageData = response?.data
      const users = Array.isArray(pageData?.content) ? pageData.content : []
      setTechnicians(users)
    } catch (err) {
      console.error('Load technicians failed:', err)
      setTechnicians([])
    } finally {
      setTechniciansLoading(false)
    }
  }, [])

  const loadData = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const response = await api.get('', getAuthConfig())
      setWorkOrders(Array.isArray(response?.data) ? response.data : [])
    } catch (err) {
      setError(extractErrorMessage(err, 'Không thể tải danh sách work order.'))
    } finally {
      setLoading(false)
    }
  }, [])

  const loadDetail = useCallback(async (id) => {
    try {
      setDetailLoading(true)
      setDetailError('')
      setDetailOpen(true)
      const response = await api.get(`/${id}`, getAuthConfig())
      setSelectedWorkOrder(response?.data || null)
    } catch (err) {
      setSelectedWorkOrder(null)
      setDetailError(extractErrorMessage(err, 'Không thể tải chi tiết work order.'))
    } finally {
      setDetailLoading(false)
    }
  }, [])

  useEffect(() => {
    loadAssetOptions()
    loadLocations()
    loadTechnicians()
    loadData()
  }, [loadAssetOptions, loadLocations, loadTechnicians, loadData])

  useEffect(() => {
    const timer = setTimeout(() => {
      if (createOpen && createAssetOpen) {
        loadAssetOptions(createAssetKeyword)
      }
    }, 300)

    return () => clearTimeout(timer)
  }, [createOpen, createAssetOpen, createAssetKeyword, loadAssetOptions])

  useEffect(() => {
    const timer = setTimeout(() => {
      if (editOpen && editAssetOpen) {
        loadAssetOptions(editAssetKeyword)
      }
    }, 300)

    return () => clearTimeout(timer)
  }, [editOpen, editAssetOpen, editAssetKeyword, loadAssetOptions])

  const filteredWorkOrders = useMemo(() => {
    let result = [...workOrders]
    const keyword = appliedFilters.keyword?.trim().toLowerCase()
    const status = appliedFilters.status?.trim()
    const priority = appliedFilters.priority?.trim()
    const archived = appliedFilters.archived?.trim()

    if (keyword) {
      result = result.filter((item) =>
        [
          item.title,
          item.description,
          getWorkOrderAssetName(item),
          getWorkOrderCategoryName(item),
          getWorkOrderLocationName(item),
          item.teamName,
          getWorkOrderPrimaryUserName(item),
          getWorkOrderAssignedUsers(item),
          item.completedBy,
          item.feedback,
          item.contractors,
          item.id ? String(item.id) : '',
          formatDateTimeValue(item.dueDate),
        ]
          .filter(Boolean)
          .some((value) => String(value).toLowerCase().includes(keyword)),
      )
    }

    if (status) {
      result = result.filter(
        (item) => String(item.status || '').toUpperCase() === status.toUpperCase(),
      )
    }

    if (priority) {
      result = result.filter(
        (item) => String(item.priority || '').toUpperCase() === priority.toUpperCase(),
      )
    }

    if (archived === 'ARCHIVED') {
      result = result.filter((item) => Boolean(item.archived))
    } else if (archived === 'ACTIVE') {
      result = result.filter((item) => !item.archived)
    }

    result.sort((a, b) => {
      const t1 = a?.dateCreated ? new Date(a.dateCreated).getTime() : 0
      const t2 = b?.dateCreated ? new Date(b.dateCreated).getTime() : 0
      return t2 - t1
    })

    return result
  }, [workOrders, appliedFilters])

  const totalElements = filteredWorkOrders.length
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / pageSize)
  const lastPage = totalPages === 0 ? true : pageNum >= totalPages - 1

  const pagedWorkOrders = useMemo(() => {
    const start = pageNum * pageSize
    return filteredWorkOrders.slice(start, start + pageSize)
  }, [filteredWorkOrders, pageNum, pageSize])

  useEffect(() => {
    if (pageNum > 0 && pageNum >= totalPages && totalPages > 0) {
      setPageNum(totalPages - 1)
    }
  }, [pageNum, totalPages])

  const isAssignedToMe = (item) =>
    currentUserId != null &&
    getWorkOrderAssignedFirstId(item) !== '' &&
    Number(getWorkOrderAssignedFirstId(item)) === Number(currentUserId)

  const getAllowedStatusesForItem = (item) => {
    const currentStatus = String(item?.status || '').toUpperCase()

    if (isManager) {
      return MANAGER_ALLOWED_STATUS_BY_CURRENT[currentStatus] || []
    }

    if (isTechnician && isAssignedToMe(item)) {
      return TECHNICIAN_ALLOWED_STATUS_BY_CURRENT[currentStatus] || []
    }

    return []
  }

  const canChangeStatusItem = (item) => getAllowedStatusesForItem(item).length > 0

  const canCompleteItem = (item) =>
    isManager && String(item?.status || '').toUpperCase() === 'PENDING'

  const handleFilterChange = (field, value) => {
    setFilterForm((prev) => ({ ...prev, [field]: value }))
  }

  const handleSearch = () => {
    setPageNum(0)
    setAppliedFilters({
      keyword: filterForm.keyword.trim(),
      status: filterForm.status,
      priority: filterForm.priority,
      archived: filterForm.archived,
    })
  }

  const handleResetFilters = () => {
    const reset = { keyword: '', status: '', priority: '', archived: '' }
    setFilterForm(reset)
    setAppliedFilters(reset)
    setPageNum(0)
  }

  const handlePageChange = (nextPage) => {
    if (nextPage < 0 || nextPage >= totalPages || nextPage === pageNum) return
    setPageNum(nextPage)
  }

  const handlePageSizeChange = (e) => {
    setPageNum(0)
    setPageSize(Number(e.target.value))
  }

  const openCreateModal = () => {
    if (!canCreate) return
    setCreateError('')
    setCreateForm(emptyWorkOrderForm)
    setCreateAssetKeyword('')
    setCreateAssetOpen(false)
    setCreateOpen(true)
    loadAssetOptions('')
  }

  const closeCreateModal = () => {
    if (createLoading) return
    setCreateOpen(false)
    setCreateError('')
    setCreateForm(emptyWorkOrderForm)
    setCreateAssetKeyword('')
    setCreateAssetOpen(false)
  }

  const handleCreateFormChange = (field, value) => {
    setCreateForm((prev) => ({ ...prev, [field]: value }))
  }

  const buildCreatePayload = (form) => ({
    title: form.title?.trim() || '',
    description: form.description?.trim() || '',
    priority: form.priority || null,
    dueDate: form.dueDate || null,
    estimatedDuration:
      form.estimatedDuration === '' || form.estimatedDuration === null
        ? null
        : Number(form.estimatedDuration),
    requiresSignature: Boolean(form.requiresSignature),
    category: form.category?.trim() || '',
    locationName: form.locationName?.trim() || '',
    teamName: form.teamName?.trim() || '',
    primaryUser: form.primaryUser?.trim() || '',
    assignedToId:
      form.assignedToId === '' || form.assignedToId === null ? null : Number(form.assignedToId),
    assetId: form.assetId === '' || form.assetId === null ? null : Number(form.assetId),
    contractors: form.contractors?.trim() || '',
  })

  const handleCreateSubmit = async () => {
    if (!createForm.title?.trim()) {
      setCreateError('Vui lòng nhập tiêu đề work order.')
      return
    }

    if (!createForm.priority) {
      setCreateError('Vui lòng chọn mức ưu tiên.')
      return
    }

    if (!createForm.assetId) {
      setCreateError('Vui lòng chọn asset.')
      return
    }

    if (!createForm.assignedToId) {
      setCreateError('Vui lòng chọn kỹ thuật viên phụ trách.')
      return
    }

    if (!createForm.dueDate) {
      setCreateError('Vui lòng chọn due date.')
      return
    }

    if (!createForm.locationName) {
      setCreateError('Vui lòng chọn location.')
      return
    }

    try {
      setCreateLoading(true)
      setCreateError('')
      await api.post('', buildCreatePayload(createForm), getAuthConfig())
      closeCreateModal()
      await loadData()
    } catch (err) {
      setCreateError(extractErrorMessage(err, 'Không thể tạo work order.'))
    } finally {
      setCreateLoading(false)
    }
  }

  const openEditModal = (item) => {
    if (!canEdit) return
    setEditError('')
    setEditingWorkOrderId(item?.id)
    setEditForm({
      title: item?.title || '',
      description: item?.description || '',
      priority: item?.priority || 'MEDIUM',
      dueDate: toDateInputValue(item?.dueDate),
      estimatedDuration: item?.estimatedDuration ?? '',
      requiresSignature: Boolean(item?.requiredSignature ?? item?.requiresSignature),
      category: getWorkOrderCategoryName(item),
      locationName: getWorkOrderLocationName(item),
      teamName: item?.teamName || '',
      primaryUser: getWorkOrderPrimaryUserName(item),
      assignedToId: getWorkOrderAssignedFirstId(item),
      assetId: getWorkOrderAssetId(item),
      contractors: item?.contractors || '',
    })
    setEditAssetKeyword('')
    setEditAssetOpen(false)
    setEditOpen(true)
    loadAssetOptions('')
  }

  const closeEditModal = () => {
    if (editLoading) return
    setEditOpen(false)
    setEditError('')
    setEditingWorkOrderId(null)
    setEditForm(emptyWorkOrderForm)
    setEditAssetKeyword('')
    setEditAssetOpen(false)
  }

  const handleEditFormChange = (field, value) => {
    setEditForm((prev) => ({ ...prev, [field]: value }))
  }

  const buildUpdatePayload = (form) => ({
    title: form.title?.trim() || '',
    description: form.description?.trim() || '',
    priority: form.priority || null,
    dueDate: form.dueDate || null,
    estimatedDuration:
      form.estimatedDuration === '' || form.estimatedDuration === null
        ? null
        : Number(form.estimatedDuration),
    requiresSignature: Boolean(form.requiresSignature),
    category: form.category?.trim() || '',
    locationName: form.locationName?.trim() || '',
    teamName: form.teamName?.trim() || '',
    primaryUser: form.primaryUser?.trim() || '',
    assignedToId:
      form.assignedToId === '' || form.assignedToId === null ? null : Number(form.assignedToId),
    assetId: form.assetId === '' || form.assetId === null ? null : Number(form.assetId),
    contractors: form.contractors?.trim() || '',
  })

  const handleEditSubmit = async () => {
    if (!editingWorkOrderId) return

    if (!editForm.title?.trim()) {
      setEditError('Vui lòng nhập tiêu đề work order.')
      return
    }

    if (!editForm.priority) {
      setEditError('Vui lòng chọn mức ưu tiên.')
      return
    }

    if (!editForm.assetId) {
      setEditError('Vui lòng chọn asset.')
      return
    }

    if (!editForm.assignedToId) {
      setEditError('Vui lòng chọn kỹ thuật viên phụ trách.')
      return
    }

    if (!editForm.dueDate) {
      setEditError('Vui lòng chọn due date.')
      return
    }

    if (!editForm.locationName) {
      setEditError('Vui lòng chọn location.')
      return
    }

    try {
      setEditLoading(true)
      setEditError('')
      await api.put(`/${editingWorkOrderId}`, buildUpdatePayload(editForm), getAuthConfig())
      closeEditModal()
      await loadData()
    } catch (err) {
      setEditError(extractErrorMessage(err, 'Không thể cập nhật work order.'))
    } finally {
      setEditLoading(false)
    }
  }

  const openDeleteModal = (item) => {
    if (!canDelete) return
    setDeleteError('')
    setDeleteTarget(item)
    setDeleteOpen(true)
  }

  const closeDeleteModal = () => {
    if (deleteLoadingId) return
    setDeleteOpen(false)
    setDeleteError('')
    setDeleteTarget(null)
  }

  const handleDeleteConfirm = async () => {
    const id = deleteTarget?.id
    if (!id) return

    try {
      setDeleteLoadingId(id)
      setDeleteError('')
      await api.delete(`/${id}`, getAuthConfig())
      closeDeleteModal()
      await loadData()
    } catch (err) {
      setDeleteError(extractErrorMessage(err, 'Không thể xóa work order.'))
    } finally {
      setDeleteLoadingId(null)
    }
  }

  const openStatusModal = (item) => {
    const allowed = getAllowedStatusesForItem(item)
    if (!allowed.length) return

    setStatusError('')
    setStatusTarget(item)
    setStatusAllowedOptions(FORM_STATUS_OPTIONS.filter((option) => allowed.includes(option.value)))
    setStatusForm({
      status: allowed[0],
      feedback: item?.feedback || '',
    })
    setStatusOpen(true)
  }

  const closeStatusModal = () => {
    if (statusLoading) return
    setStatusOpen(false)
    setStatusError('')
    setStatusTarget(null)
    setStatusAllowedOptions(FORM_STATUS_OPTIONS)
  }

  const handleStatusSubmit = async () => {
    if (!statusTarget?.id) return

    const allowed = getAllowedStatusesForItem(statusTarget)
    if (!allowed.includes(statusForm.status)) {
      setStatusError('Bạn không có quyền chuyển sang trạng thái này.')
      return
    }

    try {
      setStatusLoading(true)
      setStatusError('')
      await api.patch(
        `/${statusTarget.id}/status`,
        {
          status: statusForm.status,
          feedback: statusForm.feedback?.trim() || '',
        },
        getAuthConfig(),
      )
      closeStatusModal()
      await loadData()
    } catch (err) {
      setStatusError(extractErrorMessage(err, 'Không thể đổi trạng thái work order.'))
    } finally {
      setStatusLoading(false)
    }
  }

  const openCompleteModal = (item) => {
    if (!canCompleteItem(item)) return
    setCompleteError('')
    setCompleteTarget(item)
    setCompleteForm({
      completedByUserId: getWorkOrderAssignedFirstId(item),
      feedback: item?.feedback || '',
    })
    setCompleteOpen(true)
  }

  const closeCompleteModal = () => {
    if (completeLoading) return
    setCompleteOpen(false)
    setCompleteError('')
    setCompleteTarget(null)
  }

  const handleCompleteSubmit = async () => {
    if (!completeTarget?.id) return

    if (String(completeTarget?.status || '').toUpperCase() !== 'PENDING') {
      setCompleteError('Chỉ work order đang chờ duyệt mới có thể chuyển sang DONE.')
      return
    }

    try {
      setCompleteLoading(true)
      setCompleteError('')
      await api.patch(
        `/${completeTarget.id}/status`,
        {
          status: 'DONE',
          feedback: completeForm.feedback?.trim() || 'OK',
        },
        getAuthConfig(),
      )
      closeCompleteModal()
      await loadData()
    } catch (err) {
      setCompleteError(extractErrorMessage(err, 'Không thể chuyển work order sang DONE.'))
    } finally {
      setCompleteLoading(false)
    }
  }

  const handleArchiveToggle = async (item) => {
    if (!canArchive || !item?.id) return
    try {
      setError('')
      await api.patch(`/${item.id}/archive`, null, {
        ...getAuthConfig(),
        params: {
          archived: !Boolean(item.archived),
        },
      })
      await loadData()
    } catch (err) {
      setError(extractErrorMessage(err, 'Không thể cập nhật archive work order.'))
    }
  }

  const closeDetail = () => {
    setDetailOpen(false)
    setDetailError('')
    setSelectedWorkOrder(null)
  }

  return (
    <>
      <div className="assets-page">
        <div className="assets-card">
          <div className="assets-header">
            <div className="assets-header__top">
              <div className="assets-header__intro">
                <div className="assets-header__mini-title">Work Orders</div>
              </div>
            </div>

            <div className="filters-panel">
              <div className="filters-panel__header">
                <div className="filters-panel__title-wrap">
                  <div className="filters-panel__icon">
                    <FiFilter size={18} />
                  </div>
                  <div>
                    <div className="filters-panel__title">Bộ lọc work order</div>
                  </div>
                </div>

                <div className="filters-panel__header-right">
                  {hasActiveFilters && (
                    <div className="filters-active-chip">
                      Đang áp dụng {activeFilterCount} bộ lọc
                    </div>
                  )}

                  {canCreate && (
                    <button
                      className="btn btn-soft-blue btn-create-header"
                      onClick={openCreateModal}
                      type="button"
                    >
                      <FiPlus size={16} />
                      <span>Thêm mới</span>
                    </button>
                  )}
                </div>
              </div>

              <div className="filters-grid filters-grid--5">
                <div className="filter-field">
                  <label className="filter-label">Từ khóa</label>
                  <div className="search-box">
                    <FiSearch size={16} />
                    <input
                      type="text"
                      placeholder="Tìm kiếm"
                      value={filterForm.keyword}
                      onChange={(e) => handleFilterChange('keyword', e.target.value)}
                      onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    />
                  </div>
                </div>

                <div className="filter-field">
                  <label className="filter-label">Trạng thái</label>
                  <select
                    className="filter-select"
                    value={filterForm.status}
                    onChange={(e) => handleFilterChange('status', e.target.value)}
                  >
                    {STATUS_OPTIONS.map((option) => (
                      <option key={option.value || 'all'} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="filter-field">
                  <label className="filter-label">Ưu tiên</label>
                  <select
                    className="filter-select"
                    value={filterForm.priority}
                    onChange={(e) => handleFilterChange('priority', e.target.value)}
                  >
                    {PRIORITY_OPTIONS.map((option) => (
                      <option key={option.value || 'all'} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="filter-field">
                  <label className="filter-label">Lưu trữ</label>
                  <select
                    className="filter-select"
                    value={filterForm.archived}
                    onChange={(e) => handleFilterChange('archived', e.target.value)}
                  >
                    <option value="">Tất cả</option>
                    <option value="ACTIVE">Chưa lưu trữ</option>
                    <option value="ARCHIVED">Đã lưu trữ</option>
                  </select>
                </div>

                <div className="filter-field filter-field--actions">
                  <label className="filter-label filter-label--ghost">Thao tác</label>
                  <div className="filter-actions-row">
                    <button
                      className="btn btn-primary btn-search-compact"
                      onClick={handleSearch}
                      type="button"
                    >
                      <FiSearch size={15} />
                      <span>Tìm kiếm</span>
                    </button>
                    <button
                      className="btn btn-light btn-icon-only"
                      onClick={handleResetFilters}
                      title="Xóa bộ lọc"
                      aria-label="Xóa bộ lọc"
                      type="button"
                    >
                      <FiRotateCcw size={16} />
                    </button>
                  </div>
                </div>
              </div>

              {hasActiveFilters && (
                <div className="applied-filters">
                  {appliedFilters.keyword && (
                    <span className="applied-filter-chip">
                      Từ khóa: <strong>{appliedFilters.keyword}</strong>
                    </span>
                  )}
                  {appliedFilters.status && (
                    <span className="applied-filter-chip">
                      Trạng thái: <strong>{getStatusLabel(appliedFilters.status)}</strong>
                    </span>
                  )}
                  {appliedFilters.priority && (
                    <span className="applied-filter-chip">
                      Ưu tiên: <strong>{getPriorityLabel(appliedFilters.priority)}</strong>
                    </span>
                  )}
                  {appliedFilters.archived && (
                    <span className="applied-filter-chip">
                      Lưu trữ:{' '}
                      <strong>
                        {appliedFilters.archived === 'ARCHIVED' ? 'Đã lưu trữ' : 'Chưa lưu trữ'}
                      </strong>
                    </span>
                  )}
                </div>
              )}
            </div>
          </div>

          {loading ? (
            <div className="assets-message">Đang tải dữ liệu...</div>
          ) : error ? (
            <div className="assets-message assets-message--error">{error}</div>
          ) : pagedWorkOrders.length === 0 ? (
            <div className="assets-message">
              Không có work order nào{hasActiveFilters ? ' phù hợp với bộ lọc hiện tại.' : '.'}
            </div>
          ) : (
            <div className="list-section">
              <div className="list-section__title">
                Danh sách work orders
                <span className="list-badge">{totalElements}</span>
              </div>

              <div className="table-wrap">
                <table className="assets-table work-orders-table">
                  <thead>
                    <tr>
                      <th>STT</th>
                      <th>ID</th>
                      <th>Tiêu đề</th>
                      <th>Asset</th>
                      <th>Kỹ thuật viên</th>
                      <th>Trạng thái</th>
                      <th>Ưu tiên</th>
                      <th>Due date</th>
                      <th>Archived</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pagedWorkOrders.map((item, index) => {
                      const showAnyActionForItem =
                        canViewDetail ||
                        canEdit ||
                        canDelete ||
                        canArchive ||
                        canChangeStatusItem(item) ||
                        canCompleteItem(item)

                      return (
                        <tr key={item.id}>
                          <td>{pageNum * pageSize + index + 1}</td>
                          <td>
                            <span className="work-order-id-chip">{item.id || '-'}</span>
                          </td>
                          <td>
                            <div className="asset-name-cell">
                              <strong>{item.title || '-'}</strong>
                              {item.description && <small>{item.description}</small>}
                            </div>
                          </td>
                          <td>
                            <div className="asset-name-cell">
                              <strong>{getWorkOrderAssetName(item)}</strong>
                              {getWorkOrderAsset(item)?.id && (
                                <small>Asset ID: {getWorkOrderAsset(item).id}</small>
                              )}
                            </div>
                          </td>
                          <td>{getWorkOrderAssignedUsers(item)}</td>
                          <td>
                            <span className={getStatusBadgeClass(item.status)}>
                              {getStatusLabel(item.status)}
                            </span>
                          </td>
                          <td>{getPriorityLabel(item.priority)}</td>
                          <td>{formatDateTimeValue(item.dueDate)}</td>
                          <td>{item.archived ? 'Yes' : 'No'}</td>
                          <td>
                            {showAnyActionForItem ? (
                              <div className="action-group">
                                {canViewDetail && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => loadDetail(item.id)}
                                    title="Xem chi tiết"
                                    type="button"
                                  >
                                    <FiEye size={16} />
                                  </button>
                                )}
                                {canEdit && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => openEditModal(item)}
                                    title="Sửa"
                                    type="button"
                                  >
                                    <FiEdit2 size={16} />
                                  </button>
                                )}
                                {canChangeStatusItem(item) && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => openStatusModal(item)}
                                    title="Đổi trạng thái"
                                    type="button"
                                  >
                                    <FiTag size={16} />
                                  </button>
                                )}
                                {canCompleteItem(item) && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => openCompleteModal(item)}
                                    title="Hoàn tất"
                                    type="button"
                                  >
                                    <FiCheckCircle size={16} />
                                  </button>
                                )}
                                {canArchive && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => handleArchiveToggle(item)}
                                    title={item.archived ? 'Bỏ lưu trữ' : 'Lưu trữ'}
                                    type="button"
                                  >
                                    <FiArchive size={16} />
                                  </button>
                                )}
                                {canDelete && (
                                  <button
                                    className="icon-btn icon-btn--danger"
                                    onClick={() => openDeleteModal(item)}
                                    title="Xóa"
                                    disabled={deleteLoadingId === item.id}
                                    type="button"
                                  >
                                    {deleteLoadingId === item.id ? (
                                      <FiAlertTriangle size={16} />
                                    ) : (
                                      <FiTrash2 size={16} />
                                    )}
                                  </button>
                                )}
                              </div>
                            ) : (
                              <span className="text-muted">-</span>
                            )}
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>

              <div className="pagination-bar">
                <div className="pagination-info">
                  Hiển thị{' '}
                  <strong>{totalElements === 0 ? 0 : pageNum * pageSize + 1}</strong> -{' '}
                  <strong>
                    {totalElements === 0 ? 0 : Math.min((pageNum + 1) * pageSize, totalElements)}
                  </strong>{' '}
                  / <strong>{totalElements}</strong> bản ghi
                </div>

                <div className="pagination-right">
                  {totalPages > 0 && (
                    <div className="pagination-controls">
                      <button
                        className="page-btn"
                        disabled={pageNum === 0}
                        onClick={() => handlePageChange(pageNum - 1)}
                        type="button"
                      >
                        <FiChevronLeft size={16} />
                      </button>

                      {getVisiblePages(pageNum, totalPages).map((page, index) =>
                        typeof page === 'string' ? (
                          <span key={`${page}-${index}`} className="page-ellipsis">
                            ...
                          </span>
                        ) : (
                          <button
                            key={page}
                            className={`page-number ${page === pageNum ? 'active' : ''}`}
                            onClick={() => handlePageChange(page)}
                            type="button"
                          >
                            {page + 1}
                          </button>
                        ),
                      )}

                      <button
                        className="page-btn"
                        disabled={lastPage || totalPages === 0}
                        onClick={() => handlePageChange(pageNum + 1)}
                        type="button"
                      >
                        <FiChevronRight size={16} />
                      </button>
                    </div>
                  )}

                  <select
                    className="page-size-select page-size-select--bottom"
                    value={pageSize}
                    onChange={handlePageSizeChange}
                    disabled={loading}
                  >
                    <option value={5}>05 / Trang</option>
                    <option value={10}>10 / Trang</option>
                    <option value={20}>20 / Trang</option>
                  </select>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {detailOpen && (
        <div className="drawer-overlay" onClick={closeDetail}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Chi tiết work order</h2>
                <p>Xem thông tin đầy đủ của work order</p>
              </div>
              <button className="drawer-close" onClick={closeDetail} type="button">
                <FiX size={22} />
              </button>
            </div>

            {detailLoading ? (
              <div className="drawer-message">Đang tải chi tiết...</div>
            ) : detailError ? (
              <div className="drawer-message drawer-message--error">{detailError}</div>
            ) : !selectedWorkOrder ? (
              <div className="drawer-message">Không có dữ liệu chi tiết.</div>
            ) : (
              <div className="drawer-body">
                <div className="detail-hero">
                  <div className="detail-hero__left">
                    <div className="detail-hero__icon">
                      <FiClipboard size={30} />
                    </div>
                    <div className="detail-hero__content">
                      <h3>{selectedWorkOrder.title || '-'}</h3>
                      <p>{selectedWorkOrder.description || 'Không có mô tả'}</p>
                      <div className="detail-hero__meta">
                        <span className={getStatusBadgeClass(selectedWorkOrder.status)}>
                          {getStatusLabel(selectedWorkOrder.status)}
                        </span>
                        <span className="hero-chip">
                          <FiTag size={14} />
                          {getPriorityLabel(selectedWorkOrder.priority)}
                        </span>
                        <span className="hero-chip">
                          <FiUser size={14} />
                          {getWorkOrderAssignedUsers(selectedWorkOrder)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="detail-section">
                  <div className="detail-section__title">Thông tin cơ bản</div>
                  <div className="detail-grid detail-grid--2">
                    <DetailItem
                      icon={<FiTag size={16} />}
                      label="ID"
                      value={selectedWorkOrder.id}
                      compact
                    />
                    <DetailItem
                      icon={<FiUser size={16} />}
                      label="Asset"
                      value={getWorkOrderAssetName(selectedWorkOrder)}
                      compact
                    />
                    <DetailItem
                      icon={<FiCalendar size={16} />}
                      label="Due date"
                      value={formatDateTimeValue(selectedWorkOrder.dueDate)}
                      compact
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Estimated Duration"
                      value={selectedWorkOrder.estimatedDuration}
                      compact
                    />
                    <DetailItem
                      icon={<FiMapPin size={16} />}
                      label="Location"
                      value={getWorkOrderLocationName(selectedWorkOrder) || '-'}
                      compact
                    />
                    <DetailItem
                      icon={<FiUser size={16} />}
                      label="Kỹ thuật viên phụ trách"
                      value={getWorkOrderAssignedUsers(selectedWorkOrder)}
                      compact
                    />
                    <DetailItem
                      icon={<FiUser size={16} />}
                      label="Primary User"
                      value={getWorkOrderPrimaryUserName(selectedWorkOrder) || '-'}
                      compact
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Category"
                      value={getWorkOrderCategoryName(selectedWorkOrder) || '-'}
                      compact
                    />
                  </div>
                </div>

                <div className="detail-section">
                  <div className="detail-section__title">Thông tin hoàn tất</div>
                  <div className="detail-grid detail-grid--2">
                    <DetailItem
                      icon={<FiCheckCircle size={16} />}
                      label="Completed By"
                      value={selectedWorkOrder.completedBy}
                      compact
                    />
                    <DetailItem
                      icon={<FiCalendar size={16} />}
                      label="Completed On"
                      value={formatDateTimeValue(selectedWorkOrder.completedOn)}
                      compact
                    />
                    <DetailItem
                      icon={<FiArchive size={16} />}
                      label="Archived"
                      value={selectedWorkOrder.archived ? 'Yes' : 'No'}
                      compact
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Requires Signature"
                      value={
                        selectedWorkOrder.requiredSignature ?? selectedWorkOrder.requiresSignature
                          ? 'Yes'
                          : 'No'
                      }
                      compact
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Feedback"
                      value={selectedWorkOrder.feedback}
                      full
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Contractors"
                      value={selectedWorkOrder.contractors}
                      full
                    />
                    <DetailItem
                      icon={<FiCalendar size={16} />}
                      label="Date Created"
                      value={formatDateTimeValue(selectedWorkOrder.dateCreated)}
                      compact
                    />
                    <DetailItem
                      icon={<FiCalendar size={16} />}
                      label="Updated At"
                      value={formatDateTimeValue(selectedWorkOrder.updatedAt)}
                      compact
                    />
                  </div>
                </div>

                <WorkOrderPartSection
                  workOrderId={selectedWorkOrder?.id}
                  canManageParts={canEdit || canCreate}
                  onChanged={async () => {
                    if (selectedWorkOrder?.id) {
                      await loadDetail(selectedWorkOrder.id)
                      await loadData()
                    }
                  }}
                />
              </div>
            )}
          </div>
        </div>
      )}

      {createOpen && (
        <div className="drawer-overlay" onClick={closeCreateModal}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Thêm mới work order</h2>
                <p>Tạo work order và gửi đúng API backend</p>
              </div>
              <button className="drawer-close" onClick={closeCreateModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {createError && (
                <div className="drawer-message drawer-message--error drawer-message--inline">
                  {createError}
                </div>
              )}

              <div className="form-section">
                <div className="detail-section__title">Thông tin work order</div>
                <div className="form-grid form-grid--modal">
                  <FormField label="Tiêu đề" required>
                    <input
                      className="form-input"
                      value={createForm.title}
                      onChange={(e) => handleCreateFormChange('title', e.target.value)}
                      placeholder="Nhập tiêu đề"
                    />
                  </FormField>

                  <FormField label="Ưu tiên" required>
                    <select
                      className="form-input"
                      value={createForm.priority}
                      onChange={(e) => handleCreateFormChange('priority', e.target.value)}
                    >
                      <option value="">Chọn ưu tiên</option>
                      {FORM_PRIORITY_OPTIONS.map((item) => (
                        <option key={item.value} value={item.value}>
                          {item.label}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Asset" required>
                    <div className="asset-combobox">
                      <button
                        type="button"
                        className={`asset-combobox__control ${createAssetOpen ? 'is-open' : ''}`}
                        onClick={() => {
                          const next = !createAssetOpen
                          setCreateAssetOpen(next)
                          if (next) {
                            loadAssetOptions(createAssetKeyword)
                          }
                        }}
                      >
                        <span
                          className={
                            createForm.assetId
                              ? 'asset-combobox__value'
                              : 'asset-combobox__placeholder'
                          }
                        >
                          {createForm.assetId
                            ? getAssetOptionLabel(
                                assetOptions.find(
                                  (item) => Number(item.id) === Number(createForm.assetId),
                                ),
                              )
                            : 'Chọn asset'}
                        </span>
                        <span className="asset-combobox__arrow">⌄</span>
                      </button>

                      {createAssetOpen && (
                        <div className="asset-combobox__dropdown">
                          <div className="asset-combobox__search">
                            <input
                              type="text"
                              className="asset-combobox__search-input"
                              placeholder="Tìm asset..."
                              value={createAssetKeyword}
                              onChange={(e) => setCreateAssetKeyword(e.target.value)}
                            />
                            <FiSearch size={18} className="asset-combobox__search-icon" />
                          </div>

                          <div className="asset-combobox__list">
                            {assetOptionsLoading ? (
                              <div className="asset-combobox__empty">Đang tải asset...</div>
                            ) : assetOptions.length === 0 ? (
                              <div className="asset-combobox__empty">Không có asset phù hợp</div>
                            ) : (
                              assetOptions.map((asset) => {
                                const isSelected =
                                  Number(createForm.assetId) === Number(asset.id)

                                return (
                                  <button
                                    key={asset.id}
                                    type="button"
                                    className={`asset-combobox__item ${isSelected ? 'is-selected' : ''}`}
                                    onClick={() => {
                                      handleCreateFormChange('assetId', asset.id)
                                      setCreateAssetOpen(false)
                                    }}
                                  >
                                    <div className="asset-combobox__item-title">
                                      {asset.name || `Asset #${asset.id}`}
                                    </div>
                                    <div className="asset-combobox__item-meta">
                                      {asset.barcode || '-'}
                                      {asset.serialNumber ? ` • ${asset.serialNumber}` : ''}
                                    </div>
                                  </button>
                                )
                              })
                            )}
                          </div>
                        </div>
                      )}
                    </div>
                  </FormField>

                  <FormField label="Kỹ thuật viên phụ trách" required>
                    <select
                      className="form-input"
                      value={createForm.assignedToId}
                      onChange={(e) => handleCreateFormChange('assignedToId', e.target.value)}
                      disabled={techniciansLoading}
                      required
                    >
                      <option value="">
                        {techniciansLoading ? 'Đang tải kỹ thuật viên...' : 'Chọn kỹ thuật viên'}
                      </option>
                      {technicians.map((tech) => (
                        <option key={getUserId(tech)} value={getUserId(tech)}>
                          {getUserDisplayName(tech)}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Due date" required>
                    <input
                      type="date"
                      className="form-input"
                      value={createForm.dueDate}
                      onChange={(e) => handleCreateFormChange('dueDate', e.target.value)}
                    />
                  </FormField>

                  <FormField label="Estimated Duration">
                    <input
                      type="number"
                      className="form-input"
                      value={createForm.estimatedDuration}
                      onChange={(e) =>
                        handleCreateFormChange('estimatedDuration', e.target.value)
                      }
                      placeholder="Ví dụ: 2.5"
                    />
                  </FormField>

                  <FormField label="Category">
                    <input
                      className="form-input"
                      value={createForm.category}
                      onChange={(e) => handleCreateFormChange('category', e.target.value)}
                      placeholder="Nhập category"
                    />
                  </FormField>

                  <FormField label="Location" required>
                    <select
                      className="form-input"
                      value={createForm.locationName}
                      onChange={(e) => handleCreateFormChange('locationName', e.target.value)}
                      disabled={locationsLoading}
                    >
                      <option value="">
                        {locationsLoading ? 'Đang tải location...' : 'Chọn location'}
                      </option>
                      {locationOptions.map((location) => (
                        <option key={location.id} value={location.name}>
                          {location.name}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Team">
                    <input
                      className="form-input"
                      value={createForm.teamName}
                      onChange={(e) => handleCreateFormChange('teamName', e.target.value)}
                      placeholder="Nhập team"
                    />
                  </FormField>

                  <FormField label="Primary User">
                    <input
                      className="form-input"
                      value={createForm.primaryUser}
                      onChange={(e) => handleCreateFormChange('primaryUser', e.target.value)}
                      placeholder="Nhập primary user"
                    />
                  </FormField>

                  <FormField label="Requires Signature">
                    <select
                      className="form-input"
                      value={String(createForm.requiresSignature)}
                      onChange={(e) =>
                        handleCreateFormChange('requiresSignature', e.target.value === 'true')
                      }
                    >
                      <option value="false">No</option>
                      <option value="true">Yes</option>
                    </select>
                  </FormField>

                  <FormField label="Contractors" full>
                    <textarea
                      className="form-input form-textarea"
                      value={createForm.contractors}
                      onChange={(e) => handleCreateFormChange('contractors', e.target.value)}
                      placeholder="Nhập contractors"
                    />
                  </FormField>

                  <FormField label="Mô tả" full>
                    <textarea
                      className="form-input form-textarea"
                      value={createForm.description}
                      onChange={(e) => handleCreateFormChange('description', e.target.value)}
                      placeholder="Nhập mô tả"
                    />
                  </FormField>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button
                className="btn btn-secondary"
                onClick={closeCreateModal}
                disabled={createLoading}
                type="button"
              >
                Hủy
              </button>
              <button
                className="btn btn-primary"
                onClick={handleCreateSubmit}
                disabled={createLoading}
                type="button"
              >
                <FiSave size={16} />
                <span>{createLoading ? 'Đang lưu...' : 'Lưu work order'}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {editOpen && (
        <div className="drawer-overlay" onClick={closeEditModal}>
          <div className="drawer drawer--wide" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Cập nhật work order</h2>
                <p>Chỉnh sửa work order và gửi đúng API backend</p>
              </div>
              <button className="drawer-close" onClick={closeEditModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {editError && (
                <div className="drawer-message drawer-message--error drawer-message--inline">
                  {editError}
                </div>
              )}

              <div className="form-section">
                <div className="detail-section__title">Thông tin work order</div>
                <div className="form-grid form-grid--modal">
                  <FormField label="Tiêu đề" required>
                    <input
                      className="form-input"
                      value={editForm.title}
                      onChange={(e) => handleEditFormChange('title', e.target.value)}
                      placeholder="Nhập tiêu đề"
                    />
                  </FormField>

                  <FormField label="Ưu tiên" required>
                    <select
                      className="form-input"
                      value={editForm.priority}
                      onChange={(e) => handleEditFormChange('priority', e.target.value)}
                    >
                      <option value="">Chọn ưu tiên</option>
                      {FORM_PRIORITY_OPTIONS.map((item) => (
                        <option key={item.value} value={item.value}>
                          {item.label}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Asset" required>
                    <div className="asset-combobox">
                      <button
                        type="button"
                        className={`asset-combobox__control ${editAssetOpen ? 'is-open' : ''}`}
                        onClick={() => {
                          const next = !editAssetOpen
                          setEditAssetOpen(next)
                          if (next) {
                            loadAssetOptions(editAssetKeyword)
                          }
                        }}
                      >
                        <span
                          className={
                            editForm.assetId
                              ? 'asset-combobox__value'
                              : 'asset-combobox__placeholder'
                          }
                        >
                          {editForm.assetId
                            ? getAssetOptionLabel(
                                assetOptions.find(
                                  (item) => Number(item.id) === Number(editForm.assetId),
                                ),
                              )
                            : 'Chọn asset'}
                        </span>
                        <span className="asset-combobox__arrow">⌄</span>
                      </button>

                      {editAssetOpen && (
                        <div className="asset-combobox__dropdown">
                          <div className="asset-combobox__search">
                            <input
                              type="text"
                              className="asset-combobox__search-input"
                              placeholder="Tìm asset..."
                              value={editAssetKeyword}
                              onChange={(e) => setEditAssetKeyword(e.target.value)}
                            />
                            <FiSearch size={18} className="asset-combobox__search-icon" />
                          </div>

                          <div className="asset-combobox__list">
                            {assetOptionsLoading ? (
                              <div className="asset-combobox__empty">Đang tải asset...</div>
                            ) : assetOptions.length === 0 ? (
                              <div className="asset-combobox__empty">Không có asset phù hợp</div>
                            ) : (
                              assetOptions.map((asset) => {
                                const isSelected = Number(editForm.assetId) === Number(asset.id)

                                return (
                                  <button
                                    key={asset.id}
                                    type="button"
                                    className={`asset-combobox__item ${isSelected ? 'is-selected' : ''}`}
                                    onClick={() => {
                                      handleEditFormChange('assetId', asset.id)
                                      setEditAssetOpen(false)
                                    }}
                                  >
                                    <div className="asset-combobox__item-title">
                                      {asset.name || `Asset #${asset.id}`}
                                    </div>
                                    <div className="asset-combobox__item-meta">
                                      {asset.barcode || '-'}
                                      {asset.serialNumber ? ` • ${asset.serialNumber}` : ''}
                                    </div>
                                  </button>
                                )
                              })
                            )}
                          </div>
                        </div>
                      )}
                    </div>
                  </FormField>

                  <FormField label="Kỹ thuật viên phụ trách" required>
                    <select
                      className="form-input"
                      value={editForm.assignedToId}
                      onChange={(e) => handleEditFormChange('assignedToId', e.target.value)}
                      disabled={techniciansLoading}
                      required
                    >
                      <option value="">
                        {techniciansLoading ? 'Đang tải kỹ thuật viên...' : 'Chọn kỹ thuật viên'}
                      </option>
                      {technicians.map((tech) => (
                        <option key={getUserId(tech)} value={getUserId(tech)}>
                          {getUserDisplayName(tech)}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Due date" required>
                    <input
                      type="date"
                      className="form-input"
                      value={editForm.dueDate}
                      onChange={(e) => handleEditFormChange('dueDate', e.target.value)}
                    />
                  </FormField>

                  <FormField label="Estimated Duration">
                    <input
                      type="number"
                      className="form-input"
                      value={editForm.estimatedDuration}
                      onChange={(e) => handleEditFormChange('estimatedDuration', e.target.value)}
                      placeholder="Ví dụ: 2.5"
                    />
                  </FormField>

                  <FormField label="Category">
                    <input
                      className="form-input"
                      value={editForm.category}
                      onChange={(e) => handleEditFormChange('category', e.target.value)}
                      placeholder="Nhập category"
                    />
                  </FormField>

                  <FormField label="Location" required>
                    <select
                      className="form-input"
                      value={editForm.locationName}
                      onChange={(e) => handleEditFormChange('locationName', e.target.value)}
                      disabled={locationsLoading}
                    >
                      <option value="">
                        {locationsLoading ? 'Đang tải location...' : 'Chọn location'}
                      </option>
                      {locationOptions.map((location) => (
                        <option key={location.id} value={location.name}>
                          {location.name}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Team">
                    <input
                      className="form-input"
                      value={editForm.teamName}
                      onChange={(e) => handleEditFormChange('teamName', e.target.value)}
                      placeholder="Nhập team"
                    />
                  </FormField>

                  <FormField label="Primary User">
                    <input
                      className="form-input"
                      value={editForm.primaryUser}
                      onChange={(e) => handleEditFormChange('primaryUser', e.target.value)}
                      placeholder="Nhập primary user"
                    />
                  </FormField>

                  <FormField label="Requires Signature">
                    <select
                      className="form-input"
                      value={String(editForm.requiresSignature)}
                      onChange={(e) =>
                        handleEditFormChange('requiresSignature', e.target.value === 'true')
                      }
                    >
                      <option value="false">No</option>
                      <option value="true">Yes</option>
                    </select>
                  </FormField>

                  <FormField label="Contractors" full>
                    <textarea
                      className="form-input form-textarea"
                      value={editForm.contractors}
                      onChange={(e) => handleEditFormChange('contractors', e.target.value)}
                      placeholder="Nhập contractors"
                    />
                  </FormField>

                  <FormField label="Mô tả" full>
                    <textarea
                      className="form-input form-textarea"
                      value={editForm.description}
                      onChange={(e) => handleEditFormChange('description', e.target.value)}
                      placeholder="Nhập mô tả"
                    />
                  </FormField>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button
                className="btn btn-secondary"
                onClick={closeEditModal}
                disabled={editLoading}
                type="button"
              >
                Hủy
              </button>
              <button
                className="btn btn-primary"
                onClick={handleEditSubmit}
                disabled={editLoading}
                type="button"
              >
                <FiSave size={16} />
                <span>{editLoading ? 'Đang lưu...' : 'Lưu work order'}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {deleteOpen && (
        <div className="drawer-overlay" onClick={closeDeleteModal}>
          <div className="drawer drawer--small" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Xóa work order</h2>
                <p>
                  Bạn có chắc muốn xóa work order <strong>{deleteTarget?.id || '-'}</strong> không?
                </p>
              </div>
              <button className="drawer-close" onClick={closeDeleteModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-footer">
              <button
                className="btn btn-secondary"
                onClick={closeDeleteModal}
                disabled={Boolean(deleteLoadingId)}
                type="button"
              >
                Hủy
              </button>
              <button
                className="btn btn-danger-solid"
                onClick={handleDeleteConfirm}
                disabled={Boolean(deleteLoadingId)}
                type="button"
              >
                <FiTrash2 size={16} />
                <span>{deleteLoadingId ? 'Đang xóa...' : 'Xóa work order'}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {statusOpen && (
        <div className="drawer-overlay" onClick={closeStatusModal}>
          <div className="drawer drawer--small" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Đổi trạng thái</h2>
                <p>Cập nhật trạng thái work order</p>
              </div>
              <button className="drawer-close" onClick={closeStatusModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {statusError && (
                <div className="drawer-message drawer-message--error drawer-message--inline">
                  {statusError}
                </div>
              )}

              <div className="form-grid">
                <FormField label="Trạng thái">
                  <select
                    className="form-input"
                    value={statusForm.status}
                    onChange={(e) =>
                      setStatusForm((prev) => ({ ...prev, status: e.target.value }))
                    }
                  >
                    {statusAllowedOptions.map((item) => (
                      <option key={item.value} value={item.value}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </FormField>

                <FormField label="Feedback" full>
                  <textarea
                    className="form-input form-textarea"
                    value={statusForm.feedback}
                    onChange={(e) =>
                      setStatusForm((prev) => ({ ...prev, feedback: e.target.value }))
                    }
                    placeholder="Nhập feedback"
                  />
                </FormField>
              </div>
            </div>

            <div className="drawer-footer">
              <button
                className="btn btn-secondary"
                onClick={closeStatusModal}
                disabled={statusLoading}
                type="button"
              >
                Hủy
              </button>
              <button
                className="btn btn-primary"
                onClick={handleStatusSubmit}
                disabled={statusLoading}
                type="button"
              >
                <FiSave size={16} />
                <span>{statusLoading ? 'Đang lưu...' : 'Lưu trạng thái'}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {completeOpen && (
        <div className="drawer-overlay" onClick={closeCompleteModal}>
          <div className="drawer drawer--small" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Hoàn tất work order</h2>
              </div>
              <button className="drawer-close" onClick={closeCompleteModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {completeError && (
                <div className="drawer-message drawer-message--error drawer-message--inline">
                  {completeError}
                </div>
              )}

              <div className="form-grid">
                <FormField label="Feedback" full>
                  <textarea
                    className="form-input form-textarea"
                    value={completeForm.feedback}
                    onChange={(e) =>
                      setCompleteForm((prev) => ({ ...prev, feedback: e.target.value }))
                    }
                    placeholder="Nhập ghi chú"
                  />
                </FormField>
              </div>
            </div>

            <div className="drawer-footer">
              <button
                className="btn btn-secondary"
                onClick={closeCompleteModal}
                disabled={completeLoading}
                type="button"
              >
                Hủy
              </button>
              <button
                className="btn btn-primary"
                onClick={handleCompleteSubmit}
                disabled={completeLoading}
                type="button"
              >
                <FiCheckCircle size={16} />
                <span>{completeLoading ? 'Đang duyệt...' : 'Duyệt DONE'}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}