import React, { useEffect, useState } from "react";
import axios from "axios";
import "./WorkOrderHistories.css";

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  "https://emms-system-production-4239.up.railway.app";

const HISTORY_API = `${API_BASE}/api/work-order-histories`;
const WORK_ORDER_API = `${API_BASE}/api/work-orders`;

function getToken() {
  return (
    localStorage.getItem("token") ||
    localStorage.getItem("accessToken") ||
    localStorage.getItem("access_token") ||
    localStorage.getItem("jwt") ||
    ""
  );
}

function authHeaders() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export default function WorkOrderHistories() {
  const [histories, setHistories] = useState([]);
  const [workOrders, setWorkOrders] = useState([]);
  const [workOrderId, setWorkOrderId] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchHistories();
    fetchWorkOrders(); 
  }, []);

  // ===== GET HISTORY =====
  async function fetchHistories() {
    setLoading(true);
    try {
      const res = await axios.get(`${HISTORY_API}/done-cancelled`, {
        headers: authHeaders(),
      });
      setHistories(res.data || []);
    } catch (err) {
      console.error(err);
    }
    setLoading(false);
  }

  async function fetchWorkOrders() {
    try {
      const res = await axios.get(WORK_ORDER_API, {
        headers: authHeaders(),
      });

  
      const list = (res.data || []).filter((wo) =>
        ["DONE", "CANCELLED"].includes(String(wo.status || "").toUpperCase())
      );

      setWorkOrders(list);
    } catch (err) {
      console.error(err);
    }
  }

  // ===== FILTER =====
  async function filterByWO() {
    if (!workOrderId) {
      fetchHistories();
      return;
    }

    setLoading(true);
    try {
      const res = await axios.get(
        `${HISTORY_API}/work-order/${workOrderId}`,
        { headers: authHeaders() }
      );
      setHistories(res.data || []);
    } catch (err) {
      console.error(err);
    }
    setLoading(false);
  }

  return (
    <div className="woh-page">
      <div className="woh-shell">
        <div className="woh-header">
          <div>
            <div className="woh-eyebrow">EMMS</div>
            <h1>Work Order History</h1>
          </div>
        </div>

        {/* FILTER */}
        <div className="woh-filter-card">
          <div className="woh-filter-grid">
            <div className="woh-field">
              <span>Work Order</span>
              <select
                value={workOrderId}
                onChange={(e) => setWorkOrderId(e.target.value)}
              >
                <option value="">Tất cả</option>

                {workOrders.map((wo) => (
                  <option key={wo.id} value={wo.id}>
                    WO-{wo.id} — {wo.title || wo.assetName}
                  </option>
                ))}
              </select>
            </div>

            <div className="woh-filter-actions">
              <button className="woh-btn woh-btn-primary" onClick={filterByWO}>
                Lọc
              </button>

              <button
                className="woh-btn woh-btn-ghost"
                onClick={() => {
                  setWorkOrderId("");
                  fetchHistories();
                }}
              >
                Reset
              </button>
            </div>
          </div>
        </div>

        {/* TABLE */}
        <div className="woh-table-card">
          <div className="woh-table-head">
            <h2>Danh sách lịch sử</h2>
          </div>

          {loading ? (
            <div className="woh-empty">Loading...</div>
          ) : (
            <div className="woh-table-wrap">
              <table className="woh-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Version</th>
                    <th>WO</th>
                    <th>Saved By</th>
                    <th>Created</th>
                  </tr>
                </thead>

                <tbody>
                  {histories.map((h) => (
                    <tr key={h.id}>
                      <td>#{h.id}</td>
                      <td>
                        {h.versionName} (v{h.versionNo})
                      </td>
                      <td>WO-{h.workOrderId}</td>
                      <td>{h.savedByName}</td>
                      <td>
                        {new Date(h.createdAt).toLocaleString("vi-VN")}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}