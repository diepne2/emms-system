import { useEffect, useMemo, useState } from "react";

import {
  Activity,
  AlertTriangle,
  Clock3,
  Factory,
  RefreshCw,
  Wrench,
  Wallet,
} from "lucide-react";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import "./Dashboard.css";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const COLORS = ["#2563eb", "#14b8a6", "#f97316", "#ef4444", "#8b5cf6", "#64748b"];

function getToken() {
  const token =
    localStorage.getItem("token") ||
    localStorage.getItem("accessToken") ||
    localStorage.getItem("jwt");

  if (token) return token;

  try {
    const user = JSON.parse(localStorage.getItem("user") || "{}");
    return user.token || user.accessToken || user.jwt || "";
  } catch {
    return "";
  }
}

function isValidJwt(token) {
  return typeof token === "string" && token.split(".").length === 3;
}

async function api(path) {
  const token = getToken();

  if (!isValidJwt(token)) {
    throw new Error("Bạn chưa đăng nhập hoặc token không hợp lệ");
  }

  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  const json = await res.json().catch(() => null);

  if (!res.ok) {
    throw new Error(json?.message || `API error ${res.status}`);
  }

  return json?.data ?? json;
}

function qs(params) {
  const clean = Object.fromEntries(
    Object.entries(params).filter(
      ([, value]) => value !== "" && value !== null && value !== undefined
    )
  );

  return new URLSearchParams(clean).toString();
}

function n(value) {
  return new Intl.NumberFormat("vi-VN").format(value || 0);
}

function money(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value || 0);
}

function hoursFromSeconds(seconds) {
  return `${((seconds || 0) / 3600).toFixed(1)}h`;
}

function Kpi({ icon: Icon, label, value, hint, tone }) {
  return (
    <div className={`asset-kpi ${tone}`}>
      <div className="asset-kpi-icon">
        <Icon size={22} />
      </div>
      <div>
        <p>{label}</p>
        <h2>{value}</h2>
        <span>{hint}</span>
      </div>
    </div>
  );
}

function Card({ title, subtitle, children }) {
  return (
    <section className="asset-card">
      <div className="asset-card-head">
        <h3>{title}</h3>
        <p>{subtitle}</p>
      </div>
      <div className="asset-chart">{children}</div>
    </section>
  );
}

function AssetDropdown({ assets, value, onChange }) {
  return (
    <label className="asset-filter-select-label">
      Asset
      <select
        className="asset-filter-select"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        <option value="">Tất cả asset</option>

        {assets.map((asset) => (
          <option key={asset.id} value={asset.name}>
            {asset.name} {asset.status ? `- ${asset.status}` : ""}
          </option>
        ))}
      </select>
    </label>
  );
}

