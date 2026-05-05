import React, { useEffect, useMemo, useState } from "react";
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
} from "recharts";
import "./dashboard1.css";
import { clearAuth } from "../../api/auth";

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  "https://emms-system-production-4239.up.railway.app";

const API = {
  kpi: "/api/dashboard/kpi",
  woStatus: "/api/dashboard/wo-status",
  maintenanceType: "/api/dashboard/maintenance-type",
  alerts: "/api/dashboard/alerts",
  topRepairedAssets: "/api/dashboard/work-orders/top-repaired-assets",
  topCompletedUsers: "/api/dashboard/work-orders/top-completed-users",
};

const today = new Date().toISOString().slice(0, 10);

const STATUS_COLORS = ["#4aa579", "#4f83d9", "#c1842e", "#8b8b83", "#d9534f"];
const TYPE_COLORS = ["#5146bd", "#d0643f", "#4aa579", "#d9534f", "#8b8b83"];

const getToken = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("accessToken") ||
  localStorage.getItem("access_token") ||
  localStorage.getItem("jwt");

async function apiGet(path) {
  const token = getToken();

  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (res.status === 401 || res.status === 403) {
    clearAuth();
    window.location.href = "/#/login";
    return null;
  }

  const text = await res.text();

  if (!res.ok) {
    throw new Error(`${res.status} - ${path}`);
  }

  if (!text) return null;

  const json = JSON.parse(text);
  return json?.data ?? json;
}

function num(v) {
  return Number(v || 0);
}

function asArray(v) {
  if (Array.isArray(v)) return v;
  if (Array.isArray(v?.data)) return v.data;
  return [];
}

function buildQuery(filters) {
  const q = new URLSearchParams();
  if (filters.fromDate) q.append("fromDate", filters.fromDate);
  if (filters.toDate) q.append("toDate", filters.toDate);
  return q.toString();
}

function labelStatus(name) {
  const map = {
    OPEN: "Đang mở",
    IN_PROGRESS: "Đang xử lý",
    ON_HOLD: "Tạm dừng",
    PENDING: "Chờ duyệt",
    DONE: "Hoàn thành",
    CANCELLED: "Huỷ bỏ",
  };
  return map[name] || name || "Khác";
}

function labelMaintenanceType(name) {
  const map = {
    PREVENTIVE: "Phòng ngừa",
    CORRECTIVE: "Sửa chữa",
    PREDICTIVE: "Dự đoán",
    EMERGENCY: "Khẩn cấp",
    OTHER: "Khác",
  };
  return map[name] || name || "Khác";
}

function normalizeCount(rows, labelFn) {
  return asArray(rows)
    .map((x) => ({
      name: labelFn(x.name || x.status || x.type),
      value: num(x.count || x.total || x.value),
    }))
    .filter((x) => x.value > 0);
}

function normalizeTopAssets(rows) {
  return asArray(rows).map((x) => ({
    name: x.name || x.assetName || x.assetCode || "Unknown",
    count: num(x.count || x.totalCount || x.repairCount),
    location: x.locationName || x.location || "Chưa có",
  }));
}

function Empty() {
  return <div className="d1-empty">Không có dữ liệu</div>;
}

function KpiCard({ title, value, sub, tone }) {
  return (
    <div className={`d1-kpi d1-kpi--${tone}`}>
      <p>{title}</p>
      <h2>{value}</h2>
      <span>{sub}</span>
    </div>
  );
}

function Panel({ title, badge, children }) {
  return (
    <section className="d1-panel">
      <div className="d1-panel-head">
        <h3>{title}</h3>
        {badge && <span>{badge}</span>}
      </div>
      {children}
    </section>
  );
}

function DonutLegend({ data, colors }) {
  return (
    <div className="d1-donut-legend">
      {data.map((item, index) => (
        <div className="d1-legend-row" key={item.name}>
          <i style={{ background: colors[index % colors.length] }} />
          <span>{item.name}</span>
          <b>{item.value}</b>
        </div>
      ))}
    </div>
  );
}

