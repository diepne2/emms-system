import React, { useEffect, useMemo, useState } from "react";
import axios from "axios";
import "./Part.css";

const API_BASE = 'https://emms-system-production-4239.up.railway.app'
const PARTS_API = `${API_BASE}/parts`;

const emptyForm = {
  name: "",
  description: "",
  category: "",
  barcode: "",
  vendor: "",
  locationName: "",
  assignedTo: "",
  quantity: 0,
  cost: "",
  lastPrice: "",
  minimumQuantity: "",
  consumable: false,
};

function getToken() {
  return (
    localStorage.getItem("accessToken") ||
    localStorage.getItem("token") ||
    localStorage.getItem("jwt") ||
    sessionStorage.getItem("accessToken") ||
    sessionStorage.getItem("token") ||
    sessionStorage.getItem("jwt") ||
    ""
  );
}

function buildAxiosConfig() {
  const token = getToken();
  return {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  };
}

function normalizeNumberInput(value) {
  if (value === "" || value === null || value === undefined) return "";
  const num = Number(value);
  return Number.isNaN(num) ? "" : num;
}

function formatMoney(value) {
  if (value === null || value === undefined || value === "") return "-";
  const num = Number(value);
  if (Number.isNaN(num)) return String(value);
  return new Intl.NumberFormat("vi-VN", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(num);
}

function formatText(value) {
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

function safeJsonParse(value, fallback = null) {
  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
}

function normalizeToArray(value) {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  if (typeof value === "string") {
    const trimmed = value.trim();
    return trimmed ? [trimmed] : [];
  }
  return [];
}

function normalizeGrant(value) {
  if (!value) return "";
  let raw = String(value).trim().toUpperCase();
  if (!raw) return "";

  if (raw.startsWith("ROLE_")) {
    raw = raw.substring(5);
  }

  return raw;
}

function extractGrantValue(item) {
  if (!item) return null;

  if (typeof item === "string") return item.trim();

  if (typeof item === "object") {
    return (
      item.authority ||
      item.name ||
      item.code ||
      item.role ||
      item.permission ||
      null
    );
  }

  return null;
}

function getUserContext() {
  const userRaw = localStorage.getItem("user") || sessionStorage.getItem("user");
  const rolesRaw =
    localStorage.getItem("roles") || sessionStorage.getItem("roles");
  const authoritiesRaw =
    localStorage.getItem("authorities") ||
    sessionStorage.getItem("authorities");
  const permissionsRaw =
    localStorage.getItem("permissions") ||
    sessionStorage.getItem("permissions");
  const roleRaw = localStorage.getItem("role") || sessionStorage.getItem("role") || "";

  const user = safeJsonParse(userRaw, {});

  const roles = normalizeToArray(safeJsonParse(rolesRaw, rolesRaw || user?.roles || []));
  const authorities = normalizeToArray(
    safeJsonParse(authoritiesRaw, authoritiesRaw || user?.authorities || [])
  );
  const permissions = normalizeToArray(
    safeJsonParse(permissionsRaw, permissionsRaw || user?.permissions || [])
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
    user?.role,
    user?.roleCode,
    user?.authority,
  ]
    .map(extractGrantValue)
    .filter(Boolean)
    .map(normalizeGrant)
    .filter(Boolean);

  return {
    user,
    grants: Array.from(new Set(merged)),
  };
}

function hasAnyGrant(grants, expected = []) {
  if (!Array.isArray(grants) || !expected.length) return false;

  const normalizedUserGrants = grants.map(normalizeGrant);
  const normalizedExpected = expected.map(normalizeGrant);

  return normalizedExpected.some((item) =>
    normalizedUserGrants.includes(item)
  );
}

function extractErrorMessage(err, fallback) {
  if (!err) return fallback;

  if (err.response) {
    const data = err.response.data;

    if (typeof data === "string" && data.trim()) {
      return `HTTP ${err.response.status}: ${data}`;
    }

    if (data?.message) {
      return `HTTP ${err.response.status}: ${data.message}`;
    }

    if (data?.error) {
      return `HTTP ${err.response.status}: ${data.error}`;
    }

    return `HTTP ${err.response.status}: ${fallback}`;
  }

  if (err.request) {
    return "Không nhận được phản hồi từ backend. Kiểm tra backend/CORS/network.";
  }

  return err.message || fallback;
}

function getPartId(part) {
  return part?.id ?? part?.partId ?? null;
}

function normalizePayload(form) {
  return {
    name: form.name?.trim(),
    description: form.description?.trim() || null,
    category: form.category?.trim() || null,
    barcode: form.barcode?.trim() || null,
    vendor: form.vendor?.trim() || null,
    locationName: form.locationName?.trim() || null,
    assignedTo: form.assignedTo?.trim() || null,
    quantity:
      form.quantity === "" || form.quantity === null ? 0 : Number(form.quantity),
    cost: form.cost === "" ? null : Number(form.cost),
    lastPrice: form.lastPrice === "" ? null : Number(form.lastPrice),
    minimumQuantity:
      form.minimumQuantity === "" ? null : Number(form.minimumQuantity),
    consumable: !!form.consumable,
  };
}

function getStockMeta(part) {
  const qty = Number(part?.quantity || 0);
  const minQty = Number(part?.minimumQuantity || 0);

  if (qty <= 0) {
    return { text: "Hết hàng", cls: "badge--danger" };
  }
  if (minQty > 0 && qty <= minQty) {
    return { text: "Sắp thiếu", cls: "badge--warning" };
  }
  return { text: "Còn hàng", cls: "badge--success" };
}

function getConsumableMeta(part) {
  return part?.consumable
    ? { text: "Consumable", cls: "badge--info" }
    : { text: "Non-consumable", cls: "badge--default" };
}

/* icons */
function SearchIcon() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2">
      <circle cx="11" cy="11" r="7" />
      <path d="M21 21l-4.35-4.35" />
    </svg>
  );
}

function FilterIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M3 5h18l-7 8v5l-4 2v-7L3 5z" />
    </svg>
  );
}

function PlusIcon() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}

function RefreshIcon() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M21 12a9 9 0 1 1-2.64-6.36" />
      <path d="M21 3v6h-6" />
    </svg>
  );
}

function EyeIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

function EditIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.12 2.12 0 1 1 3 3L7 19l-4 1 1-4 12.5-12.5Z" />
    </svg>
  );
}

function DeleteIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M3 6h18" />
      <path d="M8 6V4h8v2" />
      <path d="M19 6l-1 14H6L5 6" />
      <path d="M10 11v6M14 11v6" />
    </svg>
  );
}

function ArrowUpIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 19V5" />
      <path d="M5 12l7-7 7 7" />
    </svg>
  );
}

function ArrowDownIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 5v14" />
      <path d="M19 12l-7 7-7-7" />
    </svg>
  );
}

function CloseIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M18 6 6 18M6 6l12 12" />
    </svg>
  );
}

function BoxIcon() {
  return (
    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
      <path d="m3.3 7 8.7 5 8.7-5" />
      <path d="M12 22V12" />
    </svg>
  );
}

export default function Part() {
  const { grants } = useMemo(() => getUserContext(), []);

  const [parts, setParts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [pageError, setPageError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("ALL");
  const [stockFilter, setStockFilter] = useState("ALL");

  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const [selectedPart, setSelectedPart] = useState(null);

  const [showFormModal, setShowFormModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);

  const [stockModalOpen, setStockModalOpen] = useState(false);
  const [stockMode, setStockMode] = useState("increase");
  const [stockAmount, setStockAmount] = useState("");
  const [stockTarget, setStockTarget] = useState(null);

  const [deleteTarget, setDeleteTarget] = useState(null);

  const createAllowed = hasAnyGrant(grants, ["ADMIN", "TECHNICAL_MANAGER"]);
  const editAllowed = hasAnyGrant(grants, ["ADMIN", "TECHNICAL_MANAGER"]);
  const deleteAllowed = hasAnyGrant(grants, ["ADMIN", "TECHNICAL_MANAGER"]);
  const stockAllowed = hasAnyGrant(grants, ["ADMIN", "TECHNICAL_MANAGER"]);

  useEffect(() => {
    loadParts();
  }, []);

  useEffect(() => {
    setCurrentPage(1);
  }, [search, categoryFilter, stockFilter, pageSize]);

  async function loadParts() {
    try {
      setLoading(true);
      setPageError("");
      setSuccessMessage("");

      const res = await axios.get(PARTS_API, buildAxiosConfig());
      const data = Array.isArray(res.data) ? res.data : [];
      setParts(data);
    } catch (err) {
      if (err?.response?.status === 403) {
        setParts([]);
        setPageError("Tài khoản hiện tại không có quyền xem vật tư.");
      } else if (err?.response?.status === 401) {
        setParts([]);
        setPageError("Bạn chưa đăng nhập hoặc token đã hết hạn.");
      } else {
        setPageError(extractErrorMessage(err, "Không tải được danh sách vật tư."));
      }
    } finally {
      setLoading(false);
    }
  }

  function resetForm() {
    setForm(emptyForm);
    setEditingId(null);
  }

  function openCreateModal() {
    if (!createAllowed) {
      setPageError("Bạn không có quyền thêm vật tư.");
      return;
    }
    resetForm();
    setShowFormModal(true);
  }

  function startEdit(part) {
    if (!editAllowed) {
      setPageError("Bạn không có quyền cập nhật vật tư.");
      return;
    }

    const id = getPartId(part);
    if (!id) {
      setPageError("Không xác định được ID của vật tư.");
      return;
    }

    setEditingId(id);
    setForm({
      name: part.name || "",
      description: part.description || "",
      category: part.category || "",
      barcode: part.barcode || "",
      vendor: part.vendor || "",
      locationName: part.locationName || "",
      assignedTo: part.assignedTo || "",
      quantity: part.quantity ?? 0,
      cost: part.cost ?? "",
      lastPrice: part.lastPrice ?? "",
      minimumQuantity: part.minimumQuantity ?? "",
      consumable: !!part.consumable,
    });
    setShowFormModal(true);
  }

  function handleChange(e) {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]:
        type === "checkbox"
          ? checked
          : type === "number"
          ? normalizeNumberInput(value)
          : value,
    }));
  }

  async function handleSubmit(e) {
    e.preventDefault();

    if (editingId && !editAllowed) {
      setPageError("Bạn không có quyền cập nhật vật tư.");
      return;
    }

    if (!editingId && !createAllowed) {
      setPageError("Bạn không có quyền tạo vật tư.");
      return;
    }

    if (!form.name?.trim()) {
      setPageError("Tên vật tư không được để trống.");
      return;
    }

    const payload = normalizePayload(form);

    if (payload.quantity < 0) {
      setPageError("Quantity không được âm.");
      return;
    }

    if (payload.cost !== null && payload.cost < 0) {
      setPageError("Cost không được âm.");
      return;
    }

    if (payload.lastPrice !== null && payload.lastPrice < 0) {
      setPageError("Last price không được âm.");
      return;
    }

    try {
      setSubmitting(true);
      setPageError("");
      setSuccessMessage("");

      if (editingId) {
        await axios.patch(`${PARTS_API}/${editingId}`, payload, buildAxiosConfig());
        setSuccessMessage("Cập nhật vật tư thành công.");
      } else {
        await axios.post(PARTS_API, payload, buildAxiosConfig());
        setSuccessMessage("Tạo vật tư thành công.");
      }

      setShowFormModal(false);
      resetForm();
      await loadParts();
    } catch (err) {
      setPageError(extractErrorMessage(err, "Lưu vật tư thất bại."));
    } finally {
      setSubmitting(false);
    }
  }

  async function confirmDelete() {
    if (!deleteAllowed) {
      setPageError("Bạn không có quyền xóa vật tư.");
      return;
    }
    if (!deleteTarget) return;

    const id = getPartId(deleteTarget);
    if (!id) {
      setPageError("Không xác định được ID của vật tư.");
      return;
    }

    try {
      setSubmitting(true);
      setPageError("");
      setSuccessMessage("");

      await axios.delete(`${PARTS_API}/${id}`, buildAxiosConfig());
      setSuccessMessage("Xóa vật tư thành công.");

      if (selectedPart && getPartId(selectedPart) === id) {
        setSelectedPart(null);
      }

      setDeleteTarget(null);
      await loadParts();
    } catch (err) {
      setPageError(extractErrorMessage(err, "Xóa vật tư thất bại."));
    } finally {
      setSubmitting(false);
    }
  }

  function openStockModal(part, mode) {
    if (!stockAllowed) {
      setPageError("Bạn không có quyền điều chỉnh tồn kho.");
      return;
    }

    setStockTarget(part);
    setStockMode(mode);
    setStockAmount("");
    setStockModalOpen(true);
  }

  async function submitStockAdjust() {
    if (!stockAllowed) {
      setPageError("Bạn không có quyền điều chỉnh tồn kho.");
      return;
    }
    if (!stockTarget) return;

    const id = getPartId(stockTarget);
    if (!id) {
      setPageError("Không xác định được ID của vật tư.");
      return;
    }

    const amount = Number(stockAmount);

    if (!amount || amount <= 0) {
      setPageError("Số lượng điều chỉnh phải lớn hơn 0.");
      return;
    }

    try {
      setSubmitting(true);
      setPageError("");
      setSuccessMessage("");

      const endpoint =
        stockMode === "increase" ? "increase-stock" : "decrease-stock";

      await axios.put(`${PARTS_API}/${id}/${endpoint}`, null, {
        ...buildAxiosConfig(),
        params: { amount },
      });

      setSuccessMessage(
        stockMode === "increase"
          ? "Tăng tồn kho thành công."
          : "Giảm tồn kho thành công."
      );

      setStockModalOpen(false);
      setStockTarget(null);
      setStockAmount("");
      await loadParts();
    } catch (err) {
      setPageError(extractErrorMessage(err, "Điều chỉnh tồn kho thất bại."));
    } finally {
      setSubmitting(false);
    }
  }

  const categories = useMemo(() => {
    const set = new Set();
    parts.forEach((p) => {
      if (p.category) set.add(String(p.category).trim());
    });
    return ["ALL", ...Array.from(set).sort()];
  }, [parts]);

  const filteredParts = useMemo(() => {
    let data = [...parts];

    const keyword = search.trim().toLowerCase();
    if (keyword) {
      data = data.filter((p) =>
        [
          p.name,
          p.description,
          p.category,
          p.barcode,
          p.vendor,
          p.locationName,
          p.assignedTo,
        ]
          .filter(Boolean)
          .some((field) => String(field).toLowerCase().includes(keyword))
      );
    }

    if (categoryFilter !== "ALL") {
      data = data.filter(
        (p) => String(p.category || "").trim() === categoryFilter
      );
    }

    if (stockFilter === "LOW") {
      data = data.filter((p) => {
        const qty = Number(p.quantity || 0);
        const minQty = Number(p.minimumQuantity || 0);
        return minQty > 0 && qty <= minQty;
      });
    }

    if (stockFilter === "OUT") {
      data = data.filter((p) => Number(p.quantity || 0) <= 0);
    }

    return data.sort((a, b) => {
      const an = String(a.name || "").toLowerCase();
      const bn = String(b.name || "").toLowerCase();
      return an.localeCompare(bn);
    });
  }, [parts, search, categoryFilter, stockFilter]);

  const totalPages = Math.max(1, Math.ceil(filteredParts.length / pageSize));
  const safeCurrentPage = Math.min(currentPage, totalPages);
  const startIndex = (safeCurrentPage - 1) * pageSize;
  const pagedParts = filteredParts.slice(startIndex, startIndex + pageSize);

  const stats = useMemo(() => {
    const total = parts.length;
    const totalStock = parts.reduce(
      (sum, p) => sum + Number(p.quantity || 0),
      0
    );
    const lowStock = parts.filter((p) => {
      const qty = Number(p.quantity || 0);
      const minQty = Number(p.minimumQuantity || 0);
      return minQty > 0 && qty <= minQty;
    }).length;
    const outOfStock = parts.filter((p) => Number(p.quantity || 0) <= 0).length;

    return { total, totalStock, lowStock, outOfStock };
  }, [parts]);

  return (
    <div className="assets-page">
      <div className="assets-card">
        {pageError && (
          <div className="assets-message assets-message--error">
            {pageError}
          </div>
        )}
        {successMessage && (
          <div className="assets-message">{successMessage}</div>
        )}

        <div className="assets-header">
          <div className="assets-header__top">
            <div className="assets-header__intro">
              <div className="filters-panel__icon">
                <BoxIcon />
              </div>
              <div>
                <h1 className="assets-header__mini-title">Danh mục vật tư</h1>
              </div>
            </div>

            <button
              type="button"
              className="btn btn-primary btn-create-header"
              onClick={openCreateModal}
              disabled={!createAllowed}
              title={createAllowed ? "Thêm mới" : "Bạn không có quyền thêm vật tư"}
            >
              <PlusIcon />
              <span>Thêm mới</span>
            </button>
          </div>
        </div>

        <div className="filters-panel">
          <div className="filters-panel__header">
            <div className="filters-panel__title-wrap">
              <div className="filters-panel__icon">
                <FilterIcon />
              </div>
              <div>
                <div className="filters-panel__title">Bộ lọc vật tư</div>
              </div>
            </div>

            <div className="filters-panel__header-right">
              <div className="filters-active-chip">Tổng vật tư: {stats.total}</div>
              <div className="filters-active-chip">Tổng tồn kho: {stats.totalStock}</div>
            </div>
          </div>

          <div className="filters-grid filters-grid--5">
            <div className="filter-field">
              <label className="filter-label">Từ khóa</label>
              <div className="search-box">
                <SearchIcon />
                <input
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="Tìm kiếm"
                />
              </div>
            </div>

            <div className="filter-field">
              <label className="filter-label">Danh mục</label>
              <select
                className="filter-select"
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value)}
              >
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat === "ALL" ? "Tất cả danh mục" : cat}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-field">
              <label className="filter-label">Tồn kho</label>
              <select
                className="filter-select"
                value={stockFilter}
                onChange={(e) => setStockFilter(e.target.value)}
              >
                <option value="ALL">Tất cả</option>
                <option value="LOW">Sắp thiếu</option>
                <option value="OUT">Hết hàng</option>
              </select>
            </div>

            <div className="filter-field">
              <label className="filter-label">Số dòng</label>
              <select
                className="page-size-select"
                value={pageSize}
                onChange={(e) => setPageSize(Number(e.target.value))}
              >
                <option value={5}>5 / trang</option>
                <option value={10}>10 / trang</option>
                <option value={20}>20 / trang</option>
              </select>
            </div>

            <div className="filter-field filter-field--actions">
              <label className="filter-label filter-label--ghost">Actions</label>
              <div className="filter-actions-row">
                <button
                  type="button"
                  className="btn btn-soft-blue btn-search-compact"
                  onClick={() => setCurrentPage(1)}
                >
                  <SearchIcon />
                  <span>Tìm kiếm</span>
                </button>

                <button
                  type="button"
                  className="btn btn-light btn-icon-only"
                  onClick={() => {
                    setSearch("");
                    setCategoryFilter("ALL");
                    setStockFilter("ALL");
                    setPageSize(10);
                    setCurrentPage(1);
                    loadParts();
                  }}
                  title="Làm mới"
                >
                  <RefreshIcon />
                </button>
              </div>
            </div>
          </div>

          <div className="applied-filters">
            <div className="applied-filter-chip">
              <strong>Sắp thiếu:</strong> {stats.lowStock}
            </div>
            <div className="applied-filter-chip">
              <strong>Hết hàng:</strong> {stats.outOfStock}
            </div>
            <div className="applied-filter-chip">
              <strong>Đang hiển thị:</strong> {filteredParts.length}
            </div>
          </div>
        </div>

        <div className="list-section">
          <div className="list-section__title">
            Danh sách vật tư
            <span className="list-badge">{filteredParts.length}</span>
          </div>

          <div className="table-wrap">
            <table className="assets-table">
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Danh mục</th>
                  <th>Tên vật tư</th>
                  <th>Barcode</th>
                  <th>Vendor</th>
                  <th>Vị trí</th>
                  <th>Số lượng</th>
                  <th>Giá</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="9" className="text-muted">
                      Đang tải dữ liệu...
                    </td>
                  </tr>
                ) : pagedParts.length === 0 ? (
                  <tr>
                    <td colSpan="9" className="text-muted">
                      {parts.length === 0
                        ? "Chưa có vật tư nào trong hệ thống."
                        : "Không có vật tư nào phù hợp với bộ lọc hiện tại."}
                    </td>
                  </tr>
                ) : (
                  pagedParts.map((part, index) => {
                    const id = getPartId(part);
                    const stockMeta = getStockMeta(part);
                    const consumableMeta = getConsumableMeta(part);

                    return (
                      <tr key={id ?? `${part.name}-${part.barcode}`}>
                        <td>{startIndex + index + 1}</td>
                        <td>{formatText(part.category)}</td>
                        <td>
                          <div className="asset-name-cell">
                            <strong>{formatText(part.name)}</strong>
                            <small>{formatText(part.description)}</small>
                          </div>
                        </td>
                        <td>{formatText(part.barcode)}</td>
                        <td>{formatText(part.vendor)}</td>
                        <td>{formatText(part.locationName)}</td>
                        <td>
                          <div>{Number(part.quantity || 0)}</div>
                          <div style={{ marginTop: 8 }}>
                            <span className={`badge ${stockMeta.cls}`}>
                              {stockMeta.text}
                            </span>
                          </div>
                        </td>
                        <td>
                          <div>{formatMoney(part.cost)}</div>
                          <div style={{ marginTop: 8 }}>
                            <span className={`badge ${consumableMeta.cls}`}>
                              {consumableMeta.text}
                            </span>
                          </div>
                        </td>
                        <td>
                          <div className="action-group">
                            <button
                              type="button"
                              className="icon-btn"
                              title="Xem chi tiết"
                              onClick={() => setSelectedPart(part)}
                            >
                              <EyeIcon />
                            </button>

                            <button
                              type="button"
                              className="icon-btn"
                              title={editAllowed ? "Sửa vật tư" : "Bạn không có quyền sửa vật tư"}
                              disabled={!editAllowed}
                              onClick={() => {
                                if (!editAllowed) return;
                                startEdit(part);
                              }}
                            >
                              <EditIcon />
                            </button>

                            <button
                              type="button"
                              className="icon-btn"
                              title={stockAllowed ? "Tăng tồn kho" : "Bạn không có quyền điều chỉnh tồn kho"}
                              disabled={!stockAllowed}
                              onClick={() => {
                                if (!stockAllowed) return;
                                openStockModal(part, "increase");
                              }}
                            >
                              <ArrowUpIcon />
                            </button>

                            <button
                              type="button"
                              className="icon-btn"
                              title={stockAllowed ? "Giảm tồn kho" : "Bạn không có quyền điều chỉnh tồn kho"}
                              disabled={!stockAllowed}
                              onClick={() => {
                                if (!stockAllowed) return;
                                openStockModal(part, "decrease");
                              }}
                            >
                              <ArrowDownIcon />
                            </button>

                            <button
                              type="button"
                              className="icon-btn icon-btn--danger"
                              title={deleteAllowed ? "Xóa vật tư" : "Bạn không có quyền xóa vật tư"}
                              disabled={!deleteAllowed}
                              onClick={() => {
                                if (!deleteAllowed) return;
                                setDeleteTarget(part);
                              }}
                            >
                              <DeleteIcon />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          <div className="pagination-bar">
            <div className="pagination-info">
              Hiển thị {filteredParts.length === 0 ? 0 : startIndex + 1} -{" "}
              {Math.min(startIndex + pageSize, filteredParts.length)} trên{" "}
              {filteredParts.length} bản ghi
            </div>

            <div className="pagination-right">
              <select
                className="page-size-select page-size-select--bottom"
                value={pageSize}
                onChange={(e) => setPageSize(Number(e.target.value))}
              >
                <option value={5}>5 / trang</option>
                <option value={10}>10 / trang</option>
                <option value={20}>20 / trang</option>
              </select>

              <div className="pagination-controls">
                <button
                  type="button"
                  className="page-btn"
                  disabled={safeCurrentPage <= 1}
                  onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                >
                  Trước
                </button>

                <button type="button" className="page-number active">
                  {safeCurrentPage}
                </button>

                <button
                  type="button"
                  className="page-btn"
                  disabled={safeCurrentPage >= totalPages}
                  onClick={() =>
                    setCurrentPage((p) => Math.min(totalPages, p + 1))
                  }
                >
                  Sau
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {showFormModal && (
        <div
          className="drawer-overlay"
          onClick={() => setShowFormModal(false)}
        >
          <div
            className="drawer drawer--wide"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="drawer-header">
              <div>
                <h2>{editingId ? "Cập nhật vật tư" : "Thêm vật tư mới"}</h2>
              </div>
              <button
                type="button"
                className="drawer-close"
                onClick={() => setShowFormModal(false)}
              >
                <CloseIcon />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="drawer-body">
                <div className="form-section">
                  <div className="form-grid">
                    <div className="form-field form-field--full">
                      <label className="form-label">Tên vật tư *</label>
                      <input
                        className="form-input"
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        placeholder="Nhập tên vật tư"
                        required
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Danh mục</label>
                      <input
                        className="form-input"
                        name="category"
                        value={form.category}
                        onChange={handleChange}
                        placeholder="Nhập danh mục"
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Barcode</label>
                      <input
                        className="form-input"
                        name="barcode"
                        value={form.barcode}
                        onChange={handleChange}
                        placeholder="Nhập Barcode"
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Vendor</label>
                      <input
                        className="form-input"
                        name="vendor"
                        value={form.vendor}
                        onChange={handleChange}
                        placeholder="Nhập tên nhà cung cấp"
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Vị trí</label>
                      <input
                        className="form-input"
                        name="locationName"
                        value={form.locationName}
                        onChange={handleChange}
                        placeholder="Nhập vị trí"
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Người phụ trách</label>
                      <input
                        className="form-input"
                        name="assignedTo"
                        value={form.assignedTo}
                        onChange={handleChange}
                        placeholder="Nhập tên người phụ trách"
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Số lượng</label>
                      <input
                        className="form-input"
                        type="number"
                        min="0"
                        name="quantity"
                        value={form.quantity}
                        onChange={handleChange}
                        placeholder="Nhập số lượng"
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Ngưỡng tối thiểu</label>
                      <input
                        className="form-input"
                        type="number"
                        min="0"
                        name="minimumQuantity"
                        value={form.minimumQuantity}
                        onChange={handleChange}
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Cost</label>
                      <input
                        className="form-input"
                        type="number"
                        min="0"
                        step="0.01"
                        name="cost"
                        value={form.cost}
                        onChange={handleChange}
                      />
                    </div>

                    <div className="form-field">
                      <label className="form-label">Last Price</label>
                      <input
                        className="form-input"
                        type="number"
                        min="0"
                        step="0.01"
                        name="lastPrice"
                        value={form.lastPrice}
                        onChange={handleChange}
                      />
                    </div>

                    <div className="form-field form-field--full">
                      <label className="form-label">
                        <input
                          type="checkbox"
                          name="consumable"
                          checked={form.consumable}
                          onChange={handleChange}
                          style={{ marginRight: 8 }}
                        />
                        Consumable / Non-repairable
                      </label>
                    </div>

                    <div className="form-field form-field--full">
                      <label className="form-label">Mô tả</label>
                      <textarea
                        className="form-textarea"
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        rows={4}
                        placeholder="Nhập mô tả"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <div className="drawer-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => {
                    resetForm();
                    setShowFormModal(false);
                  }}
                  disabled={submitting}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting}
                >
                  {submitting
                    ? "Đang lưu..."
                    : editingId
                    ? "Cập nhật"
                    : "Tạo mới"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {selectedPart && (
        <div
          className="drawer-overlay"
          onClick={() => setSelectedPart(null)}
        >
          <div className="drawer" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <div>
                <h2>Chi tiết vật tư</h2>
                <p>Thông tin đầy đủ của vật tư được chọn.</p>
              </div>
              <button
                type="button"
                className="drawer-close"
                onClick={() => setSelectedPart(null)}
              >
                <CloseIcon />
              </button>
            </div>

            <div className="drawer-body">
              <div className="detail-hero">
                <div className="detail-hero__left">
                  <div className="detail-hero__icon">
                    <BoxIcon />
                  </div>
                  <div className="detail-hero__content">
                    <h3>{formatText(selectedPart.name)}</h3>
                    <p>{formatText(selectedPart.description)}</p>
                    <div className="detail-hero__meta">
                      <span className="hero-chip">
                        {getStockMeta(selectedPart).text}
                      </span>
                      <span className="hero-chip">
                        Barcode: {formatText(selectedPart.barcode)}
                      </span>
                      <span className="hero-chip">
                        Qty: {formatText(selectedPart.quantity)}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="detail-section">
                <div className="detail-section__title">Thông tin chung</div>
                <div className="detail-grid detail-grid--2">
                  <div className="detail-item">
                    <div className="detail-item__label">Danh mục</div>
                    <div className="detail-item__value">
                      {formatText(selectedPart.category)}
                    </div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Vendor</div>
                    <div className="detail-item__value">
                      {formatText(selectedPart.vendor)}
                    </div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Vị trí</div>
                    <div className="detail-item__value">
                      {formatText(selectedPart.locationName)}
                    </div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Người phụ trách</div>
                    <div className="detail-item__value">
                      {formatText(selectedPart.assignedTo)}
                    </div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Cost</div>
                    <div className="detail-item__value">
                      {formatMoney(selectedPart.cost)}
                    </div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Last Price</div>
                    <div className="detail-item__value">
                      {formatMoney(selectedPart.lastPrice)}
                    </div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Minimum Quantity</div>
                    <div className="detail-item__value">
                      {formatText(selectedPart.minimumQuantity)}
                    </div>
                  </div>
                  <div className="detail-item">
                    <div className="detail-item__label">Consumable</div>
                    <div className="detail-item__value">
                      {selectedPart.consumable ? "Yes" : "No"}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setSelectedPart(null)}
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {stockModalOpen && stockTarget && (
        <div
          className="drawer-overlay"
          onClick={() => setStockModalOpen(false)}
        >
          <div
            className="drawer drawer--small"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="drawer-header">
              <div>
                <h2>
                  {stockMode === "increase" ? "Tăng tồn kho" : "Giảm tồn kho"}
                </h2>
                <p>{stockTarget.name}</p>
              </div>
              <button
                type="button"
                className="drawer-close"
                onClick={() => setStockModalOpen(false)}
              >
                <CloseIcon />
              </button>
            </div>

            <div className="drawer-body">
              <div className="form-field">
                <label className="form-label">Số lượng</label>
                <input
                  className="form-input"
                  type="number"
                  min="1"
                  value={stockAmount}
                  onChange={(e) => setStockAmount(e.target.value)}
                  placeholder="Nhập số lượng"
                />
              </div>
            </div>

            <div className="drawer-footer">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setStockModalOpen(false)}
              >
                Hủy
              </button>
              <button
                type="button"
                className={
                  stockMode === "increase" ? "btn btn-primary" : "btn btn-soft-blue"
                }
                onClick={submitStockAdjust}
                disabled={submitting}
              >
                {submitting
                  ? "Đang xử lý..."
                  : stockMode === "increase"
                  ? "Xác nhận tăng"
                  : "Xác nhận giảm"}
              </button>
            </div>
          </div>
        </div>
      )}

      {deleteTarget && (
        <div
          className="drawer-overlay"
          onClick={() => setDeleteTarget(null)}
        >
          <div
            className="drawer drawer--small"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="drawer-header">
              <div>
                <h2>Xóa vật tư</h2>
                <p>Xác nhận trước khi xóa dữ liệu.</p>
              </div>
              <button
                type="button"
                className="drawer-close"
                onClick={() => setDeleteTarget(null)}
              >
                <CloseIcon />
              </button>
            </div>

            <div className="drawer-body">
              <div className="delete-box">
                <div className="delete-box__icon">
                  <DeleteIcon />
                </div>
                <div className="delete-box__content">
                  <h3>{formatText(deleteTarget.name)}</h3>
                  <p>
                    Bạn có chắc muốn xóa vật tư này không? Hành động này không thể hoàn tác.
                  </p>
                </div>
              </div>
            </div>

            <div className="drawer-footer">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setDeleteTarget(null)}
              >
                Hủy
              </button>
              <button
                type="button"
                className="btn btn-danger-solid"
                onClick={confirmDelete}
                disabled={submitting}
              >
                {submitting ? "Đang xóa..." : "Xóa vật tư"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}