import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import axios from 'axios';
import {
  DndContext,
  PointerSensor,
  closestCorners,
  DragOverlay,
  useDroppable,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  SortableContext,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import {
  ClipboardList,
  PlayCircle,
  PauseCircle,
  CheckCircle2,
  Clock3,
  User,
  CalendarDays,
  Wrench,
  RefreshCw,
  GripVertical,
  MoreHorizontal,
  Send,
} from 'lucide-react';
import './WorkOrdersDashboard.css';

const API_BASE_URL = 'https://emms-system-production-4239.up.railway.app/api';

const STATUS_COLUMNS = [
  { key: 'OPEN', label: 'TO DO', icon: <ClipboardList size={16} /> },
  { key: 'IN_PROGRESS', label: 'IN PROGRESS', icon: <PlayCircle size={16} /> },
  { key: 'ON_HOLD', label: 'ON HOLD', icon: <PauseCircle size={16} /> },
  { key: 'PENDING', label: 'PENDING', icon: <Clock3 size={16} /> },
  { key: 'DONE', label: 'DONE', icon: <CheckCircle2 size={16} />, done: true },
];

const PRIORITY_CLASS_MAP = {
  LOW: 'wo-board-card__priority--low',
  MEDIUM: 'wo-board-card__priority--medium',
  HIGH: 'wo-board-card__priority--high',
  URGENT: 'wo-board-card__priority--urgent',
};

const TECHNICIAN_TRANSITIONS = {
  OPEN: [
    { to: 'IN_PROGRESS', label: 'Bắt đầu', icon: <PlayCircle size={14} /> },
    { to: 'ON_HOLD', label: 'Tạm dừng', icon: <PauseCircle size={14} /> },
    
  ],
  IN_PROGRESS: [
    { to: 'ON_HOLD', label: 'Tạm dừng', icon: <PauseCircle size={14} /> },
    { to: 'PENDING', label: 'Gửi duyệt', icon: <Send size={14} /> },
  ],
  ON_HOLD: [
    { to: 'IN_PROGRESS', label: 'Tiếp tục', icon: <PlayCircle size={14} /> },
    { to: 'OPEN', label: 'Mở lại', icon: <ClipboardList size={14} /> },
  ],
  PENDING: [],
  DONE: [],
  CANCELLED: [],
};

const MANAGER_TRANSITIONS = {
  OPEN: [
    { to: 'IN_PROGRESS', label: 'Bắt đầu', icon: <PlayCircle size={14} /> },
    { to: 'ON_HOLD', label: 'Tạm dừng', icon: <PauseCircle size={14} /> },
  ],
  IN_PROGRESS: [
    { to: 'ON_HOLD', label: 'Tạm dừng', icon: <PauseCircle size={14} /> },
    { to: 'PENDING', label: 'Chờ duyệt', icon: <Clock3 size={14} /> },
  ],
  ON_HOLD: [
    { to: 'IN_PROGRESS', label: 'Tiếp tục', icon: <PlayCircle size={14} /> },
    { to: 'OPEN', label: 'Mở lại', icon: <ClipboardList size={14} /> },
  ],
  PENDING: [
    { to: 'DONE', label: 'Duyệt xong', icon: <CheckCircle2 size={14} /> },
    { to: 'IN_PROGRESS', label: 'Trả lại làm', icon: <PlayCircle size={14} /> },
    { to: 'ON_HOLD', label: 'Tạm dừng', icon: <PauseCircle size={14} /> },
  ],
  DONE: [],
  CANCELLED: [],
};

const getToken = () =>
  localStorage.getItem('accessToken') ||
  localStorage.getItem('token') ||
  localStorage.getItem('access_token') ||
  sessionStorage.getItem('accessToken') ||
  sessionStorage.getItem('token') ||
  sessionStorage.getItem('access_token') ||
  '';

const safeJsonParse = (value, fallback) => {
  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
};

const normalizeToArray = (value) => {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  if (typeof value === 'string') {
    const trimmed = value.trim();
    return trimmed ? [trimmed] : [];
  }
  return [];
};

const normalizeGrant = (value) => {
  if (!value) return '';
  let raw = String(value).trim().toUpperCase();
  if (raw.startsWith('ROLE_')) raw = raw.substring(5);
  return raw;
};

const extractGrantValue = (item) => {
  if (!item) return null;
  if (typeof item === 'string') return item.trim();
  if (typeof item === 'object') {
    return item.authority || item.name || item.code || item.role || item.permission || null;
  }
  return null;
};

const getUserContext = () => {
  const userRaw = localStorage.getItem('user') || sessionStorage.getItem('user');
  const rolesRaw = localStorage.getItem('roles') || sessionStorage.getItem('roles');
  const authoritiesRaw = localStorage.getItem('authorities') || sessionStorage.getItem('authorities');
  const permissionsRaw = localStorage.getItem('permissions') || sessionStorage.getItem('permissions');
  const roleRaw = localStorage.getItem('role') || sessionStorage.getItem('role') || '';

  const user = safeJsonParse(userRaw, {});
  const roles = normalizeToArray(safeJsonParse(rolesRaw, rolesRaw || user?.roles || []));
  const authorities = normalizeToArray(
    safeJsonParse(authoritiesRaw, authoritiesRaw || user?.authorities || []),
  );
  const permissions = normalizeToArray(
    safeJsonParse(permissionsRaw, permissionsRaw || user?.permissions || []),
  );
  const singleRole = normalizeToArray(roleRaw);

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
    .filter(Boolean);

  return { user, grants: Array.from(new Set(merged)) };
};

const hasAnyGrant = (grants, expected = []) => {
  const normalizedUserGrants = (grants || []).map(normalizeGrant);
  const normalizedExpected = expected.map(normalizeGrant);
  return normalizedExpected.some((item) => normalizedUserGrants.includes(item));
};

const normalizeStatus = (status) => String(status || '').trim().toUpperCase();
const normalizePriority = (priority) => String(priority || 'MEDIUM').trim().toUpperCase();

const extractErrorMessage = (err, fallback) => {
  if (!err) return fallback;

  if (err.response) {
    const data = err.response.data;
    if (typeof data === 'string' && data.trim()) return `HTTP ${err.response.status}: ${data}`;
    if (data?.message) return `HTTP ${err.response.status}: ${data.message}`;
    if (data?.error) return `HTTP ${err.response.status}: ${data.error}`;
    return `HTTP ${err.response.status}: ${fallback}`;
  }

  if (err.request) {
    return 'Không nhận được phản hồi từ backend. Kiểm tra backend/CORS/network.';
  }

  return err.message || fallback;
};

const formatDate = (value) => {
  if (!value) return 'Chưa có hạn';
  const str = String(value);
  if (str.includes('T')) return str.split('T')[0];
  return str.length >= 10 ? str.slice(0, 10) : str;
};

const compareWorkOrders = (a, b) => {
  const aTime = a?.createdAt || a?.dateCreated || a?.updatedAt || '';
  const bTime = b?.createdAt || b?.dateCreated || b?.updatedAt || '';
  const aMillis = aTime ? new Date(aTime).getTime() : 0;
  const bMillis = bTime ? new Date(bTime).getTime() : 0;

  if (aMillis !== bMillis) return bMillis - aMillis;
  return Number(b?.id || 0) - Number(a?.id || 0);
};

const getAssetName = (wo) => wo?.asset?.name || wo?.assetName || '-';

const getAssigneeName = (wo) => {
  const user = wo?.assignedTo;
  if (!user) {
    return wo?.assignedToName || 'Chưa phân công';
  }

  return (
    user.fullName ||
    [user.firstName, user.lastName].filter(Boolean).join(' ') ||
    user.username ||
    user.email ||
    'Chưa phân công'
  );
};

const getAllowedActions = (workOrder, context) => {
  const currentStatus = normalizeStatus(workOrder?.status);

  if (context.isManager) {
    return MANAGER_TRANSITIONS[currentStatus] || [];
  }

  if (context.isTechnician) {
    return TECHNICIAN_TRANSITIONS[currentStatus] || [];
  }

  return [];
};

const getFallbackActions = (workOrder, context) => {
  const currentStatus = normalizeStatus(workOrder?.status);

  if (currentStatus === 'DONE') {
    return [
      {
        to: 'DONE',
        label: 'Đã hoàn tất',
        icon: <CheckCircle2 size={14} />,
        disabled: true,
        reason: 'Work order đã hoàn tất.',
      },
    ];
  }

  if (currentStatus === 'PENDING') {
    if (context.isManager) {
      return [
        {
          to: 'PENDING',
          label: 'Chờ xử lý',
          icon: <Clock3 size={14} />,
          disabled: true,
          reason: 'Đang chờ quản lý xử lý.',
        },
      ];
    }

    return [
      {
        to: 'PENDING',
        label: 'Chờ duyệt',
        icon: <Clock3 size={14} />,
        disabled: true,
        reason: 'Work order đang chờ quản lý duyệt.',
      },
    ];
  }

  if (context.isTechnician) {
    return [
      {
        to: currentStatus,
        label: 'Không có thao tác',
        icon: <MoreHorizontal size={14} />,
        disabled: true,
        reason: 'Không có thao tác hợp lệ cho trạng thái hiện tại.',
      },
    ];
  }

  return [
    {
      to: currentStatus || 'UNKNOWN',
      label: 'Không có thao tác',
      icon: <MoreHorizontal size={14} />,
      disabled: true,
      reason: 'Không có thao tác hợp lệ cho trạng thái hiện tại.',
    },
  ];
};

const canDragItem = (workOrder, context, actions) => {
  if (!actions?.length) return false;
  if (context.isManager) return true;
  return context.isTechnician;
};

const resolveDropStatus = (overId) => {
  const value = String(overId || '');
  if (value.startsWith('column-')) return value.replace('column-', '');
  return null;
};

export default function WorkOrdersDashboard() {
  const [workOrders, setWorkOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [changingId, setChangingId] = useState(null);
  const [error, setError] = useState('');
  const [activeId, setActiveId] = useState(null);
  const [contextMenu, setContextMenu] = useState(null);

  const token = getToken();
  const { grants } = useMemo(() => getUserContext(), [token]);

  const isAdmin = hasAnyGrant(grants, ['ADMIN']);
  const isTechnicalManager = hasAnyGrant(grants, ['TECHNICAL_MANAGER']);
  const isTechnician = hasAnyGrant(grants, ['TECHNICIAN']);
  const isOperator = hasAnyGrant(grants, ['OPERATOR']);
  const isManager = isAdmin || isTechnicalManager;

  const menuRef = useRef(null);
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 6 } }));

  const axiosInstance = useMemo(
    () =>
      axios.create({
        baseURL: API_BASE_URL,
        timeout: 15000,
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
      }),
    [token],
  );

  const buildActionContext = useCallback(
    () => ({ isManager, isTechnician, isOperator }),
    [isManager, isTechnician, isOperator],
  );

  const fetchWorkOrders = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const response = await axiosInstance.get('/work-orders/my');
      setWorkOrders(Array.isArray(response?.data) ? response.data : []);
    } catch (err) {
      setError(extractErrorMessage(err, 'Không tải được danh sách work order'));
      setWorkOrders([]);
    } finally {
      setLoading(false);
    }
  }, [axiosInstance]);

  useEffect(() => {
    fetchWorkOrders();
  }, [fetchWorkOrders]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setContextMenu(null);
      }
    };

    const handleScrollClose = () => setContextMenu(null);

    const handleEscape = (event) => {
      if (event.key === 'Escape') setContextMenu(null);
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('scroll', handleScrollClose, true);
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('scroll', handleScrollClose, true);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  const groupedWorkOrders = useMemo(() => {
    const groups = { OPEN: [], IN_PROGRESS: [], ON_HOLD: [], PENDING: [], DONE: [] };

    workOrders.forEach((wo) => {
      const status = normalizeStatus(wo?.status);
      if (groups[status]) groups[status].push(wo);
    });

    Object.keys(groups).forEach((key) => groups[key].sort(compareWorkOrders));
    return groups;
  }, [workOrders]);

  const activeWorkOrder = useMemo(() => {
    if (!activeId) return null;
    return workOrders.find((item) => String(item.id) === String(activeId)) || null;
  }, [activeId, workOrders]);

  const handleChangeStatus = useCallback(
    async (workOrder, nextStatus) => {
      if (!workOrder?.id || !nextStatus) return;

      const actions = getAllowedActions(workOrder, buildActionContext());
      const allowed = actions.some(
        (action) => normalizeStatus(action.to) === normalizeStatus(nextStatus),
      );

      if (!allowed) {
        setError('Bạn không được phép chuyển trạng thái này.');
        return;
      }

      if (normalizeStatus(workOrder.status) === normalizeStatus(nextStatus)) return;

      try {
        setChangingId(workOrder.id);
        setError('');
        setContextMenu(null);

        await axiosInstance.patch(`/work-orders/${workOrder.id}/status`, {
          status: nextStatus,
          feedback: null,
        });

        setWorkOrders((prev) =>
          prev.map((item) =>
            item.id === workOrder.id
              ? {
                  ...item,
                  status: nextStatus,
                  completedOn:
                    nextStatus === 'DONE' || nextStatus === 'PENDING'
                      ? item.completedOn || new Date().toISOString()
                      : null,
                }
              : item,
          ),
        );
      } catch (err) {
        setError(extractErrorMessage(err, 'Cập nhật trạng thái thất bại'));
        await fetchWorkOrders();
      } finally {
        setChangingId(null);
      }
    },
    [axiosInstance, buildActionContext, fetchWorkOrders],
  );

  const handleDragStart = useCallback((event) => {
    if (!event?.active?.id) return;
    setActiveId(String(event.active.id));
    setContextMenu(null);
  }, []);

  const handleDragEnd = useCallback(
    async (event) => {
      const { active, over } = event;
      setActiveId(null);

      if (!active || !over) return;

      const dragged = workOrders.find((item) => String(item.id) === String(active.id));
      if (!dragged) return;

      const targetStatus = resolveDropStatus(over.id);
      if (!targetStatus) return;
      if (normalizeStatus(dragged.status) === normalizeStatus(targetStatus)) return;

      const allowedActions = getAllowedActions(dragged, buildActionContext());
      const allowedToDrop = allowedActions.some(
        (action) => normalizeStatus(action.to) === normalizeStatus(targetStatus),
      );

      if (!allowedToDrop) {
        setError('Không thể kéo thả sang cột này do không đúng quyền hoặc không đúng luồng.');
        return;
      }

      await handleChangeStatus(dragged, targetStatus);
    },
    [workOrders, buildActionContext, handleChangeStatus],
  );

  return (
    <div className="wo-jira-page">
      <div className="wo-jira-toolbar">
        <div className="wo-jira-toolbar__left">
          <h2>Work Orders Dashboard</h2>
          <span className="wo-jira-toolbar__summary">
            {isManager
              ? `Hiển thị work orders của bạn (${workOrders.length})`
              : `Hiển thị work orders được assign cho bạn (${workOrders.length})`}
          </span>
        </div>

        <div className="wo-jira-toolbar__right">
          <button
            type="button"
            className="wo-board-card__mini-btn"
            onClick={fetchWorkOrders}
            disabled={loading}
          >
            <RefreshCw size={14} style={{ marginRight: 6 }} />
            {loading ? 'Đang tải...' : 'Làm mới'}
          </button>
        </div>
      </div>

      {error ? <div className="wo-dashboard-error">{error}</div> : null}

      <DndContext
        sensors={sensors}
        collisionDetection={closestCorners}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
        onDragCancel={() => setActiveId(null)}
      >
        <div className="wo-jira-board wo-jira-board--five-cols">
          {STATUS_COLUMNS.map((column) => {
            const items = groupedWorkOrders[column.key] || [];

            return (
              <KanbanColumn
                key={column.key}
                column={column}
                items={items}
                activeWorkOrder={activeWorkOrder}
                actionContext={buildActionContext()}
              >
                {items.length === 0 ? (
                  <div className="wo-jira-column__empty">Không có work order nào</div>
                ) : (
                  <SortableContext
                    items={items.map((item) => String(item.id))}
                    strategy={verticalListSortingStrategy}
                  >
                    {items.map((wo) => (
                      <SortableWorkOrderCard
                        key={wo.id}
                        workOrder={wo}
                        changing={changingId === wo.id}
                        onChangeStatus={handleChangeStatus}
                        actionContext={buildActionContext()}
                        contextMenu={contextMenu}
                        setContextMenu={setContextMenu}
                        menuRef={menuRef}
                      />
                    ))}
                  </SortableContext>
                )}
              </KanbanColumn>
            );
          })}
        </div>

        <DragOverlay>
          {activeWorkOrder ? <WorkOrderCardPreview workOrder={activeWorkOrder} /> : null}
        </DragOverlay>
      </DndContext>
    </div>
  );
}

