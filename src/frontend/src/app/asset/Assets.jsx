import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import axios from 'axios'
import './Assets.css'
import {
  FiSearch,
  FiChevronLeft,
  FiChevronRight,
  FiEye,
  FiLayers,
  FiX,
  FiBox,
  FiMapPin,
  FiUser,
  FiTag,
  FiTool,
  FiInfo,
  FiCalendar,
  FiEdit2,
  FiPlus,
  FiTrash2,
  FiAlertTriangle,
  FiFilter,
  FiRotateCcw,
  FiSave,
  FiSlash,
} from 'react-icons/fi'

const API_BASE_URL = 'https://emms-system-production-4239.up.railway.app/api/assets'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
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
    headers: token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : {},
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

  if (raw.startsWith('ROLE_')) {
    raw = raw.substring(5)
  }

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
  const authoritiesRaw =
    localStorage.getItem('authorities') || sessionStorage.getItem('authorities')
  const permissionsRaw =
    localStorage.getItem('permissions') || sessionStorage.getItem('permissions')
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

  return {
    user,
    grants: Array.from(new Set(merged)),
  }
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

    if (typeof data === 'string' && data.trim()) {
      return `HTTP ${err.response.status}: ${data}`
    }

    if (data?.message) {
      return `HTTP ${err.response.status}: ${data.message}`
    }

    if (data?.error) {
      return `HTTP ${err.response.status}: ${data.error}`
    }

    return `HTTP ${err.response.status}: ${fallback}`
  }

  if (err.request) {
    return 'Không nhận được phản hồi từ backend. Kiểm tra backend/CORS/network.'
  }

  return err.message || fallback
}

const getAssetId = (asset) => asset?.id ?? asset?.assetId ?? null

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'OPERATIONAL', label: 'Hoạt động' },
  { value: 'STANDBY', label: 'Chờ vận hành' },
  { value: 'INSPECTION', label: 'Đang kiểm tra' },
  { value: 'COMMISSIONING', label: 'Đang chạy thử' },
  { value: 'DOWN', label: 'Ngừng hoạt động' },
  { value: 'MAINTENANCE', label: 'Đang bảo trì' },
  { value: 'EMERGENCY_SHUTDOWN', label: 'Dừng khẩn cấp' },
  { value: 'DECOMMISSIONED', label: 'Ngừng sử dụng' },
]

const FORM_STATUS_OPTIONS = STATUS_OPTIONS.filter((item) => item.value)

const getStatusLabel = (status) => {
  const found = STATUS_OPTIONS.find((item) => item.value === status)
  return found?.label || status || 'Tất cả trạng thái'
}

const getWarrantySortLabel = (value) => {
  switch (value) {
    case 'nearest':
      return 'Ngày gần nhất'
    case 'farthest':
      return 'Ngày xa nhất'
    default:
      return 'Mặc định'
  }
}

const buildSearchCriteria = (filters, pageNum, pageSize) => {
  const safeFilters = filters || {}
  const name = (safeFilters.name || '').trim()
  const barcode = (safeFilters.barcode || '').trim()
  const status = (safeFilters.status || '').trim()
  const warrantySort = (safeFilters.warrantySort || '').trim()

  const filterFields = []

  if (name) {
    filterFields.push({
      field: 'name',
      operation: 'cn',
      value: name,
    })
  }

  if (barcode) {
    filterFields.push({
      field: 'barcode',
      operation: 'cn',
      value: barcode,
    })
  }

  if (status) {
    filterFields.push({
      field: 'status',
      operation: 'eq',
      value: status,
    })
  }

  let direction = 'DESC'
  let sortField = 'id'

  if (warrantySort === 'nearest') {
    sortField = 'warrantyExpiryDate'
    direction = 'ASC'
  } else if (warrantySort === 'farthest') {
    sortField = 'warrantyExpiryDate'
    direction = 'DESC'
  }

  return {
    filterFields,
    direction,
    pageNum,
    pageSize,
    sortField,
  }
}

