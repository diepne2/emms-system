import { useEffect, useMemo, useState } from "react";
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  Layers,
  RefreshCw,
  XCircle,
} from "lucide-react";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import "./Dashboard.css";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const COLORS = ["#0f766e", "#2563eb", "#f97316", "#dc2626", "#8b5cf6"];

function getToken() {
  const raw =
    localStorage.getItem("token") ||
    localStorage.getItem("accessToken") ||
    localStorage.getItem("jwt");

  if (!raw) return null;

  try {
    const parsed = JSON.parse(raw);
    return String(parsed.token || parsed.accessToken || parsed.jwt || raw)
      .replace("Bearer ", "")
      .trim();
  } catch {
    return String(raw).replace("Bearer ", "").trim();
  }
}

async function api(path) {
  const jwt = getToken();

  const headers = {
    "Content-Type": "application/json",
  };

  if (jwt) {
    headers.Authorization = `Bearer ${jwt}`;
  }

  const res = await fetch(`${API_BASE}${path}`, { headers });
  const json = await res.json().catch(() => null);

  if (res.status === 401) {
    localStorage.removeItem("token");
    localStorage.removeItem("accessToken");
    localStorage.removeItem("jwt");
    throw new Error(json?.message || "401 Unauthorized - vui lòng đăng nhập lại");
  }

  if (res.status === 403) {
    throw new Error("403 Forbidden - tài khoản không có quyền ADMIN hoặc TECHNICAL_MANAGER");
  }

  if (!res.ok) {
    throw new Error(json?.message || `API ${res.status}`);
  }

  return json?.data ?? json;
}

function qs(params) {
  const clean = Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== "")
  );

  return new URLSearchParams(clean).toString();
}

function n(value) {
  return new Intl.NumberFormat("vi-VN").format(value || 0);
}

function formatMonth(value) {
  if (!value) return "";

  if (typeof value === "string") return value;

  if (typeof value === "object") {
    const year = value.year;
    const month = value.month || value.monthValue;
    if (year && month) {
      return `${year}-${String(month).padStart(2, "0")}`;
    }
  }

  return String(value);
}

function Kpi({ icon: Icon, label, value, hint, tone }) {
  return (
    <section className={`req-kpi ${tone}`}>
      <div className="req-kpi-icon">
        <Icon size={22} />
      </div>
      <div>
        <p>{label}</p>
        <h2>{value}</h2>
        <span>{hint}</span>
      </div>
    </section>
  );
}

function Card({ title, subtitle, children }) {
  return (
    <section className="req-card">
      <div className="req-card-head">
        <div>
          <h3>{title}</h3>
          <p>{subtitle}</p>
        </div>
      </div>

      <div className="req-chart">{children}</div>
    </section>
  );
}

