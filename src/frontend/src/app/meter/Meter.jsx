import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Legend,
  CartesianGrid,
} from "recharts";
import "./meter.css";

const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || "https://emms-system-production-4239.up.railway.app";

const api = axios.create({ baseURL: API_BASE_URL });

const TRIGGER_CONDITIONS = [
  "GREATER_THAN",
  "GREATER_THAN_OR_EQUAL",
  "LESS_THAN",
  "LESS_THAN_OR_EQUAL",
  "EQUAL",
  "DELTA_GREATER_THAN",
  "DELTA_GREATER_THAN_OR_EQUAL",
];

const PRIORITIES = ["NONE", "LOW", "MEDIUM", "HIGH", "URGENT"];

const DEFAULT_METER_FORM = {
  id: null,
  name: "",
  unit: "hours",
  updateFrequency: 1,
  assetId: "",
};

const DEFAULT_TRIGGER_FORM = {
  id: null,
  name: "",
  triggerCondition: "GREATER_THAN_OR_EQUAL",
  triggerValue: "",
  cooldownMinutes: 60,
  recurrent: true,
  active: true,
  priority: "MEDIUM",
};

const getToken = () =>
  localStorage.getItem("accessToken") ||
  localStorage.getItem("token") ||
  localStorage.getItem("access_token") ||
  sessionStorage.getItem("accessToken") ||
  sessionStorage.getItem("token") ||
  sessionStorage.getItem("access_token") ||
  "";

const getAuthConfig = () => {
  const token = getToken();
  return token ? { headers: { Authorization: `Bearer ${token}` } } : {};
};

const parseJsonSafe = (value) => {
  try {
    return value ? JSON.parse(value) : null;
  } catch {
    return null;
  }
};

const getCurrentRole = () => {
  const user =
    parseJsonSafe(localStorage.getItem("user")) ||
    parseJsonSafe(localStorage.getItem("currentUser")) ||
    parseJsonSafe(sessionStorage.getItem("user")) ||
    parseJsonSafe(sessionStorage.getItem("currentUser")) ||
    {};

  const rawRole =
    user?.role?.code ||
    user?.role?.name ||
    user?.role ||
    localStorage.getItem("role") ||
    sessionStorage.getItem("role") ||
    "";

  return String(rawRole).replace(/^ROLE_/, "").toUpperCase();
};

const isManagerRole = () => ["ADMIN", "TECHNICAL_MANAGER"].includes(getCurrentRole());

const extractList = (payload) => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload?.items)) return payload.items;
  return [];
};

const pickId = (item) =>
  item?.id ?? item?.meterId ?? item?.assetId ?? item?.userId ?? item?.triggerId ?? null;

const extractErrorMessage = (err, fallback = "Có lỗi xảy ra") => {
  if (err?.response) {
    return `HTTP ${err.response.status}: ${
      err.response.data?.message || err.response.data?.error || fallback
    }`;
  }
  if (err?.request) return "Không thể kết nối backend.";
  return err?.message || fallback;
};

const toNumberOrNull = (value) => {
  if (value === "" || value === null || value === undefined) return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
};

const formatDateTime = (value) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString("vi-VN");
};

const formatChartLabel = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" });
};

const getAssetName = (meter) =>
  meter?.asset?.name || meter?.assetName || meter?.asset?.assetName || "-";


const getAssetDisplayName = (asset) =>
  asset?.name || asset?.assetName || asset?.code || `Asset #${pickId(asset) || ""}`;

const getAssetMeta = (asset) => {
  const parts = [asset?.code || asset?.assetCode, asset?.locationName || asset?.location?.name, asset?.status].filter(Boolean);
  return parts.join(" • ");
};

