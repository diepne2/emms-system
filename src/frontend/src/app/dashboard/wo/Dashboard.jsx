import { useEffect, useMemo, useState } from "react";
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  RefreshCw,
  UserRound,
  Wrench,
} from "lucide-react";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import "./Dashboard.css";

const BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "https://emms-system-production-4239.up.railway.app"
const COLORS = ["#2563eb", "#14b8a6", "#f97316", "#ef4444", "#8b5cf6"];

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

  if (!jwt) {
    throw new Error("Chưa đăng nhập. Vui lòng login lại.");
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${jwt}`,
    },
  });

  const json = await res.json().catch(() => null);

  if (res.status === 401) {
    localStorage.removeItem("token");
    localStorage.removeItem("accessToken");
    localStorage.removeItem("jwt");
    throw new Error(json?.message || "401 Unauthorized - token hết hạn hoặc không hợp lệ");
  }

  if (res.status === 403) {
    throw new Error("403 Forbidden - đã đăng nhập nhưng vẫn bị chặn quyền");
  }

  if (!res.ok) {
    throw new Error(json?.message || `API ${res.status}`);
  }

  return json?.data ?? json;
}

function qs(params) {
  const clean = Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== "")
  );
  return new URLSearchParams(clean).toString();
}

function n(value) {
  return new Intl.NumberFormat("vi-VN").format(value || 0);
}

function Kpi({ icon: Icon, label, value, hint, tone }) {
  return (
    <section className={`wo-kpi ${tone}`}>
      <div className="wo-kpi-icon">
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
    <section className="wo-card">
      <div className="wo-card-head">
        <div>
          <h3>{title}</h3>
          <p>{subtitle}</p>
        </div>
      </div>
      <div className="wo-chart">{children}</div>
    </section>
  );
}

export default function WODashboard() {
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
    statuses: null,
    statusesByDate: [],
    countByWeek: [],
    timeByWeek: [],
    countByUser: [],
    hours: null,
    incompleteStats: null,
    incompleteByUser: [],
    incompleteByAsset: [],
    priorityStats: null,
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

      const [
        stats,
        statuses,
        statusesByDate,
        countByWeek,
        timeByWeek,
        countByUser,
        hours,
        incompleteStats,
        incompleteByUser,
        incompleteByAsset,
        priorityStats,
      ] = await Promise.all([
        api(`/api/dashboard/work-orders/stats?${query}`),
        api(`/api/dashboard/work-orders/statuses?${query}`),
        api(`/api/dashboard/work-orders/statuses-by-date?${query}`),
        api(`/api/dashboard/work-orders/count-by-week?${query}`),
        api(`/api/dashboard/work-orders/time-by-week?${query}`),
        api(`/api/dashboard/work-orders/count-by-user?${query}`),
        api(`/api/dashboard/work-orders/hours?${query}`),
        api(`/api/dashboard/work-orders/incomplete-stats?${query}`),
        api(`/api/dashboard/work-orders/incomplete-by-user?${query}`),
        api(`/api/dashboard/work-orders/incomplete-by-asset?${query}`),
        api(`/api/dashboard/work-orders/priority-stats?${query}`),
      ]);

      setData({
        stats,
        statuses,
        statusesByDate: statusesByDate || [],
        countByWeek: countByWeek || [],
        timeByWeek: timeByWeek || [],
        countByUser: countByUser || [],
        hours,
        incompleteStats,
        incompleteByUser: incompleteByUser || [],
        incompleteByAsset: incompleteByAsset || [],
        priorityStats,
      });
    } catch (err) {
      setError(err.message || "Không tải được dữ liệu Work Order Dashboard");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const statusChart = data.statuses
    ? [
        { name: "Open", value: data.statuses.openCount || 0 },
        { name: "In Progress", value: data.statuses.inProgressCount || 0 },
        { name: "On Hold", value: data.statuses.onHoldCount || 0 },
        { name: "Done", value: data.statuses.doneCount || 0 },
      ]
    : [];

  const priority = data.priorityStats || {};

  return (
    <main className="wo-page">
      <header className="wo-hero">
        <div>
          <p className="wo-eyebrow">Work Order Command Center</p>
          <h1>Work Order Dashboard</h1>
          <span>Theo dõi KPI, trạng thái, backlog và giờ bảo trì.</span>
        </div>

        <div className="wo-filters">
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

      {error && <div className="wo-error">{error}</div>}

      <section className="wo-kpi-grid">
        <Kpi icon={Wrench} label="Total WO" value={n(data.stats?.totalCount)} hint="Tổng work order" tone="blue" />
        <Kpi icon={CheckCircle2} label="Completed" value={n(data.stats?.completedCount)} hint="WO hoàn thành" tone="green" />
        <Kpi icon={Clock3} label="Avg Cycle" value={`${data.stats?.averageCycleTimeHours || 0}h`} hint="Thời gian xử lý TB" tone="orange" />
        <Kpi icon={CheckCircle2} label="Completion Rate" value={`${data.stats?.completionRate || 0}%`} hint="Tỷ lệ hoàn thành" tone="purple" />
        <Kpi icon={AlertTriangle} label="Incomplete" value={n(data.incompleteStats?.totalIncompleteCount)} hint="WO chưa hoàn thành" tone="red" />
        <Kpi icon={Clock3} label="Avg Age" value={`${data.incompleteStats?.averageAgeDays || 0}d`} hint="Tuổi backlog TB" tone="orange" />
      </section>

      <section className="wo-grid two">
        <Card title="WO Statuses" subtitle="Số lượng work order theo trạng thái">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={statusChart}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="value" name="Count" radius={[10, 10, 0, 0]}>
                {statusChart.map((_, index) => (
                  <Cell key={index} fill={COLORS[index % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Statuses by Date" subtitle="Open / In Progress / Done theo ngày">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data.statusesByDate}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Line type="monotone" dataKey="openCount" name="Open" stroke="#2563eb" strokeWidth={3} />
              <Line type="monotone" dataKey="inProgressCount" name="In Progress" stroke="#f97316" strokeWidth={3} />
              <Line type="monotone" dataKey="doneCount" name="Done" stroke="#14b8a6" strokeWidth={3} />
            </LineChart>
          </ResponsiveContainer>
        </Card>
      </section>

      <section className="wo-grid two">
        <Card title="WO Count by Week" subtitle="Tổng WO theo tuần">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data.countByWeek}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="weekStart" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="totalCount" name="Total" fill="#2563eb" radius={[10, 10, 0, 0]} />
              <Bar dataKey="compliantCount" name="Done" fill="#14b8a6" radius={[10, 10, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Time by Week" subtitle="Giờ bảo trì ước tính theo tuần">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data.timeByWeek}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="weekStart" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Area
                type="monotone"
                dataKey="totalHours"
                name="Total hours"
                stroke="#ea580c"
                fill="#ea580c"
                fillOpacity={0.18}
                strokeWidth={3}
              />
            </AreaChart>
          </ResponsiveContainer>
        </Card>
      </section>

      <section className="wo-grid two">
        <section className="wo-card">
          <div className="wo-card-head">
            <h3>WO by User</h3>
            <p>Số lượng work order theo nhân sự</p>
          </div>

          <div className="wo-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>User</th>
                  <th>Total WO</th>
                </tr>
              </thead>
              <tbody>
                {data.countByUser.slice(0, 10).map((item) => (
                  <tr key={item.id || item.username}>
                    <td>
                      <span className="user-cell">
                        <UserRound size={16} />
                        {item.fullName || item.username || "Unknown"}
                      </span>
                    </td>
                    <td>{n(item.totalCount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="wo-card">
          <div className="wo-card-head">
            <h3>Incomplete by Asset</h3>
            <p>Backlog theo tài sản</p>
          </div>

          <div className="wo-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Incomplete</th>
                  <th>Avg Age</th>
                </tr>
              </thead>
              <tbody>
                {data.incompleteByAsset.slice(0, 10).map((item) => (
                  <tr key={item.id || item.name}>
                    <td>{item.name || "Unknown"}</td>
                    <td>{n(item.incompleteCount)}</td>
                    <td>{item.averageAgeDays || 0}d</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section className="wo-card wo-priority">
        <div className="wo-card-head">
          <h3>Priority Stats</h3>
          <p>Thống kê work order theo mức độ ưu tiên</p>
        </div>

        <div className="wo-priority-grid">
          <div className="wo-priority-box">
            <span>None</span>
            <strong>{n(priority.nonePriority?.count)}</strong>
            <p>{priority.nonePriority?.estimatedHours || 0}h</p>
          </div>

          <div className="wo-priority-box">
            <span>Low</span>
            <strong>{n(priority.lowPriority?.count)}</strong>
            <p>{priority.lowPriority?.estimatedHours || 0}h</p>
          </div>

          <div className="wo-priority-box">
            <span>Medium</span>
            <strong>{n(priority.mediumPriority?.count)}</strong>
            <p>{priority.mediumPriority?.estimatedHours || 0}h</p>
          </div>

          <div className="wo-priority-box">
            <span>High</span>
            <strong>{n(priority.highPriority?.count)}</strong>
            <p>{priority.highPriority?.estimatedHours || 0}h</p>
          </div>
        </div>
      </section>
    </main>
  );
}