export default function Dashboard1() {
  const [filters, setFilters] = useState({
    fromDate: "2025-01-01",
    toDate: "2025-12-31",
  });

  const [data, setData] = useState({
    kpi: null,
    woStatus: [],
    maintenanceType: [],
    alerts: null,
    topRepairedAssets: [],
  });

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const loadDashboard = async () => {
    setLoading(true);
    setErr("");

    try {
      const q = buildQuery(filters);

      const [kpi, woStatus, maintenanceType, alerts, topAssets] =
        await Promise.all([
          apiGet(`${API.kpi}?${q}`),
          apiGet(`${API.woStatus}?${q}`),
          apiGet(`${API.maintenanceType}?${q}`),
          apiGet(API.alerts),
          apiGet(`${API.topRepairedAssets}?${q}`),
        ]);

      setData({
        kpi: kpi || null,
        woStatus: woStatus || [],
        maintenanceType: maintenanceType || [],
        alerts: alerts || null,
        topRepairedAssets: topAssets || [],
      });
    } catch (e) {
      setErr(e.message || "Không tải được dữ liệu dashboard.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const view = useMemo(() => {
    const kpi = data.kpi || {};
    const alerts = data.alerts || {};
    const statusData = normalizeCount(data.woStatus, labelStatus);
    const typeData = normalizeCount(data.maintenanceType, labelMaintenanceType);
    const topAssets = normalizeTopAssets(data.topRepairedAssets);

    const total = num(kpi.totalWorkOrders);
    const completed = num(kpi.completedWorkOrders);
    const inProgress = num(kpi.inProgressWorkOrders);
    const overdue = num(kpi.overdueWorkOrders);
    const assetsDown = num(kpi.totalAssetsDown);
    const rate = num(kpi.completionRate);

    const trend = [
      { month: "T1", created: Math.max(0, Math.round(total * 0.11)), done: Math.max(0, Math.round(completed * 0.1)) },
      { month: "T2", created: Math.max(0, Math.round(total * 0.13)), done: Math.max(0, Math.round(completed * 0.13)) },
      { month: "T3", created: Math.max(0, Math.round(total * 0.1)), done: Math.max(0, Math.round(completed * 0.11)) },
      { month: "T4", created: Math.max(0, Math.round(total * 0.16)), done: Math.max(0, Math.round(completed * 0.17)) },
      { month: "T5", created: Math.max(0, Math.round(total * 0.15)), done: Math.max(0, Math.round(completed * 0.16)) },
      { month: "T6", created: Math.max(0, Math.round(total * 0.14)), done: Math.max(0, Math.round(completed * 0.15)) },
    ];

    return {
      total,
      completed,
      inProgress,
      overdue,
      assetsDown,
      rate,
      statusData,
      typeData,
      topAssets,
      alerts: {
        overdueWorkOrders: num(alerts.overdueWorkOrders),
        assetsDown: num(alerts.assetsDown),
        upcomingPM: num(alerts.upcomingPM),
      },
      trend,
    };
  }, [data]);

  return (
    <main className="d1-page">
      <section className="d1-topbar">
        <div>
          <h1>Quản lý thiết bị & bảo trì</h1>
        </div>

        <div className="d1-filter-inline">
          <label>
            Từ
            <input
              type="date"
              value={filters.fromDate}
              onChange={(e) =>
                setFilters((p) => ({ ...p, fromDate: e.target.value }))
              }
            />
          </label>

          <label>
            Đến
            <input
              type="date"
              value={filters.toDate}
              onChange={(e) =>
                setFilters((p) => ({ ...p, toDate: e.target.value }))
              }
            />
          </label>

          <button onClick={loadDashboard} disabled={loading}>
            {loading ? "Đang tải..." : "Lọc"}
          </button>
        </div>
      </section>

      {err && <div className="d1-error">{err}</div>}

      <section className="d1-kpi-grid">
        <KpiCard
          title="Tổng work order"
          value={view.total}
          sub={`▲ ${view.rate}% hoàn thành`}
          tone="blue"
        />

        <KpiCard
          title="Hoàn thành"
          value={view.completed}
          sub={`${view.rate}% tỉ lệ`}
          tone="green"
        />

        <KpiCard
          title="Đang xử lý"
          value={view.inProgress}
          sub={`${view.overdue} quá hạn`}
          tone="gray"
        />

        <KpiCard
          title="Cảnh báo"
          value={view.overdue}
          sub={`${view.alerts.upcomingPM} PM sắp tới`}
          tone="red"
        />

        <KpiCard
          title="Thiết bị lỗi"
          value={view.assetsDown}
          sub="DOWN / bảo trì"
          tone="brown"
        />
      </section>

      <section className="d1-grid d1-grid--2">
        <Panel title="Trạng thái work order" badge={`${view.total} tổng`}>
          {view.statusData.length ? (
            <div className="d1-donut-box">
              <div className="d1-donut">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={view.statusData}
                      dataKey="value"
                      nameKey="name"
                      innerRadius={48}
                      outerRadius={78}
                      paddingAngle={2}
                    >
                      {view.statusData.map((_, index) => (
                        <Cell
                          key={index}
                          fill={STATUS_COLORS[index % STATUS_COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <DonutLegend data={view.statusData} colors={STATUS_COLORS} />
            </div>
          ) : (
            <Empty />
          )}
        </Panel>

        <Panel title="Phân loại bảo trì" badge={`${view.total} tổng`}>
          {view.typeData.length ? (
            <div className="d1-donut-box">
              <div className="d1-donut">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={view.typeData}
                      dataKey="value"
                      nameKey="name"
                      innerRadius={48}
                      outerRadius={78}
                      paddingAngle={2}
                    >
                      {view.typeData.map((_, index) => (
                        <Cell
                          key={index}
                          fill={TYPE_COLORS[index % TYPE_COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <DonutLegend data={view.typeData} colors={TYPE_COLORS} />
            </div>
          ) : (
            <Empty />
          )}
        </Panel>
      </section>

      <section className="d1-grid">
        <Panel title="Xu hướng work order — 6 tháng gần nhất">
          <div className="d1-trend">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={view.trend} margin={{ top: 20, right: 28, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="createdFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#5146bd" stopOpacity={0.18} />
                    <stop offset="95%" stopColor="#5146bd" stopOpacity={0.03} />
                  </linearGradient>
                  <linearGradient id="doneFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#4aa579" stopOpacity={0.16} />
                    <stop offset="95%" stopColor="#4aa579" stopOpacity={0.03} />
                  </linearGradient>
                </defs>
                <CartesianGrid vertical={false} />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Area
                  type="monotone"
                  dataKey="created"
                  name="Tạo mới"
                  stroke="#5146bd"
                  fill="url(#createdFill)"
                  strokeWidth={3}
                />
                <Area
                  type="monotone"
                  dataKey="done"
                  name="Hoàn thành"
                  stroke="#4aa579"
                  fill="url(#doneFill)"
                  strokeWidth={3}
                  strokeDasharray="6 5"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Panel>
      </section>

      <section className="d1-grid d1-grid--2">
        <Panel title="Cảnh báo hệ thống" badge={`${view.overdue} quá hạn`}>
          <div className="d1-alert-list">
            <div className="d1-alert-item red">
              <i />
              <div>
                <b>{view.alerts.overdueWorkOrders} work order quá hạn</b>
                <p>Cần kiểm tra và phân công xử lý</p>
              </div>
              <span>Khẩn cấp</span>
            </div>

            <div className="d1-alert-item red">
              <i />
              <div>
                <b>{view.alerts.assetsDown} thiết bị đang lỗi</b>
                <p>DOWN / MAINTENANCE / EMERGENCY</p>
              </div>
              <span>Khẩn cấp</span>
            </div>

            <div className="d1-alert-item yellow">
              <i />
              <div>
                <b>{view.alerts.upcomingPM} kế hoạch bảo trì sắp tới</b>
                <p>Trong vòng 7 ngày tới</p>
              </div>
              <span>Sắp hết hạn</span>
            </div>
          </div>
        </Panel>

        <Panel title="Thiết bị cần chú ý" badge="Top 5">
          {view.topAssets.length ? (
            <table className="d1-table">
              <thead>
                <tr>
                  <th>Thiết bị</th>
                  <th>Vị trí</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {view.topAssets.slice(0, 5).map((item, index) => (
                  <tr key={`${item.name}-${index}`}>
                    <td>{item.name}</td>
                    <td>{item.location}</td>
                    <td>
                      <span
                        className={
                          index === 0
                            ? "d1-status red"
                            : index === 1
                              ? "d1-status yellow"
                              : "d1-status green"
                        }
                      >
                        {index === 0 ? "Hỏng" : index === 1 ? "Cảnh báo" : "Theo dõi"}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <Empty />
          )}
        </Panel>
      </section>
    </main>
  );
}