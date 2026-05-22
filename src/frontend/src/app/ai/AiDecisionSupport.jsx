import React, { useMemo, useState } from "react";
import {
  analyzeAI,
  quickAnalyze,
  monthlyAnalyze,
  yearlyAnalyze,
} from "./aiService";
import "./AiDecisionSupport.css";

const DEFAULT_QUESTION = "Phân tích rủi ro bảo trì thiết bị hiện tại";

const formatRiskClass = (level = "") =>
  level.toString().trim().toLowerCase().replace(/\s+/g, "-") || "unknown";

const formatNumber = (value) => {
  const number = Number(value);
  return Number.isFinite(number) ? number.toLocaleString("vi-VN") : "--";
};

const getStatusClass = (status = "") =>
  status.toString().trim().toLowerCase().replace(/\s+/g, "-") || "unknown";

export default function AiDecisionSupport() {
  const [question, setQuestion] = useState("");
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [year, setYear] = useState(new Date().getFullYear());
  const [loading, setLoading] = useState(false);
  const [activeAction, setActiveAction] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const safeQuestion = question.trim() || DEFAULT_QUESTION;

  const stats = useMemo(() => {
    const risks = result?.risks || [];
    const totalAssets = risks.length;
    const urgentAssets = risks.filter((asset) => {
      const level = formatRiskClass(asset.riskLevel);
      return ["critical", "high", "very-high"].includes(level);
    }).length;

    const totalRiskScore = risks.reduce(
      (sum, asset) => sum + (Number(asset.riskScore) || 0),
      0
    );
    const avgRiskScore = totalAssets ? Math.round(totalRiskScore / totalAssets) : 0;

    const totalWorkOrders = risks.reduce(
      (sum, asset) => sum + (Number(asset.workOrders) || 0),
      0
    );

    return {
      totalAssets,
      urgentAssets,
      avgRiskScore,
      totalWorkOrders,
    };
  }, [result]);

  const runRequest = async (requestFn, actionName) => {
    setLoading(true);
    setActiveAction(actionName);
    setError("");
    setResult(null);

    try {
      const res = await requestFn();
      setResult(res.data.data);
    } catch (err) {
      console.error(err);
      setError(
        err?.response?.data?.message ||
          "Không thể kết nối AI service. Vui lòng kiểm tra backend hoặc cấu hình CORS."
      );
    } finally {
      setLoading(false);
      setActiveAction("");
    }
  };

  const handleCustomAnalyze = () => {
    runRequest(
      () =>
        analyzeAI({
          question: safeQuestion,
          from: null,
          to: null,
        }),
      "analyze"
    );
  };

  const handleQuickAnalyze = () => {
    runRequest(() => quickAnalyze(safeQuestion), "quick");
  };

  const handleMonthlyAnalyze = () => {
    runRequest(() => monthlyAnalyze(safeQuestion, month, year), "monthly");
  };

  const handleYearlyAnalyze = () => {
    runRequest(() => yearlyAnalyze(safeQuestion, year), "yearly");
  };

  const actions = [
    {
      id: "analyze",
      label: "Phân tích 30 ngày",
      hint: "Xu hướng gần nhất",
      onClick: handleCustomAnalyze,
    },
    {
      id: "quick",
      label: "Quick AI",
      hint: "Kết quả nhanh",
      onClick: handleQuickAnalyze,
    },
    {
      id: "monthly",
      label: "Theo tháng",
      hint: `${month}/${year}`,
      onClick: handleMonthlyAnalyze,
    },
    {
      id: "yearly",
      label: "Theo năm",
      hint: `${year}`,
      onClick: handleYearlyAnalyze,
    },
  ];

  return (
    <main className="ai-page">
      <section className="ai-shell">
        <div className="ai-card ai-query-card">
          <div className="ai-section-heading">
            <div>
              <h2>Yêu cầu phân tích</h2>
            </div>
          </div>

          <label className="ai-field-label" htmlFor="ai-question">
            Nội dung cần AI hỗ trợ
          </label>
          <textarea
            id="ai-question"
            className="ai-textarea"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Nhập câu hỏi"
          />

          <div className="ai-control-panel">
            <div className="ai-filter-row">
              <div className="ai-filter">
                <label htmlFor="ai-month">Tháng</label>
                <input
                  id="ai-month"
                  type="number"
                  min="1"
                  max="12"
                  value={month}
                  onChange={(e) => setMonth(Number(e.target.value))}
                />
              </div>

              <div className="ai-filter">
                <label htmlFor="ai-year">Năm</label>
                <input
                  id="ai-year"
                  type="number"
                  min="2000"
                  value={year}
                  onChange={(e) => setYear(Number(e.target.value))}
                />
              </div>
            </div>

            <div className="ai-button-group">
              {actions.map((action) => (
                <button
                  key={action.id}
                  type="button"
                  className="ai-action-button"
                  onClick={action.onClick}
                  disabled={loading}
                >
                  <span>{loading && activeAction === action.id ? "Đang chạy" : action.label}</span>
                  <small>{action.hint}</small>
                </button>
              ))}
            </div>
          </div>

          {loading && (
            <div className="ai-loading" role="status">
              <span className="ai-spinner" />
              AI đang tổng hợp dữ liệu và đánh giá rủi ro...
            </div>
          )}

          {error && <div className="ai-error">{error}</div>}
        </div>

        {!result && !loading && !error && (
          <div className="ai-empty-state">
            <div className="ai-empty-icon">AI</div>
            <div>
              <h3>Sẵn sàng phân tích</h3>
            </div>
          </div>
        )}

        {result && (
          <section className="ai-results-grid">
            <div className="ai-stat-card">
              <span>Thiết bị rủi ro</span>
              <strong>{formatNumber(stats.totalAssets)}</strong>
              <small>Được AI đánh giá</small>
            </div>
            <div className="ai-stat-card warning">
              <span>Cần ưu tiên</span>
              <strong>{formatNumber(stats.urgentAssets)}</strong>
              <small>Critical / High</small>
            </div>
            <div className="ai-stat-card">
              <span>Điểm rủi ro TB</span>
              <strong>{formatNumber(stats.avgRiskScore)}</strong>
              <small>Trên danh sách thiết bị</small>
            </div>
            <div className="ai-stat-card">
              <span>Work Orders</span>
              <strong>{formatNumber(stats.totalWorkOrders)}</strong>
              <small>Tổng lệnh liên quan</small>
            </div>

            <article className="ai-card ai-result ai-summary-card">
              <div className="ai-section-heading">
                <div>
                  <h2>Tổng quan phân tích</h2>
                </div>
              </div>
              <p className="ai-summary-text">
                {result.summary || "Chưa có nội dung tổng quan."}
              </p>
            </article>

            <article className="ai-card ai-result">
              <div className="ai-section-heading compact">
                <h2>Khuyến nghị</h2>
              </div>
              {result.recommendations?.length > 0 ? (
                <ul className="ai-insight-list">
                  {result.recommendations.map((item, index) => (
                    <li key={index}>{item}</li>
                  ))}
                </ul>
              ) : (
                <p className="ai-muted">Không có khuyến nghị.</p>
              )}
            </article>

            <article className="ai-card ai-result">
              <div className="ai-section-heading compact">
                <h2>Hành động ưu tiên</h2>
              </div>
              {result.priorityActions?.length > 0 ? (
                <ul className="ai-insight-list priority">
                  {result.priorityActions.map((item, index) => (
                    <li key={index}>{item}</li>
                  ))}
                </ul>
              ) : (
                <p className="ai-muted">Không có hành động ưu tiên.</p>
              )}
            </article>

            <article className="ai-card ai-table-card">
              <div className="ai-section-heading">
                <div>
                  <h2>Risk Assets</h2>
                  <p>Danh sách thiết bị có dấu hiệu bất thường hoặc cần theo dõi.</p>
                </div>
              </div>

              <div className="ai-table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Asset</th>
                      <th>Status</th>
                      <th>Risk Score</th>
                      <th>Risk Level</th>
                      <th>Work Orders</th>
                      <th>Downtime</th>
                    </tr>
                  </thead>

                  <tbody>
                    {result.risks?.length > 0 ? (
                      result.risks.map((asset, index) => (
                        <tr key={asset.id || `${asset.name}-${index}`}>
                          <td>
                            <div className="asset-cell">
                              <span className="asset-avatar">
                                {(asset.name || "A").charAt(0).toUpperCase()}
                              </span>
                              <div>
                                <strong>{asset.name || "Unknown asset"}</strong>
                                <small>ID: {asset.id || "--"}</small>
                              </div>
                            </div>
                          </td>
                          <td>
                            <span className={`status-pill ${getStatusClass(asset.status)}`}>
                              {asset.status || "Unknown"}
                            </span>
                          </td>
                          <td>
                            <strong>{formatNumber(asset.riskScore)}</strong>
                          </td>
                          <td>
                            <span className={`risk-badge ${formatRiskClass(asset.riskLevel)}`}>
                              {asset.riskLevel || "Unknown"}
                            </span>
                          </td>
                          <td>{formatNumber(asset.workOrders)}</td>
                          <td>{asset.downtime || "--"}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td className="ai-table-empty" colSpan="6">
                          Không có dữ liệu thiết bị rủi ro.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </article>

            <details className="ai-json ai-card">
              <summary>Raw JSON response</summary>
              <pre>{JSON.stringify(result, null, 2)}</pre>
            </details>
          </section>
        )}
      </section>
    </main>
  );
}
