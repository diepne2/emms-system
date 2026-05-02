import React, { useEffect, useMemo, useState } from "react";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
} from "recharts";
import "./dashboard1.css";

const API_BASE = "";

const API = {
  topRepairedAssets: "/api/dashboard/work-orders/top-repaired-assets",
  topCompletedUsers: "/api/dashboard/work-orders/top-completed-users",
};

const today = new Date().toISOString().slice(0, 10);

const getToken = () =>
  localStorage.getItem("token") ||
  localStorage.getItem("accessToken") ||
  localStorage.getItem("jwt");

async function apiGet(path) {
  const token = getToken();

  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  const text = await res.text();

  if (!res.ok) {
    throw new Error(`${res.status} - ${path}`);
  }

  if (!text) return null;

  const json = JSON.parse(text);
  return json?.data ?? json;
}

function asArray(v) {
  if (Array.isArray(v)) return v;
  if (Array.isArray(v?.data)) return v.data;
  return [];
}

function num(v) {
  return Number(v || 0);
}

function buildQuery(filters) {
  const q = new URLSearchParams();

  if (filters.fromDate) q.append("fromDate", filters.fromDate);
  if (filters.toDate) q.append("toDate", filters.toDate);

  return q.toString();
}

function Empty() {
  return <div className="d1-empty">Không có dữ liệu.</div>;
}

function Panel({ title, desc, children }) {
  return (
    <section className="d1-panel">
      <div className="d1-panel__head">
        <div>
          <h3>{title}</h3>
          <p>{desc}</p>
        </div>
      </div>
      <div className="d1-panel__body">{children}</div>
    </section>
  );
}

function normalizeTopRepairedAssets(rows) {
  return asArray(rows).map((x) => ({
    assetName: x.name || x.assetName || "Unknown",
    count: num(x.totalCount || x.count || x.repairCount),
  }));
}

function normalizeTopCompletedUsers(rows) {
  return asArray(rows).map((x) => ({
    userName: x.fullName || x.username || "Unknown",
    count: num(x.completedCount || x.totalCount || x.count),
  }));
}

export default function Dashboard1() {
  const [filters, setFilters] = useState({
    fromDate: "2024-01-01",
    toDate: today,
  });

  const [data, setData] = useState({
    topRepairedAssets: [],
    topCompletedUsers: [],
  });

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const loadDashboard = async () => {
    setLoading(true);
    setErr("");

    try {
      const q = buildQuery(filters);

      const [assets, users] = await Promise.all([
        apiGet(`${API.topRepairedAssets}?${q}`),
        apiGet(`${API.topCompletedUsers}?${q}`),
      ]);

      setData({
        topRepairedAssets: assets,
        topCompletedUsers: users,
      });
    } catch (e) {
      setErr(e.message || "Không tải được dashboard.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const chart = useMemo(
    () => ({
      topRepairedAssets: normalizeTopRepairedAssets(data.topRepairedAssets),
      topCompletedUsers: normalizeTopCompletedUsers(data.topCompletedUsers),
    }),
    [data]
  );

  return (
    <main className="d1-page">
      <section className="d1-hero">
        <div>
          <p className="d1-eyebrow">CMMS Analytics</p>
          <h1>Dashboard Work Order</h1>
        </div>

        <button className="d1-primary" onClick={loadDashboard} disabled={loading}>
          {loading ? "Loading..." : "⟳ Refresh"}
        </button>
      </section>

      <section className="d1-filters">
        <label>
          From date
          <input
            type="date"
            value={filters.fromDate}
            onChange={(e) =>
              setFilters((p) => ({ ...p, fromDate: e.target.value }))
            }
          />
        </label>

        <label>
          To date
          <input
            type="date"
            value={filters.toDate}
            onChange={(e) =>
              setFilters((p) => ({ ...p, toDate: e.target.value }))
            }
          />
        </label>

        <button className="d1-secondary" onClick={loadDashboard} disabled={loading}>
          Apply filters
        </button>
      </section>

      {err && <div className="d1-alert">{err}</div>}

      <section className="d1-grid d1-grid--2">
        <Panel
          title="Top 10 Repaired Assets"
          desc="Thiết bị có số lần sửa chữa nhiều nhất."
        >
          {chart.topRepairedAssets.length ? (
            <div className="d1-chart">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chart.topRepairedAssets}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="assetName" />
                  <YAxis />
                  <Tooltip />
                  <Bar
                    dataKey="count"
                    name="Repair count"
                    fill="#f97316"
                    radius={[8, 8, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <Empty />
          )}
        </Panel>

        <Panel
          title="Top 10 Completed Users"
          desc="User hoàn thành Work Order nhiều nhất."
        >
          {chart.topCompletedUsers.length ? (
            <div className="d1-chart">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chart.topCompletedUsers}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="userName" />
                  <YAxis />
                  <Tooltip />
                  <Bar
                    dataKey="count"
                    name="Completed WO"
                    fill="#2563eb"
                    radius={[8, 8, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <Empty />
          )}
        </Panel>
      </section>
    </main>
  );
}