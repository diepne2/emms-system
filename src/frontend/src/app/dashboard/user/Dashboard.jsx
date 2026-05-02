import { useEffect, useMemo, useState } from "react";
import { CheckCircle2, RefreshCw, UserRound, UsersRound, Wrench } from "lucide-react";
import {
  Bar,
  BarChart,
  CartesianGrid,
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

function getToken() {
  const raw =
    localStorage.getItem("token") ||
    localStorage.getItem("accessToken") ||
    localStorage.getItem("jwt");

  if (!raw) return null;

  let token = raw;

  try {
    const parsed = JSON.parse(raw);
    token = parsed.token || parsed.accessToken || parsed.jwt || raw;
  } catch {
    token = raw;
  }

  token = String(token).replace("Bearer ", "").trim();

  if (token.split(".").length !== 3) {
    localStorage.removeItem("token");
    localStorage.removeItem("accessToken");
    localStorage.removeItem("jwt");
    return null;
  }

  return token;
}

async function api(path) {
  const jwt = getToken();

  if (!jwt) {
    window.location.href = "/login";
    throw new Error("Token không hợp lệ. Vui lòng đăng nhập lại.");
  }

  const res = await fetch(`${API_BASE}${path}`, {
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
    window.location.href = "/login";
    throw new Error(json?.message || "Unauthorized");
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
    <section className={`user-kpi ${tone}`}>
      <div className="user-kpi-icon">
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
    <section className="user-card">
      <div className="user-card-head">
        <div>
          <h3>{title}</h3>
          <p>{subtitle}</p>
        </div>
      </div>
      <div className="user-chart">{children}</div>
    </section>
  );
}

function mergeStats(list) {
  const createdCount = list.reduce((sum, x) => sum + (x?.createdCount || 0), 0);
  const completedCount = list.reduce((sum, x) => sum + (x?.completedCount || 0), 0);

  return {
    createdCount,
    completedCount,
    completionRate: createdCount === 0 ? 0 : Math.round((completedCount * 10000) / createdCount) / 100,
  };
}

function mergeStatsByDay(listOfLists) {
  const map = new Map();

  listOfLists.flat().forEach((row) => {
    if (!row?.date) return;

    const old = map.get(row.date) || {
      date: row.date,
      createdCount: 0,
      completedCount: 0,
    };

    old.createdCount += row.createdCount || 0;
    old.completedCount += row.completedCount || 0;

    map.set(row.date, old);
  });

  return Array.from(map.values()).sort((a, b) => a.date.localeCompare(b.date));
}

function getUserLabel(user) {
  return (
    user.fullName ||
    user.name ||
    user.username ||
    user.email ||
    `User #${user.userId || user.id}`
  );
}

function getUserId(user) {
  return user.userId || user.id;
}

export default function UserDashboard() {
  const defaultRange = useMemo(() => {
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - 30);

    return {
      userId: "ALL",
      status: "ALL",
      fromDate: from.toISOString().slice(0, 10),
      toDate: to.toISOString().slice(0, 10),
    };
  }, []);

  const [filters, setFilters] = useState(defaultRange);
  const [users, setUsers] = useState([]);
  const [data, setData] = useState({
    stats: null,
    statsByDay: [],
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const filteredUsers = useMemo(() => {
    return users.filter((user) => {
      if (filters.status === "ALL") return true;
      return String(user.status || "").toUpperCase() === filters.status;
    });
  }, [users, filters.status]);

  const selectedUserName = useMemo(() => {
    if (filters.userId === "ALL") return "Tất cả user";
    const user = users.find((u) => String(getUserId(u)) === String(filters.userId));
    return user ? getUserLabel(user) : `User #${filters.userId}`;
  }, [filters.userId, users]);

  async function loadUsers() {
    const result = await api("/api/users/all");
    setUsers(result || []);
  }

  async function loadDashboard() {
    try {
      setLoading(true);
      setError("");

      const query = qs({
        fromDate: filters.fromDate,
        toDate: filters.toDate,
      });

      const targetUsers =
        filters.userId === "ALL"
          ? filteredUsers
          : filteredUsers.filter((u) => String(getUserId(u)) === String(filters.userId));

      if (targetUsers.length === 0) {
        setData({
          stats: { createdCount: 0, completedCount: 0, completionRate: 0 },
          statsByDay: [],
        });
        return;
      }

      const statResults = await Promise.all(
        targetUsers.map((user) => api(`/api/dashboard/users/${getUserId(user)}/stats?${query}`))
      );

      const dayResults = await Promise.all(
        targetUsers.map((user) => api(`/api/dashboard/users/${getUserId(user)}/stats-by-day?${query}`))
      );

      setData({
        stats: mergeStats(statResults),
        statsByDay: mergeStatsByDay(dayResults),
      });
    } catch (err) {
      setError(err.message || "Không tải được dữ liệu User Dashboard");
    } finally {
      setLoading(false);
    }
  }

  async function loadAll() {
    try {
      setLoading(true);
      setError("");

      const userResult = await api("/api/users/all");
      setUsers(userResult || []);

      const query = qs({
        fromDate: filters.fromDate,
        toDate: filters.toDate,
      });

      const targetUsers = userResult || [];

      const statResults = await Promise.all(
        targetUsers.map((user) => api(`/api/dashboard/users/${getUserId(user)}/stats?${query}`))
      );

      const dayResults = await Promise.all(
        targetUsers.map((user) => api(`/api/dashboard/users/${getUserId(user)}/stats-by-day?${query}`))
      );

      setData({
        stats: mergeStats(statResults),
        statsByDay: mergeStatsByDay(dayResults),
      });
    } catch (err) {
      setError(err.message || "Không tải được dữ liệu User Dashboard");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  return (
    <main className="user-page">
      <header className="user-hero">
        <div>
          <p className="user-eyebrow">Technician Performance</p>
          <h1>User Dashboard</h1>
        </div>

        <div className="user-filters">
          <label>
            Status
            <select
              value={filters.status}
              onChange={(e) =>
                setFilters({
                  ...filters,
                  status: e.target.value,
                  userId: "ALL",
                })
              }
            >
              <option value="ALL">Tất cả status</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="INVITED">INVITED</option>
              <option value="SUSPENDED">SUSPENDED</option>
            </select>
          </label>

          <label>
            User
            <select
              value={filters.userId}
              onChange={(e) => setFilters({ ...filters, userId: e.target.value })}
            >
              <option value="ALL">Tất cả user</option>
              {filteredUsers.map((user) => (
                <option key={getUserId(user)} value={getUserId(user)}>
                  {getUserLabel(user)} {user.status ? `- ${user.status}` : ""}
                </option>
              ))}
            </select>
          </label>

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

          <button onClick={loadDashboard} disabled={loading}>
            <RefreshCw size={16} className={loading ? "spin" : ""} />
            Refresh
          </button>
        </div>
      </header>

      {error && <div className="user-error">{error}</div>}

      <section className="user-kpi-grid">
        <Kpi
          icon={filters.userId === "ALL" ? UsersRound : UserRound}
          label="User"
          value={selectedUserName}
          hint={`${filteredUsers.length} user trong filter`}
          tone="blue"
        />

        <Kpi
          icon={Wrench}
          label="Created WO"
          value={n(data.stats?.createdCount)}
          hint="WO được giao / tạo"
          tone="orange"
        />

        <Kpi
          icon={CheckCircle2}
          label="Completed WO"
          value={n(data.stats?.completedCount)}
          hint="WO đã hoàn thành"
          tone="green"
        />

        <Kpi
          icon={CheckCircle2}
          label="Completion Rate"
          value={`${data.stats?.completionRate || 0}%`}
          hint="Tỷ lệ hoàn thành"
          tone="purple"
        />
      </section>

      <section className="user-grid two">
        <Card title="Created vs Completed by Day" subtitle="So sánh WO tạo/giao và WO hoàn thành">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data.statsByDay}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="createdCount" name="Created" fill="#2563eb" radius={[8, 8, 0, 0]} />
              <Bar dataKey="completedCount" name="Completed" fill="#14b8a6" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Completion Trend" subtitle="Xu hướng WO hoàn thành theo ngày">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data.statsByDay}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Line type="monotone" dataKey="completedCount" name="Completed" stroke="#14b8a6" strokeWidth={3} />
              <Line type="monotone" dataKey="createdCount" name="Created" stroke="#2563eb" strokeWidth={3} />
            </LineChart>
          </ResponsiveContainer>
        </Card>
      </section>

      <section className="user-card user-table-card">
        <div className="user-card-head">
          <div>
            <h3>Daily Detail</h3>
            <p>Chi tiết created/completed theo từng ngày</p>
          </div>
        </div>

        <div className="user-table-wrap">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Created</th>
                <th>Completed</th>
              </tr>
            </thead>
            <tbody>
              {data.statsByDay.map((row) => (
                <tr key={row.date}>
                  <td>{row.date}</td>
                  <td>{n(row.createdCount)}</td>
                  <td>{n(row.completedCount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}