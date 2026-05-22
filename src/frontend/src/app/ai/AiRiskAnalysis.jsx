import React, { useEffect, useMemo, useState } from "react";
import { analyzeAI } from "./aiService";
import "./AiRiskAnalysis.css";

export default function AiRiskAnalysis() {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadRiskAnalysis = async () => {
    setLoading(true);
    setError("");

    try {
      const res = await analyzeAI({
        question:
          "Phân tích mức độ rủi ro thiết bị dựa trên Work Order, downtime và cảnh báo sớm.",
        from: null,
        to: null,
      });

      setResult(res.data.data);
    } catch (e) {
      console.error("AI Risk Analysis error:", e);
      setError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Không thể tải dữ liệu phân tích AI."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRiskAnalysis();
  }, []);

  const risks = result?.risks || [];

  const stats = useMemo(() => {
    const critical = risks.filter((item) => item.riskLevel === "CRITICAL").length;
    const high = risks.filter((item) => item.riskLevel === "HIGH").length;
    const medium = risks.filter((item) => item.riskLevel === "MEDIUM").length;
    const aiWarnings = risks.filter((item) => item.riskScore >= 85).length;

    return {
      totalAssets: result?.totalAssets || risks.length || 0,
      critical,
      high,
      medium,
      aiWarnings,
    };
  }, [result, risks]);

  const getRecommendation = (asset) => {
    if (asset.riskScore >= 85) {
      return "Kiểm tra khẩn cấp và ưu tiên bảo trì ngay.";
    }

    if (asset.riskScore >= 65) {
      return "Lập kế hoạch bảo trì trong thời gian gần.";
    }

    if (asset.riskScore >= 40) {
      return "Theo dõi định kỳ và kiểm tra hàng tuần.";
    }

    return "Thiết bị ổn định, tiếp tục giám sát định kỳ.";
  };

  const getWarningText = (asset) => {
    if (asset.riskScore >= 85) return "Nguy hiểm";
    if (asset.riskScore >= 65) return "Cảnh báo";
    return "Ổn định";
  };

  const getWarningClass = (asset) => {
    if (asset.riskScore >= 85) return "danger";
    if (asset.riskScore >= 65) return "warning";
    return "stable";
  };

  return (
    <div className="ai-risk-page">
      <div className="ai-risk-hero">
        <h1>AI Risk Analysis</h1>
        <p>
          Phân tích mức độ rủi ro thiết bị dựa trên Work Order, downtime và cảnh báo sớm.
        </p>
      </div>

      {error && <div className="ai-risk-error">{error}</div>}

      <div className="ai-risk-stats-grid">
        <div className="ai-risk-stat-card">
          <span>Tổng thiết bị</span>
          <strong>{stats.totalAssets}</strong>
        </div>

        <div className="ai-risk-stat-card">
          <span>CRITICAL</span>
          <strong>{stats.critical}</strong>
        </div>

        <div className="ai-risk-stat-card">
          <span>HIGH</span>
          <strong className="text-red">{stats.high}</strong>
        </div>

        <div className="ai-risk-stat-card">
          <span>MEDIUM</span>
          <strong className="text-orange">{stats.medium}</strong>
        </div>

        <div className="ai-risk-stat-card small-card">
          <span>Cảnh báo AI</span>
          <strong className="text-orange">{stats.aiWarnings}</strong>
        </div>
      </div>

      <div className="ai-risk-action-row">
        <button
          className="ai-risk-reload-btn"
          onClick={loadRiskAnalysis}
          disabled={loading}
        >
          {loading ? "Đang tải..." : "Tải lại phân tích"}
        </button>
      </div>

      <div className="ai-risk-table-card">
        <div className="ai-risk-table-header">
          <h2>Danh sách rủi ro thiết bị</h2>
        </div>

        {loading && (
          <div className="ai-risk-loading">
            Đang phân tích dữ liệu thiết bị...
          </div>
        )}

        {!loading && risks.length === 0 && (
          <div className="ai-risk-empty">
            Chưa có dữ liệu rủi ro thiết bị.
          </div>
        )}

        {!loading && risks.length > 0 && (
          <div className="ai-risk-table-wrapper">
            <table className="ai-risk-table">
              <thead>
                <tr>
                  <th>Thiết bị</th>
                  <th>Status</th>
                  <th>WO</th>
                  <th>Downtime</th>
                  <th>Risk</th>
                  <th>Level</th>
                  <th>AI Warning</th>
                  <th>Recommendation</th>
                </tr>
              </thead>

              <tbody>
                {risks.map((asset) => (
                  <tr key={asset.id}>
                    <td className="asset-cell">
                      <strong>{asset.name}</strong>
                      <span>ID:{asset.id}</span>
                    </td>

                    <td>{asset.status}</td>

                    <td>{asset.workOrders}</td>

                    <td>{asset.downtime}</td>

                    <td className="risk-score-cell">
                      <strong>{asset.riskScore}/100</strong>
                      <div className="risk-progress">
                        <div
                          className={`risk-progress-fill ${asset.riskLevel?.toLowerCase()}`}
                          style={{ width: `${asset.riskScore}%` }}
                        />
                      </div>
                    </td>

                    <td>
                      <span className={`risk-level ${asset.riskLevel?.toLowerCase()}`}>
                        {asset.riskLevel}
                      </span>
                    </td>

                    <td>
                      <span className={`ai-warning ${getWarningClass(asset)}`}>
                        {getWarningText(asset)}
                      </span>
                    </td>

                    <td>{getRecommendation(asset)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}