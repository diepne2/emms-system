import { useEffect, useState } from "react";
import "./Labor.css";

const API_BASE = "http://localhost:8080/api";
const LABOR_API = `${API_BASE}/labors`;
const WORK_ORDERS_API = `${API_BASE}/work-orders/my`;

export default function Labor() {
  const [workOrders, setWorkOrders] = useState([]);
  const [labors, setLabors] = useState([]);
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    workOrderId: "",
    startedAt: "",
    hourlyRate: 0,
  });

  const getToken = () =>
    localStorage.getItem("token") ||
    localStorage.getItem("accessToken") ||
    localStorage.getItem("jwt") ||
    "";

  const headers = () => {
    const token = getToken();

    return {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
  };

  const readResponse = async (res) => {
    const text = await res.text();
    if (!text) return null;

    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  };

  const normalizeList = (data) => {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.content)) return data.content;
    if (Array.isArray(data?.data)) return data.data;
    return [];
  };

  const getErrorMessage = (data, fallback) => {
    if (!data) return fallback;
    if (typeof data === "string") return data;
    return data.message || data.error || fallback;
  };

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

  useEffect(() => {
    loadWorkOrders();
    loadLabors();
  }, []);

  const handleChange = (e) => {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  // 🔥 FIX DATE
  const formatDateForBackend = (value) => {
    if (!value) return null;

    // 2026-04-27T10:59 → 2026-04-27T10:59:00
    if (value.length === 16) {
      return `${value}:00`;
    }

    return value;
  };

  const handleCreate = async () => {
    if (!form.workOrderId) {
      alert("Chọn Work Order");
      return;
    }

    if (!form.startedAt) {
      alert("Chọn thời gian");
      return;
    }

    setLoading(true);

    try {
      const payload = {
        workOrderId: Number(form.workOrderId),
        startedAt: formatDateForBackend(form.startedAt), // ✅ FIX
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

      alert("Tạo thành công");

      setForm({
        workOrderId: "",
        startedAt: "",
        hourlyRate: 0,
      });

      loadLabors();
    } catch (err) {
      console.error(err);
      alert("Lỗi kết nối");
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (value) => {
    if (!value) return "";
    return new Date(value).toLocaleString("vi-VN");
  };

  return (
    <div className="labor-page">
      <div className="labor-shell">
        <h1>Work Log</h1>

        <div className="labor-layout">
          {/* FORM */}
          <div className="labor-panel">
            <h2>Create Labor</h2>

            <div className="labor-form">
              <label>Work Order</label>
              <select
                name="workOrderId"
                value={form.workOrderId}
                onChange={handleChange}
              >
                <option value="">-- Chọn --</option>
                {workOrders.map((wo) => (
                  <option key={wo.id} value={wo.id}>
                    #{wo.id} - {wo.title || wo.name}
                  </option>
                ))}
              </select>

              <label>Started At</label>
              <input
                type="datetime-local"
                name="startedAt"
                value={form.startedAt}
                onChange={handleChange}
              />

              <label>Hourly Rate</label>
              <input
                type="number"
                name="hourlyRate"
                value={form.hourlyRate}
                onChange={handleChange}
              />

              <button onClick={handleCreate} disabled={loading}>
                {loading ? "Creating..." : "Create"}
              </button>
            </div>
          </div>

          {/* LIST */}
          <div className="labor-list-card">
            <h2>Labor List</h2>

            {labors.length === 0 ? (
              <p>No data</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>WO</th>
                    <th>Status</th>
                    <th>Started</th>
                    <th>Ended</th>
                    <th>Rate</th>
                  </tr>
                </thead>

                <tbody>
                  {labors.map((l) => (
                    <tr key={l.id}>
                      <td>{l.id}</td>
                      <td>{l.workOrderCode || l.workOrderId}</td>
                      <td>{l.status}</td>
                      <td>{formatDate(l.startedAt)}</td>
                      <td>{formatDate(l.endedAt)}</td>
                      <td>{l.hourlyRate}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}