export default function Dashboard() {
  const defaultRange = useMemo(() => {
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - 30);

    return {
      fromDate: from.toISOString().slice(0, 10),
      toDate: to.toISOString().slice(0, 10),
      assetName: "",
    };
  }, []);

  const [filters, setFilters] = useState(defaultRange);
  const [assets, setAssets] = useState([]);
  const [data, setData] = useState({
    overview: null,
    stats: null,
    downtimesByAsset: [],
    downtimesByDate: [],
    meantimeByDate: [],
    meantimes: null,
    mtbfByAsset: [],
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const downtimeByDateChart = useMemo(() => {
    return (data.downtimesByDate || []).map((item) => ({
      ...item,
      totalDowntimeHours: Number(((item.totalDowntime || 0) / 3600).toFixed(2)),
    }));
  }, [data.downtimesByDate]);

  async function loadAssets() {
    try {
      const res = await api("/api/dashboard/assets/options");

      const list = Array.isArray(res)
        ? res
        : Array.isArray(res?.data)
        ? res.data
        : Array.isArray(res?.content)
        ? res.content
        : [];

      setAssets(list);
    } catch (err) {
      console.error("Load assets failed:", err);
      setAssets([]);
    }
  }

  async function load() {
    try {
      setLoading(true);
      setError("");

      const query = qs(filters);

      const [
        overview,
        stats,
        downtimesByAsset,
        downtimesByDate,
        meantimeByDate,
        meantimes,
        mtbfByAsset,
      ] = await Promise.all([
        api(`/api/dashboard/assets/overview?${query}`),
        api(`/api/dashboard/assets/stats?${query}`),
        api(`/api/dashboard/assets/downtimes/by-asset?${query}`),
        api(`/api/dashboard/assets/downtimes/by-date?${query}`),
        api(`/api/dashboard/assets/downtimes/meantime-by-date?${query}`),
        api(`/api/dashboard/assets/meantimes?${query}`),
        api(`/api/dashboard/assets/mtbf/by-asset?${query}`),
      ]);

      setData({
        overview,
        stats,
        downtimesByAsset: downtimesByAsset || [],
        downtimesByDate: downtimesByDate || [],
        meantimeByDate: meantimeByDate || [],
        meantimes,
        mtbfByAsset: mtbfByAsset || [],
      });
    } catch (err) {
      setError(err.message || "Không tải được dữ liệu Asset Dashboard");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAssets();
    load();
  }, []);

  return (
    <main className="asset-page">
      <header className="asset-hero">
        <div>
          <p className="asset-eyebrow">Asset Intelligence</p>
          <h1>Asset Dashboard</h1>
        </div>

        <div className="asset-filters">
          <label>
            From
            <input
              type="date"
              value={filters.fromDate}
              onChange={(e) =>
                setFilters({ ...filters, fromDate: e.target.value })
              }
            />
          </label>

          <label>
            To
            <input
              type="date"
              value={filters.toDate}
              onChange={(e) =>
                setFilters({ ...filters, toDate: e.target.value })
              }
            />
          </label>

          <AssetDropdown
            assets={assets}
            value={filters.assetName}
            onChange={(assetName) => setFilters({ ...filters, assetName })}
          />

          <button onClick={load} disabled={loading}>
            <RefreshCw size={16} className={loading ? "spin" : ""} />
            Refresh
          </button>
        </div>
      </header>

      {error && <div className="asset-error">{error}</div>}

      <section className="asset-kpi-grid">
        <Kpi
          icon={Factory}
          label="Availability"
          value={`${data.stats?.availability || 0}%`}
          hint="Tỷ lệ sẵn sàng"
          tone="blue"
        />

        <Kpi
          icon={AlertTriangle}
          label="Downtime Events"
          value={n(data.stats?.downtimeEvents)}
          hint={hoursFromSeconds(data.stats?.totalDowntime)}
          tone="red"
        />

        <Kpi
          icon={Activity}
          label="MTBF"
          value={hoursFromSeconds(data.overview?.mtbf)}
          hint="Mean time between failures"
          tone="green"
        />

        <Kpi
          icon={Wrench}
          label="MTTR"
          value={hoursFromSeconds(data.overview?.mttr)}
          hint="Mean time to repair"
          tone="orange"
        />

        <Kpi
          icon={Clock3}
          label="Maintenance Interval"
          value={`${data.meantimes?.maintenanceIntervalHours || 0}h`}
          hint="Khoảng cách bảo trì TB"
          tone="purple"
        />

        <Kpi
          icon={Wallet}
          label="Total Cost"
          value={money(data.overview?.totalCost)}
          hint="Chi phí WO thực tế"
          tone="blue"
        />
      </section>

      <section className="asset-grid two">
        <Card title="Downtime by Asset" subtitle="Top tài sản có số lần downtime cao">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={(data.downtimesByAsset || []).slice(0, 10)}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar
                dataKey="downtimeCount"
                name="Downtime events"
                radius={[10, 10, 0, 0]}
              >
                {(data.downtimesByAsset || []).slice(0, 10).map((_, index) => (
                  <Cell key={index} fill={COLORS[index % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card title="Downtime Hours by Date" subtitle="Tổng downtime theo ngày">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={downtimeByDateChart}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Area
                type="monotone"
                dataKey="totalDowntimeHours"
                name="Downtime hours"
                stroke="#2563eb"
                fill="#2563eb"
                fillOpacity={0.16}
                strokeWidth={3}
              />
            </AreaChart>
          </ResponsiveContainer>
        </Card>
      </section>

      <section className="asset-grid two">
        <Card title="Average Downtime by Date" subtitle="Thời gian downtime trung bình">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data.meantimeByDate || []}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Area
                type="monotone"
                dataKey="averageDowntimeHours"
                name="Average downtime hours"
                stroke="#f97316"
                fill="#f97316"
                fillOpacity={0.18}
                strokeWidth={3}
              />
            </AreaChart>
          </ResponsiveContainer>
        </Card>

        <section className="asset-card">
          <div className="asset-card-head">
            <h3>MTBF by Asset</h3>
            <p>Xếp hạng độ tin cậy tài sản</p>
          </div>

          <div className="asset-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>MTBF Hours</th>
                </tr>
              </thead>
              <tbody>
                {(data.mtbfByAsset || []).slice(0, 10).map((item) => (
                  <tr key={item.id}>
                    <td>{item.name}</td>
                    <td>{item.mtbfHours}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </section>
    </main>
  );
}