function KanbanColumn({ column, items, children, activeWorkOrder, actionContext }) {
  const canDropHere = useMemo(() => {
    if (!activeWorkOrder) return true;
    const actions = getAllowedActions(activeWorkOrder, actionContext);
    return actions.some((action) => normalizeStatus(action.to) === normalizeStatus(column.key));
  }, [activeWorkOrder, actionContext, column.key]);

  const { setNodeRef, isOver } = useDroppable({
    id: `column-${column.key}`,
    data: { status: column.key, dropDisabled: !canDropHere },
    disabled: !canDropHere,
  });

  return (
    <div
      ref={setNodeRef}
      className={`wo-jira-column ${column.done ? 'wo-jira-column--done' : ''} ${
        !canDropHere ? 'wo-jira-column--drop-disabled' : ''
      } ${isOver && canDropHere ? 'wo-jira-column--drop-active' : ''}`}
    >
      <div className={`wo-jira-column__header ${column.done ? 'wo-jira-column__header--done' : ''}`}>
        <div className={`wo-jira-column__title ${column.done ? 'wo-jira-column__title--done' : ''}`}>
          {column.icon}
          <span>{column.label}</span>
        </div>
        <span className={`wo-jira-column__count ${column.done ? 'wo-jira-column__count--done' : ''}`}>
          {items.length}
        </span>
      </div>

      <div className="wo-jira-column__body">{children}</div>
    </div>
  );
}