const getStatusBadgeClass = (status) => {
  switch ((status || '').toUpperCase()) {
    case 'OPERATIONAL':
    case 'STANDBY':
    case 'INSPECTION':
    case 'COMMISSIONING':
      return 'badge badge--success'
    case 'DOWN':
    case 'EMERGENCY_SHUTDOWN':
      return 'badge badge--danger'
    case 'MAINTENANCE':
      return 'badge badge--info'
    case 'DECOMMISSIONED':
      return 'badge badge--default'
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

const emptyAssetForm = {
  name: '',
  barcode: '',
  status: 'OPERATIONAL',
  category: '',
  locationName: '',
  assignedTo: '',
  warrantyExpiryDate: '',
  description: '',
  serialNumber: '',
  parentAssetName: '',
  area: '',
  vendor: '',
  contractor: '',
  teamNames: '',
  associatedParts: '',
  additionalInfo: '',
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

function FormField({ label, children, full = false }) {
  return (
    <div className={`form-field ${full ? 'form-field--full' : ''}`}>
      <label className="form-label">{label}</label>
      {children}
    </div>
  )
}

export default function Assets() {
  const [searchParams, setSearchParams] = useSearchParams()

  const parentIdParam = searchParams.get('parentId')
  const parsedParentId = parentIdParam ? Number(parentIdParam) : null
  const parentId = Number.isFinite(parsedParentId) ? parsedParentId : null

  const { grants } = useMemo(() => getUserContext(), [])
  const isAuthenticated = Boolean(getToken())

  const canViewDetail = isAuthenticated
  const canCreate = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canEdit = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canDelete = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canDecommission = hasAnyGrant(grants, ['ADMIN', 'TECHNICAL_MANAGER'])
  const canViewChildren = isAuthenticated

  const [filterForm, setFilterForm] = useState({
    name: '',
    barcode: '',
    status: '',
    warrantySort: '',
  })

  const [appliedFilters, setAppliedFilters] = useState({
    name: '',
    barcode: '',
    status: '',
    warrantySort: '',
  })

  const [assets, setAssets] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [pageNum, setPageNum] = useState(0)
  const [pageSize, setPageSize] = useState(5)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [lastPage, setLastPage] = useState(false)

  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [selectedAsset, setSelectedAsset] = useState(null)

  const [createOpen, setCreateOpen] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [createError, setCreateError] = useState('')
  const [createForm, setCreateForm] = useState(emptyAssetForm)

  const [editOpen, setEditOpen] = useState(false)
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const [editForm, setEditForm] = useState(emptyAssetForm)
  const [editingAssetId, setEditingAssetId] = useState(null)

  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteError, setDeleteError] = useState('')
  const [deleteLoadingId, setDeleteLoadingId] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)

  const [decommissionLoadingId, setDecommissionLoadingId] = useState(null)

  const pageTitle = useMemo(() => (parentId ? 'Thiết bị con' : 'Danh sách thiết bị'), [parentId])

  const hasActiveFilters = useMemo(
    () =>
      Boolean(
        appliedFilters.name?.trim() ||
          appliedFilters.barcode?.trim() ||
          appliedFilters.status?.trim() ||
          appliedFilters.warrantySort?.trim(),
      ),
    [appliedFilters],
  )

  const activeFilterCount = useMemo(() => {
    let count = 0
    if (appliedFilters.name?.trim()) count += 1
    if (appliedFilters.barcode?.trim()) count += 1
    if (appliedFilters.status?.trim()) count += 1
    if (appliedFilters.warrantySort?.trim()) count += 1
    return count
  }, [appliedFilters])

  const loadRootAssets = useCallback(async (page = 0, size = 5, filters = {}) => {
    try {
      setLoading(true)
      setError('')

      const response = await api.post(
        '/search',
        buildSearchCriteria(filters, page, size),
        getAuthConfig(),
      )

      const pageData = response?.data || {}

      setAssets(Array.isArray(pageData?.content) ? pageData.content : [])
      setPageNum(Number.isFinite(pageData?.number) ? pageData.number : 0)
      setTotalPages(Number.isFinite(pageData?.totalPages) ? pageData.totalPages : 0)
      setTotalElements(Number.isFinite(pageData?.totalElements) ? pageData.totalElements : 0)
      setLastPage(Boolean(pageData?.last))
    } catch (err) {
      setError(extractErrorMessage(err, 'Không thể tải danh sách thiết bị.'))
    } finally {
      setLoading(false)
    }
  }, [])

  const loadChildrenAssets = useCallback(
    async (filters = {}) => {
      if (!parentId) return

      try {
        setLoading(true)
        setError('')

        const response = await api.get(`/${parentId}/children`, getAuthConfig())
        const children = Array.isArray(response?.data) ? response.data : []

        const name = (filters?.name || '').trim().toLowerCase()
        const barcode = (filters?.barcode || '').trim().toLowerCase()
        const status = (filters?.status || '').trim().toLowerCase()
        const warrantySort = (filters?.warrantySort || '').trim()

        let filtered = children

        if (name) {
          filtered = filtered.filter((item) =>
            [item.name, item.description]
              .filter(Boolean)
              .some((value) => String(value).toLowerCase().includes(name)),
          )
        }

        if (barcode) {
          filtered = filtered.filter((item) =>
            String(item?.barcode || '')
              .toLowerCase()
              .includes(barcode),
          )
        }

        if (status) {
          filtered = filtered.filter(
            (item) => String(item?.status || '').toLowerCase() === status,
          )
        }

        if (warrantySort === 'nearest') {
          filtered = [...filtered].sort((a, b) => {
            const aDate = a?.warrantyExpiryDate ? new Date(a.warrantyExpiryDate).getTime() : Number.MAX_SAFE_INTEGER
            const bDate = b?.warrantyExpiryDate ? new Date(b.warrantyExpiryDate).getTime() : Number.MAX_SAFE_INTEGER
            return aDate - bDate
          })
        } else if (warrantySort === 'farthest') {
          filtered = [...filtered].sort((a, b) => {
            const aDate = a?.warrantyExpiryDate ? new Date(a.warrantyExpiryDate).getTime() : Number.MIN_SAFE_INTEGER
            const bDate = b?.warrantyExpiryDate ? new Date(b.warrantyExpiryDate).getTime() : Number.MIN_SAFE_INTEGER
            return bDate - aDate
          })
        }

        setAssets(filtered)
        setPageNum(0)
        setTotalPages(1)
        setTotalElements(filtered.length)
        setLastPage(true)
      } catch (err) {
        setError(extractErrorMessage(err, 'Không thể tải danh sách thiết bị con.'))
      } finally {
        setLoading(false)
      }
    },
    [parentId],
  )

  const loadData = useCallback(
    async (page = 0, size = 5, filters = {}) => {
      if (parentId) {
        await loadChildrenAssets(filters)
      } else {
        await loadRootAssets(page, size, filters)
      }
    },
    [parentId, loadChildrenAssets, loadRootAssets],
  )

  const loadAssetDetail = useCallback(async (assetId) => {
    try {
      setDetailLoading(true)
      setDetailError('')
      setDetailOpen(true)

      const response = await api.get(`/${assetId}`, getAuthConfig())
      setSelectedAsset(response?.data || null)
    } catch (err) {
      setSelectedAsset(null)
      setDetailError(extractErrorMessage(err, 'Không thể tải chi tiết thiết bị.'))
    } finally {
      setDetailLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData(pageNum, pageSize, appliedFilters)
  }, [pageNum, pageSize, appliedFilters, parentId, loadData])

  const handleFilterChange = (field, value) => {
    setFilterForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleSearch = () => {
    setPageNum(0)
    setAppliedFilters({
      name: filterForm.name.trim(),
      barcode: filterForm.barcode.trim(),
      status: filterForm.status,
      warrantySort: filterForm.warrantySort,
    })
  }

  const handleResetFilters = () => {
    const emptyFilters = {
      name: '',
      barcode: '',
      status: '',
      warrantySort: '',
    }

    setFilterForm(emptyFilters)
    setAppliedFilters(emptyFilters)
    setPageNum(0)
  }

  const handlePageChange = (nextPage) => {
    if (nextPage < 0 || nextPage >= totalPages || nextPage === pageNum) return
    setPageNum(nextPage)
  }

  const handlePageSizeChange = (e) => {
    const nextSize = Number(e.target.value)
    setPageNum(0)
    setPageSize(nextSize)
  }

  const handleViewDetails = (asset) => {
    const id = getAssetId(asset)
    if (!id || !canViewDetail) return
    loadAssetDetail(id)
  }

  const openCreateModal = () => {
    if (!canCreate) return
    setCreateError('')
    setCreateForm({
      ...emptyAssetForm,
      parentAssetName: '',
    })
    setCreateOpen(true)
  }

  const closeCreateModal = () => {
    if (createLoading) return
    setCreateOpen(false)
    setCreateError('')
    setCreateForm(emptyAssetForm)
  }

  const handleCreateFormChange = (field, value) => {
    setCreateForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const buildPayload = (form) => ({
    name: form.name?.trim() || '',
    barcode: form.barcode?.trim() || '',
    status: String(form.status || '').trim().toUpperCase(),
    category: form.category?.trim() || '',
    locationName: form.locationName?.trim() || '',
    assignedTo: form.assignedTo?.trim() || '',
    warrantyExpiryDate: form.warrantyExpiryDate || null,
    description: form.description?.trim() || '',
    serialNumber: form.serialNumber?.trim() || '',
    parentAssetName: form.parentAssetName?.trim() || '',
    area: form.area?.trim() || '',
    vendor: form.vendor?.trim() || '',
    contractor: form.contractor?.trim() || '',
    teamNames: form.teamNames?.trim() || '',
    associatedParts: form.associatedParts?.trim() || '',
    additionalInfo: form.additionalInfo?.trim() || '',
  })

  const handleCreateSubmit = async () => {
    if (!canCreate) return

    try {
      setCreateLoading(true)
      setCreateError('')

      await api.post('', buildPayload(createForm), getAuthConfig())
      closeCreateModal()
      await loadData(pageNum, pageSize, appliedFilters)
    } catch (err) {
      setCreateError(extractErrorMessage(err, 'Không thể tạo thiết bị.'))
    } finally {
      setCreateLoading(false)
    }
  }

  const openEditModal = (asset) => {
    if (!canEdit) return
    setEditError('')
    setEditingAssetId(getAssetId(asset))
    setEditForm({
      name: asset?.name || '',
      barcode: asset?.barcode || '',
      status: asset?.status || 'OPERATIONAL',
      category: asset?.category || '',
      locationName: asset?.locationName || '',
      assignedTo: asset?.assignedTo || '',
      warrantyExpiryDate: asset?.warrantyExpiryDate || '',
      description: asset?.description || '',
      serialNumber: asset?.serialNumber || '',
      parentAssetName: asset?.parentAssetName || '',
      area: asset?.area || '',
      vendor: asset?.vendor || '',
      contractor: asset?.contractor || '',
      teamNames: asset?.teamNames || '',
      associatedParts: asset?.associatedParts || '',
      additionalInfo: asset?.additionalInfo || '',
    })
    setEditOpen(true)
  }

  const closeEditModal = () => {
    if (editLoading) return
    setEditOpen(false)
    setEditError('')
    setEditingAssetId(null)
    setEditForm(emptyAssetForm)
  }

  const handleEditFormChange = (field, value) => {
    setEditForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleEditSubmit = async () => {
    if (!editingAssetId || !canEdit) return

    try {
      setEditLoading(true)
      setEditError('')

      await api.put(`/${editingAssetId}`, buildPayload(editForm), getAuthConfig())
      closeEditModal()
      await loadData(pageNum, pageSize, appliedFilters)
    } catch (err) {
      setEditError(extractErrorMessage(err, 'Không thể cập nhật thiết bị.'))
    } finally {
      setEditLoading(false)
    }
  }

  const openDeleteModal = (asset) => {
    if (!canDelete) return
    setDeleteError('')
    setDeleteTarget(asset)
    setDeleteOpen(true)
  }

  const closeDeleteModal = () => {
    if (deleteLoadingId) return
    setDeleteOpen(false)
    setDeleteError('')
    setDeleteTarget(null)
  }

  const handleDeleteConfirm = async () => {
    const id = getAssetId(deleteTarget)
    if (!id || !canDelete) return

    try {
      setDeleteLoadingId(id)
      setDeleteError('')
      await api.delete(`/${id}`, getAuthConfig())
      closeDeleteModal()
      await loadData(pageNum, pageSize, appliedFilters)
    } catch (err) {
      setDeleteError(extractErrorMessage(err, 'Không thể xóa thiết bị.'))
    } finally {
      setDeleteLoadingId(null)
    }
  }

  const handleDecommission = async (asset) => {
    const id = getAssetId(asset)
    if (!id || !canDecommission) return
    if ((asset?.status || '').toUpperCase() === 'DECOMMISSIONED') return

    try {
      setDecommissionLoadingId(id)
      setError('')
      await api.put(`/${id}/decommission`, {}, getAuthConfig())
      await loadData(pageNum, pageSize, appliedFilters)
    } catch (err) {
      setError(extractErrorMessage(err, 'Không thể ngừng sử dụng thiết bị.'))
    } finally {
      setDecommissionLoadingId(null)
    }
  }

  const handleViewChildren = (asset) => {
    const id = getAssetId(asset)
    if (!id || !canViewChildren) return

    setFilterForm({
      name: '',
      barcode: '',
      status: '',
      warrantySort: '',
    })
    setAppliedFilters({
      name: '',
      barcode: '',
      status: '',
      warrantySort: '',
    })
    setPageNum(0)
    setSearchParams({ parentId: String(id) })
  }

  const handleBackToRoot = () => {
    setFilterForm({
      name: '',
      barcode: '',
      status: '',
      warrantySort: '',
    })
    setAppliedFilters({
      name: '',
      barcode: '',
      status: '',
      warrantySort: '',
    })
    setPageNum(0)
    setSearchParams({})
  }

  const closeDetail = () => {
    setDetailOpen(false)
    setDetailError('')
    setSelectedAsset(null)
  }

  const visiblePages = useMemo(() => getVisiblePages(pageNum, totalPages), [pageNum, totalPages])

  const startItem = totalElements === 0 ? 0 : pageNum * pageSize + 1
  const endItem = totalElements === 0 ? 0 : Math.min((pageNum + 1) * pageSize, totalElements)

  const showAnyAction =
    canViewDetail || canEdit || canDelete || canViewChildren || canDecommission

  return (
    <>
      <div className="assets-page">
        <div className="assets-card">
          <div className="assets-header">
            <div className="assets-header__top">
              <div className="assets-header__intro">
                {parentId && (
                  <button
                    className="btn btn-secondary btn-back-icon"
                    onClick={handleBackToRoot}
                    title="Quay về danh sách gốc"
                    aria-label="Quay về danh sách gốc"
                    type="button"
                  >
                    <FiChevronLeft size={18} />
                  </button>
                )}

                <div className="assets-header__mini-title">{pageTitle}</div>
              </div>
            </div>

            <div className="filters-panel">
              <div className="filters-panel__header">
                <div className="filters-panel__title-wrap">
                  <div className="filters-panel__icon">
                    <FiFilter size={18} />
                  </div>

                  <div>
                    <div className="filters-panel__title">Bộ lọc thiết bị</div>
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
                  <label className="filter-label">Tên thiết bị</label>
                  <div className="search-box">
                    <FiSearch size={16} />
                    <input
                      type="text"
                      placeholder="Tìm kiếm"
                      value={filterForm.name}
                      onChange={(e) => handleFilterChange('name', e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') handleSearch()
                      }}
                    />
                  </div>
                </div>

                <div className="filter-field">
                  <label className="filter-label">Mã thiết bị</label>
                  <div className="search-box">
                    <FiTag size={16} />
                    <input
                      type="text"
                      placeholder="Nhập mã thiết bị"
                      value={filterForm.barcode}
                      onChange={(e) => handleFilterChange('barcode', e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') handleSearch()
                      }}
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
                  <label className="filter-label">Ngày bảo hành</label>
                  <select
                    className="filter-select"
                    value={filterForm.warrantySort}
                    onChange={(e) => handleFilterChange('warrantySort', e.target.value)}
                  >
                    <option value="">Mặc định</option>
                    <option value="nearest">Gần nhất</option>
                    <option value="farthest">Xa nhất</option>
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
                  {appliedFilters.name && (
                    <span className="applied-filter-chip">
                      Tên: <strong>{appliedFilters.name}</strong>
                    </span>
                  )}

                  {appliedFilters.barcode && (
                    <span className="applied-filter-chip">
                      Mã: <strong>{appliedFilters.barcode}</strong>
                    </span>
                  )}

                  {appliedFilters.status && (
                    <span className="applied-filter-chip">
                      Trạng thái: <strong>{getStatusLabel(appliedFilters.status)}</strong>
                    </span>
                  )}

                  {appliedFilters.warrantySort && (
                    <span className="applied-filter-chip">
                      Sắp xếp ngày: <strong>{getWarrantySortLabel(appliedFilters.warrantySort)}</strong>
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
          ) : assets.length === 0 ? (
            <div className="assets-message">
              Không có thiết bị nào{hasActiveFilters ? ' phù hợp với bộ lọc hiện tại.' : '.'}
            </div>
          ) : (
            <div className="list-section">
              <div className="list-section__title">
                Danh sách thiết bị
                <span className="list-badge">{totalElements}</span>
              </div>

              <div className="table-wrap">
                <table className="assets-table">
                  <thead>
                    <tr>
                      <th>STT</th>
                      <th>Mã thiết bị</th>
                      <th>Tên thiết bị</th>
                      <th>Danh mục</th>
                      <th>Trạng thái</th>
                      <th>Vị trí</th>
                      <th>Người phụ trách</th>
                      <th>Ngày bảo hành</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assets.map((asset, index) => {
                      const isDecommissioned =
                        String(asset?.status || '').toUpperCase() === 'DECOMMISSIONED'

                      return (
                        <tr key={getAssetId(asset)}>
                          <td>{pageNum * pageSize + index + 1}</td>
                          <td>{asset.barcode || getAssetId(asset) || '-'}</td>
                          <td>
                            <div className="asset-name-cell">
                              <strong>{asset.name || '-'}</strong>
                              {asset.description && <small>{asset.description}</small>}
                            </div>
                          </td>
                          <td>{asset.category || '-'}</td>
                          <td>
                            <span className={getStatusBadgeClass(asset.status)}>
                              {getStatusLabel(asset.status)}
                            </span>
                          </td>
                          <td>{asset.locationName || '-'}</td>
                          <td>{asset.assignedTo || '-'}</td>
                          <td>{asset.warrantyExpiryDate || '-'}</td>
                          <td>
                            {showAnyAction ? (
                              <div className="action-group">
                                {canViewDetail && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => handleViewDetails(asset)}
                                    title="Xem chi tiết"
                                    type="button"
                                  >
                                    <FiEye size={16} />
                                  </button>
                                )}

                                {canEdit && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => openEditModal(asset)}
                                    title="Sửa"
                                    type="button"
                                  >
                                    <FiEdit2 size={16} />
                                  </button>
                                )}

                                {canDecommission && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => handleDecommission(asset)}
                                    title={
                                      isDecommissioned
                                        ? 'Đã ngừng sử dụng'
                                        : 'Ngừng sử dụng thiết bị'
                                    }
                                    disabled={
                                      isDecommissioned ||
                                      decommissionLoadingId === getAssetId(asset)
                                    }
                                    type="button"
                                  >
                                    <FiSlash size={16} />
                                  </button>
                                )}

                                {canDelete && (
                                  <button
                                    className="icon-btn icon-btn--danger"
                                    onClick={() => openDeleteModal(asset)}
                                    title="Xóa"
                                    disabled={deleteLoadingId === getAssetId(asset)}
                                    type="button"
                                  >
                                    {deleteLoadingId === getAssetId(asset) ? (
                                      <FiAlertTriangle size={16} />
                                    ) : (
                                      <FiTrash2 size={16} />
                                    )}
                                  </button>
                                )}

                                {canViewChildren && (
                                  <button
                                    className="icon-btn"
                                    onClick={() => handleViewChildren(asset)}
                                    title="Xem thiết bị con"
                                    type="button"
                                  >
                                    <FiLayers size={16} />
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
                  Hiển thị <strong>{startItem}</strong> - <strong>{endItem}</strong> /{' '}
                  <strong>{totalElements}</strong> bản ghi
                </div>

                <div className="pagination-right">
                  {!parentId && totalPages > 0 && (
                    <div className="pagination-controls">
                      <button
                        className="page-btn"
                        disabled={pageNum === 0}
                        onClick={() => handlePageChange(pageNum - 1)}
                        type="button"
                      >
                        <FiChevronLeft size={16} />
                      </button>

                      {visiblePages.map((page, index) =>
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

                  {!parentId && (
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
                  )}
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
                <h2>Chi tiết thiết bị</h2>
                <p>Xem thông tin đầy đủ của thiết bị</p>
              </div>
              <button className="drawer-close" onClick={closeDetail} type="button">
                <FiX size={22} />
              </button>
            </div>

            {detailLoading ? (
              <div className="drawer-message">Đang tải chi tiết...</div>
            ) : detailError ? (
              <div className="drawer-message drawer-message--error">{detailError}</div>
            ) : !selectedAsset ? (
              <div className="drawer-message">Không có dữ liệu chi tiết.</div>
            ) : (
              <div className="drawer-body">
                <div className="detail-hero">
                  <div className="detail-hero__left">
                    <div className="detail-hero__icon">
                      <FiBox size={30} />
                    </div>

                    <div className="detail-hero__content">
                      <h3>{selectedAsset.name || '-'}</h3>
                      <p>{selectedAsset.description || 'Không có mô tả'}</p>

                      <div className="detail-hero__meta">
                        <span className={getStatusBadgeClass(selectedAsset.status)}>
                          {getStatusLabel(selectedAsset.status)}
                        </span>
                        <span className="hero-chip">
                          <FiTag size={14} />
                          {selectedAsset.barcode || 'Chưa có mã'}
                        </span>
                        <span className="hero-chip">
                          <FiInfo size={14} />
                          {selectedAsset.category || 'Chưa có danh mục'}
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
                      label="Mã / Barcode"
                      value={selectedAsset.barcode}
                      compact
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Danh mục"
                      value={selectedAsset.category}
                      compact
                    />
                    <DetailItem
                      icon={<FiMapPin size={16} />}
                      label="Vị trí"
                      value={selectedAsset.locationName}
                    />
                    <DetailItem
                      icon={<FiUser size={16} />}
                      label="Người phụ trách"
                      value={selectedAsset.assignedTo}
                    />
                    <DetailItem
                      icon={<FiCalendar size={16} />}
                      label="Ngày bảo hành"
                      value={selectedAsset.warrantyExpiryDate}
                    />
                    <DetailItem
                      icon={<FiTool size={16} />}
                      label="Serial Number"
                      value={selectedAsset.serialNumber}
                    />
                    <DetailItem
                      icon={<FiLayers size={16} />}
                      label="Thiết bị cha"
                      value={selectedAsset.parentAssetName}
                    />
                    <DetailItem
                      icon={<FiMapPin size={16} />}
                      label="Khu vực"
                      value={selectedAsset.area}
                    />
                  </div>
                </div>

                <div className="detail-section">
                  <div className="detail-section__title">Thông tin mở rộng</div>
                  <div className="detail-grid detail-grid--2">
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Vendor"
                      value={selectedAsset.vendor}
                    />
                    <DetailItem
                      icon={<FiUser size={16} />}
                      label="Contractor"
                      value={selectedAsset.contractor}
                    />
                    <DetailItem
                      icon={<FiUser size={16} />}
                      label="Team Names"
                      value={selectedAsset.teamNames}
                    />
                    <DetailItem
                      icon={<FiTool size={16} />}
                      label="Associated Parts"
                      value={selectedAsset.associatedParts}
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Thông tin thêm"
                      value={selectedAsset.additionalInfo}
                      full
                    />
                    <DetailItem
                      icon={<FiInfo size={16} />}
                      label="Mô tả"
                      value={selectedAsset.description}
                      full
                    />
                  </div>
                </div>
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
                <h2>Thêm thiết bị</h2>
                <p>Tạo mới asset trong hệ thống</p>
              </div>
              <button className="drawer-close" onClick={closeCreateModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {createError && <div className="assets-message assets-message--error">{createError}</div>}

              <div className="form-section">
                <div className="detail-section__title">Thông tin chính</div>
                <div className="form-grid">
                  <FormField label="Tên thiết bị">
                    <input
                      className="form-input"
                      value={createForm.name}
                      onChange={(e) => handleCreateFormChange('name', e.target.value)}
                      placeholder="Nhập tên thiết bị"
                    />
                  </FormField>

                  <FormField label="Mã / Barcode">
                    <input
                      className="form-input"
                      value={createForm.barcode}
                      onChange={(e) => handleCreateFormChange('barcode', e.target.value)}
                      placeholder="Nhập mã thiết bị"
                    />
                  </FormField>

                  <FormField label="Trạng thái">
                    <select
                      className="filter-select"
                      value={createForm.status}
                      onChange={(e) => handleCreateFormChange('status', e.target.value)}
                    >
                      {FORM_STATUS_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Danh mục">
                    <input
                      className="form-input"
                      value={createForm.category}
                      onChange={(e) => handleCreateFormChange('category', e.target.value)}
                      placeholder="Nhập danh mục"
                    />
                  </FormField>

                  <FormField label="Vị trí">
                    <input
                      className="form-input"
                      value={createForm.locationName}
                      onChange={(e) => handleCreateFormChange('locationName', e.target.value)}
                      placeholder="Nhập vị trí"
                    />
                  </FormField>

                  <FormField label="Người phụ trách">
                    <input
                      className="form-input"
                      value={createForm.assignedTo}
                      onChange={(e) => handleCreateFormChange('assignedTo', e.target.value)}
                      placeholder="Nhập người phụ trách"
                    />
                  </FormField>

                  <FormField label="Ngày bảo hành">
                    <input
                      type="date"
                      className="form-input"
                      value={createForm.warrantyExpiryDate}
                      onChange={(e) => handleCreateFormChange('warrantyExpiryDate', e.target.value)}
                    />
                  </FormField>

                  <FormField label="Serial Number">
                    <input
                      className="form-input"
                      value={createForm.serialNumber}
                      onChange={(e) => handleCreateFormChange('serialNumber', e.target.value)}
                      placeholder="Nhập serial number"
                    />
                  </FormField>

                  <FormField label="Thiết bị cha">
                    <input
                      className="form-input"
                      value={createForm.parentAssetName}
                      onChange={(e) => handleCreateFormChange('parentAssetName', e.target.value)}
                      placeholder="Nhập tên thiết bị cha"
                    />
                  </FormField>

                  <FormField label="Khu vực">
                    <input
                      className="form-input"
                      value={createForm.area}
                      onChange={(e) => handleCreateFormChange('area', e.target.value)}
                      placeholder="Nhập khu vực"
                    />
                  </FormField>
                </div>
              </div>

              <div className="form-section">
                <div className="detail-section__title">Thông tin bổ sung</div>
                <div className="form-grid">
                  <FormField label="Vendor">
                    <input
                      className="form-input"
                      value={createForm.vendor}
                      onChange={(e) => handleCreateFormChange('vendor', e.target.value)}
                      placeholder="Nhập vendor"
                    />
                  </FormField>

                  <FormField label="Contractor">
                    <input
                      className="form-input"
                      value={createForm.contractor}
                      onChange={(e) => handleCreateFormChange('contractor', e.target.value)}
                      placeholder="Nhập contractor"
                    />
                  </FormField>

                  <FormField label="Team Names">
                    <input
                      className="form-input"
                      value={createForm.teamNames}
                      onChange={(e) => handleCreateFormChange('teamNames', e.target.value)}
                      placeholder="Nhập team names"
                    />
                  </FormField>

                  <FormField label="Associated Parts">
                    <input
                      className="form-input"
                      value={createForm.associatedParts}
                      onChange={(e) => handleCreateFormChange('associatedParts', e.target.value)}
                      placeholder="Nhập parts liên quan"
                    />
                  </FormField>

                  <FormField label="Mô tả" full>
                    <textarea
                      className="form-textarea"
                      value={createForm.description}
                      onChange={(e) => handleCreateFormChange('description', e.target.value)}
                      placeholder="Nhập mô tả"
                    />
                  </FormField>

                  <FormField label="Thông tin thêm" full>
                    <textarea
                      className="form-textarea"
                      value={createForm.additionalInfo}
                      onChange={(e) => handleCreateFormChange('additionalInfo', e.target.value)}
                      placeholder="Nhập thông tin thêm"
                    />
                  </FormField>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeCreateModal} type="button">
                Hủy
              </button>
              <button
                className="btn btn-primary"
                onClick={handleCreateSubmit}
                disabled={createLoading}
                type="button"
              >
                <FiSave size={16} />
                <span>{createLoading ? 'Đang lưu...' : 'Lưu thiết bị'}</span>
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
                <h2>Cập nhật thiết bị</h2>
                <p>Chỉnh sửa thông tin asset</p>
              </div>
              <button className="drawer-close" onClick={closeEditModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {editError && <div className="assets-message assets-message--error">{editError}</div>}

              <div className="form-section">
                <div className="detail-section__title">Thông tin chính</div>
                <div className="form-grid">
                  <FormField label="Tên thiết bị">
                    <input
                      className="form-input"
                      value={editForm.name}
                      onChange={(e) => handleEditFormChange('name', e.target.value)}
                      placeholder="Nhập tên thiết bị"
                    />
                  </FormField>

                  <FormField label="Mã / Barcode">
                    <input
                      className="form-input"
                      value={editForm.barcode}
                      onChange={(e) => handleEditFormChange('barcode', e.target.value)}
                      placeholder="Nhập mã thiết bị"
                    />
                  </FormField>

                  <FormField label="Trạng thái">
                    <select
                      className="filter-select"
                      value={editForm.status}
                      onChange={(e) => handleEditFormChange('status', e.target.value)}
                    >
                      {FORM_STATUS_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField label="Danh mục">
                    <input
                      className="form-input"
                      value={editForm.category}
                      onChange={(e) => handleEditFormChange('category', e.target.value)}
                      placeholder="Nhập danh mục"
                    />
                  </FormField>

                  <FormField label="Vị trí">
                    <input
                      className="form-input"
                      value={editForm.locationName}
                      onChange={(e) => handleEditFormChange('locationName', e.target.value)}
                      placeholder="Nhập vị trí"
                    />
                  </FormField>

                  <FormField label="Người phụ trách">
                    <input
                      className="form-input"
                      value={editForm.assignedTo}
                      onChange={(e) => handleEditFormChange('assignedTo', e.target.value)}
                      placeholder="Nhập người phụ trách"
                    />
                  </FormField>

                  <FormField label="Ngày bảo hành">
                    <input
                      type="date"
                      className="form-input"
                      value={editForm.warrantyExpiryDate}
                      onChange={(e) => handleEditFormChange('warrantyExpiryDate', e.target.value)}
                    />
                  </FormField>

                  <FormField label="Serial Number">
                    <input
                      className="form-input"
                      value={editForm.serialNumber}
                      onChange={(e) => handleEditFormChange('serialNumber', e.target.value)}
                      placeholder="Nhập serial number"
                    />
                  </FormField>

                  <FormField label="Thiết bị cha">
                    <input
                      className="form-input"
                      value={editForm.parentAssetName}
                      onChange={(e) => handleEditFormChange('parentAssetName', e.target.value)}
                      placeholder="Nhập tên thiết bị cha"
                    />
                  </FormField>

                  <FormField label="Khu vực">
                    <input
                      className="form-input"
                      value={editForm.area}
                      onChange={(e) => handleEditFormChange('area', e.target.value)}
                      placeholder="Nhập khu vực"
                    />
                  </FormField>
                </div>
              </div>

              <div className="form-section">
                <div className="detail-section__title">Thông tin bổ sung</div>
                <div className="form-grid">
                  <FormField label="Vendor">
                    <input
                      className="form-input"
                      value={editForm.vendor}
                      onChange={(e) => handleEditFormChange('vendor', e.target.value)}
                      placeholder="Nhập vendor"
                    />
                  </FormField>

                  <FormField label="Contractor">
                    <input
                      className="form-input"
                      value={editForm.contractor}
                      onChange={(e) => handleEditFormChange('contractor', e.target.value)}
                      placeholder="Nhập contractor"
                    />
                  </FormField>

                  <FormField label="Team Names">
                    <input
                      className="form-input"
                      value={editForm.teamNames}
                      onChange={(e) => handleEditFormChange('teamNames', e.target.value)}
                      placeholder="Nhập team names"
                    />
                  </FormField>

                  <FormField label="Associated Parts">
                    <input
                      className="form-input"
                      value={editForm.associatedParts}
                      onChange={(e) => handleEditFormChange('associatedParts', e.target.value)}
                      placeholder="Nhập parts liên quan"
                    />
                  </FormField>

                  <FormField label="Mô tả" full>
                    <textarea
                      className="form-textarea"
                      value={editForm.description}
                      onChange={(e) => handleEditFormChange('description', e.target.value)}
                      placeholder="Nhập mô tả"
                    />
                  </FormField>

                  <FormField label="Thông tin thêm" full>
                    <textarea
                      className="form-textarea"
                      value={editForm.additionalInfo}
                      onChange={(e) => handleEditFormChange('additionalInfo', e.target.value)}
                      placeholder="Nhập thông tin thêm"
                    />
                  </FormField>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeEditModal} type="button">
                Hủy
              </button>
              <button
                className="btn btn-primary"
                onClick={handleEditSubmit}
                disabled={editLoading}
                type="button"
              >
                <FiSave size={16} />
                <span>{editLoading ? 'Đang cập nhật...' : 'Lưu thay đổi'}</span>
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
                <h2>Xóa thiết bị</h2>
                <p>Xác nhận thao tác xóa asset</p>
              </div>
              <button className="drawer-close" onClick={closeDeleteModal} type="button">
                <FiX size={22} />
              </button>
            </div>

            <div className="drawer-body">
              {deleteError && <div className="assets-message assets-message--error">{deleteError}</div>}

              <div className="delete-box">
                <div className="filters-panel__icon">
                  <FiAlertTriangle size={18} />
                </div>
                <div className="delete-box__content">
                  <h3>{deleteTarget?.name || 'Thiết bị'}</h3>
                  <p>
                    Bạn có chắc muốn xóa thiết bị này không? Thao tác này không thể hoàn tác.
                  </p>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button className="btn btn-secondary" onClick={closeDeleteModal} type="button">
                Hủy
              </button>
              <button
                className="btn btn-danger-solid"
                onClick={handleDeleteConfirm}
                disabled={Boolean(deleteLoadingId)}
                type="button"
              >
                <FiTrash2 size={16} />
                <span>{deleteLoadingId ? 'Đang xóa...' : 'Xóa thiết bị'}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}