export default function RequestDashboard() {
  const defaultRange = useMemo(() => {
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - 30);

    return {
      fromDate: from.toISOString().slice(0, 10),
      toDate: to.toISOString().slice(0, 10),
    };
  }, []);

  const [filters, setFilters] = useState(defaultRange);
  const [data, setData] = useState({
    stats: null,
    byCategory: [],
    resolvedByDate: [],
    byMonth: [],
    byPriority: null,
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function load() {
    try {
      setLoading(true);
      setError("");

      const query = qs({
        fromDate: filters.fromDate,
        toDate: filters.toDate,
      });

      const [stats, byCategory, resolvedByDate, byMonth, byPriority] = await Promise.all([
        api(`/api/dashboard/requests/stats?${query}`),
        api(`/api/dashboard/requests/by-category?${query}`),
        api(`/api/dashboard/requests/resolved-by-date?${query}`),
        api(`/api/dashboard/requests/by-month?${query}`),
        api(`/api/dashboard/requests/by-priority?${query}`),
      ]);

      setData({
        stats,
        byCategory: byCategory || [],
        resolvedByDate: resolvedByDate || [],
        byMonth: byMonth || [],
        byPriority,
      });
    } catch (err) {
      setError(err.message || "Không tải được dữ liệu Request Dashboard");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const priority = data.byPriority || {};

  return (
    <main className="req-page">
      <header className="req-hero">
        <div>
          <p className="req-eyebrow">Request Intelligence</p>
          <h1>Request Dashboard</h1>
          <span>Theo dõi yêu cầu, trạng thái xử lý, thời gian xử lý và mức độ ưu tiên.</span>
        </div>

        <div className="req-filters">
          <label>
            From
            <input
              type="date"
              value={filters.fromDate}
              onChange={(e) => setFilters({ ...filters, fromDate: e.target.value })}
            />
          </label>

          <label>
            To
            <input
              type="date"
              value={filters.toDate}
              onChange={(e) => setFilters({ ...filters, toDate: e.target.value })}
            />
          </label>

          <button onClick={load} disabled={loading}>
            <RefreshCw size={16} className={loading ? "spin" : ""} />
            Refresh
          </button>
        </div>
      </header>

      {error && <div className="req-error">{error}</div>}

      <section className="req-kpi-grid">
        <Kpi
          icon={CheckCircle2}
          label="Approved / Resolved"
          value={n(data.stats?.approvedCount)}
          hint="Yêu cầu đã xử lý"
          tone="green"
        />

        <Kpi
          icon={Clock3}
          label="Pending"
          value={n(data.stats?.pendingCount)}
          hint="Yêu cầu đang chờ"
          tone="orange"
        />

        <Kpi
          icon={XCircle}
          label="Cancelled / Rejected"
          value={n(data.stats?.cancelledCount)}
          hint="Yêu cầu bị hủy/từ chối"
          tone="red"
        />

        <Kpi
          icon={Layers}
          label="Avg Cycle Time"
          value={`${data.stats?.averageCycleTimeHours || 0}h`}
          hint="Thời gian xử lý trung bình"
          tone="blue"
        />
      </section>

      <section className="req-grid two">
        <Card title="Requests by Location" subtitle="Số lượng request theo location">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data.byCategory.slice(0, 10)}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="requestCount" name="Requests" radius={[10, 10, 0, 0]}>
                {data.byCategory.slice(0, 10).map((_, index) => (
                  <Cell key={index} fill={COLORS[index % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Received vs Resolved" subtitle="Request nhận mới và đã xử lý theo ngày">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data.resolvedByDate}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Legend />
              <Area
                type="monotone"
                dataKey="receivedCount"
                name="Received"
                stroke="#2563eb"
                fill="#2563eb"
                fillOpacity={0.14}
                strokeWidth={3}
              />
              <Area
                type="monotone"
                dataKey="resolvedCount"
                name="Resolved"
                stroke="#0f766e"
                fill="#0f766e"
                fillOpacity={0.14}
                strokeWidth={3}
              />
            </AreaChart>
          </ResponsiveContainer>
        </Card>
      </section>

      <section className="req-grid two">
        <Card title="Average Cycle Time by Month" subtitle="Thời gian xử lý trung bình theo tháng">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data.byMonth}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis
                dataKey="month"
                tick={{ fontSize: 12 }}
                tickFormatter={formatMonth}
              />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip labelFormatter={formatMonth} />
              <Bar
                dataKey="averageCycleTimeDays"
                name="Average cycle days"
                fill="#f97316"
                radius={[10, 10, 0, 0]}
              />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <section className="req-card req-priority">
          <div className="req-card-head">
            <div>
              <h3>Priority Breakdown</h3>
              <p>Số lượng request theo mức độ ưu tiên</p>
            </div>
          </div>

          <div className="priority-grid">
            <div className="priority-box">
              <div>
                <AlertTriangle size={18} />
                None
              </div>
              <strong>{n(priority.nonePriority?.count)}</strong>
            </div>

            <div className="priority-box">
              <div>
                <AlertTriangle size={18} />
                Low
              </div>
              <strong>{n(priority.lowPriority?.count)}</strong>
            </div>

            <div className="priority-box">
              <div>
                <AlertTriangle size={18} />
                Medium
              </div>
              <strong>{n(priority.mediumPriority?.count)}</strong>
            </div>

            <div className="priority-box">
              <div>
                <AlertTriangle size={18} />
                High
              </div>
              <strong>{n(priority.highPriority?.count)}</strong>
            </div>
          </div>
        </section>
      </section>
    </main>
  );
}