function normalizeText(value) {
  return String(value ?? "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim();
}

function AssetCombobox({ assets, value, disabled, loading, totalAssets, onChange }) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");

  const selectedAsset = useMemo(
    () => assets.find((asset) => String(pickId(asset)) === String(value)) || null,
    [assets, value]
  );

  const filteredAssets = useMemo(() => {
    const q = normalizeText(search);
    if (!q) return assets;

    return assets.filter((asset) => {
      const text = normalizeText([
        asset?.name,
        asset?.assetName,
        asset?.code,
        asset?.assetCode,
        asset?.serialNumber,
        asset?.locationName,
        asset?.location?.name,
        asset?.status,
        pickId(asset),
      ].filter(Boolean).join(" "));

      return text.includes(q);
    });
  }, [assets, search]);

  const assetTotal = Number.isFinite(totalAssets) ? totalAssets : assets.length;

  useEffect(() => {
    if (disabled) setOpen(false);
  }, [disabled]);

  return (
    <div className="meter-asset-combobox">
      <button
        type="button"
        className={`meter-asset-combobox__control ${open ? "is-open" : ""}`}
        disabled={disabled}
        onClick={() => setOpen((prev) => !prev)}
      >
        <span className={selectedAsset ? "meter-asset-combobox__value" : "meter-asset-combobox__placeholder"}>
          {selectedAsset ? getAssetDisplayName(selectedAsset) : "-- Chọn asset --"}
        </span>
        <span className="meter-asset-combobox__count">{assetTotal} asset</span>
        <span className="meter-asset-combobox__arrow">⌄</span>
      </button>

      {open && !disabled && (
        <div className="meter-asset-combobox__dropdown">
          <input
            className="meter-asset-combobox__search"
            value={search}
            autoFocus
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Escape") setOpen(false);
            }}
            placeholder={`Tìm tương đối theo tên, mã, vị trí... (${assetTotal})`}
          />
          <div className="meter-asset-combobox__list">
            {loading ? (
              <div className="meter-asset-combobox__empty">Đang tải asset...</div>
            ) : filteredAssets.length === 0 ? (
              <div className="meter-asset-combobox__empty">Không tìm thấy asset phù hợp.</div>
            ) : (
              filteredAssets.slice(0, 100).map((asset) => {
                const id = pickId(asset);
                const active = String(id) === String(value);
                return (
                  <button
                    type="button"
                    key={id}
                    className={`meter-asset-combobox__item ${active ? "is-selected" : ""}`}
                    onMouseDown={(e) => e.preventDefault()}
                    onClick={() => {
                      onChange(String(id));
                      setOpen(false);
                      setSearch("");
                    }}
                  >
                    <strong>{getAssetDisplayName(asset)}</strong>
                    {getAssetMeta(asset) && <small>{getAssetMeta(asset)}</small>}
                  </button>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function StatusMessage({ type = "info", children }) {
  if (!children) return null;
  return <div className={`meter-message meter-message--${type}`}>{children}</div>;
}

function MeterForm({ assets, assetTotal, assetLoading, selectedMeter, canManage, onSaved, onCancel }) {
  const [form, setForm] = useState(DEFAULT_METER_FORM);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const isEditing = Boolean(form.id);

  useEffect(() => {
    if (!selectedMeter) {
      setForm(DEFAULT_METER_FORM);
      return;
    }

    setForm({
      id: pickId(selectedMeter),
      name: selectedMeter.name || "",
      unit: selectedMeter.unit || "hours",
      updateFrequency: selectedMeter.updateFrequency ?? 1,
      assetId: selectedMeter.asset?.id || selectedMeter.assetId || "",
    });
  }, [selectedMeter]);

  const updateField = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const resetCreate = () => {
    setMessage("");
    setForm(DEFAULT_METER_FORM);
    onCancel?.();
  };

  const submit = async (event) => {
    event.preventDefault();
    setMessage("");

    if (!canManage) {
      setMessage("Bạn không có quyền tạo/sửa meter.");
      return;
    }

    if (!form.name.trim()) {
      setMessage("Tên meter không được để trống.");
      return;
    }

    if (!form.unit.trim()) {
      setMessage("Đơn vị không được để trống.");
      return;
    }

    const assetId = toNumberOrNull(form.assetId);
    if (!assetId) {
      setMessage("Vui lòng chọn asset.");
      return;
    }

    const updateFrequency = toNumberOrNull(form.updateFrequency);
    if (!updateFrequency || updateFrequency < 1) {
      setMessage("Update frequency phải >= 1.");
      return;
    }

    const payload = {
      name: form.name.trim(),
      unit: form.unit.trim(),
      updateFrequency,
      assetId,
    };

    try {
      setSaving(true);
      if (isEditing) {
        await api.put(`/api/meters/${form.id}`, payload, getAuthConfig());
        setMessage("Cập nhật meter thành công.");
      } else {
        await api.post("/api/meters", payload, getAuthConfig());
        setMessage("Tạo meter thành công.");
        setForm(DEFAULT_METER_FORM);
      }
      await onSaved?.();
    } catch (err) {
      setMessage(extractErrorMessage(err, "Không thể lưu meter."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="meter-card meter-card--form">
      <div className="meter-card__header">
        <div>
          <h3>{isEditing ? "Sửa meter" : "Tạo meter"}</h3>

        </div>
        {isEditing && (
          <button type="button" className="meter-btn meter-btn--ghost" onClick={resetCreate}>
            Tạo mới
          </button>
        )}
      </div>

      <StatusMessage type={message.startsWith("HTTP") || message.includes("không") ? "error" : "success"}>
        {message}
      </StatusMessage>

      <form className="meter-form" onSubmit={submit}>
        <label className="meter-field">
          <span>Tên meter</span>
          <input
            className="meter-input"
            value={form.name}
            disabled={!canManage}
            onChange={(e) => updateField("name", e.target.value)}
            placeholder="Nhập tên meter"
          />
        </label>

        <label className="meter-field">
          <span>Đơn vị</span>
          <input
            className="meter-input"
            value={form.unit}
            disabled={!canManage}
            onChange={(e) => updateField("unit", e.target.value)}
            placeholder="Nhập đơn vị"
          />
        </label>

        <label className="meter-field meter-field--full">
          <AssetCombobox
            assets={assets}
            totalAssets={assetTotal}
            loading={assetLoading}
            value={form.assetId}
            disabled={!canManage}
            onChange={(assetId) => updateField("assetId", assetId)}
          />
        </label>

        <label className="meter-field">
          <span>Update frequency</span>
          <input
            className="meter-input"
            type="number"
            min="1"
            value={form.updateFrequency}
            disabled={!canManage}
            onChange={(e) => updateField("updateFrequency", e.target.value)}
          />
        </label>

        <div className="meter-form__actions">
          <button className="meter-btn meter-btn--primary" disabled={saving || !canManage} type="submit">
            {saving ? "Đang lưu..." : isEditing ? "Lưu thay đổi" : "Tạo meter"}
          </button>
        </div>
      </form>
    </section>
  );
}

function MeterList({ meters, selectedId, onSelect, onEdit, canManage, onDelete }) {
  return (
    <section className="meter-card meter-card--list">
      <div className="meter-card__header">
        <div>
          <h3>Danh sách meter</h3>
          <p>{meters.length} meter đang cấu hình</p>
        </div>
      </div>

      <div className="meter-list">
        {meters.length === 0 ? (
          <div className="meter-empty">Chưa có meter nào.</div>
        ) : (
          meters.map((meter) => {
            const id = pickId(meter);
            const active = String(id) === String(selectedId);
            return (
              <button
                type="button"
                key={id}
                className={`meter-list-item ${active ? "meter-list-item--active" : ""}`}
                onClick={() => onSelect(id)}
              >
                <div className="meter-list-item__main">
                  <strong>{meter.name || `Meter #${id}`}</strong>
                  <span>{getAssetName(meter)}</span>
                </div>
                <div className="meter-list-item__side">
                  <span>{meter.unit || "-"}</span>
                  {canManage && (
                    <span className="meter-list-item__actions" onClick={(e) => e.stopPropagation()}>
                      <button type="button" onClick={() => onEdit(meter)}>Sửa</button>
                      <button type="button" className="danger" onClick={() => onDelete(meter)}>Xóa</button>
                    </span>
                  )}
                </div>
              </button>
            );
          })
        )}
      </div>
    </section>
  );
}

function MeterDetail({ meter }) {
  return (
    <section className="meter-card meter-card--detail">
      <div className="meter-detail-hero">
        <div>
          <p className="meter-eyebrow">Chi tiết Meter</p>
          <h2>{meter?.name || "Chọn meter"}</h2>
        </div>
        {meter && <div className="meter-unit-badge">{meter.unit || "unit"}</div>}
      </div>

      {meter && (
        <div className="meter-summary-grid">
          <div className="meter-summary-item">
            <span>ID</span>
            <strong>#{pickId(meter)}</strong>
          </div>
          <div className="meter-summary-item">
            <span>Asset</span>
            <strong>{getAssetName(meter)}</strong>
          </div>
          <div className="meter-summary-item">
            <span>Unit</span>
            <strong>{meter.unit || "-"}</strong>
          </div>
          <div className="meter-summary-item">
            <span>Frequency</span>
            <strong>{meter.updateFrequency ?? "-"}</strong>
          </div>
        </div>
      )}
    </section>
  );
}

function ReadingPanel({ meter, readings, loading, onReadingCreated }) {
  const [value, setValue] = useState("");
  const [recordedAt, setRecordedAt] = useState("");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  const chartData = useMemo(() => {
    return [...readings]
      .sort((a, b) => new Date(a.recordedAt || 0) - new Date(b.recordedAt || 0))
      .map((item) => ({
        ...item,
        value: Number(item.value ?? 0),
        deltaValue: Number(item.deltaValue ?? 0),
      }));
  }, [readings]);

  const submit = async (event) => {
    event.preventDefault();
    setMessage("");

    if (!meter) {
      setMessage("Vui lòng chọn meter trước.");
      return;
    }

    const n = toNumberOrNull(value);
    if (n === null || n < 0) {
      setMessage("Reading value phải >= 0.");
      return;
    }

    const payload = {
      meterId: pickId(meter),
      value: n,
      ...(recordedAt ? { recordedAt } : {}),
    };

    try {
      setSaving(true);
      const res = await api.post("/api/readings", payload, getAuthConfig());
      setValue("");
      setRecordedAt("");
      if (res.data?.triggeredWorkOrderId) {
        setMessage(`Đã ghi reading và tự tạo Work Order #${res.data.triggeredWorkOrderId}.`);
      } else {
        setMessage("Ghi reading thành công.");
      }
      await onReadingCreated?.();
    } catch (err) {
      setMessage(extractErrorMessage(err, "Không thể ghi reading."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="meter-card">
      <div className="meter-card__header">
        <div>
          <h3>Reading & chart</h3>
        </div>
      </div>

      <StatusMessage type={message.startsWith("HTTP") || message.includes("không") ? "error" : "success"}>
        {message}
      </StatusMessage>

      <form className="meter-reading-form" onSubmit={submit}>
        <input
          className="meter-input"
          type="number"
          min="0"
          step="0.01"
          value={value}
          disabled={!meter || saving}
          onChange={(e) => setValue(e.target.value)}
          placeholder={`Nhập reading${meter?.unit ? ` (${meter.unit})` : ""}`}
        />
        <input
          className="meter-input"
          type="datetime-local"
          value={recordedAt}
          disabled={!meter || saving}
          onChange={(e) => setRecordedAt(e.target.value)}
        />
        <button className="meter-btn meter-btn--primary" disabled={!meter || saving} type="submit">
          {saving ? "Đang ghi..." : "Ghi reading"}
        </button>
      </form>

      <div className="meter-chart-wrap">
        {loading ? (
          <div className="meter-empty">Đang tải readings...</div>
        ) : chartData.length === 0 ? (
          <div className="meter-empty">Chưa có dữ liệu chart.</div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="recordedAt" tickFormatter={formatChartLabel} />
              <YAxis />
              <Tooltip labelFormatter={formatDateTime} />
              <Legend />
              <Line type="monotone" dataKey="value" name="Reading" dot={{ r: 4 }} activeDot={{ r: 6 }} />
              <Line type="monotone" dataKey="deltaValue" name="Delta" dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="meter-table-wrap">
        <table className="meter-table">
          <thead>
            <tr>
              <th>Thời gian</th>
              <th>Value</th>
              <th>Delta</th>
              <th>Auto WO</th>
            </tr>
          </thead>
          <tbody>
            {readings.length === 0 ? (
              <tr><td colSpan="4" className="meter-empty-cell">Chưa có reading.</td></tr>
            ) : (
              readings.map((reading) => (
                <tr key={pickId(reading) || `${reading.recordedAt}-${reading.value}`} className={reading.triggered ? "row-triggered" : ""}>
                  <td>{formatDateTime(reading.recordedAt)}</td>
                  <td>{reading.value ?? "-"}</td>
                  <td>{reading.deltaValue ?? "-"}</td>
                  <td>{reading.triggeredWorkOrderId ? `WO #${reading.triggeredWorkOrderId}` : "-"}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function TriggerPanel({ meter, triggers, canManage, onChanged }) {
  const [form, setForm] = useState(DEFAULT_TRIGGER_FORM);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const isEditing = Boolean(form.id);

  useEffect(() => {
    if (!meter) {
      setForm(DEFAULT_TRIGGER_FORM);
      return;
    }
    setForm((prev) => ({
      ...prev,
      id: null,
      name: prev.name || `${meter.name || "Meter"} threshold`,
    }));
  }, [meter?.id]);

  const updateField = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const edit = (trigger) => {
    setMessage("");
    setForm({
      id: pickId(trigger),
      name: trigger.name || "",
      triggerCondition: trigger.triggerCondition || "GREATER_THAN_OR_EQUAL",
      triggerValue: trigger.triggerValue ?? "",
      cooldownMinutes: trigger.cooldownMinutes ?? 60,
      recurrent: Boolean(trigger.recurrent),
      active: trigger.active !== false,
      priority: trigger.priority || "MEDIUM",
    });
  };

  const reset = () => {
    setMessage("");
    setForm({
      ...DEFAULT_TRIGGER_FORM,
      name: meter ? `${meter.name || "Meter"} threshold` : "",
    });
  };

  const submit = async (event) => {
    event.preventDefault();
    setMessage("");

    if (!canManage) {
      setMessage("Bạn không có quyền tạo/sửa trigger.");
      return;
    }

    if (!meter) {
      setMessage("Vui lòng chọn meter trước.");
      return;
    }

    const triggerValue = toNumberOrNull(form.triggerValue);
    const cooldownMinutes = toNumberOrNull(form.cooldownMinutes);
    if (!form.name.trim()) {
      setMessage("Tên trigger không được để trống.");
      return;
    }
    if (triggerValue === null || triggerValue < 0) {
      setMessage("Trigger value phải >= 0.");
      return;
    }
    if (cooldownMinutes === null || cooldownMinutes < 0) {
      setMessage("Cooldown phải >= 0.");
      return;
    }

    const payload = {
      meterId: pickId(meter),
      name: form.name.trim(),
      triggerCondition: form.triggerCondition,
      triggerValue,
      cooldownMinutes,
      recurrent: Boolean(form.recurrent),
      active: Boolean(form.active),
      priority: form.priority,
    };

    try {
      setSaving(true);
      if (isEditing) {
        await api.put(`/api/work-order-meter-triggers/${form.id}`, payload, getAuthConfig());
        setMessage("Cập nhật trigger thành công.");
      } else {
        await api.post("/api/work-order-meter-triggers", payload, getAuthConfig());
        setMessage("Tạo trigger thành công.");
      }
      reset();
      await onChanged?.();
    } catch (err) {
      setMessage(extractErrorMessage(err, "Không thể lưu trigger."));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (trigger) => {
    if (!canManage) return;
    const id = pickId(trigger);
    if (!id) return;
    const ok = window.confirm(`Xóa trigger "${trigger.name || id}"?`);
    if (!ok) return;
    try {
      await api.delete(`/api/work-order-meter-triggers/${id}`, getAuthConfig());
      setMessage("Xóa trigger thành công.");
      await onChanged?.();
    } catch (err) {
      setMessage(extractErrorMessage(err, "Không thể xóa trigger."));
    }
  };

  return (
    <section className="meter-card">
      <div className="meter-card__header">
        <div>
          <h3>Trigger tự sinh Work Order</h3>
        </div>
        {isEditing && <button className="meter-btn meter-btn--ghost" onClick={reset}>Tạo mới</button>}
      </div>

      <StatusMessage type={message.startsWith("HTTP") || message.includes("không") ? "error" : "success"}>
        {message}
      </StatusMessage>

      {canManage && (
        <form className="meter-trigger-form" onSubmit={submit}>
          <input
            className="meter-input"
            value={form.name}
            disabled={!meter || saving}
            onChange={(e) => updateField("name", e.target.value)}
            placeholder="Tên trigger"
          />
          <select
            className="meter-input"
            value={form.triggerCondition}
            disabled={!meter || saving}
            onChange={(e) => updateField("triggerCondition", e.target.value)}
          >
            {TRIGGER_CONDITIONS.map((condition) => <option key={condition} value={condition}>{condition}</option>)}
          </select>
          <input
            className="meter-input"
            type="number"
            min="0"
            step="0.01"
            value={form.triggerValue}
            disabled={!meter || saving}
            onChange={(e) => updateField("triggerValue", e.target.value)}
            placeholder="Ngưỡng"
          />
          <input
            className="meter-input"
            type="number"
            min="0"
            value={form.cooldownMinutes}
            disabled={!meter || saving}
            onChange={(e) => updateField("cooldownMinutes", e.target.value)}
            placeholder="Cooldown phút"
          />
          <select
            className="meter-input"
            value={form.priority}
            disabled={!meter || saving}
            onChange={(e) => updateField("priority", e.target.value)}
          >
            {PRIORITIES.map((priority) => <option key={priority} value={priority}>{priority}</option>)}
          </select>
          <label className="meter-check"><input type="checkbox" checked={form.recurrent} disabled={!meter || saving} onChange={(e) => updateField("recurrent", e.target.checked)} /> Lặp lại</label>
          <label className="meter-check"><input type="checkbox" checked={form.active} disabled={!meter || saving} onChange={(e) => updateField("active", e.target.checked)} /> Active</label>
          <button className="meter-btn meter-btn--primary" disabled={!meter || saving} type="submit">
            {saving ? "Đang lưu..." : isEditing ? "Lưu trigger" : "Tạo trigger"}
          </button>
        </form>
      )}

      <div className="meter-table-wrap">
        <table className="meter-table">
          <thead>
            <tr>
              <th>Tên</th>
              <th>Điều kiện</th>
              <th>Ngưỡng</th>
              <th>Cooldown</th>
              <th>Trạng thái</th>
              {canManage && <th>Action</th>}
            </tr>
          </thead>
          <tbody>
            {triggers.length === 0 ? (
              <tr><td colSpan={canManage ? 6 : 5} className="meter-empty-cell">Chưa có trigger active.</td></tr>
            ) : (
              triggers.map((trigger) => (
                <tr key={pickId(trigger)}>
                  <td>{trigger.name || "-"}</td>
                  <td>{trigger.triggerCondition || "-"}</td>
                  <td>{trigger.triggerValue ?? "-"}</td>
                  <td>{trigger.cooldownMinutes ?? 0} phút</td>
                  <td><span className={`meter-pill ${trigger.active !== false ? "meter-pill--success" : ""}`}>{trigger.active !== false ? "Active" : "Inactive"}</span></td>
                  {canManage && (
                    <td>
                      <div className="meter-row-actions">
                        <button type="button" onClick={() => edit(trigger)}>Sửa</button>
                        <button type="button" className="danger" onClick={() => remove(trigger)}>Xóa</button>
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default function Meter() {
  const [meters, setMeters] = useState([]);
  const [assets, setAssets] = useState([]);
  const [assetTotal, setAssetTotal] = useState(0);
  const [assetLoading, setAssetLoading] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [selectedMeter, setSelectedMeter] = useState(null);
  const [editingMeter, setEditingMeter] = useState(null);
  const [readings, setReadings] = useState([]);
  const [triggers, setTriggers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [message, setMessage] = useState("");

  const canManage = isManagerRole();

  const selectedMeterFromList = useMemo(
    () => meters.find((item) => String(pickId(item)) === String(selectedId)) || null,
    [meters, selectedId]
  );

  const loadAssets = async () => {
    setAssetLoading(true);
    try {
      const payloads = [
        { keyword: "", page: 0, size: 2000 },
        { page: 0, size: 2000 },
        { filters: [], page: 0, size: 2000 },
      ];

      let lastError = null;
      for (const payload of payloads) {
        try {
          const res = await api.post("/api/assets/search", payload, getAuthConfig());
          const list = extractList(res.data);
          setAssets(list);
          setAssetTotal(
            Number.isFinite(res.data?.totalElements)
              ? res.data.totalElements
              : Number.isFinite(res.data?.total)
                ? res.data.total
                : list.length
          );
          return;
        } catch (err) {
          lastError = err;
        }
      }
      throw lastError;
    } catch (err) {
      console.error("Load assets failed:", err);
      setAssets([]);
      setAssetTotal(0);
    } finally {
      setAssetLoading(false);
    }
  };

  const loadMeters = async (keepSelected = true) => {
    const res = await api.get("/api/meters", getAuthConfig());
    const list = extractList(res.data);
    setMeters(list);

    if (!keepSelected || !selectedId) {
      setSelectedId(pickId(list[0]) || null);
    } else if (!list.some((item) => String(pickId(item)) === String(selectedId))) {
      setSelectedId(pickId(list[0]) || null);
    }
  };

  const loadMeterDetail = async (meterId) => {
    if (!meterId) {
      setSelectedMeter(null);
      setReadings([]);
      setTriggers([]);
      return;
    }

    try {
      setDetailLoading(true);
      const [meterRes, readingRes, triggerRes] = await Promise.all([
        api.get(`/api/meters/${meterId}`, getAuthConfig()),
        api.get(`/api/readings/meter/${meterId}`, getAuthConfig()),
        api.get(`/api/work-order-meter-triggers/meter/${meterId}`, getAuthConfig()),
      ]);
      setSelectedMeter(meterRes.data || selectedMeterFromList);
      setReadings(extractList(readingRes.data));
      setTriggers(extractList(triggerRes.data));
    } catch (err) {
      setMessage(extractErrorMessage(err, "Không thể tải chi tiết meter."));
      setSelectedMeter(selectedMeterFromList);
    } finally {
      setDetailLoading(false);
    }
  };

  const initialLoad = async () => {
    try {
      setLoading(true);
      setMessage("");
      await Promise.all([loadAssets(), loadMeters(false)]);
    } catch (err) {
      setMessage(extractErrorMessage(err, "Không thể tải meter."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    initialLoad();
  }, []);

  useEffect(() => {
    loadMeterDetail(selectedId);
  }, [selectedId]);

  const refreshAll = async () => {
    await loadMeters(true);
    if (selectedId) await loadMeterDetail(selectedId);
  };

  const deleteMeter = async (meter) => {
    if (!canManage) return;
    const id = pickId(meter);
    if (!id) return;
    const ok = window.confirm(`Xóa meter "${meter.name || id}"?`);
    if (!ok) return;

    try {
      await api.delete(`/api/meters/${id}`, getAuthConfig());
      setMessage("Xóa meter thành công.");
      if (String(selectedId) === String(id)) setSelectedId(null);
      setEditingMeter(null);
      await loadMeters(false);
    } catch (err) {
      setMessage(extractErrorMessage(err, "Không thể xóa meter."));
    }
  };

  const activeMeter = selectedMeter || selectedMeterFromList;

  return (
    <main className="meter-page">
      <header className="meter-topbar">
        <div>
          <h1>Quản lý Meter</h1>
        </div>
        <div className="meter-topbar__stats">
          <span>{meters.length}</span>
          <small>Meters</small>
        </div>
      </header>

      <StatusMessage type={message.startsWith("HTTP") || message.includes("Không") ? "error" : "success"}>
        {message}
      </StatusMessage>

      {!canManage && (
        <StatusMessage type="info">
          Tài khoản hiện tại chỉ có quyền xem và ghi reading. Tạo/sửa/xóa meter hoặc trigger cần ADMIN / TECHNICAL_MANAGER.
        </StatusMessage>
      )}

      {loading ? (
        <div className="meter-card"><div className="meter-empty">Đang tải dữ liệu...</div></div>
      ) : (
        <div className="meter-layout">
          <aside className="meter-sidebar">
            <MeterForm
              assets={assets}
              assetTotal={assetTotal}
              assetLoading={assetLoading}
              selectedMeter={editingMeter}
              canManage={canManage}
              onSaved={refreshAll}
              onCancel={() => setEditingMeter(null)}
            />
            <MeterList
              meters={meters}
              selectedId={selectedId}
              canManage={canManage}
              onSelect={(id) => setSelectedId(id)}
              onEdit={(meter) => setEditingMeter(meter)}
              onDelete={deleteMeter}
            />
          </aside>

          <section className="meter-content">
            <MeterDetail meter={activeMeter} />
            {detailLoading && <StatusMessage type="info">Đang tải chi tiết...</StatusMessage>}
            <ReadingPanel meter={activeMeter} readings={readings} loading={detailLoading} onReadingCreated={() => loadMeterDetail(selectedId)} />
            <TriggerPanel meter={activeMeter} triggers={triggers} canManage={canManage} onChanged={() => loadMeterDetail(selectedId)} />
          </section>
        </div>
      )}
    </main>
  );
}