function SortableWorkOrderCard({
  workOrder,
  changing,
  onChangeStatus,
  actionContext,
  contextMenu,
  setContextMenu,
  menuRef,
}) {
  const actions = getAllowedActions(workOrder, actionContext);
  const displayActions = actions.length > 0 ? actions : getFallbackActions(workOrder, actionContext);
  const dragAllowed = canDragItem(workOrder, actionContext, actions);

  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: String(workOrder.id),
    disabled: !dragAllowed || changing,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.65 : 1,
  };

  const priority = normalizePriority(workOrder?.priority);
  const assigneeName = getAssigneeName(workOrder);
  const dueDateText = formatDate(workOrder?.dueDate);
  const assetName = getAssetName(workOrder);
  const description = workOrder?.description?.trim() || 'Không có mô tả';
  const menuOpen = contextMenu?.workOrderId === workOrder.id;

  const openContextMenu = (event) => {
    event.preventDefault();
    if (!actions.length || changing) return;
    setContextMenu({ workOrderId: workOrder.id, x: event.clientX, y: event.clientY, actions });
  };

  const openMoreMenu = (event) => {
    event.preventDefault();
    event.stopPropagation();
    if (!actions.length || changing) return;
    const rect = event.currentTarget.getBoundingClientRect();
    setContextMenu({ workOrderId: workOrder.id, x: rect.left, y: rect.bottom + 6, actions });
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      onContextMenu={openContextMenu}
      className={`wo-board-card ${normalizeStatus(workOrder?.status) === 'DONE' ? 'wo-board-card--done' : ''} ${
        menuOpen ? 'wo-board-card--menu-open' : ''
      }`}
    >
      <div className="wo-board-card__top">
        <div className="wo-board-card__title-wrap">
          {dragAllowed ? (
            <button
              type="button"
              className="wo-board-card__drag-handle"
              aria-label="Kéo thả"
              {...attributes}
              {...listeners}
            >
              <GripVertical size={16} />
            </button>
          ) : (
            <div className="wo-board-card__drag-placeholder" />
          )}

          <div className="wo-board-card__title-box">
            <div className="wo-board-card__title">
              {workOrder?.title || `Work Order #${workOrder?.id}`}
            </div>
            <div className="wo-board-card__id">#{workOrder?.id}</div>
          </div>
        </div>

        <div className="wo-board-card__top-right">
          <span className={`wo-board-card__priority ${PRIORITY_CLASS_MAP[priority] || ''}`}>
            {priority}
          </span>
          <button
            type="button"
            className="wo-board-card__more-btn"
            onClick={openMoreMenu}
            disabled={!actions.length || changing}
            title={!actions.length ? 'Không có thao tác hợp lệ' : 'Xem thêm thao tác'}
          >
            <MoreHorizontal size={16} />
          </button>
        </div>
      </div>

      <div className="wo-board-card__desc">{description}</div>

      <div className="wo-board-card__meta-list">
        <div className="wo-board-card__meta-row">
          <Wrench size={14} />
          <span>{assetName}</span>
        </div>
        <div className="wo-board-card__meta-row">
          <User size={14} />
          <span>{assigneeName}</span>
        </div>
        <div className="wo-board-card__meta-row">
          <CalendarDays size={14} />
          <span>{dueDateText}</span>
        </div>
      </div>

      <div className="wo-board-card__actions-inline">
        {displayActions.slice(0, 2).map((action) => (
          <button
            key={`${workOrder.id}-${action.to}-${action.label}`}
            type="button"
            className={`wo-board-card__mini-btn ${action.disabled ? 'wo-board-card__mini-btn--disabled' : ''}`}
            onClick={() => {
              if (!action.disabled) onChangeStatus(workOrder, action.to);
            }}
            disabled={changing || action.disabled}
            title={action.reason || ''}
          >
            {action.icon}
            <span>{action.label}</span>
          </button>
        ))}
      </div>

      {!actions.length && displayActions[0]?.reason ? (
        <div className="wo-board-card__action-note">{displayActions[0].reason}</div>
      ) : null}

      {menuOpen ? (
        <div
          ref={menuRef}
          className="wo-card-menu"
          style={{ top: contextMenu.y, left: contextMenu.x }}
        >
          {contextMenu.actions.map((action) => (
            <button
              key={`${workOrder.id}-${action.to}-menu`}
              type="button"
              className="wo-card-menu__item"
              onClick={() => onChangeStatus(workOrder, action.to)}
              disabled={changing}
            >
              {action.icon}
              <span>{action.label}</span>
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}

function WorkOrderCardPreview({ workOrder }) {
  const priority = normalizePriority(workOrder?.priority);

  return (
    <div className="wo-board-card wo-board-card--drag-preview">
      <div className="wo-board-card__top">
        <div className="wo-board-card__title-wrap">
          <div className="wo-board-card__drag-handle">
            <GripVertical size={16} />
          </div>
          <div className="wo-board-card__title-box">
            <div className="wo-board-card__title">
              {workOrder?.title || `Work Order #${workOrder?.id}`}
            </div>
            <div className="wo-board-card__id">#{workOrder?.id}</div>
          </div>
        </div>
        <span className={`wo-board-card__priority ${PRIORITY_CLASS_MAP[priority] || ''}`}>
          {priority}
        </span>
      </div>
      <div className="wo-board-card__desc">
        {workOrder?.description?.trim() || 'Không có mô tả'}
      </div>
    </div>
  );
}