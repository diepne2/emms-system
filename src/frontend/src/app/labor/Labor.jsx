import { useEffect, useMemo, useState } from "react";
import "./Labor.css";

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  "https://emms-system-production-4239.up.railway.app";

const LABOR_API = `${API_BASE}/api/labors`;
const WORK_ORDERS_API = `${API_BASE}/api/work-orders/my`;

function getToken() {
  return (
    localStorage.getItem("token") ||
    localStorage.getItem("accessToken") ||
    localStorage.getItem("access_token") ||
    localStorage.getItem("jwt") ||
    ""
  );
}

function headers() {
  const token = getToken();
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

async function readResponse(res) {
  const text = await res.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function normalizeList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  return [];
}

function getErrorMessage(data, fallback) {
  if (!data) return fallback;
  if (typeof data === "string") return data;
  return data.message || data.error || fallback;
}

function formatDateForBackend(value) {
  if (!value) return null;
  if (value.length === 16) return `${value}:00`;
  return value;
}

function formatDate(value) {
  if (!value) return "—";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return date.toLocaleString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function formatCurrency(value) {
  const number = Number(value || 0);
  return number.toLocaleString("vi-VN") + " đ/giờ";
}

function statusLabel(status) {
  const s = String(status || "").toUpperCase();

  if (s === "RUNNING") return "Đang thực hiện";
  if (s === "STOPPED") return "Đã dừng";
  if (s === "DONE") return "Hoàn tất";
  if (s === "CANCELLED") return "Đã hủy";

  return status || "Không rõ";
}

function statusClass(status) {
  return String(status || "unknown").toLowerCase();
}

export default function Labor() {
  const [workOrders, setWorkOrders] = useState([]);
  const [labors, setLabors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(false);
  const [keyword, setKeyword] = useState("");

  const [form, setForm] = useState({
    workOrderId: "",
    startedAt: "",
    hourlyRate: 0,
  });

  const loadWorkOrders = async () => {
    try {
      const res = await fetch(WORK_ORDERS_API, {
        headers: headers(),
      });

      const data = await readResponse(res);

      if (!res.ok) {
        console.error("LOAD WO ERROR:", res.status, data);
        return;
      }

      setWorkOrders(normalizeList(data));
    } catch (err) {
      console.error(err);
    }
  };

  const loadLabors = async () => {
    try {
      const res = await fetch(LABOR_API, {
        headers: headers(),
      });

      const data = await readResponse(res);

      if (!res.ok) {
        console.error("LOAD LABOR ERROR:", res.status, data);
        return;
      }

      setLabors(normalizeList(data));
    } catch (err) {
      console.error(err);
    }
  };

  const loadAll = async () => {
    setPageLoading(true);
    await Promise.all([loadWorkOrders(), loadLabors()]);
    setPageLoading(false);
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handleChange = (e) => {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const handleCreate = async () => {
    if (!form.workOrderId) {
      alert("Vui lòng chọn Work Order.");
      return;
    }

    if (!form.startedAt) {
      alert("Vui lòng chọn thời gian bắt đầu.");
      return;
    }

    setLoading(true);

    try {
      const payload = {
        workOrderId: Number(form.workOrderId),
        startedAt: formatDateForBackend(form.startedAt),
        hourlyRate: Number(form.hourlyRate || 0),
        duration: 0,
        includeToTotalTime: true,
        status: "RUNNING",
      };

      const res = await fetch(LABOR_API, {
        method: "POST",
        headers: headers(),
        body: JSON.stringify(payload),
      });

      const data = await readResponse(res);

      if (!res.ok) {
        console.error("CREATE ERROR:", res.status, data);
        alert(getErrorMessage(data, `Lỗi ${res.status}`));
        return;
      }

      alert("Ghi nhận công việc thành công.");

      setForm({
        workOrderId: "",
        startedAt: "",
        hourlyRate: 0,
      });

      loadLabors();
    } catch (err) {
      console.error(err);
      alert("Không thể kết nối đến máy chủ.");
    } finally {
      setLoading(false);
    }
  };

  const filteredLabors = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    if (!q) return labors;

    return labors.filter((item) =>
      [
        item.id,
        item.workOrderCode,
        item.workOrderId,
        item.status,
        item.startedAt,
        item.endedAt,
        item.hourlyRate,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(q)
    );
  }, [labors, keyword]);

  const runningCount = labors.filter(
    (item) => String(item.status || "").toUpperCase() === "RUNNING"
  ).length;

  const stoppedCount = labors.filter(
    (item) => String(item.status || "").toUpperCase() === "STOPPED"
  ).length;

  return (
    <div className="labor-page">
      <div className="labor-shell">
        <header className="labor-header">
          <div>
            <h1>Ghi nhận công việc</h1>
            <p>
              Theo dõi thời gian làm việc của kỹ thuật viên theo từng Work Order.
            </p>
          </div>

          <div className="labor-header-actions">
            <span className="labor-chip">{labors.length} bản ghi</span>
            <button className="labor-btn labor-btn-light" onClick={loadAll}>
              Làm mới
            </button>
          </div>
        </header>

        <section className="labor-stats">
          <div className="labor-stat">
            <span>Tổng bản ghi</span>
            <strong>{labors.length}</strong>
          </div>
          <div className="labor-stat">
            <span>Đang thực hiện</span>
            <strong>{runningCount}</strong>
          </div>
          <div className="labor-stat">
            <span>Đã dừng</span>
            <strong>{stoppedCount}</strong>
          </div>
          <div className="labor-stat">
            <span>Work Order của tôi</span>
            <strong>{workOrders.length}</strong>
          </div>
        </section>

        <div className="labor-layout">
          <section className="labor-panel">
            <div className="labor-card-head">
              <div>
                <h2>Thêm ghi nhận</h2>
                <p>Ghi nhận thời gian bắt đầu làm việc cho Work Order.</p>
              </div>
            </div>

            <div className="labor-form">
              <label>Work Order</label>
              <select
                name="workOrderId"
                value={form.workOrderId}
                onChange={handleChange}
              >
                <option value="">Chọn Work Order</option>
                {workOrders.map((wo) => (
                  <option key={wo.id} value={wo.id}>
                    WO-{wo.id} — {wo.title || wo.name || wo.assetName || "Không có tên"}
                  </option>
                ))}
              </select>

              <label>Thời gian bắt đầu</label>
              <input
                type="datetime-local"
                name="startedAt"
                value={form.startedAt}
                onChange={handleChange}
              />

              <label>Đơn giá theo giờ</label>
              <input
                type="number"
                name="hourlyRate"
                min="0"
                value={form.hourlyRate}
                onChange={handleChange}
                placeholder="VD: 50000"
              />

              <button onClick={handleCreate} disabled={loading}>
                {loading ? "Đang lưu..." : "Ghi nhận công việc"}
              </button>
            </div>
          </section>

          <section className="labor-list-card">
            <div className="labor-card-head labor-card-head-row">
              <div>
                <h2>Danh sách ghi nhận</h2>
                <p>Quản lý các bản ghi nhân công đã tạo.</p>
              </div>

              <div className="labor-search">
                <input
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  placeholder="Tìm theo WO, trạng thái..."
                />
              </div>
            </div>

            {pageLoading ? (
              <div className="labor-empty">
                <strong>Đang tải dữ liệu...</strong>
                <span>Vui lòng chờ trong giây lát.</span>
              </div>
            ) : filteredLabors.length === 0 ? (
              <div className="labor-empty">
                <strong>Chưa có dữ liệu</strong>
                <span>Chưa tìm thấy bản ghi nhân công phù hợp.</span>
              </div>
            ) : (
              <div className="labor-table-wrap">
                <table className="labor-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Work Order</th>
                      <th>Trạng thái</th>
                      <th>Bắt đầu</th>
                      <th>Kết thúc</th>
                      <th>Đơn giá</th>
                    </tr>
                  </thead>

                  <tbody>
                    {filteredLabors.map((item) => (
                      <tr key={item.id}>
                        <td>
                          <span className="labor-id">#{item.id}</span>
                        </td>
                        <td>
                          <span className="labor-wo">
                            {item.workOrderCode || `WO-${item.workOrderId || "—"}`}
                          </span>
                        </td>
                        <td>
                          <span
                            className={`labor-status-badge ${statusClass(item.status)}`}
                          >
                            {statusLabel(item.status)}
                          </span>
                        </td>
                        <td>{formatDate(item.startedAt)}</td>
                        <td>{formatDate(item.endedAt)}</td>
                        <td>{formatCurrency(item.hourlyRate)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}