import React, { useEffect, useMemo, useState } from "react";
import axios from "axios";
import "./WorkOrderHistories.css";

const HISTORY_API_BASE = "http://localhost:8080/api/work-order-histories";
const WORK_ORDER_API_BASE = "http://localhost:8080/api/work-orders";

function getToken() {
  return localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken") || "";
}

function getCurrentUser() {
  try {
    const raw =
      localStorage.getItem("user") ||
      sessionStorage.getItem("user") ||
      localStorage.getItem("currentUser") ||
      sessionStorage.getItem("currentUser");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function getRoleCode(user) {
  if (!user) return "";
  return (user.roleCode || user.role?.code || user.authority || user.roleName || "").toString().toUpperCase();
}

function canDelete(user) {
  const role = getRoleCode(user);
  return role.includes("ADMIN") || role.includes("TECHNICAL_MANAGER") || role.includes("QUANLYKYTHUAT");
}

function formatDateTime(value) {
  if (!value) return "-";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString("vi-VN");
}

function formatDate(value) {
  if (!value) return "-";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString("vi-VN");
}

function prettyJson(value) {
  if (!value) return "";
  try {
    const parsed = typeof value === "string" ? JSON.parse(value) : value;
    return JSON.stringify(parsed, null, 2);
  } catch {
    return String(value);
  }
}

function parseSnapshot(snapshotJson) {
  if (!snapshotJson) return null;
  try {
    return typeof snapshotJson === "string" ? JSON.parse(snapshotJson) : snapshotJson;
  } catch {
    return null;
  }
}

function formatValue(value) {
  if (value === null || value === undefined || value === "") return "-";
  if (typeof value === "boolean") return value ? "Có" : "Không";
  return String(value);
}

function normalizeStatus(status) {
  return (status || "").toString().trim().toUpperCase();
}

function getStatusClass(status) {
  const s = normalizeStatus(status).toLowerCase();
  return s || "unknown";
}

function buildSnapshotRows(snapshot) {
  if (!snapshot) return [];
  return [
    { label: "Work Order ID", value: snapshot.id },
    { label: "Tiêu đề", value: snapshot.title },
    { label: "Mô tả", value: snapshot.description },
    { label: "Trạng thái", value: snapshot.status },
    { label: "Độ ưu tiên", value: snapshot.priority },
    { label: "Đã lưu trữ", value: snapshot.archived },
    { label: "Hạn xử lý", value: snapshot.dueDate },
    { label: "Hoàn tất lúc", value: snapshot.completedOn },
    { label: "Người hoàn tất", value: snapshot.completedBy },
    { label: "Phản hồi", value: snapshot.feedback },
    { label: "Asset ID", value: snapshot.assetId },
    { label: "Tên Asset", value: snapshot.assetName },
    { label: "Assigned To ID", value: snapshot.assignedToId },
    { label: "Assigned To Name", value: snapshot.assignedToName },
  ];
}

function countByStatus(histories, fallbackWorkOrders, status) {
  const target = normalizeStatus(status);
  if (histories.length > 0) {
    return histories.filter((item) => normalizeStatus(parseSnapshot(item.snapshotJson)?.status) === target).length;
  }
  return fallbackWorkOrders.filter((wo) => normalizeStatus(wo.status) === target).length;
}

export default function WorkOrderHistories() {
  const currentUser = useMemo(() => getCurrentUser(), []);
  const [items, setItems] = useState([]);
  const [fallbackWorkOrders, setFallbackWorkOrders] = useState([]);
  const [filteredWorkOrderId, setFilteredWorkOrderId] = useState("");
  const [keyword, setKeyword] = useState("");
  const [selected, setSelected] = useState(null);
  const [mode, setMode] = useState("DONE_CANCELLED");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchDoneAndCancelled();
  }, []);

  const authHeaders = () => ({
    Authorization: `Bearer ${getToken()}`,
    "Content-Type": "application/json",
  });

  async function fetchDoneAndCancelled() {
    try {
      setLoading(true);
      setMode("DONE_CANCELLED");
      setSelected(null);
      const res = await axios.get(`${HISTORY_API_BASE}/done-cancelled`, { headers: authHeaders() });
      const histories = Array.isArray(res.data) ? res.data : [];
      setItems(histories);
      if (histories.length === 0) await fetchDoneCancelledWorkOrdersFallback();
      else setFallbackWorkOrders([]);
    } catch (error) {
      console.error("fetchDoneAndCancelled error:", error);
      setItems([]);
      await fetchDoneCancelledWorkOrdersFallback();
    } finally {
      setLoading(false);
    }
  }

  async function fetchDoneCancelledWorkOrdersFallback() {
    try {
      const res = await axios.get(WORK_ORDER_API_BASE, { headers: authHeaders() });
      const workOrders = Array.isArray(res.data) ? res.data : [];
      setFallbackWorkOrders(workOrders.filter((wo) => ["DONE", "CANCELLED"].includes(normalizeStatus(wo.status))));
    } catch (error) {
      console.error("fetchDoneCancelledWorkOrdersFallback error:", error);
      setFallbackWorkOrders([]);
    }
  }

  async function fetchByWorkOrderId(workOrderId) {
    if (!workOrderId) return fetchDoneAndCancelled();
    try {
      setLoading(true);
      setMode("BY_WORK_ORDER");
      setSelected(null);
      const res = await axios.get(`${HISTORY_API_BASE}/work-order/${workOrderId}`, { headers: authHeaders() });
      setItems(Array.isArray(res.data) ? res.data : []);
      setFallbackWorkOrders([]);
    } catch (error) {
      console.error("fetchByWorkOrderId error:", error);
      setItems([]);
      setFallbackWorkOrders([]);
      alert(error?.response?.data?.message || "Không tải được lịch sử theo Work Order");
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!canDelete(currentUser)) return alert("Bạn không có quyền xóa lịch sử");
    if (!window.confirm("Bạn có chắc muốn xóa lịch sử này?")) return;
    try {
      await axios.delete(`${HISTORY_API_BASE}/${id}`, { headers: authHeaders() });
      setItems((prev) => prev.filter((item) => item.id !== id));
      if (selected?.id === id) setSelected(null);
      alert("Xóa lịch sử thành công");
    } catch (error) {
      console.error("delete history error:", error);
      alert(error?.response?.data?.message || "Xóa lịch sử thất bại");
    }
  }

  function handleResetFilter() {
    setFilteredWorkOrderId("");
    setKeyword("");
    setSelected(null);
    fetchDoneAndCancelled();
  }

  const visibleItems = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    if (!q) return items;
    return items.filter((item) =>
      [item.id, item.versionName, item.versionNo, item.note, item.snapshotJson, item.workOrderId, item.savedById, item.savedByName, item.createdAt]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(q)
    );
  }, [items, keyword]);

  const visibleFallbackWorkOrders = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    if (!q) return fallbackWorkOrders;
    return fallbackWorkOrders.filter((wo) =>
      [wo.id, wo.title, wo.status, wo.priority, wo.assetName, wo.completedBy, wo.completedOn, wo.dueDate]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(q)
    );
  }, [fallbackWorkOrders, keyword]);

  const selectedSnapshot = useMemo(() => parseSnapshot(selected?.snapshotJson), [selected]);
  const selectedSnapshotRows = useMemo(() => buildSnapshotRows(selectedSnapshot), [selectedSnapshot]);
  const showingHistoryTable = visibleItems.length > 0;
  const showingFallbackTable = !showingHistoryTable && visibleFallbackWorkOrders.length > 0;
  const totalRows = showingHistoryTable ? visibleItems.length : visibleFallbackWorkOrders.length;
  const doneCount = countByStatus(visibleItems, visibleFallbackWorkOrders, "DONE");
  const cancelledCount = countByStatus(visibleItems, visibleFallbackWorkOrders, "CANCELLED");

  return (
    <div className="woh-page">
      <div className="woh-shell">
        <div className="woh-page-head">
          <div>
            <div className="woh-breadcrumb">Trang chủ / Work Orders / Lịch sử</div>
            <h1>Work Order History</h1>
          </div>
          <div className="woh-head-actions">
            <span className="woh-chip woh-chip-green">{mode === "DONE_CANCELLED" ? "DONE + CANCELLED" : `WO-${filteredWorkOrderId}`}</span>
            <span className="woh-chip">{loading ? "Đang tải" : "Ready"}</span>
          </div>
        </div>

        <section className="woh-panel woh-filter-panel">
          <div className="woh-filter-grid">
            <label className="woh-control">
              <span>Work Order ID</span>
              <input type="number" min="1" value={filteredWorkOrderId} onChange={(e) => setFilteredWorkOrderId(e.target.value)} placeholder="VD: 1018" />
            </label>
            <label className="woh-control woh-search-control">
              <span>Tìm kiếm nhanh</span>
              <input type="text" value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="ID, version, note, người lưu, trạng thái..." />
            </label>
            <div className="woh-filter-actions">
              <button className="woh-btn woh-btn-primary" onClick={() => fetchByWorkOrderId(filteredWorkOrderId)}>Lọc WO</button>
              <button className="woh-btn woh-btn-secondary" onClick={fetchDoneAndCancelled}>Gộp Done/Cancel</button>
              <button className="woh-btn woh-btn-light" onClick={handleResetFilter}>Reset</button>
            </div>
          </div>
        </section>

        <section className="woh-stat-strip" aria-label="Tổng quan lịch sử work order">
          <div className="woh-stat-item"><span>Tổng bản ghi</span><strong>{totalRows}</strong></div>
          <div className="woh-stat-item"><span>Done</span><strong>{doneCount}</strong></div>
          <div className="woh-stat-item"><span>Cancelled</span><strong>{cancelledCount}</strong></div>
          <div className="woh-stat-item"><span>Phạm vi</span><strong>{filteredWorkOrderId ? `WO-${filteredWorkOrderId}` : "All WO"}</strong></div>
        </section>

        <section className="woh-panel woh-table-card">
          {loading ? (
            <div className="woh-empty"><strong>Đang tải dữ liệu...</strong><span>Vui lòng chờ trong giây lát.</span></div>
          ) : showingHistoryTable ? (
            <>
              <div className="woh-card-head">
                <div><h2>Lịch sử Work Order</h2><p>Danh sách snapshot của các Work Order DONE / CANCELLED.</p></div>
                <span className="woh-count-badge">{visibleItems.length} bản ghi</span>
              </div>
              <div className="woh-table-wrap">
                <table className="woh-table">
                  <thead><tr><th>ID</th><th>Version</th><th>No.</th><th>Work Order</th><th>Saved By</th><th>Note</th><th>Created At</th><th>Thao tác</th></tr></thead>
                  <tbody>
                    {visibleItems.map((item) => {
                      const snapshot = parseSnapshot(item.snapshotJson);
                      return (
                        <tr key={item.id} className="woh-row-clickable" onClick={() => setSelected(item)}>
                          <td><span className="woh-id-badge">#{item.id}</span></td>
                          <td><div className="woh-version-cell"><strong>{item.versionName || "-"}</strong>{snapshot?.status && <span className={`woh-status-pill woh-status-${getStatusClass(snapshot.status)}`}>{snapshot.status}</span>}</div></td>
                          <td><span className="woh-version-badge">v{item.versionNo ?? "-"}</span></td>
                          <td className="woh-wo-code">WO-{item.workOrderId ?? snapshot?.id ?? "-"}</td>
                          <td>{item.savedByName || item.savedById || "-"}</td>
                          <td className="woh-note-cell">{item.note || <span className="woh-muted-text">Không có ghi chú</span>}</td>
                          <td>{formatDateTime(item.createdAt)}</td>
                          <td className="woh-action-cell" onClick={(e) => e.stopPropagation()}>
                            <button className="woh-btn woh-btn-secondary" onClick={() => setSelected(item)}>Xem</button>
                            {canDelete(currentUser) && <button className="woh-btn woh-btn-danger" onClick={() => handleDelete(item.id)}>Xóa</button>}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </>
          ) : showingFallbackTable ? (
            <>
              <div className="woh-card-head">
                <div><h2>Work Order Done / Cancelled</h2><p>Hiển thị WO đã đóng khi history chưa có bản ghi snapshot.</p></div>
                <span className="woh-count-badge">{visibleFallbackWorkOrders.length} WO</span>
              </div>
              <div className="woh-table-wrap">
                <table className="woh-table">
                  <thead><tr><th>ID</th><th>Title</th><th>Status</th><th>Priority</th><th>Asset</th><th>Completed By</th><th>Completed On</th><th>Due Date</th></tr></thead>
                  <tbody>
                    {visibleFallbackWorkOrders.map((wo) => (
                      <tr key={wo.id}>
                        <td><span className="woh-id-badge">WO-{wo.id}</span></td>
                        <td>{wo.title || "-"}</td>
                        <td><span className={`woh-status-pill woh-status-${getStatusClass(wo.status)}`}>{wo.status || "-"}</span></td>
                        <td><span className={`woh-priority-pill woh-priority-${getStatusClass(wo.priority)}`}>{wo.priority || "-"}</span></td>
                        <td>{wo.assetName || "-"}</td><td>{wo.completedBy || "-"}</td><td>{formatDateTime(wo.completedOn)}</td><td>{formatDate(wo.dueDate)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          ) : (
            <div className="woh-empty"><strong>Không có dữ liệu phù hợp</strong><span>Không tìm thấy lịch sử hoặc Work Order Done/Cancelled nào theo bộ lọc hiện tại.</span></div>
          )}
        </section>

        {selected && (
          <div className="woh-modal-backdrop" onClick={() => setSelected(null)}>
            <div className="woh-modal woh-modal-lg" onClick={(e) => e.stopPropagation()}>
              <div className="woh-modal-header"><div><h2>Chi tiết lịch sử #{selected.id}</h2><p>{selected.versionName || "Work Order History"}</p></div><button className="woh-icon-btn" onClick={() => setSelected(null)} aria-label="Đóng">✕</button></div>
              <div className="woh-detail-grid">
                <div className="woh-detail-item"><span>ID</span><strong>{selected.id}</strong></div>
                <div className="woh-detail-item"><span>Version Name</span><strong>{selected.versionName || "-"}</strong></div>
                <div className="woh-detail-item"><span>Version No</span><strong>v{selected.versionNo ?? "-"}</strong></div>
                <div className="woh-detail-item"><span>Work Order ID</span><strong>WO-{selected.workOrderId ?? selectedSnapshot?.id ?? "-"}</strong></div>
                <div className="woh-detail-item"><span>Saved By</span><strong>{selected.savedByName || selected.savedById || "-"}</strong></div>
                <div className="woh-detail-item"><span>Created At</span><strong>{formatDateTime(selected.createdAt)}</strong></div>
              </div>
              <div className="woh-detail-section"><h3>Note</h3><div className="woh-detail-box">{selected.note || "-"}</div></div>
              <div className="woh-detail-section"><h3>Snapshot Preview</h3>{!selectedSnapshot ? <div className="woh-detail-box">Không parse được snapshot</div> : <div className="woh-snapshot-table-wrap"><table className="woh-snapshot-table"><tbody>{selectedSnapshotRows.map((row) => <tr key={row.label}><th>{row.label}</th><td>{formatValue(row.value)}</td></tr>)}</tbody></table></div>}</div>
              <div className="woh-detail-section"><h3>Snapshot JSON</h3><pre className="woh-json-box">{prettyJson(selected.snapshotJson) || "-"}</pre></div>
              <div className="woh-modal-actions">{canDelete(currentUser) && <button className="woh-btn woh-btn-danger" onClick={() => handleDelete(selected.id)}>Xóa lịch sử</button>}<button className="woh-btn woh-btn-light" onClick={() => setSelected(null)}>Đóng</button></div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
